package lny.cinematic;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL32C;

// Resizes a picture to the size it is shown at, the way video players do: a Catmull-Rom filter, widened when shrinking so
// that every source pixel counts, in two passes (across, then down). Mipmaps and linear filtering blur the video visibly
// more. The result is drawn pixel for pixel.
final class Scaler {
   private static final String VERTEX = """
      #version 150 core
      void main() {
         // one triangle covering the target
         vec2 corner = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
         gl_Position = vec4(corner * 2.0 - 1.0, 0.0, 1.0);
      }
      """;
   private static final String FRAGMENT = """
      #version 150 core
      uniform sampler2D source;
      uniform int across; // 1: this pass scales the width, 0: the height
      uniform float scale; // source pixels per target pixel along it
      out vec4 colour;

      float catmullRom(float x) {
         x = abs(x);
         if (x < 1.0) {
            return (1.5 * x - 2.5) * x * x + 1.0;
         }
         if (x < 2.0) {
            return ((-0.5 * x + 2.5) * x - 4.0) * x + 2.0;
         }
         return 0.0;
      }

      void main() {
         ivec2 size = textureSize(source, 0);
         ivec2 target = ivec2(gl_FragCoord.xy);
         int extent = across == 1 ? size.x : size.y;
         float stretch = max(scale, 1.0);
         float centre = (float(across == 1 ? target.x : target.y) + 0.5) * scale - 0.5;
         int first = int(floor(centre - 2.0 * stretch)) + 1;
         int last = int(floor(centre + 2.0 * stretch));
         vec4 sum = vec4(0.0);
         float total = 0.0;
         for (int i = first; i <= last; i++) {
            float weight = catmullRom((float(i) - centre) / stretch);
            int j = clamp(i, 0, extent - 1);
            sum += weight * texelFetch(source, across == 1 ? ivec2(j, target.y) : ivec2(target.x, j), 0);
            total += weight;
         }
         colour = clamp(sum / total, 0.0, 1.0);
      }
      """;

   private final int program;
   private final int sourceUniform;
   private final int acrossUniform;
   private final int scaleUniform;
   private final int vertexArray;
   private final Target across = new Target(); // the first pass: the target's width, the source's height

   // A texture holding one picture at the size it is shown at
   static final class Target {
      int texture;
      int framebuffer;
      int width;
      int height;
      int content = -1; // which picture it holds, up to the caller

      private void resize(int width, int height) {
         if (this.texture == 0) {
            this.texture = Cinematic.texture(false);
            this.framebuffer = GL32C.glGenFramebuffers();
         }
         if (width == this.width && height == this.height) {
            return;
         }
         this.width = width;
         this.height = height;
         GL32C.glBindTexture(GL32C.GL_TEXTURE_2D, this.texture);
         GL32C.glTexImage2D(GL32C.GL_TEXTURE_2D, 0, GL32C.GL_RGBA8, width, height, 0, GL32C.GL_RGBA, GL32C.GL_UNSIGNED_BYTE, (ByteBuffer)null);
         GL32C.glBindFramebuffer(GL32C.GL_FRAMEBUFFER, this.framebuffer);
         GL32C.glFramebufferTexture2D(GL32C.GL_FRAMEBUFFER, GL32C.GL_COLOR_ATTACHMENT0, GL32C.GL_TEXTURE_2D, this.texture, 0);
      }

      void delete() {
         GL32C.glDeleteTextures(this.texture);
         GL32C.glDeleteFramebuffers(this.framebuffer);
         this.texture = 0;
         this.width = 0;
         this.height = 0;
         this.content = -1;
      }
   }

   Scaler() {
      int vertex = compile(GL32C.GL_VERTEX_SHADER, VERTEX);
      int fragment = compile(GL32C.GL_FRAGMENT_SHADER, FRAGMENT);
      this.program = GL32C.glCreateProgram();
      GL32C.glAttachShader(this.program, vertex);
      GL32C.glAttachShader(this.program, fragment);
      GL32C.glLinkProgram(this.program);
      GL32C.glDeleteShader(vertex);
      GL32C.glDeleteShader(fragment);
      if (GL32C.glGetProgrami(this.program, GL32C.GL_LINK_STATUS) == GL32C.GL_FALSE) {
         String log = GL32C.glGetProgramInfoLog(this.program);
         GL32C.glDeleteProgram(this.program);
         throw new IllegalStateException("Can't link the scaler: " + log);
      }
      this.sourceUniform = GL32C.glGetUniformLocation(this.program, "source");
      this.acrossUniform = GL32C.glGetUniformLocation(this.program, "across");
      this.scaleUniform = GL32C.glGetUniformLocation(this.program, "scale");
      this.vertexArray = GL32C.glGenVertexArrays();
   }

