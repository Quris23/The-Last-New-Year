package lny.cinematic;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.neoforged.fml.earlydisplay.ColourScheme;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.earlydisplay.ElementShader;
import net.neoforged.fml.earlydisplay.PerformanceInfo;
import net.neoforged.fml.earlydisplay.RenderElement;
import net.neoforged.fml.earlydisplay.STBHelper;
import net.neoforged.fml.earlydisplay.SimpleBufferBuilder;
import net.neoforged.fml.earlydisplay.SimpleFont;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.ImmediateWindowHandler;
import net.neoforged.fml.loading.FMLConfig.ConfigValue;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// NeoForge's early loading window (fancymodloader 4.0.45's DisplayWindow, same behaviour) with the Last New Year trailer
// played over it, see Cinematic. Selected by earlyWindowProvider = "lnycinematic" in config/fml.toml.
//
// It is a DisplayWindow because NeoForgeLoadingOverlay takes one; every public method is overridden and the superclass
// state is never used. The overlay is kept up until the cinematic is over (see holdingReload). Unlike the original, the screen is drawn at the size it is shown at (so the video keeps its
// resolution), while the loading screen's own elements are drawn as before into an 854x480 layer that is scaled up
// without filtering, keeping their crisp pixels.
public class CinematicWindow extends DisplayWindow {
   static final Logger LOGGER = LoggerFactory.getLogger("LNYCINEMATIC");
   // for the cinematic's messages: the game logs a lot while it loads, the screen must not wait on that
   private static final ThreadPoolExecutor LOG = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
      Thread thread = new Thread(r, "LNY cinematic log");
      thread.setDaemon(true);
      return thread;
   });
   private static final int[][] GL_VERSIONS = new int[][]{{4, 6}, {4, 5}, {4, 4}, {4, 3}, {4, 2}, {4, 1}, {4, 0}, {3, 3}, {3, 2}};
   private static final int LAYOUT_WIDTH = 854;
   private static final int LAYOUT_HEIGHT = 480;
   private static final String ERROR_URL = "https://links.neoforged.net/early-display-errors";
   private static final long MINFRAMETIME = TimeUnit.MILLISECONDS.toNanos(10L);
   private final AtomicBoolean animationTimerTrigger = new AtomicBoolean(true);
   private final Semaphore renderLock = new Semaphore(1);
   private final ReentrantLock crashLock = new ReentrantLock();
   private ColourScheme colourScheme;
   private ElementShader elementShader;
   private RenderElement.DisplayContext context;
   private List<RenderElement> elements;
   private RenderElement progressBars;
   private RenderElement mojang;
   private int framecount;
   private Framebuffer framebuffer;
   private Framebuffer layer;
   private SimpleBufferBuilder layerQuad;
   private ScheduledFuture<?> windowTick;
   private ScheduledFuture<?> initializationFuture;
   private Object performanceInfo;
   private long window;
   private ScheduledExecutorService renderScheduler;
   private int fbWidth;
   private int fbHeight;
   private int winWidth;
   private int winHeight;
   private int winX;
   private int winY;
   private boolean maximized;
   // options.txt's fullscreen: the window covers the screen from the start, borderless until Minecraft makes it fullscreen
   private boolean fullscreen;
   private boolean windowedRestored;
   private int windowedWidth; // the size the game was launched with
   private int windowedHeight;
   private int monitorX;
   private int monitorY;
   private int monitorWidth;
   private int monitorHeight;
   private Supplier<?> minecraft;
   private String glVersion;
   private SimpleFont font;
   private Runnable repaintTick = () -> {};
   private long nextFrameTime = 0L;
   private Method loadingOverlay;
   private volatile Cinematic cinematic;

   static {
      LOG.prestartAllCoreThreads();
   }

   static void log(String message) {
      LOG.execute(() -> LOGGER.info(message));
   }

   @Override
   public String name() {
      return "lnycinematic";
   }

   @Override
   public Runnable initialize(String[] arguments) {
      WindowsGhosting.disable();
      OptionParser parser = new OptionParser();
      ArgumentAcceptingOptionSpec<String> mcversionopt = parser.accepts("fml.mcVersion").withRequiredArg().ofType(String.class);
      ArgumentAcceptingOptionSpec<String> forgeversionopt = parser.accepts("fml.neoForgeVersion").withRequiredArg().ofType(String.class);
      ArgumentAcceptingOptionSpec<Integer> widthopt = parser.accepts("width")
         .withRequiredArg()
         .ofType(Integer.class)
         .defaultsTo(FMLConfig.getIntConfigValue(ConfigValue.EARLY_WINDOW_WIDTH));
      ArgumentAcceptingOptionSpec<Integer> heightopt = parser.accepts("height")
         .withRequiredArg()
         .ofType(Integer.class)
         .defaultsTo(FMLConfig.getIntConfigValue(ConfigValue.EARLY_WINDOW_HEIGHT));
      OptionSpecBuilder maximizedopt = parser.accepts("earlywindow.maximized");
      parser.allowsUnrecognizedOptions();
      OptionSet parsed = parser.parse(arguments);
      this.winWidth = parsed.valueOf(widthopt);
      this.winHeight = parsed.valueOf(heightopt);
      FMLConfig.updateConfig(ConfigValue.EARLY_WINDOW_WIDTH, this.winWidth);
      FMLConfig.updateConfig(ConfigValue.EARLY_WINDOW_HEIGHT, this.winHeight);
      Map<String, String> options = Map.of();
      try {
         options = Files.readAllLines(FMLPaths.GAMEDIR.get().resolve(Paths.get("options.txt")))
            .stream()
            .map(l -> l.split(":"))
            .filter(a -> a.length == 2)
            .collect(Collectors.toMap(a -> a[0], a -> a[1], (a, b) -> b));
      } catch (IOException ignored) {
      }
      if (System.getenv("FML_EARLY_WINDOW_DARK") != null) {
         this.colourScheme = ColourScheme.BLACK;
      } else {
         this.colourScheme = Boolean.parseBoolean(options.getOrDefault("darkMojangStudiosBackground", "false")) ? ColourScheme.BLACK : ColourScheme.RED;
      }

      this.maximized = parsed.has(maximizedopt) || FMLConfig.getBoolConfigValue(ConfigValue.EARLY_WINDOW_MAXIMIZED);
      this.fullscreen = Boolean.parseBoolean(options.getOrDefault("fullscreen", "false"));
      this.windowedWidth = this.winWidth;
      this.windowedHeight = this.winHeight;
      String forgeVersion = parsed.valueOf(forgeversionopt);
      StartupNotificationManager.modLoaderConsumer().ifPresent(c -> c.accept("NeoForge loading " + forgeVersion));
      try {
         Constructor<?> ctor = PerformanceInfo.class.getDeclaredConstructor();
         ctor.setAccessible(true);
         this.performanceInfo = ctor.newInstance();
      } catch (ReflectiveOperationException e) {
         throw new IllegalStateException("Can't create the loading screen's performance info", e);
      }
      try {
         // master volume times the loading screen's own (witherstormmod's slider among the sound options)
         float volume = 1.0F;
         try {
            volume = Float.parseFloat(options.getOrDefault("soundCategory_master", "1.0"))
               * Float.parseFloat(options.getOrDefault("lny_cinematicVolume", "1.0"));
         } catch (NumberFormatException ignored) {
         }
         int guiScale = 0;
         try {
            guiScale = Integer.parseInt(options.getOrDefault("guiScale", "0"));
         } catch (NumberFormatException ignored) {
         }
         boolean unicode = Boolean.parseBoolean(options.getOrDefault("forceUnicodeFont", "false"));
         this.cinematic = new Cinematic(Math.max(0.0F, Math.min(1.0F, volume)), guiScale, unicode);
         this.cinematic.start();
      } catch (Throwable e) {
         LOGGER.error("The cinematic can't play, showing the plain loading screen", e);
         this.dropCinematic();
      }
      return this.start(parsed.valueOf(mcversionopt), forgeVersion);
   }

   private void dropCinematic() {
      Cinematic cinematic = this.cinematic;
      this.cinematic = null;
      if (cinematic != null) {
         cinematic.close();
      }
   }

   private void renderThreadFunc() {
      if (this.renderLock.tryAcquire()) {
         try {
            long nt;
            if ((nt = System.nanoTime()) >= this.nextFrameTime) {
               this.nextFrameTime = nt + MINFRAMETIME;
               GLFW.glfwMakeContextCurrent(this.window);
               this.context.elementShader().activate();
               this.context.elementShader().updateScreenSizeUniform(this.context.scaledWidth(), this.context.scaledHeight());
               this.paintFramebuffer(this.fbWidth, this.fbHeight, 1.0F);
               this.context.elementShader().clear();
               this.framebuffer.deactivate();
               GL32C.glViewport(0, 0, this.fbWidth, this.fbHeight);
               this.framebuffer.draw(this.fbWidth, this.fbHeight, this.colourScheme.background());
               GLFW.glfwSwapBuffers(this.window);
            }
         } catch (Throwable e) {
            LOGGER.error("BARF", e);
         } finally {
            if (this.windowTick != null) {
               GLFW.glfwMakeContextCurrent(0L);
            }

            this.renderLock.release();
         }
      }
   }

   private void initRender(String mcVersion, String forgeVersion) {
      GLFW.glfwMakeContextCurrent(this.window);
      GLFW.glfwSwapInterval(1);
      GL.createCapabilities();
      LOGGER.info("GL info: " + GL32C.glGetString(7937) + " GL version " + GL32C.glGetString(7938) + ", " + GL32C.glGetString(7936));
      this.elementShader = new ElementShader();

      try {
         this.elementShader.init();
      } catch (Throwable e) {
         LOGGER.error("Crash during shader initialization", e);
         this.crashElegantly("An error occurred initializing shaders.");
      }

      GL32C.glClearColor(this.colourScheme.background().redf(), this.colourScheme.background().greenf(), this.colourScheme.background().bluef(), 1.0F);
      this.context = new RenderElement.DisplayContext(LAYOUT_WIDTH, LAYOUT_HEIGHT, 1, this.elementShader, this.colourScheme, (PerformanceInfo)this.performanceInfo);
      this.framebuffer = new Framebuffer(LAYOUT_WIDTH, LAYOUT_HEIGHT, GL32C.GL_NEAREST);
      this.layer = new Framebuffer(LAYOUT_WIDTH, LAYOUT_HEIGHT, GL32C.GL_NEAREST);
      this.layerQuad = new SimpleBufferBuilder(256);

      try {
         this.font = new SimpleFont("Monocraft.ttf", 1, 200000, 6);
      } catch (Throwable e) {
         LOGGER.error("Crash during font initialization", e);
         this.crashElegantly("An error occurred initializing a font for rendering. " + e.getMessage());
      }

      this.progressBars = RenderElement.progressBars(this.font);
      this.elements = new ArrayList<>(
         Arrays.asList(
            RenderElement.fox(this.font),
            RenderElement.logMessageOverlay(this.font),
            RenderElement.forgeVersionOverlay(this.font, mcVersion + "-" + forgeVersion.split("-")[0]),
            RenderElement.performanceBar(this.font),
            this.progressBars
         )
      );
      Calendar date = Calendar.getInstance();
      if (FMLConfig.getBoolConfigValue(ConfigValue.EARLY_WINDOW_SQUIR) || date.get(Calendar.MONTH) == Calendar.APRIL && date.get(Calendar.DAY_OF_MONTH) == 1) {
         this.elements.add(0, RenderElement.squir());
      }

      if (this.cinematic != null) {
         try {
            this.cinematic.initGl(this.window);
         } catch (Throwable e) {
            LOGGER.error("The cinematic can't draw, showing the plain loading screen", e);
            this.dropCinematic();
         }
      }

      GL32C.glEnable(3042);
      GL32C.glBlendFunc(770, 771);
      GLFW.glfwMakeContextCurrent(0L);
      // every frame of the video instead of the original's 20 repaints a second
      this.windowTick = this.renderScheduler.scheduleAtFixedRate(this::renderThreadFunc, 16L, 16L, TimeUnit.MILLISECONDS);
      Method update = findMethod(PerformanceInfo.class, "update");
      this.renderScheduler.scheduleAtFixedRate(() -> {
         try {
            update.invoke(this.performanceInfo);
         } catch (ReflectiveOperationException ignored) {
         }
      }, 0L, 500L, TimeUnit.MILLISECONDS);
      this.renderScheduler.scheduleAtFixedRate(() -> this.animationTimerTrigger.set(true), 1L, 50L, TimeUnit.MILLISECONDS);
   }

   private static Method findMethod(Class<?> type, String name) {
      try {
         Method method = type.getDeclaredMethod(name);
         method.setAccessible(true);
         return method;
      } catch (NoSuchMethodException e) {
         throw new IllegalStateException(e);
      }
   }

   // Paints the screen into this.framebuffer, first sized to how big the screen is shown in a window of the given size:
   // the cinematic at that resolution, and the loading screen's elements in their own layer, scaled up from 854x480
   private void paintFramebuffer(int windowWidth, int windowHeight, float alpha) {
      if (windowWidth > 0 && windowHeight > 0) {
         float scale = Math.min(windowWidth / (float)LAYOUT_WIDTH, windowHeight / (float)LAYOUT_HEIGHT);
         this.framebuffer.resize(Math.max(1, Math.round(LAYOUT_WIDTH * scale)), Math.max(1, Math.round(LAYOUT_HEIGHT * scale)));
      }
      GL32C.glEnable(GL32C.GL_BLEND);
      // colour blended as usual, alpha accumulated so that the layer holds premultiplied colour and the screen stays opaque
      GL32C.glBlendFuncSeparate(GL32C.GL_SRC_ALPHA, GL32C.GL_ONE_MINUS_SRC_ALPHA, GL32C.GL_ONE, GL32C.GL_ONE_MINUS_SRC_ALPHA);
      Cinematic cinematic = this.cinematic;
      if (cinematic != null) {
         try {
            cinematic.update(windowWidth, windowHeight, this.framebuffer.width, this.framebuffer.height);
         } catch (Throwable e) {
            LOGGER.error("The cinematic failed, showing the plain loading screen", e);
            this.dropCinematic();
            cinematic = null;
         }
      }

      boolean loadingScreen = cinematic == null || cinematic.showsLoadingScreen();
      if (loadingScreen) {
         this.layer.activate();
         GL32C.glViewport(0, 0, LAYOUT_WIDTH, LAYOUT_HEIGHT);
         GL32C.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         GL32C.glClear(GL32C.GL_COLOR_BUFFER_BIT);
         if (this.mojang != null && (cinematic == null || cinematic.showsMojangLogo())) {
            this.mojang.render(this.context, this.framecount);
         }
         int drop = cinematic == null ? 0 : cinematic.barsDrop();
         this.elements.removeIf(element -> {
            if (element == this.progressBars && drop != 0) {
               // the layout's y grows downwards, the viewport's upwards; the layer is shown flipped
               GL32C.glViewport(0, drop, LAYOUT_WIDTH, LAYOUT_HEIGHT);
               boolean keep = element.render(this.context, this.framecount);
               GL32C.glViewport(0, 0, LAYOUT_WIDTH, LAYOUT_HEIGHT);
               return !keep;
            }
            return !element.render(this.context, this.framecount);
         });
      }

      this.framebuffer.activate();
      GL32C.glViewport(0, 0, this.framebuffer.width, this.framebuffer.height);
      ColourScheme.Colour background = this.colourScheme.background();
      GL32C.glClearColor(background.redf(), background.greenf(), background.bluef(), alpha);
      GL32C.glClear(GL32C.GL_COLOR_BUFFER_BIT);
      if (cinematic != null) {
         try {
            cinematic.drawVideo(this.context);
         } catch (Throwable e) {
            LOGGER.error("The cinematic failed, showing the plain loading screen", e);
            this.dropCinematic();
            cinematic = null;
         }
      }
      if (loadingScreen) {
         GL32C.glBlendFunc(GL32C.GL_ONE, GL32C.GL_ONE_MINUS_SRC_ALPHA);
         Cinematic.quad(this.layerQuad, this.elementShader, this.layer.texture, 0, LAYOUT_WIDTH, 0, LAYOUT_HEIGHT, 1.0F, 1.0F);
         GL32C.glBlendFuncSeparate(GL32C.GL_SRC_ALPHA, GL32C.GL_ONE_MINUS_SRC_ALPHA, GL32C.GL_ONE, GL32C.GL_ONE_MINUS_SRC_ALPHA);
      }
      if (cinematic != null) {
         try {
            cinematic.drawUi(this.context);
         } catch (Throwable e) {
            LOGGER.error("The cinematic failed, showing the plain loading screen", e);
            this.dropCinematic();
         }
      }
      GL32C.glBlendFunc(GL32C.GL_SRC_ALPHA, GL32C.GL_ONE_MINUS_SRC_ALPHA); // as the original leaves it
      if (this.animationTimerTrigger.compareAndSet(true, false)) {
         this.framecount++;
      }
   }

   // Used by NeoForgeLoadingOverlay once Minecraft has the window: paints into the framebuffer it then draws, letterboxed
   // in the window at the size this framebuffer is made
   @Override
   public void render(int alpha) {
      if (this.fullscreen && !this.windowedRestored && GLFW.glfwGetWindowMonitor(this.window) != 0L) {
         this.windowedRestored = true;
         this.restoreWindowed();
      }
      int currentVAO = GL32C.glGetInteger(GL32C.GL_VERTEX_ARRAY_BINDING);
      int currentFB = GL32C.glGetInteger(GL32C.GL_DRAW_FRAMEBUFFER_BINDING);
      int[] width = new int[1];
      int[] height = new int[1];
      GLFW.glfwGetFramebufferSize(this.window, width, height);
      this.elementShader.activate();
      this.elementShader.updateScreenSizeUniform(this.context.scaledWidth(), this.context.scaledHeight());
      this.paintFramebuffer(width[0], height[0], alpha / 255.0F);
      this.elementShader.clear();
      this.framebuffer.deactivate();
      GL32C.glBindVertexArray(currentVAO);
      GL32C.glBindFramebuffer(GL32C.GL_FRAMEBUFFER, currentFB);
   }

   // Once Minecraft has made the borderless window fullscreen: it took the window's screen-wide size as the one to go back
   // to on leaving fullscreen (F11); that becomes the size the game was launched with, centred, as it would have been,
   // and the window gets its frame back for then
   private void restoreWindowed() {
      GLFW.glfwSetWindowAttrib(this.window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
      try {
         Object minecraft = this.minecraft.get();
         Object window = minecraft.getClass().getMethod("getWindow").invoke(minecraft);
         String[] fields = {"windowedX", "windowedY", "windowedWidth", "windowedHeight"};
         int[] values = {
            (this.monitorWidth - this.windowedWidth) / 2 + this.monitorX,
            (this.monitorHeight - this.windowedHeight) / 2 + this.monitorY,
            this.windowedWidth,
            this.windowedHeight
         };
         for (int i = 0; i < fields.length; i++) {
            Field field = window.getClass().getDeclaredField(fields[i]);
            field.setAccessible(true);
            field.setInt(window, values[i]);
         }
      } catch (ReflectiveOperationException | RuntimeException e) {
         LOGGER.warn("Can't set the window size to go back to from fullscreen", e);
      }
   }

   public Runnable start(String mcVersion, String forgeVersion) {
      this.renderScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
         Thread thread = Executors.defaultThreadFactory().newThread(r);
         thread.setDaemon(true);
         thread.setPriority(Thread.NORM_PRIORITY + 2); // the cinematic keeps its frame rate while the game loads on every core
         return thread;
      });
      this.initWindow(mcVersion);
      this.initializationFuture = this.renderScheduler.schedule(() -> this.initRender(mcVersion, forgeVersion), 1L, TimeUnit.MILLISECONDS);
      return this::periodicTick;
   }

   @Override
   public String getGLVersion() {
      return this.glVersion;
   }

   private void crashElegantly(String errorDetails) {
      this.crashLock.lock();
      this.dropCinematic();
      StringBuilder msgBuilder = new StringBuilder(2000);
      msgBuilder.append("Failed to initialize the mod loading system and display.\n");
      msgBuilder.append("\n\n");
      msgBuilder.append("Failure details:\n");
      msgBuilder.append(errorDetails);
      msgBuilder.append("\n\n");
      msgBuilder.append("If you click yes, we will try and open " + ERROR_URL + " in your default browser");
      LOGGER.error("ERROR DISPLAY\n{}", msgBuilder);
      Thread thread = new Thread(() -> {
         boolean res = TinyFileDialogs.tinyfd_messageBox("Minecraft: NeoForge", msgBuilder.toString(), "yesno", "error", false);
         if (res) {
            try {
               Desktop.getDesktop().browse(URI.create(ERROR_URL));
            } catch (IOException e) {
               TinyFileDialogs.tinyfd_messageBox(
                  "Minecraft: NeoForge", "Sadly, we couldn't open your browser.\nVisit " + ERROR_URL, "ok", "error", false
               );
            }
         }
      }, "crash-report");
      thread.setDaemon(true);
      thread.start();

      try {
         thread.join();
      } catch (InterruptedException ignored) {
      }

      System.exit(1);
   }

   public void initWindow(String mcVersion) {
      long glfwInitBegin = System.nanoTime();
      if (!GLFW.glfwInit()) {
         this.crashElegantly("We are unable to initialize the graphics system.\nglfwInit failed.\n");
         throw new IllegalStateException("Unable to initialize GLFW");
      }
      long glfwInitEnd = System.nanoTime();
      if (glfwInitEnd - glfwInitBegin > 1.0E9) {
         LOGGER.error("WARNING : glfwInit took {} seconds to start.", (glfwInitEnd - glfwInitBegin) / 1.0E9);
      }

      this.handleLastGLFWError((error, description) -> LOGGER.error(String.format("Suppressing Last GLFW error: [0x%X]%s", error, description)));
      GLFW.glfwDefaultWindowHints();
      GLFW.glfwWindowHint(139265, 196609);
      GLFW.glfwWindowHint(139275, 221185);
      GLFW.glfwWindowHint(131076, 0);
      GLFW.glfwWindowHint(131075, 1);
      if (mcVersion != null) {
         String vanillaWindowTitle = "Minecraft* " + mcVersion;
         GLFW.glfwWindowHintString(147457, vanillaWindowTitle);
         GLFW.glfwWindowHintString(147458, vanillaWindowTitle);
      }

      long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
      if (primaryMonitor == 0L) {
         LOGGER.error("Failed to find a primary monitor - this means LWJGL isn't working properly");
         this.crashElegantly("Failed to locate a primary monitor.\nglfwGetPrimaryMonitor failed.\n");
         throw new IllegalStateException("Can't find a primary monitor");
      }
      GLFWVidMode vidmode = GLFW.glfwGetVideoMode(primaryMonitor);
      if (vidmode == null) {
         LOGGER.error("Failed to get the current display video mode.");
         this.crashElegantly("Failed to get current display resolution.\nglfwGetVideoMode failed.\n");
         throw new IllegalStateException("Can't get a resolution");
      }
      int[] x = new int[1];
      int[] y = new int[1];
      GLFW.glfwGetMonitorPos(primaryMonitor, x, y);
      this.monitorX = x[0];
      this.monitorY = y[0];
      this.monitorWidth = vidmode.width();
      this.monitorHeight = vidmode.height();
      if (this.fullscreen) {
         // not Minecraft's own fullscreen (it would undo that while taking the window over): a borderless window over
         // the whole screen, which it then switches to fullscreen without anything to see
         GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
         this.winWidth = this.monitorWidth;
         this.winHeight = this.monitorHeight;
      }
      LOGGER.info("Screen {}x{}, window {}", this.monitorWidth, this.monitorHeight, this.fullscreen ? "covering it" : this.maximized ? "maximized" : this.winWidth + "x" + this.winHeight);
      long window = 0L;
      AtomicBoolean successfulWindow = new AtomicBoolean(false);
      ScheduledFuture<?> windowFailFuture = this.renderScheduler.schedule(() -> {
         if (!successfulWindow.get()) {
            this.crashElegantly("Timed out trying to setup the Game Window.");
         }
      }, 30L, TimeUnit.SECONDS);
      int versidx = 0;
      List<String> skipVersions = FMLConfig.getListConfigValue(ConfigValue.EARLY_WINDOW_SKIP_GL_VERSIONS);
      String[] lastGLError = new String[GL_VERSIONS.length];

      do {
         String glVersionToTry = GL_VERSIONS[versidx][0] + "." + GL_VERSIONS[versidx][1];
         if (skipVersions.contains(glVersionToTry)) {
            LOGGER.info("Skipping GL version " + glVersionToTry + " because of configuration");
            versidx++;
         } else {
            LOGGER.info("Trying GL version " + glVersionToTry);
            GLFW.glfwWindowHint(139266, GL_VERSIONS[versidx][0]);
            GLFW.glfwWindowHint(139267, GL_VERSIONS[versidx][1]);
            GLFW.glfwWindowHint(139272, 204801);
            GLFW.glfwWindowHint(139270, 1);
            window = GLFW.glfwCreateWindow(this.winWidth, this.winHeight, "Minecraft: NeoForge Loading...", 0L, 0L);
            int erridx = versidx;
            this.handleLastGLFWError(
               (error, description) -> lastGLError[erridx] = String.format(
                  "Trying %d.%d: GLFW error: [0x%X]%s", GL_VERSIONS[erridx][0], GL_VERSIONS[erridx][1], error, description
               )
            );
            if (lastGLError[versidx] != null) {
               LOGGER.trace(lastGLError[versidx]);
            }

            versidx++;
         }
      } while (window == 0L && versidx < GL_VERSIONS.length);

      if (versidx == GL_VERSIONS.length && window == 0L) {
         LOGGER.error("Failed to find any valid GLFW profile. " + lastGLError[0]);
         this.crashElegantly(
            "Failed to find a valid GLFW profile.\nWe tried "
               + Arrays.stream(GL_VERSIONS)
                  .map(p -> p[0] + "." + p[1])
                  .filter(o -> !skipVersions.contains(o))
                  .collect(Collector.of(() -> new StringJoiner(", ").setEmptyValue("no versions"), StringJoiner::add, StringJoiner::merge, StringJoiner::toString))
               + " but none of them worked.\n"
               + Arrays.stream(lastGLError).filter(Objects::nonNull).collect(Collectors.joining("\n"))
         );
         throw new IllegalStateException("Failed to create a GLFW window with any profile");
      }
      successfulWindow.set(true);
      if (!windowFailFuture.cancel(true)) {
         throw new IllegalStateException("We died but didn't somehow?");
      }
      String requestedVersion = GL_VERSIONS[versidx - 1][0] + "." + GL_VERSIONS[versidx - 1][1];
      int maj = GLFW.glfwGetWindowAttrib(window, 139266);
      int min = GLFW.glfwGetWindowAttrib(window, 139267);
      String gotVersion = maj + "." + min;
      LOGGER.info("Requested GL version " + requestedVersion + " got version " + gotVersion);
      this.glVersion = gotVersion;
      this.window = window;
      if (this.maximized && !this.fullscreen) {
         GLFW.glfwMaximizeWindow(window);
      }

      GLFW.glfwGetWindowSize(window, x, y);
      this.winWidth = x[0];
      this.winHeight = y[0];
      if (this.fullscreen) {
         GLFW.glfwSetWindowPos(window, this.monitorX, this.monitorY);
      } else if (!this.maximized) {
         // (the original centres a maximized window too, moving it off the place maximizing gave it)
         GLFW.glfwSetWindowPos(window, (this.monitorWidth - this.winWidth) / 2 + this.monitorX, (this.monitorHeight - this.winHeight) / 2 + this.monitorY);
      }
      int[] channels = new int[1];

      try (GLFWImage.Buffer glfwImgBuffer = GLFWImage.create(MemoryUtil.getAllocator().malloc(GLFWImage.SIZEOF), 1);
           GLFWImage glfwImages = GLFWImage.malloc()) {
         ByteBuffer imgBuffer = STBHelper.loadImageFromClasspath("neoforged_icon.png", 20000, x, y, channels);
         glfwImgBuffer.put(glfwImages.set(x[0], y[0], imgBuffer));
         glfwImgBuffer.flip();
         GLFW.glfwSetWindowIcon(window, glfwImgBuffer);
         STBImage.stbi_image_free(imgBuffer);
      } catch (NullPointerException e) {
         System.err.println("Failed to load NeoForged icon");
      }

      this.handleLastGLFWError((error, description) -> LOGGER.debug(String.format("Suppressing GLFW icon error: [0x%X]%s", error, description)));
      GLFW.glfwSetFramebufferSizeCallback(window, this::fbResize);
      GLFW.glfwSetWindowPosCallback(window, this::winMove);
      GLFW.glfwSetWindowSizeCallback(window, this::winResize);
      GLFW.glfwShowWindow(window);
      GLFW.glfwGetWindowPos(window, x, y);
      this.handleLastGLFWError((error, description) -> LOGGER.debug(String.format("Suppressing GLFW get window position error: [0x%X]%s", error, description)));
      this.winX = x[0];
      this.winY = y[0];
      GLFW.glfwGetFramebufferSize(window, x, y);
      this.fbWidth = x[0];
      this.fbHeight = y[0];
      GLFW.glfwPollEvents();
   }

   private void winResize(long window, int width, int height) {
      if (window == this.window && width != 0 && height != 0) {
         this.winWidth = width;
         this.winHeight = height;
      }
   }

   private void fbResize(long window, int width, int height) {
      if (window == this.window && width != 0 && height != 0) {
         this.fbWidth = width;
         this.fbHeight = height;
      }
   }

   private void winMove(long window, int x, int y) {
      if (window == this.window) {
         this.winX = x;
         this.winY = y;
      }
   }

   private void handleLastGLFWError(BiConsumer<Integer, String> handler) {
      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
         int error = GLFW.glfwGetError(pointerbuffer);
         if (error != 0) {
            long pDescription = pointerbuffer.get();
            String description = pDescription == 0L ? "" : MemoryUtil.memUTF8(pDescription);
            handler.accept(error, description);
         }
      }
   }

   @Override
   public long setupMinecraftWindow(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitorSupplier) {
      try {
         this.initializationFuture.get(30L, TimeUnit.SECONDS);
      } catch (ExecutionException | InterruptedException e) {
         throw new RuntimeException(e);
      } catch (TimeoutException e) {
         Thread.dumpStack();
         this.crashElegantly("We seem to be having trouble initializing the window, waited for 30 seconds");
      }

      ImmediateWindowHandler.updateProgress("Initializing Game Graphics");

      while (!this.windowTick.isDone()) {
         this.windowTick.cancel(false);
      }

      int tries = 0;
      boolean renderlockticket = false;

      do {
         try {
            renderlockticket = this.renderLock.tryAcquire(100L, TimeUnit.MILLISECONDS);
            if (++tries > 9) {
               Thread.dumpStack();
               this.crashElegantly("We seem to be having trouble handing off the window, tried for 1 second");
            }
         } catch (InterruptedException e) {
            Thread.interrupted();
         }
      } while (!renderlockticket);

      this.renderLock.release();
      GLFW.glfwMakeContextCurrent(this.window);
      GLFW.glfwSetWindowTitle(this.window, title.get());
      GLFW.glfwSwapInterval(0);
      GLFW.glfwSetFramebufferSizeCallback(this.window, null).free();
      GLFW.glfwSetWindowPosCallback(this.window, null).free();
      GLFW.glfwSetWindowSizeCallback(this.window, null).free();
      this.repaintTick = this::renderThreadFunc;
      this.windowTick = null;
      return this.window;
   }

   @Override
   public boolean positionWindow(Optional<Object> monitor, IntConsumer widthSetter, IntConsumer heightSetter, IntConsumer xSetter, IntConsumer ySetter) {
      widthSetter.accept(this.winWidth);
      heightSetter.accept(this.winHeight);
      xSetter.accept(this.winX);
      ySetter.accept(this.winY);
      return true;
   }

   @Override
   public void updateFramebufferSize(IntConsumer width, IntConsumer height) {
      width.accept(this.fbWidth);
      height.accept(this.fbHeight);
   }

   // NeoForgeLoadingOverlay closes as soon as the reload it shows is done; it is given a view of that reload which is only
   // done once the cinematic is over too
   @Override
   @SuppressWarnings("unchecked")
   public <T> Supplier<T> loadingOverlay(Supplier<?> mc, Supplier<?> ri, Consumer<Optional<Throwable>> ex, boolean fade) {
      this.minecraft = mc;
      Supplier<?> reload = () -> this.holdingReload(ri.get());
      try {
         return (Supplier<T>)this.loadingOverlay.invoke(null, mc, reload, ex, this);
      } catch (Throwable e) {
         throw new IllegalStateException("How did you get here?", e);
      }
   }

   // A ReloadInstance passing everything on to the real one, except that it is not done while the cinematic plays. It is
   // also how the cinematic learns that Minecraft has loaded.
   private Object holdingReload(Object reload) {
      try {
         Class<?> type = Class.forName("net.minecraft.server.packs.resources.ReloadInstance", false, reload.getClass().getClassLoader());
         Method isDone = type.getMethod("isDone");
         return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
               return switch (method.getName()) {
                  case "equals" -> proxy == args[0];
                  case "hashCode" -> System.identityHashCode(proxy);
                  default -> "held " + reload;
               };
            }
            Object result;
            try {
               result = method.invoke(reload, args);
            } catch (InvocationTargetException e) {
               throw e.getCause();
            }
            Cinematic cinematic = this.cinematic;
            if (method.equals(isDone) && (Boolean)result && cinematic != null) {
               cinematic.markLoaded();
               return !cinematic.playing();
            }
            return result;
         });
      } catch (ReflectiveOperationException | RuntimeException e) {
         LOGGER.error("Can't keep the loading screen up for the cinematic, it will end once Minecraft has loaded", e);
         return reload;
      }
   }

   @Override
   public void updateModuleReads(ModuleLayer layer) {
      Module fm = layer.findModule("neoforge").orElseThrow();
      this.getClass().getModule().addReads(fm);
      Class<?> clz = Class.forName(fm, "net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay");
      Map<String, Method> methods = Arrays.stream(clz.getMethods())
         .filter(m -> Modifier.isStatic(m.getModifiers()))
         .collect(Collectors.toMap(Method::getName, Function.identity()));
      this.loadingOverlay = methods.get("newInstance");
   }

   @Override
   public int getFramebufferTextureId() {
      return this.framebuffer.texture;
   }

   @Override
   public RenderElement.DisplayContext context() {
      return this.context;
   }

   @Override
   public void periodicTick() {
      GLFW.glfwPollEvents();
      this.repaintTick.run();
   }

   @Override
   public void addMojangTexture(int textureId) {
      this.mojang = RenderElement.mojang(textureId, this.framecount);
   }

   @Override
   public void close() {
      Cinematic cinematic = this.cinematic;
      this.dropCinematic();
      if (cinematic != null) {
         cinematic.deleteGl();
      }
      this.renderScheduler.shutdown();
      this.framebuffer.close();
      this.layer.close();
      this.layerQuad.close();
      this.context.elementShader().close();
      SimpleBufferBuilder.destroy();
   }

   @Override
   public void crash(String message) {
      this.crashElegantly(message);
   }

   // An offscreen target (EarlyFramebuffer is not open to other packages)
   private static final class Framebuffer {
      int width;
      int height;
      final int framebuffer;
      final int texture;

      Framebuffer(int width, int height, int filter) {
         this.framebuffer = GL32C.glGenFramebuffers();
         this.texture = GL32C.glGenTextures();
         int unit = GL32C.glGetInteger(GL32C.GL_ACTIVE_TEXTURE);
         GL32C.glActiveTexture(GL32C.GL_TEXTURE0 + Cinematic.unit());
         GL32C.glBindTexture(GL32C.GL_TEXTURE_2D, this.texture);
         GL32C.glTexParameteri(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_MIN_FILTER, filter);
         GL32C.glTexParameteri(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_MAG_FILTER, filter);
         GL32C.glTexParameteri(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_WRAP_S, GL32C.GL_CLAMP_TO_EDGE);
         GL32C.glTexParameteri(GL32C.GL_TEXTURE_2D, GL32C.GL_TEXTURE_WRAP_T, GL32C.GL_CLAMP_TO_EDGE);
         GL32C.glActiveTexture(unit);
         this.resize(width, height);
         GL32C.glBindFramebuffer(GL32C.GL_FRAMEBUFFER, this.framebuffer);
         GL32C.glFramebufferTexture2D(GL32C.GL_FRAMEBUFFER, GL32C.GL_COLOR_ATTACHMENT0, GL32C.GL_TEXTURE_2D, this.texture, 0);
         GL32C.glBindFramebuffer(GL32C.GL_FRAMEBUFFER, 0);
      }

      void resize(int width, int height) {
         if (width == this.width && height == this.height) {
            return;
         }
         this.width = width;
         this.height = height;
         int unit = GL32C.glGetInteger(GL32C.GL_ACTIVE_TEXTURE);
         GL32C.glActiveTexture(GL32C.GL_TEXTURE0 + Cinematic.unit());
         GL32C.glBindTexture(GL32C.GL_TEXTURE_2D, this.texture);
         GL32C.glTexImage2D(GL32C.GL_TEXTURE_2D, 0, GL32C.GL_RGBA8, width, height, 0, GL32C.GL_RGBA, GL32C.GL_UNSIGNED_BYTE, (ByteBuffer)null);
         GL32C.glActiveTexture(unit);
      }

      void activate() {
         GL32C.glBindFramebuffer(GL32C.GL_FRAMEBUFFER, this.framebuffer);
      }

      void deactivate() {
         GL32C.glBindFramebuffer(GL32C.GL_FRAMEBUFFER, 0);
      }

      // Into the window's own framebuffer (it is sized to fit): centred, flipped as the layout's y grows downwards
      void draw(int windowWidth, int windowHeight, ColourScheme.Colour background) {
         int left = Math.max(0, (windowWidth - this.width) / 2);
         int top = Math.max(0, (windowHeight - this.height) / 2);
         GL32C.glBindFramebuffer(GL32C.GL_DRAW_FRAMEBUFFER, 0);
         GL32C.glBindFramebuffer(GL32C.GL_READ_FRAMEBUFFER, this.framebuffer);
         GL32C.glClearColor(background.redf(), background.greenf(), background.bluef(), 1.0F);
         GL32C.glClear(GL32C.GL_COLOR_BUFFER_BIT);
         GL32C.glBlitFramebuffer(0, this.height, this.width, 0, left, top, left + this.width, top + this.height, GL32C.GL_COLOR_BUFFER_BIT, GL32C.GL_NEAREST);
         GL32C.glBindFramebuffer(GL32C.GL_FRAMEBUFFER, 0);
      }

      void close() {
         GL32C.glDeleteTextures(this.texture);
         GL32C.glDeleteFramebuffers(this.framebuffer);
      }
   }
}
