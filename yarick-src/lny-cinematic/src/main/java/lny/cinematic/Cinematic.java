package lny.cinematic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.earlydisplay.ElementShader;
import net.neoforged.fml.earlydisplay.QuadHelper;
import net.neoforged.fml.earlydisplay.RenderElement;
import net.neoforged.fml.earlydisplay.SimpleBufferBuilder;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL32C;

// The trailer shown over the early loading screen: video frames up to VIDEO_END, then one still (the closing logo over
// stars) faded in by a brightness table, all timed by the music; at the end only the stars fade out and the logo stays on
// black, which is how reimagined-intro's intro begins once the loading overlay has faded out. It starts out black and silent
// until it can keep up (see leadIn), and while it plays the game's loading pools run below normal priority (LoaderThreads).
// If the pack has not finished loading by the time that logo has fully come up, it rises a little and the stars dim while
// the usual loading bars show under it, until loading is done (the logo goes back down as the stars fade); the loading
// overlay is held (CinematicWindow.holdingReload) until the cinematic is over or skipped.
//
// Drawn in the loading screen's 854x480 layout. The video and the still are scaled to the size the screen is shown at
// (Scaler) and drawn pixel for pixel. The prompts in the corner are Minecraft's font at Minecraft's GUI scale, placed like
// reimagined-intro's "ESC to skip".
final class Cinematic {
   private static final double FPS = 24.0;
   private static final double VIDEO_END = 64.0;
   private static final int VIDEO_FRAMES = (int)(VIDEO_END * FPS);
   private static final double LEAD_IN = 1.0; // seconds of black before the trailer at least
   private static final double LEAD_IN_MAX = 8.0; // and at most
   private static final double SMOOTH = 0.75; // seconds the screen must have been drawn without a stall before it starts
   private static final long STALL = 60_000_000L; // nanoseconds between two frames that make a stall (they come every 16 ms)
   private static final double DECODE_BUDGET = 0.6 * FrameDecoder.THREADS / FPS; // seconds a frame may take to decode then
   private static final float HELD_DIM = 0.6F; // brightness of the stars around the held logo
   private static final float HELD_RISE = 34.0F; // how far the held logo moves up, layout pixels
   private static final int BARS_DROP = 112; // how far the loading bars move down under the held logo, layout pixels
   private static final double HOLD_MOVE = 0.7; // seconds the logo takes to rise and the stars to dim
   private static final double PROMPT_AT = 1.5; // when "ESC to skip" shows up, as in reimagined-intro
   private static final float PROMPT_ALPHA = 0x66 / 255.0F; // reimagined-intro's colour, 0x66FFFFFF
   private static final int PROMPT_LINE = 11; // GUI pixels from the top of "Modpack loaded" to the prompt under it
   private static final int UNIT = 20; // texture unit for everything drawn here, well above the ones Minecraft keeps track of

   private enum State {
      PLAYING,
      SKIPPED,
      DONE
   }

   // A line of text, its size in font pixels
   private record Label(int texture, int width, int height) {}

   private final MusicPlayer music;
   private final FrameDecoder decoder = new FrameDecoder(VIDEO_FRAMES);
   private final LoaderThreads loaders = new LoaderThreads();
   private final float[] tail;
   private final int lastFull; // the tail's last frame at full brightness: after it only the stars fade
   private final double holdAt;
   private final double end;
   private final int guiScaleOption;
   private final boolean forceUnicode;
   private final SimpleBufferBuilder bb = new SimpleBufferBuilder(4096);
   private long window;
   private Scaler scaler; // null if it can't be had: the pictures are then filtered linearly
   private int videoTexture;
   private int videoFrame = -1; // the frame in videoTexture
   private int videoWidth;
   private int videoHeight;
   private final Scaler.Target videoScaled = new Scaler.Target();
   private int starsTexture;
   private int logoTexture; // premultiplied, with its glow
   private int stillWidth; // of both
   private int stillHeight;
   private final Scaler.Target starsScaled = new Scaler.Target();
   private final Scaler.Target logoScaled = new Scaler.Target();
   private Label skipPrompt;
   private Label loadedNotice;
   private Label continuePrompt;
   private volatile State state = State.PLAYING;
   private volatile boolean loaded; // Minecraft has finished loading
   private long blackSince = -1L; // when the lead-in began
   private long lastFrame; // when the lead-in last drew a frame
   private long smoothSince; // since when it has drawn them without a stall
   private boolean started; // the lead-in is over, the music plays
   private double startBlack; // how the lead-in ended, for report()
   private double startSmooth;
   private double startDecode;
   private boolean startFrame;
   private boolean startSound;
   private int dropped; // video frames the decoder fell behind on
   private double firstDrop = -1.0;
   private boolean reported;
   private boolean held;
   private long heldNanos;
   private double resumeAt = -1.0; // music seconds when a held cinematic went on
   private float lift; // 0..1, how far the held logo has risen
   private float dim; // 0..1, how far the stars around it have dimmed: as far as the logo had risen when loading was done
   private boolean inputDown;
   private long loadedNanos = -1L;
   private double time;
   private int windowWidth; // the window's framebuffer
   private int windowHeight;
   private int screenWidth; // the part of it the screen is drawn in (letterboxed)
   private int screenHeight;

