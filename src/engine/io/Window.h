#ifndef RACINGMAYBE_WINDOW
#define RACINGMAYBE_WINDOW

#define STB_IMAGE_IMPLEMENTATION
#include <string>
#include <glad/gl.h>
#include <GLFW/glfw3.h>

enum CursorType {
    cursorNormal,
    cursorCanPoint,
    cursorIsPoint,
    cursorCanHold,
    cursorIsHold
};

inline static int WIDTH, HEIGHT;
void createWindow(bool fullscreen, bool vsync);
void destoryWindow();
void setCursor(CursorType cursor);
void setFullscreen(bool fullscreen);
void mouseStateHide(bool lock);
void mouseStateToPrevious();
GLFWwindow* getWindow();

public:
    inline static float WIDTH, HEIGHT;
    Window(bool fullscreen, bool vsync);
    ~Window();
    void setCursor(CursorType cursor);
    void setFullscreen(bool fullscreen);
    void mouseStateHide(bool lock);
    void mouseStateToPrevious();

    GLFWwindow *getWindow() {
        return window;
    }
    bool isFullscreen() {
        return fullscreen == 1;
    }
//    bool isFocused() {
//        return focused;
//    }
private:

    inline static bool focused;
    // private Action closingProtocol;
//private SceneHandler sceneHandler;
    int fullscreen = -1;
    GLFWwindow *window;
    GLFWmonitor *monitor;
    bool previousMouseStateVisible{};
    GLFWcursor *cursorNormal, *cursorCanPoint, *cursorIsPoint, *cursorCanHold, *cursorIsHold;
    CursorType cursorTypeSelected;
//
    void updateWithinWindow(int currWidth);
    GLFWcursor *createCursor(const char *path, float xPercent);
    GLFWimage createGLFWImage(const char *path);
    GLFWmonitor * getCurrentMonitor(GLFWwindow *window, GLFWmonitor *monitor);

};

#endif
