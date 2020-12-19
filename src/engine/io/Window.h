//
// Created by Jens Benz on 19.12.2020.
//
#include <GLFW\glfw3.h>
#include <string>
#ifndef UNTITLED3_WINDOW_H
#define UNTITLED3_WINDOW_H

enum CursorType {
    cursorNormal,
    cursorCanPoint,
    cursorIsPoint,
    cursorCanHold,
    cursorIsHold
};

class Window {

public:
    static int WIDTH, HEIGHT;
    Window(bool fullscreen, bool vsync);
    ~Window();
    constexpr void setCursor(CursorType cursor);
    void setFullscreen(bool fullscreen);
    void mouseStateHide(bool lock);
    void mouseStateToPrevious();
    void updateViewport();
    GLFWwindow* getWindow() {
        return window;
    }
    bool isFullscreen() {
        return fullscreen == 1;
    }
    bool isFocused() {
        return focused;
    }
private:

    // private Action closingProtocol;
//private SceneHandler sceneHandler;
    int fullscreen = -1;
    GLFWwindow *window;
    GLFWmonitor *monitor;
    bool previousMouseStateVisible;
    GLFWcursor *cursorNormal, *cursorCanPoint, *cursorIsPoint, *cursorCanHold, *cursorIsHold;
    CursorType &cursorTypeSelected;
    bool focused;

    void updateWithinWindow(int currWidth);
    constexpr GLFWcursor *createCursor(const char *path, float xPercent);
    constexpr GLFWimage *createGLFWImage(const char *path);
    GLFWmonitor * getCurrentMonitor();

};


#endif //UNTITLED3_WINDOW_H
