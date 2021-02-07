package engine.io;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_DONT_CARE;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorPos;
import static org.lwjgl.glfw.GLFW.glfwGetMonitors;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.glfw.GLFW.glfwSetWindowMonitor;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.glDebugMessageControlARB;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;

import elem.CursorType;
import main.Game;
import scenes.SceneHandler;

public class Window {

	public static int WIDTH, HEIGHT;
	private int client_width, client_height;

	// private Action closingProtocol;
	private SceneHandler sceneHandler;
	private boolean updateViewport;
	private int fullscreen = -1;
	private long window, monitor;
	private boolean previousMouseState;
	private long cursorNormal, cursorCanPoint, cursorIsPoint, cursorCanHold, cursorIsHold;
	private CursorType cursorTypeSelected;
	private boolean focused;

	public Window(boolean fullscreen, boolean vsync) {

		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize glfw");
		}
		
		long beforeGraphicsDev = System.currentTimeMillis();
		// New
		long primaryMonitor = glfwGetPrimaryMonitor();
		GLFWVidMode mode = glfwGetVideoMode(primaryMonitor);
		int currWidth = mode.width();
		// Set client size to one resolution lower than the current one
	
		// Old
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
//				.getDefaultScreenDevice();
//		int currWidth = gd.getDisplayMode().getWidth();

		
		updateWithinWindow(currWidth);
		
		long afterGraphicsDev = System.currentTimeMillis();
		System.out.println("Graphics time: " + (afterGraphicsDev - beforeGraphicsDev) + "ms");
		
		GLFWErrorCallback.createPrint().set();