   private static int compile(int type, String source) {
      int shader = GL32C.glCreateShader(type);
      GL32C.glShaderSource(shader, source);
      GL32C.glCompileShader(shader);
      if (GL32C.glGetShaderi(shader, GL32C.GL_COMPILE_STATUS) == GL32C.GL_FALSE) {
         String log = GL32C.glGetShaderInfoLog(shader);
         GL32C.glDeleteShader(shader);
         throw new IllegalStateException("Can't compile the scaler: " + log);
      }
      return shader;
   }

   // Scales the source texture (sourceWidth x sourceHeight) into target, made width x height. Leaves the GL state as it
   // found it (after the hand-off this runs inside Minecraft's rendering).
   void scale(int source, int sourceWidth, int sourceHeight, Target target, int width, int height) {
      int program = GL32C.glGetInteger(GL32C.GL_CURRENT_PROGRAM);
      int vertexArray = GL32C.glGetInteger(GL32C.GL_VERTEX_ARRAY_BINDING);
      int drawFramebuffer = GL32C.glGetInteger(GL32C.GL_DRAW_FRAMEBUFFER_BINDING);
      int readFramebuffer = GL32C.glGetInteger(GL32C.GL_READ_FRAMEBUFFER_BINDING);
      int unit = GL32C.glGetInteger(GL32C.GL_ACTIVE_TEXTURE);
      int[] viewport = new int[4];
      GL32C.glGetIntegerv(GL32C.GL_VIEWPORT, viewport);
      boolean blend = GL32C.glIsEnabled(GL32C.GL_BLEND);
      boolean scissor = GL32C.glIsEnabled(GL32C.GL_SCISSOR_TEST);
      boolean depth = GL32C.glIsEnabled(GL32C.GL_DEPTH_TEST);
      boolean cull = GL32C.glIsEnabled(GL32C.GL_CULL_FACE);
      GL32C.glDisable(GL32C.GL_BLEND);
      GL32C.glDisable(GL32C.GL_SCISSOR_TEST);
      GL32C.glDisable(GL32C.GL_DEPTH_TEST);
      GL32C.glDisable(GL32C.GL_CULL_FACE);
      GL32C.glActiveTexture(GL32C.GL_TEXTURE0 + Cinematic.unit());
      GL32C.glUseProgram(this.program);
      GL32C.glBindVertexArray(this.vertexArray);
      GL32C.glUniform1i(this.sourceUniform, Cinematic.unit());

      this.across.resize(width, sourceHeight);
      target.resize(width, height);
      this.pass(source, this.across, 1, sourceWidth / (float)width);
      this.pass(this.across.texture, target, 0, sourceHeight / (float)height);

      GL32C.glBindTexture(GL32C.GL_TEXTURE_2D, 0);
      GL32C.glBindVertexArray(vertexArray);
      GL32C.glUseProgram(program);
      GL32C.glActiveTexture(unit);
      GL32C.glBindFramebuffer(GL32C.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
      GL32C.glBindFramebuffer(GL32C.GL_READ_FRAMEBUFFER, readFramebuffer);
      GL32C.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
      setEnabled(GL32C.GL_BLEND, blend);
      setEnabled(GL32C.GL_SCISSOR_TEST, scissor);
      setEnabled(GL32C.GL_DEPTH_TEST, depth);
      setEnabled(GL32C.GL_CULL_FACE, cull);
   }

   private void pass(int source, Target into, int across, float scale) {
      GL32C.glBindFramebuffer(GL32C.GL_FRAMEBUFFER, into.framebuffer);
      GL32C.glViewport(0, 0, into.width, into.height);
      GL32C.glBindTexture(GL32C.GL_TEXTURE_2D, source);
      GL32C.glUniform1i(this.acrossUniform, across);
      GL32C.glUniform1f(this.scaleUniform, scale);
      GL32C.glDrawArrays(GL32C.GL_TRIANGLES, 0, 3);
   }

   private static void setEnabled(int capability, boolean enabled) {
      if (enabled) {
         GL32C.glEnable(capability);
      } else {
         GL32C.glDisable(capability);
      }
   }

   void delete() {
      this.across.delete();
      GL32C.glDeleteVertexArrays(this.vertexArray);
      GL32C.glDeleteProgram(this.program);
   }
}
