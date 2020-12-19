//
// Created by Jens Benz on 19.12.2020.
//

#include <algorithm>
#include "Window.h"
#include "stb_image.h"
#include "src/main/Game.h"

void glfwErrors(int error_code, const char *description) {
    throw std::string("GLFW ERROR: ").append(reinterpret_cast<const char *>(error_code)).append("\n").append(description);
}

Window::Window(bool fullscreen, bool vsync) {


    // Set client size to one resolution lower than the current one
    monitor = glfwGetPrimaryMonitor();
    auto mode = glfwGetVideoMode(monitor);
    updateWithinWindow(mode->width);

    glfwSetErrorCallback(glfwErrors);

    if (!glfwInit()) {
        throw std::string("Unable to initialize glfw");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
    glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
//    if (Platform.get() == Platform.MACOSX) {
//        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
//    }
    glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, Game::DEBUG ? GLFW_TRUE : GLFW_FALSE);

    monitor = glfwGetPrimaryMonitor();

    window = glfwCreateWindow(WIDTH, HEIGHT, "Racingmaybe", NULL,
                              NULL);
    if (window == NULL)
        throw std::string("Failed to create the glfw window");

    glfwSetWindowFocusCallback(window,[](window, focused)
    {
        this->focused = focused;
    });

    // ICON
    new Thread(()->
    {
        GLFWImage icon = createGLFWImage("/pics/icon.png");
        GLFWImage.Buffer
        icons = GLFWImage.malloc(1);
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
    try
    (MemoryStack
    stack = stackPush()) {
        IntBuffer pWidth = stack.mallocInt(1);
        IntBuffer pHeight = stack.mallocInt(1);

        // Get the window size passed to glfwCreateWindow
        glfwGetWindowSize(window, pWidth, pHeight);
    }

    // center
    setFullscreen(fullscreen);

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);

    glfwSwapInterval(vsync ? 1 : 0);

    // Opengl

//    updateViewport = true;
    updateViewport();
}

void Window::updateViewport() {
//		IntBuffer width = IntBuffer.allocate(1);
//		IntBuffer height = IntBuffer.allocate(1);
//		GLFW.glfwGetFramebufferSize(window, width, height);
    glfwMakeContextCurrent(window);
    glViewport(0, 0, WIDTH, HEIGHT);
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

constexpr void Window::setCursor(CursorType cursor) {
    if (&cursorTypeSelected != nullptr && cursorTypeSelected == cursor)
        return;

    cursorTypeSelected = cursor;
    GLFWcursor *glfwCursor;
    switch (cursor) {
        case CursorType::cursorCanHold :
            glfwCursor = this->cursorCanHold;
            break;
        case CursorType::cursorCanPoint :
            glfwCursor = this->cursorCanPoint;
            break;
        case CursorType::cursorIsHold :
            glfwCursor = this->cursorIsHold;
            break;
        case CursorType::cursorIsPoint :
            glfwCursor = this->cursorIsPoint;
            break;
        case CursorType::cursorNormal :
            glfwCursor = this->cursorNormal;
            break;
    }
    glfwSetCursor(window, glfwCursor);
}

constexpr GLFWcursor *Window::createCursor(const char *path, float xPercent) {
    GLFWimage *cursor = createGLFWImage(path);
    return glfwCreateCursor(cursor, (int) (cursor->width * xPercent), 0);
}

constexpr GLFWimage *Window::createGLFWImage(const char *path) {
    int w;
    int h;
    int comp;
    unsigned char *image = stbi_load(path, &w, &h, &comp, STBI_rgb_alpha);
    // TODO free stb images

    if (image == nullptr)
        throw (std::string("Failed to load texture at ").append(path));

    GLFWimage result = GLFWimage{};
    result.width = w;
    result.height = h;
    result.pixels = image;

    return &result;
}

void Window::setFullscreen(bool fullscreen) {

    GLFWmonitor *monitor = getCurrentMonitor();
    const GLFWvidmode *vidmode = glfwGetVideoMode(monitor);

    if (this->monitor == monitor && this->fullscreen == (fullscreen ? 1 : 0))
        return;

    this->monitor = monitor;
    this->fullscreen = fullscreen ? 1 : 0;

    if (fullscreen) {
        // switch to fullscreen

        // set width based on the right monitor
        WIDTH = vidmode->width;
        HEIGHT = vidmode->height;
    } else {
        // switch to windowed
        updateWithinWindow(vidmode->width);
        monitor = nullptr;
    }

    int *wx;
    int *wy;
    glfwGetWindowPos(window, wx, wy);

    glfwSetWindowMonitor(window, monitor, *wx, *wy,
                         WIDTH, HEIGHT, monitor == nullptr ? GLFW_DONT_CARE : vidmode.refreshRate());

    // if windowed
    if (monitor == nullptr && *wx == 0 && *wy == 0) {
        glfwSetWindowPos(window, (vidmode->width - WIDTH) / 2,
                         (vidmode->height - HEIGHT) / 2);
    }

    // move drawing of graphics to the right place
//    updateViewport = true;
    updateViewport();
}

void Window::updateWithinWindow(int currWidth) {
    int client_width = currWidth / 1.25f;

    const int incVal = 64;
    int foundIndex;
    int i = 1;
    do {
        foundIndex = incVal * i;
        if (client_width <= foundIndex)
            break;
        i++;
    } while (true);

    client_width = foundIndex;

    int client_height = client_width * 9 / 16;

    WIDTH = client_width;
    HEIGHT = client_height;

//    TODO if (sceneHandler != null)
//        sceneHandler.updateResolution();
}

GLFWmonitor *Window::getCurrentMonitor() {
    int wx, wy, ww, wh;
    int mx, my, mw, mh;
    int overlap, bestoverlap = 0;
    GLFWmonitor *monitorTemp, *bestmonitor = monitor;
    const GLFWvidmode *mode;

    glfwGetWindowPos(window, &wx, &wy);
    glfwGetWindowSize(window, &ww, &wh);
    int *monitorCount;
    GLFWmonitor **monitors = glfwGetMonitors(monitorCount);

    for (int i = 0; i < *monitorCount; i++) {
        monitorTemp = monitors[i];
        mode = glfwGetVideoMode(monitorTemp);
        glfwGetMonitorPos(monitor, &mx, &my);
        mw = mode->width;
        mh = mode->height;

        overlap = std::max(0, std::min(wx + ww, mx + mw) - std::max(wx, mx))
                  * std::max(0, std::min(wy + wh, my + mh) - std::max(wy, my));

        if (bestoverlap < overlap) {
            bestoverlap = overlap;
            bestmonitor = monitorTemp;
        }
    }
    return bestmonitor;
}

Window::~Window() {
    glfwDestroyWindow(window);
    glfwDestroyCursor(cursorNormal);
    glfwDestroyCursor(cursorCanPoint);
    glfwDestroyCursor(cursorIsPoint);
    glfwDestroyCursor(cursorCanHold);
    glfwDestroyCursor(cursorIsHold);
}
