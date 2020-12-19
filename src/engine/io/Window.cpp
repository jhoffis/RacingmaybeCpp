//
// Created by Jens Benz on 19.12.2020.
//

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

}

void Window::setFullscreen(bool fullscreen) {

}

Window::~Window() {

}

void Window::updateWithinWindow(int currWidth) {

}

long Window::createCursor(String path, float xPercent) {
    return 0;
}

GLFWimage Window::createGLFWImage(String path) {
    return GLFWimage();
}

long Window::getCurrentMonitor() {
    return 0;
}
