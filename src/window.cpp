#include "window.h"
#include "main.h"

#include <stdexcept>
#include <algorithm>
#include <stb_image.h>
#include <filesystem>
#include <iostream>



GLFWcursor* glfwCursorNormal, * glfwCursorCanPoint, * glfwCursorIsPoint, * glfwCursorCanHold, * glfwCursorIsHold;
bool _focused;
int _fullscreen = -1;
GLFWwindow* _window;
GLFWmonitor* _monitor;
bool _previousMouseStateVisible;
Window::CursorType _cursorTypeSelected;


GLFWwindow* Window::getWindow() {
    return _window;
}

void glfwErrors(int error_code, const char *description) {
    throw std::runtime_error(std::string("GLFW ERROR: ").append(reinterpret_cast<const char *>(error_code)).append("\n").append(description));
}

void updateWithinWindow(int currWidth) {
    int client_width = currWidth / 1.25f;

    const int incVal = 64;
    int foundWidth;
    int i = 1;
    do {
        foundWidth = incVal * i;
        if (client_width <= foundWidth)
            break;
        i++;
    } while (true);

    client_width = foundWidth;

    int client_height = client_width * 9 / 16;

    Window::WIDTH = client_width;
    Window::HEIGHT = client_height;

    //    TODO if (sceneHandler != null)
    //        sceneHandler.updateResolution();
}

