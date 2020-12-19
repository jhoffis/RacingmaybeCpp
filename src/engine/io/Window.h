//
// Created by Jens Benz on 19.12.2020.
//
#include <string>
#include <glad\glad.h>
#include <GLFW\glfw3.h>
#ifndef UNTITLED3_WINDOW_H
#define UNTITLED3_WINDOW_H
#define STB_IMAGE_IMPLEMENTATION

enum CursorType {
    cursorNormal,
    cursorCanPoint,
    cursorIsPoint,
    cursorCanHold,
    cursorIsHold
};

class Window {

public:
    inline static int WIDTH, HEIGHT;
    Window(bool fullscreen, bool vsync);
    ~Window();
    void setCursor(CursorType cursor);
    void setFullscreen(bool fullscreen);
    void mouseStateHide(bool lock);
    void mouseStateToPrevious();
    GLFWwindow *getWindow() {
        return window;
    }
//    bool isFullscreen() {
//        return fullscreen == 1;
//    }
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

#endif //UNTITLED3_WINDOW_H