   // guiScale and forceUnicodeFont as in options.txt
   Cinematic(float volume, int guiScale, boolean forceUnicode) throws IOException {
      this.music = new MusicPlayer(FrameDecoder.read("/lnycinematic/music.ogg"), volume);
      this.guiScaleOption = guiScale;
      this.forceUnicode = forceUnicode;
      List<Float> levels = new ArrayList<>();
      try (InputStream in = Cinematic.class.getResourceAsStream("/lnycinematic/tail.txt");
           BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII))) {
         for (String line; (line = reader.readLine()) != null; ) {
            if (!line.isBlank()) {
               levels.add(Float.parseFloat(line.trim()));
            }
         }
      }
      this.tail = new float[levels.size()];
      int lastFull = 0;
      for (int i = 0; i < this.tail.length; i++) {
         this.tail[i] = levels.get(i);
         if (this.tail[i] >= 0.999F) {
            lastFull = i;
         }
      }
      this.lastFull = lastFull;
      this.holdAt = VIDEO_END + lastFull / FPS;
      this.end = VIDEO_END + this.tail.length / FPS;
   }

   void start() {
      this.music.open();
      this.decoder.start();
   }

   // On the render thread, with the window's GL context current
   void initGl(long window) throws IOException {
      this.window = window;
      try {
         this.scaler = new Scaler();
      } catch (RuntimeException e) {
         CinematicWindow.LOGGER.warn("The cinematic can't be scaled sharply, it is filtered linearly instead", e);
      }
      this.videoTexture = texture(this.scaler == null);
      this.starsTexture = this.still("stars", 3);
      this.logoTexture = this.still("logo", 4);
      this.skipPrompt = label("skip");
      this.loadedNotice = label("loaded");
      this.continuePrompt = label("continue");
   }

   private int still(String name, int channels) throws IOException {
      FrameDecoder.Frame still = FrameDecoder.decode("/lnycinematic/" + name + ".png", 0, channels);
      try {
         int texture = texture(this.scaler == null);
         upload(texture, still, true);
         this.stillWidth = still.width();
         this.stillHeight = still.height();
         return texture;
      } finally {
         still.free();
      }
   }

   // Filtered linearly, or with crisp pixels for what is drawn at a whole multiple of its size
   static int texture(boolean linear) {
      int texture = GL32C.glGenTextures();
      int unit = GL32C.glGetInteger(GL32C.GL_ACTIVE_TEXTURE);
      GL32C.glActiveTexture(GL32C.GL_TEXTURE0 + UNIT);
      GL32C.glBindTexture(GL32C.GL_TEXTURE_2D, texture);
      GL32C.glTexParameteri(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_MIN_FILTER, linear ? GL32C.GL_LINEAR : GL32C.GL_NEAREST);
      GL32C.glTexParameteri(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_MAG_FILTER, linear ? GL32C.GL_LINEAR : GL32C.GL_NEAREST);
      GL32C.glTexParameteri(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_WRAP_S, GL32C.GL_CLAMP_TO_EDGE);
      GL32C.glTexParameteri(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_WRAP_T, GL32C.GL_CLAMP_TO_EDGE);
      GL32C.glActiveTexture(unit);
      return texture;
   }

   private static void upload(int texture, FrameDecoder.Frame frame, boolean allocate) {
      int unit = GL32C.glGetInteger(GL32C.GL_ACTIVE_TEXTURE);
      GL32C.glActiveTexture(GL32C.GL_TEXTURE0 + UNIT);
      GL32C.glBindTexture(GL32C.GL_TEXTURE_2D, texture);
      GL32C.glPixelStorei(GL32C.GL_UNPACK_ALIGNMENT, frame.width() * frame.channels() % 4 == 0 ? 4 : 1);
      GL32C.glPixelStorei(GL32C.GL_UNPACK_ROW_LENGTH, 0);
      GL32C.glPixelStorei(GL32C.GL_UNPACK_SKIP_PIXELS, 0);
      GL32C.glPixelStorei(GL32C.GL_UNPACK_SKIP_ROWS, 0);
      int format = frame.channels() == 3 ? GL32C.GL_RGB : GL32C.GL_RGBA;
      if (allocate) {
         int internal = frame.channels() == 3 ? GL32C.GL_RGB8 : GL32C.GL_RGBA8;
         GL32C.glTexImage2D(GL32C.GL_TEXTURE_2D, 0, internal, frame.width(), frame.height(), 0, format, GL32C.GL_UNSIGNED_BYTE, frame.pixels());
      } else {
         GL32C.glTexSubImage2D(GL32C.GL_TEXTURE_2D, 0, 0, 0, frame.width(), frame.height(), format, GL32C.GL_UNSIGNED_BYTE, frame.pixels());
      }
      GL32C.glPixelStorei(GL32C.GL_UNPACK_ALIGNMENT, 4);
      GL32C.glActiveTexture(unit);
   }

   private static Label label(String name) throws IOException {
      FrameDecoder.Frame image = FrameDecoder.decode("/lnycinematic/labels/" + name + ".png", 0, 4);
      try {
         int texture = texture(false);
         upload(texture, image, true);
         return new Label(texture, image.width(), image.height());
      } finally {
         image.free();
      }
   }

   // From the loading overlay, on the main thread
   void markLoaded() {
      this.loaded = true;
   }

   // The loading overlay stays up while this is true
   boolean playing() {
      return this.state == State.PLAYING;
   }

   // Whether the usual loading screen (bars, messages, fox) is drawn
   boolean showsLoadingScreen() {
      return this.state == State.SKIPPED || this.waiting();
   }

   // The Mojang logo of NeoForge's loading overlay only belongs to the plain loading screen, not under the trailer's logo
   boolean showsMojangLogo() {
      return this.state == State.SKIPPED;
   }

   // How far the loading bars are moved down: under the held logo, which covers the middle of the screen where they go
   int barsDrop() {
      return this.waiting() ? BARS_DROP : 0;
   }

   // Held on the closing logo until Minecraft has loaded
   private boolean waiting() {
      return this.state == State.PLAYING && this.held && this.resumeAt < 0.0;
   }

   private void finish(State state, float fade) {
      if (this.state == State.PLAYING) {
         this.report();
         this.loaders.restore();
         this.state = state;
         this.music.fadeOut(fade);
         this.decoder.stop();
      }
   }

   // Once a frame, before drawing: the window's framebuffer size and the size of the screen drawn in the middle of it
   void update(int windowWidth, int windowHeight, int screenWidth, int screenHeight) {
      if (windowWidth > 0 && windowHeight > 0) {
         this.windowWidth = windowWidth;
         this.windowHeight = windowHeight;
         this.screenWidth = screenWidth;
         this.screenHeight = screenHeight;
      }
      if (this.state != State.PLAYING) {
         return;
      }
      long now = System.nanoTime();
      this.loaders.lower(now);
      if (!this.started && !this.leadIn(now)) {
         return;
      }
      double music = this.music.seconds();
      boolean loaded = this.loaded;
      if (loaded && this.loadedNanos < 0L) {
         this.loadedNanos = now;
      }
      if (this.resumeAt >= 0.0) {
         this.time = this.holdAt + (music - this.resumeAt);
      } else if (music >= this.holdAt && !loaded) {
         if (!this.held) {
            this.held = true;
            this.heldNanos = now;
         }
         this.time = this.holdAt;
      } else if (this.held) {
         this.resumeAt = music;
         this.time = this.holdAt;
      } else {
         this.time = music;
      }
      if (this.waiting()) {
         this.lift = (float)Math.min(1.0, (now - this.heldNanos) / 1.0E9 / HOLD_MOVE);
         this.dim = this.lift;
      } else if (this.resumeAt >= 0.0) {
         // back to where reimagined-intro's intro has it by the time the stars are gone
         this.lift = (float)(this.dim * Math.max(0.0, 1.0 - (this.time - this.holdAt) / (this.end - this.holdAt)));
      }
      if (this.time >= this.end) {
         this.finish(State.DONE, 2.0F);
         return;
      }
      if (this.time < VIDEO_END) {
         this.decoder.want(Math.min((int)(this.time * FPS), VIDEO_FRAMES - 1));
      } else {
         this.decoder.stop();
         this.report();
      }
      this.input(loaded);
   }

   // How the start went, logged once the video is over: logging (and even building a string for the first time) can wait
   // on the game, which is busy at the start
   private void report() {
      if (this.started && !this.reported) {
         this.reported = true;
         CinematicWindow.log(String.format(
            "Cinematic started after %.2f s of black (drawn smoothly for %.2f s, first frame %s, sound %s, frames decoding in %.1f ms); %s; %d loading threads held below normal priority",
            this.startBlack,
            this.startSmooth,
            this.startFrame ? "ready" : "not ready",
            this.startSound ? "ready" : "not ready",
            this.startDecode * 1000.0,
            this.dropped == 0 ? "no frame came too late" : String.format("%d frames came too late, the first at %.2f s", this.dropped, this.firstDrop),
            this.loaders.count()
         ));
      }
   }

   // Black and silent until the trailer can be shown from its first frame. The window comes up just as the game starts
   // loading at its heaviest (its mods scanned on every core, the graphics driver compiling, Java Sound opening) and early
   // frames used to stall for seconds: the music ran on, and the Mojang logo was over by the time the picture moved.
   // Meanwhile the first frame is drawn as it will be shown but at no brightness, so that whatever is done the first time
   // is done before. Goes on once it has been black for LEAD_IN, drawn without a stall for SMOOTH, the first frame is up,
   // the sound device is open and frames decode fast enough; or after LEAD_IN_MAX whatever the state.
   private boolean leadIn(long now) {
      if (this.blackSince < 0L) {
         this.blackSince = now;
         this.smoothSince = now;
      } else if (now - this.lastFrame > STALL) {
         this.smoothSince = now;
      }
      this.lastFrame = now;
      double black = (now - this.blackSince) / 1.0E9;
      double smooth = (now - this.smoothSince) / 1.0E9;
      boolean frame = this.videoFrame >= 0;
      boolean sound = this.music.ready();
      boolean ready = black >= LEAD_IN && smooth >= SMOOTH && frame && sound && this.decoder.decodesWithin(DECODE_BUDGET);
      if (ready || black >= LEAD_IN_MAX) {
         this.started = true;
         this.startBlack = black;
         this.startSmooth = smooth;
         this.startDecode = this.decoder.decodeSeconds();
         this.startFrame = frame;
         this.startSound = sound;
         this.decoder.warmedUp();
         this.music.play();
      }
      return this.started;
   }

   private boolean showsPrompt() {
      return this.state == State.PLAYING && this.time >= PROMPT_AT && !this.showsLoadingScreen() && this.screenWidth > 0;
   }

   // Minecraft's GUI scale in a window this big (Window.calculateScale), so that the prompts come out exactly as large as
   // reimagined-intro's
   private int guiScale() {
      int scale = 1;
      while (scale != this.guiScaleOption
         && scale < this.windowWidth
         && scale < this.windowHeight
         && this.windowWidth / (scale + 1) >= 320
         && this.windowHeight / (scale + 1) >= 240) {
         scale++;
      }
      if (this.forceUnicode && scale % 2 != 0) {
         scale++;
      }
      return scale;
   }

   // Where a label goes on the screen, in its pixels {x0, y0, x1, y1}: as reimagined-intro draws "ESC to skip", right-aligned
   // 16 GUI pixels from the right edge with its top 20 from the bottom; raised by `up` GUI pixels
   private int[] place(Label label, int up) {
      int scale = this.guiScale();
      int guiWidth = -Math.floorDiv(-this.screenWidth, scale);
      int guiHeight = -Math.floorDiv(-this.screenHeight, scale);
      int x0 = (guiWidth - label.width() - 16) * scale;
      int y0 = (guiHeight - 20 - up) * scale;
      return new int[]{x0, y0, x0 + label.width() * scale, y0 + label.height() * scale};
   }

   private void input(boolean loaded) {
      if (this.window == 0L || this.time < 0.5) {
         return;
      }
      boolean down = GLFW.glfwGetKey(this.window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS
         || GLFW.glfwGetKey(this.window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS
         || GLFW.glfwGetKey(this.window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS
         || GLFW.glfwGetMouseButton(this.window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS && this.overPrompt();
      if (down && !this.inputDown) {
         // once loaded, skipping goes straight on to the menu like the end of the video does
         this.finish(loaded ? State.DONE : State.SKIPPED, loaded ? 2.0F : 1.5F);
      }
      this.inputDown = down;
   }

   // Whether the mouse is on the prompt shown in the corner, which can be clicked as well
   private boolean overPrompt() {
      if (!this.showsPrompt()) {
         return false;
      }
      int[] ww = new int[1];
      int[] wh = new int[1];
      GLFW.glfwGetWindowSize(this.window, ww, wh);
      if (ww[0] <= 0 || wh[0] <= 0) {
         return false;
      }
      double[] cx = new double[1];
      double[] cy = new double[1];
      GLFW.glfwGetCursorPos(this.window, cx, cy);
      // window coordinates to the screen's pixels: the screen is shown letterboxed in the middle of the window
      double x = cx[0] * this.windowWidth / ww[0] - (this.windowWidth - this.screenWidth) / 2.0;
      double y = cy[0] * this.windowHeight / wh[0] - (this.windowHeight - this.screenHeight) / 2.0;
      boolean loaded = this.loadedNanos >= 0L;
      int[] r = this.place(loaded ? this.continuePrompt : this.skipPrompt, 0);
      if (loaded) {
         int[] notice = this.place(this.loadedNotice, PROMPT_LINE);
         r = new int[]{Math.min(r[0], notice[0]), notice[1], r[2], r[3]};
      }
      int slack = 2 * this.guiScale();
      return x >= r[0] - slack && x < r[2] + slack && y >= r[1] - slack && y < r[3] + slack;
   }

   void drawVideo(RenderElement.DisplayContext ctx) {
      if (this.state == State.DONE) {
         // the loading overlay keeps this up for two seconds more, then fades out to reimagined-intro's first frame: the same
         this.still(ctx, 0.0F, 1.0F, 0.0F);
         return;
      }
      if (this.state != State.PLAYING) {
         return;
      }
      ElementShader shader = ctx.elementShader();
      float brightness = this.started ? 1.0F : 0.0F; // the lead-in is black, whatever the loading screen's colours
      if (this.time < VIDEO_END) {
         FrameDecoder.Frame frame = this.decoder.take((int)(this.time * FPS));
         if (frame != null) {
            int skipped = frame.index() - this.videoFrame - 1;
            if (skipped > 0) {
               if (this.firstDrop < 0.0) {
                  this.firstDrop = (this.videoFrame + 1) / FPS;
               }
               this.dropped += skipped;
            }
            upload(this.videoTexture, frame, this.videoFrame < 0);
            this.videoFrame = frame.index();
            this.videoWidth = frame.width();
            this.videoHeight = frame.height();
            frame.free();
         }
         if (this.videoFrame >= 0) {
            int texture = this.scaled(this.videoTexture, this.videoWidth, this.videoHeight, this.videoFrame, this.videoScaled);
            this.quad(shader, texture, 0, ctx.width(), 0, ctx.height(), brightness, 1.0F);
         } else if (!this.started) {
            this.quad(shader, this.starsTexture, 0, ctx.width(), 0, ctx.height(), 0.0F, 1.0F);
         }
      } else {
         int i = Math.min((int)((this.time - VIDEO_END) * FPS), this.tail.length - 1);
         float stars = this.tail[i] * (1.0F - (1.0F - HELD_DIM) * smooth(this.dim));
         this.still(ctx, stars, i <= this.lastFull ? this.tail[i] : 1.0F, smooth(this.lift));
      }
   }

   // The closing logo over the stars, each as bright as given, the logo risen as far as given (0..1)
   private void still(RenderElement.DisplayContext ctx, float stars, float logo, float rise) {
      ElementShader shader = ctx.elementShader();
      int texture = this.scaled(this.starsTexture, this.stillWidth, this.stillHeight, 0, this.starsScaled);
      this.quad(shader, texture, 0, ctx.width(), 0, ctx.height(), stars, 1.0F);
      // risen by whole pixels of the screen, so that the scaled logo stays sharp
      float pixel = this.screenHeight > 0 ? ctx.height() / (float)this.screenHeight : 1.0F;
      float y = Math.round(-HELD_RISE * rise / pixel) * pixel;
      texture = this.scaled(this.logoTexture, this.stillWidth, this.stillHeight, 0, this.logoScaled);
      // premultiplied: the glow adds light where the logo is transparent
      GL32C.glBlendFuncSeparate(GL32C.GL_ONE, GL32C.GL_ONE_MINUS_SRC_ALPHA, GL32C.GL_ONE, GL32C.GL_ONE_MINUS_SRC_ALPHA);
      this.quad(shader, texture, 0, ctx.width(), y, y + ctx.height(), logo, 1.0F);
      GL32C.glBlendFuncSeparate(GL32C.GL_SRC_ALPHA, GL32C.GL_ONE_MINUS_SRC_ALPHA, GL32C.GL_ONE, GL32C.GL_ONE_MINUS_SRC_ALPHA);
   }

   // Eased in and out
   private static float smooth(float x) {
      return x * x * (3.0F - 2.0F * x);
   }

   // What to draw for a picture covering the screen: the picture itself where it is as big as the screen already (or
   // there is no scaler), else its copy scaled to the screen's size, made again once the picture or the size changes
   private int scaled(int texture, int width, int height, int content, Scaler.Target target) {
      int w = this.screenWidth;
      int h = this.screenHeight;
      if (this.scaler == null || w <= 0 || h <= 0 || w == width && h == height) {
         return texture;
      }
      if (target.content != content || target.width != w || target.height != h) {
         this.scaler.scale(texture, width, height, target, w, h);
         target.content = content;
      }
      return target.texture;
   }

   // "ESC to skip", or once Minecraft has loaded "Modpack loaded" over "ESC to continue"
   void drawUi(RenderElement.DisplayContext ctx) {
      if (!this.showsPrompt()) {
         return;
      }
      if (this.loadedNanos >= 0L) {
         float since = (System.nanoTime() - this.loadedNanos) / 1.0E9F;
         this.drawLabel(ctx, this.loadedNotice, PROMPT_LINE, PROMPT_ALPHA * Math.min(1.0F, since / 0.3F));
         this.drawLabel(ctx, this.continuePrompt, 0, PROMPT_ALPHA);
      } else {
         this.drawLabel(ctx, this.skipPrompt, 0, PROMPT_ALPHA);
      }
   }

   // Pixel for pixel: the layout is stretched over the screen, so its pixels are converted back
   private void drawLabel(RenderElement.DisplayContext ctx, Label label, int up, float alpha) {
      int[] r = this.place(label, up);
      float sx = ctx.width() / (float)this.screenWidth;
      float sy = ctx.height() / (float)this.screenHeight;
      this.quad(ctx.elementShader(), label.texture(), r[0] * sx, r[2] * sx, r[1] * sy, r[3] * sy, 1.0F, alpha);
   }

   void quad(ElementShader shader, int texture, float x0, float x1, float y0, float y1, float brightness, float alpha) {
      quad(this.bb, shader, texture, x0, x1, y0, y1, brightness, alpha);
   }

   static void quad(SimpleBufferBuilder bb, ElementShader shader, int texture, float x0, float x1, float y0, float y1, float brightness, float alpha) {
      int unit = GL32C.glGetInteger(GL32C.GL_ACTIVE_TEXTURE);
      GL32C.glActiveTexture(GL32C.GL_TEXTURE0 + UNIT);
      GL32C.glBindTexture(GL32C.GL_TEXTURE_2D, texture);
      GL32C.glActiveTexture(unit);
      shader.updateTextureUniform(UNIT);
      shader.updateRenderTypeUniform(ElementShader.RenderType.TEXTURE);
      bb.begin(SimpleBufferBuilder.Format.POS_TEX_COLOR, SimpleBufferBuilder.Mode.QUADS);
      int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
      int v = Math.round(Math.max(0.0F, Math.min(1.0F, brightness)) * 255.0F);
      QuadHelper.loadQuad(bb, x0, x1, y0, y1, 0.0F, 1.0F, 0.0F, 1.0F, a << 24 | v << 16 | v << 8 | v);
      bb.draw();
   }

   static int unit() {
      return UNIT;
   }

   void close() {
      this.finish(State.SKIPPED, 1.0F);
   }

   // With the GL context current, once nothing is drawn any more
   void deleteGl() {
      GL32C.glDeleteTextures(this.videoTexture);
      GL32C.glDeleteTextures(this.starsTexture);
      GL32C.glDeleteTextures(this.logoTexture);
      this.videoScaled.delete();
      this.starsScaled.delete();
      this.logoScaled.delete();
      if (this.scaler != null) {
         this.scaler.delete();
      }
      for (Label label : new Label[]{this.skipPrompt, this.loadedNotice, this.continuePrompt}) {
         if (label != null) {
            GL32C.glDeleteTextures(label.texture());
         }
      }
      this.bb.close();
   }
}