GLFWmonitor* getCurrentMonitor(GLFWwindow *window, GLFWmonitor *monitor) {
    int wx, wy, ww, wh;
    int mx, my, mw, mh;
    int overlap, bestoverlap = 0;
    GLFWmonitor *monitorTemp, *bestmonitor = monitor;
    const GLFWvidmode *mode;

    glfwGetWindowPos(window, &wx, &wy);
    glfwGetWindowSize(window, &ww, &wh);
    int monitorCount = 0;
    GLFWmonitor **monitors = glfwGetMonitors(&monitorCount);

    for (int i = 0; i < monitorCount; i++) {
        monitorTemp = monitors[i];
        mode = glfwGetVideoMode(monitorTemp);
        glfwGetMonitorPos(monitorTemp, &mx, &my);
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


void Window::mouseStateHide(bool lock) {
    _previousMouseStateVisible = glfwGetInputMode(_window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED;
    glfwSetInputMode(_window, GLFW_CURSOR,
                     lock ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
}

void Window::mouseStateToPrevious() {
    mouseStateHide(_previousMouseStateVisible);
}

void Window::setCursor(CursorType cursor) {
    _cursorTypeSelected = cursor;
    GLFWcursor *glfwCursor;
    switch (cursor) {
        case CursorType::cursorCanHold :
            glfwCursor = glfwCursorCanHold;
            break;
        case CursorType::cursorCanPoint :
            glfwCursor = glfwCursorCanPoint;
            break;
        case CursorType::cursorIsHold :
            glfwCursor = glfwCursorIsHold;
            break;
        case CursorType::cursorIsPoint :
            glfwCursor = glfwCursorIsPoint;
            break;
        case CursorType::cursorNormal :
            glfwCursor = glfwCursorNormal;
            break;
    }
    glfwSetCursor(_window, glfwCursor);
}

GLFWimage createGLFWImage(const char *path) {
    int w;
    int h;
    int comp;
    std::string currPath = std::filesystem::current_path().string().append("/res/");
    const char *realPath = reinterpret_cast<const char *>(currPath.append(path).c_str());
    unsigned char *image = stbi_load(realPath, &w, &h, &comp, STBI_rgb_alpha);
    // TODO free stb images

    if (image == nullptr)
        throw std::runtime_error(std::string("Failed to load texture at ").append(realPath));

    GLFWimage resultImg;
    resultImg.width = w;
    resultImg.height = h;
    resultImg.pixels = image;

    return resultImg;
}

GLFWcursor* createCursor(const char* path, float xPercent) {
    GLFWimage cursor = createGLFWImage(path);
    return glfwCreateCursor(&cursor, (int)(cursor.width * xPercent), 0);
}

void Window::setFullscreen(bool fullscreenTemp) {

    GLFWmonitor *monitorTemp = getCurrentMonitor(_window, _monitor);
    const GLFWvidmode *vidmode = glfwGetVideoMode(monitorTemp);

    if (_monitor == monitorTemp && _fullscreen == (fullscreenTemp ? 1 : 0))
        return;

    _monitor = monitorTemp;
    _fullscreen = fullscreenTemp ? 1 : 0;

    if (fullscreenTemp) {
        // switch to fullscreen

        // set width based on the right monitor
        WIDTH = vidmode->width;
        HEIGHT = vidmode->height;
    } else {
        // switch to windowed
        updateWithinWindow(vidmode->width);
        monitorTemp = nullptr;
    }

    int wx, wy;
    glfwGetWindowPos(_window, &wx, &wy);

    glfwSetWindowMonitor(_window, monitorTemp, wx, wy,
                         WIDTH, HEIGHT, monitorTemp == nullptr ? GLFW_DONT_CARE : vidmode->refreshRate);

    // if windowed
    if (monitorTemp == nullptr && wx == 0 && wy == 0) {
        glfwSetWindowPos(_window, (vidmode->width - WIDTH) / 2,
                         (vidmode->height - HEIGHT) / 2);
    }

    // move drawing of graphics to the right place
}

void Window::switchFullscreen() {
    setFullscreen(_fullscreen == 0);
}

void Window::createWindow(bool fullscreen, bool vsync) {

    // Set client size to one resolution lower than the current one

    if (!glfwInit()) {
        glfwTerminate();
        throw std::runtime_error("Unable to initialize glfw");
    }

    _monitor = glfwGetPrimaryMonitor();
    auto mode = glfwGetVideoMode(_monitor);
    updateWithinWindow(mode->width);

    glfwSetErrorCallback(glfwErrors);

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, true);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
    
    // vk
    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
    
    //gl
    /*
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, DEBUG ? GLFW_TRUE : GLFW_FALSE);
    */
    
    //    if (Platform.get() == Platform.MACOSX) {
    //        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
    //    }

    _monitor = glfwGetPrimaryMonitor();
    //    int monitorCount = 0;
    //    GLFWmonitor **monitors = glfwGetMonitors(&monitorCount);

    _window = glfwCreateWindow(WIDTH, HEIGHT, "Racingmaybe", nullptr, nullptr);
    if (_window == nullptr) {
        glfwTerminate();
        throw std::runtime_error("Failed to create the glfw window");
    }

    glfwSetWindowFocusCallback(_window, [](auto window, auto f)
        {
            _focused = f;
        });

    // ICON
//    new Thread(()->
//    {
    const GLFWimage icon = createGLFWImage("pics/icon.png");
    glfwSetWindowIcon(_window, 1, &icon);

    // Cursor
    glfwCursorNormal = createCursor("pics/cursor.png", 0);
    float xPercentCursorHand = 0.27f;
    glfwCursorCanPoint = createCursor("pics/cursorCanPoint.png", xPercentCursorHand);
    glfwCursorIsPoint = createCursor("pics/cursorIsPoint.png", xPercentCursorHand);
    glfwCursorCanHold = createCursor("pics/cursorCanHold.png", xPercentCursorHand);
    glfwCursorIsHold = createCursor("pics/cursorIsHold.png", xPercentCursorHand);
    setCursor(CursorType::cursorNormal);
    mouseStateHide(false);
    //    }).start();

        // Get the thread stack and push a new frame
    //    try
    //    (MemoryStack
    //    stack = stackPush()) {
    //        IntBuffer pWidth = stack.mallocInt(1);
    //        IntBuffer pHeight = stack.mallocInt(1);
    //
    //        // Get the window size passed to glfwCreateWindow
    //        glfwGetWindowSize(window, pWidth, pHeight);
    //    }

        // center
    setFullscreen(fullscreen);

    /* // Opengl
    // Make the OpenGL context current
    glfwMakeContextCurrent(_window);
    glfwSwapInterval(vsync);

    if (!gladLoadGL(static_cast<GLADloadfunc>(glfwGetProcAddress)))
    {
        glfwTerminate();
        throw std::runtime_error("Failed to initialize GLAD");
    }
    */

    /*
    //    updateViewport = true;
    glfwSetFramebufferSizeCallback(_window, [](GLFWwindow* window, int width, int height) {
        glfwMakeContextCurrent(window);
  //      glViewport(0, 0, width, height);
        });
        */

    //glViewport(0, 0, WIDTH, HEIGHT);
}

void Window::createWindowSurface(VkInstance instance, VkSurfaceKHR* surface) {
    if (glfwCreateWindowSurface(instance, _window, nullptr, surface) != VK_SUCCESS) {
        throw std::runtime_error("Failed to create window surface");
    }
}


void Window::destoryWindow() {
    glfwDestroyWindow(_window);
    glfwDestroyCursor(glfwCursorNormal);
    glfwDestroyCursor(glfwCursorCanPoint);
    glfwDestroyCursor(glfwCursorIsPoint);
    glfwDestroyCursor(glfwCursorCanHold);
    glfwDestroyCursor(glfwCursorIsHold);
//    delete window;
//    delete monitor;
    glfwTerminate();
}
