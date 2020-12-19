//
// Created by Jens Benz on 19.12.2020.
//

#include <algorithm>
#include "Window.h"


Window::Window(bool fullscreen, bool vsync) {


    // Set client size to one resolution lower than the current one
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();
    int currWidth = gd.getDisplayMode().getWidth();
    updateWithinWindow(currWidth);

    GLFWErrorCallback.createPrint().set();

    if (!glfwInit()) {
        throw new IllegalStateException("Unable to initialize glfw");
    }

    glfwDefaultWindowHints();
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

    monitor = glfwGetPrimaryMonitor();

    window = glfwCreateWindow(client_width, client_height, "Racingmaybe", NULL,
                              NULL);
    if (window == NULL)
        throw new RuntimeException("Failed to create the glfw window");

    GLFW.glfwSetWindowFocusCallback(window, (window, focused) -> {
        this.focused = focused;
    });

    // ICON
    new Thread(() -> {
        GLFWImage icon = createGLFWImage("/pics/icon.png");
        GLFWImage.Buffer icons = GLFWImage.malloc(1);
        icons.put(0, icon);
        glfwSetWindowIcon(window, icons);

        // Cursor
        cursorNormal = createCursor("/pics/cursor.png", 0);
        float xPercentCursorHand = 0.27f;
        cursorCanPoint = createCursor("/pics/cursorCanPoint.png", xPercentCursorHand);
        cursorIsPoint = createCursor("/pics/cursorIsPoint.png", xPercentCursorHand);
        cursorCanHold = createCursor("/pics/cursorCanHold.png", xPercentCursorHand);
        cursorIsHold = createCursor("/pics/cursorIsHold.png", xPercentCursorHand);
        setCursor(CursorType.cursorNormal);
    }).start();

    // Get the thread stack and push a new frame
    try (MemoryStack stack = stackPush()) {
        IntBuffer pWidth = stack.mallocInt(1);
        IntBuffer pHeight = stack.mallocInt(1);

        // Get the window size passed to glfwCreateWindow
        glfwGetWindowSize(window, pWidth, pHeight);
    }

    // center
    setFullscreen(fullscreen);

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);

    GLFW.glfwSwapInterval(vsync ? 1 : 0);

    // Opengl

    updateViewport = true;

}

void Window::updateViewport() {
//		IntBuffer width = IntBuffer.allocate(1);
//		IntBuffer height = IntBuffer.allocate(1);
//		GLFW.glfwGetFramebufferSize(window, width, height);
    glfwMakeContextCurrent(window);
    glViewport( 0, 0, WIDTH, HEIGHT);
//        updateViewport = false;
}

void Window::mouseStateHide(bool lock) {
    previousMouseStateVisible = glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED;
    glfwSetInputMode(window, GLFW_CURSOR,
                     lock ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
}

void Window::mouseStateToPrevious() {
    mouseStateHide(previousMouseStateVisible);
}

void Window::setCursor(CursorType cursor) {
//    if (cursorTypeSelected != null && cursorTypeSelected == cursor)
//        return;
//
//    cursorTypeSelected = cursor;
//
//    GLFW.glfwSetCursor(window,
//    switch (cursor) {
//        case cursorCanHold :
//            yield this.cursorCanHold;
//        case cursorCanPoint :
//            yield this.cursorCanPoint;
//        case cursorIsHold :
//            yield this.cursorIsHold;
//        case cursorIsPoint :
//            yield this.cursorIsPoint;
//        case cursorNormal :
//            yield this.cursorNormal;
//    });
}

long Window::createCursor(String path, float xPercent) {
//    GLFWImage cursor = createGLFWImage(path);
//    return GLFW.glfwCreateCursor(cursor, (int) (cursor.width() * xPercent), 0);
}

GLFWimage Window::createGLFWImage(String path) {
//    BufferedImage image = null;
//    try {
//        image = ImageIO.read(Window.class.getResourceAsStream(path));
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//
//    ByteBuffer buffer = BufferUtils
//            .createByteBuffer(image.getWidth() * image.getHeight() * 4);
//    for (int i = 0; i < image.getHeight(); i++) {
//        for (int j = 0; j < image.getWidth(); j++) {
//            int colorSpace = image.getRGB(j, i);
//            buffer.put((byte) ((colorSpace << 8) >> 24));
//            buffer.put((byte) ((colorSpace << 16) >> 24));
//            buffer.put((byte) ((colorSpace << 24) >> 24));
//            buffer.put((byte) (colorSpace >> 24));
//        }
//    }
//    buffer.flip();
//    final GLFWImage result = GLFWImage.create();
//    result.set(image.getWidth(), image.getHeight(), buffer);
//    return result;
}

void Window::setFullscreen(bool fullscreen) {

    int width = 0;
    int height = 0;
    GLFWmonitor *monitorTemp = getCurrentMonitor();
    const GLFWvidmode *vidmode = glfwGetVideoMode(monitor);

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

void Window::updateWithinWindow(int currWidth) {
//    client_width = (int) (currWidth / 1.25f);
//
//    final int incVal = 64;
//    int foundIndex;
//    int i = 1;
//    do {
//        foundIndex = incVal * i;
//        if (client_width <= foundIndex)
//            break;
//        i++;
//    } while (true);
//
//    client_width = foundIndex;
//
//    client_height = client_width * 9 / 16;
//
//    WIDTH = client_width;
//    HEIGHT = client_height;
//
//    if (sceneHandler != null)
//        sceneHandler.updateResolution();
}

GLFWmonitor * Window::getCurrentMonitor() {
    int *wx, *wy, *ww, *wh;
    int *mx, *my, mw, mh;
    int overlap, bestoverlap = 0;
    GLFWmonitor *monitorTemp, *bestmonitor = monitor;
    const GLFWvidmode *mode;

    glfwGetWindowPos(window, wx, wy);
    glfwGetWindowSize(window, ww, wh);
    int *monitorCount;
    GLFWmonitor **monitors = glfwGetMonitors(monitorCount);

    for (int i = 0; i < *monitorCount; i++) {
        monitorTemp = monitors[i];
        mode = glfwGetVideoMode(monitorTemp);
        glfwGetMonitorPos(monitor, mx, my);
        mw = mode->width;
        mh = mode->height;

        overlap = std::max(0, std::min(*wx + *ww, *mx + mw) - std::max(*wx, *mx))
                * std::max(0, std::min(*wy + *wh, *my + mh) - std::max(*wy, *my));

        if (bestoverlap < overlap) {
            bestoverlap = overlap;
            bestmonitor = monitorTemp;
        }
    }
    return bestmonitor;
}

Window::~Window() {

}