		long beforeHints = System.currentTimeMillis();
	
//		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		if (Platform.get() == Platform.MACOSX) {
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		}
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, Game.DEBUG ? GLFW_TRUE : GLFW_FALSE);
		
		long afterHints = System.currentTimeMillis();
		System.out.println("Hints time: " + (afterHints - beforeHints) + "ms");

		window = glfwCreateWindow(client_width, client_height, "Racingmaybe", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the glfw window");
		
		long afterWindow = System.currentTimeMillis();
		System.out.println("Creation of Window time: " + (afterWindow - afterHints) + "ms");

		GLFW.glfwSetWindowFocusCallback(window, (window, focused) -> {
			this.focused = focused;
		});
		
		// ICON
		setIcon("./images/icon.png");
			
		// Cursor
		cursorNormal = createCursor("./images/cursor.png", 0);
		float xPercentCursorHand = 0.27f;
		cursorCanPoint = createCursor("./images/cursorCanPoint.png", xPercentCursorHand);
		cursorIsPoint = createCursor("./images/cursorIsPoint.png", xPercentCursorHand);
		cursorCanHold = createCursor("./images/cursorCanHold.png", xPercentCursorHand);
		cursorIsHold = createCursor("./images/cursorIsHold.png", xPercentCursorHand);
		setCursor(CursorType.cursorNormal);
		
		
		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);
		}
		
		// center
		setFullscreen(fullscreen);
		
		
		long beforeOpenGL = System.currentTimeMillis();
		// Make the OpenGL context current
		glfwMakeContextCurrent(window);

		GLFW.glfwSwapInterval(vsync ? 1 : 0);			
		
		// Opengl
		GLCapabilities caps = GL.createCapabilities();
		
		long afterOpenGL = System.currentTimeMillis();
		System.out.println("OpenGL time: " + (afterOpenGL - beforeOpenGL) + "ms");

		if (caps.OpenGL43) {
			GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_API,
					GL43.GL_DEBUG_TYPE_OTHER,
					GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null,
					false);
		} else if (caps.GL_KHR_debug) {
			KHRDebug.glDebugMessageControl(KHRDebug.GL_DEBUG_SOURCE_API,
					KHRDebug.GL_DEBUG_TYPE_OTHER,
					KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null,
					false);
		} else if (caps.GL_ARB_debug_output) {
			glDebugMessageControlARB(GL_DEBUG_SOURCE_API_ARB,
					GL_DEBUG_TYPE_OTHER_ARB, GL_DEBUG_SEVERITY_LOW_ARB,
					(IntBuffer) null, false);
		}
		
		

		updateViewport = true;
		
		System.out.println("WIDTH: " + WIDTH + ", HEIGHT: " + HEIGHT);
	}
	
	public void updateWithinWindow(int currWidth) {
		client_width = (int) (currWidth / 1.25f);
		
		final int incVal = 64;
		int foundIndex;
		int i = 1;
		do {
			foundIndex = incVal * i;
			if (client_width <= foundIndex)
				break;
			i++;
		} while (true);
		
		client_width = foundIndex;
		
		client_height = client_width * 9 / 16;

		WIDTH = client_width;
		HEIGHT = client_height;
		
		if (sceneHandler != null)
			sceneHandler.updateResolution();
	}

	public void setCursor(CursorType cursor) {
		if (cursorTypeSelected != null && cursorTypeSelected == cursor)
			return;
		
		cursorTypeSelected = cursor;

		GLFW.glfwSetCursor(window,
				switch (cursor) {
					case cursorCanHold :
						yield this.cursorCanHold;
					case cursorCanPoint :
						yield this.cursorCanPoint;
					case cursorIsHold :
						yield this.cursorIsHold;
					case cursorIsPoint :
						yield this.cursorIsPoint;
					case cursorNormal :
						yield this.cursorNormal;
				});
	}
	
	private long createCursor(String path, float xPercent) {
			//STBImage.stbi_set_flip_vertically_on_load(false);
			int[] widthBuffer = new int[1];
			int[] heightBuffer = new int[1];
			int[] channelsBuffer = new int[1];
			
			ByteBuffer data = STBImage.stbi_load(path, widthBuffer, heightBuffer, channelsBuffer, 4);
			GLFWImage cursor = GLFWImage.malloc();
			cursor.set(widthBuffer[0], heightBuffer[0], data);
			return GLFW.glfwCreateCursor(cursor, (int) (cursor.width() * xPercent), 0);
		}


	public void update() {
		glfwPollEvents();
		GL40.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT); // Clear the
															// framebuffer
	}

	public void swapBuffers() {
		glfwSwapBuffers(window);
	}
	
	/**
	 * changes state of the window
	 */
	public void setFullscreen(boolean fullscreen) {

		int width = 0;
		int height = 0;
		long monitor = 0; 
		GLFWVidMode vidmode = null;
		monitor = getCurrentMonitor();
		vidmode = glfwGetVideoMode(monitor);
		
		if(this.monitor == monitor && this.fullscreen == (fullscreen ? 1 : 0))
			return;
		
		this.monitor = monitor;
		this.fullscreen = fullscreen ? 1 : 0;
			
		if (fullscreen) {
			// switch to fullscreen

			// set width based on the right monitor
			width = vidmode.width();
			height = vidmode.height();
		} else {
			// switch to windowed
			
			updateWithinWindow(vidmode.width());

			width = client_width;
			height = client_height;
			monitor = NULL;
		}

		WIDTH = width;
		HEIGHT = height;
		
		IntBuffer xb = BufferUtils.createIntBuffer(1);
		IntBuffer yb = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowPos(window, xb, yb);
		
		int x = xb.get();
		int y = yb.get();
		
		glfwSetWindowMonitor(window, monitor, x, y,
				width, height, monitor == NULL ? GLFW_DONT_CARE : vidmode.refreshRate());
		
		// if windowed
		if (monitor == NULL && x == 0 && y == 0) {
			glfwSetWindowPos(window, (vidmode.width() - width) / 2,
					(vidmode.height() - height) / 2);
		}
		
		// move drawing of graphics to the right place
		updateViewport = true;
	}

	
	/** Determines the current monitor that the specified window is being displayed on.
	 * If the monitor could not be determined, the primary monitor will be returned.
	 *
	 * @return The current monitor on which the window is being displayed, or the primary monitor if one could not be determined
	 * @author <a href="https://stackoverflow.com/a/31526753/2398263">Shmo</a><br>
	 * Ported to LWJGL by Brian_Entei edited by Jhoffis */
	private long getCurrentMonitor() {
	    int[] wx = {0}, wy = {0}, ww = {0}, wh = {0};
	    int[] mx = {0}, my = {0}, mw = {0}, mh = {0};
	    int overlap, bestoverlap;
	    long bestmonitor;
	    PointerBuffer monitors;
	    GLFWVidMode mode;

	    bestoverlap = 0;
	    bestmonitor = monitor;

	    glfwGetWindowPos(window, wx, wy);
	    glfwGetWindowSize(window, ww, wh);
	    monitors = glfwGetMonitors();

	    while(monitors.hasRemaining()) {
	        long monitor = monitors.get();
	        mode = glfwGetVideoMode(monitor);
	        glfwGetMonitorPos(monitor, mx, my);
	        mw[0] = mode.width();
	        mh[0] = mode.height();

	        overlap =
	                Math.max(0, Math.min(wx[0] + ww[0], mx[0] + mw[0]) - Math.max(wx[0], mx[0])) *
	                Math.max(0, Math.min(wy[0] + wh[0], my[0] + mh[0]) - Math.max(wy[0], my[0]));

	        if (bestoverlap < overlap) {
	            bestoverlap = overlap;
	            bestmonitor = monitor;
	        }
	    }
	    return bestmonitor;
	}

	public void mouseStateHide(boolean lock) {
		previousMouseState = GLFW.glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED;
		glfwSetInputMode(window, GLFW_CURSOR,
				lock ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
	}

	public void mouseStateToPrevious() {
		mouseStateHide(previousMouseState);
	}

	public long getWindow() {
		return window;
	}

	public boolean isFullscreen() {
		return fullscreen == 1;
	}

	public boolean shouldUpdateViewport() {
		return updateViewport;
	}

	public void updateViewport() {
//		IntBuffer width = IntBuffer.allocate(1);
//		IntBuffer height = IntBuffer.allocate(1);
//		GLFW.glfwGetFramebufferSize(window, width, height);
		glfwMakeContextCurrent(window);
        GL11.glViewport( 0, 0, WIDTH, HEIGHT);
        updateViewport = false;		
	}
	
	public void destroy() {
		// if (closingProtocol != null)
		// closingProtocol.run();
		glfwDestroyWindow(window);
		GLFW.glfwDestroyCursor(cursorNormal);
		GLFW.glfwDestroyCursor(cursorCanPoint); 
		GLFW.glfwDestroyCursor(cursorIsPoint); 
		GLFW.glfwDestroyCursor(cursorCanHold); 
		GLFW.glfwDestroyCursor(cursorIsHold);
	}

	public boolean isClosing() {
		return glfwWindowShouldClose(window);
	}
	
	public void setSceneHandler(SceneHandler sceneHandler) {
		this.sceneHandler = sceneHandler;
	}

	public boolean isFocused() {
		return focused;
	}
	
	public void setIcon(String path)
	{
		GLFWImage.Buffer icons = GLFWImage.malloc(1);
		//STBImage.stbi_set_flip_vertically_on_load(false);
		int[] widthBuffer = new int[1];
		int[] heightBuffer = new int[1];
		int[] channelsBuffer = new int[1];
		
		ByteBuffer data = STBImage.stbi_load(path, widthBuffer, heightBuffer, channelsBuffer, 4);
		GLFWImage icon = GLFWImage.malloc();
		icon.set(widthBuffer[0], heightBuffer[0], data);
		icons.put(0,icon);
		icon.free();
			
		glfwSetWindowIcon(window, icons);
		icons.free();
	}	
}
