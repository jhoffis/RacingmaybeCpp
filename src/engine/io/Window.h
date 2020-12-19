//
// Created by Jens Benz on 19.12.2020.
//
#include <GLFW\glfw3.h>
#ifndef UNTITLED3_WINDOW_H
#define UNTITLED3_WINDOW_H

int WIDTH, HEIGHT;

enum CursorType {
    cursorNormal,
    cursorCanPoint,
    cursorIsPoint,
    cursorCanHold,
    cursorIsHold
};

class Window {

public:

    Window(bool fullscreen, bool vsync);
    ~Window();
    void setCursor(CursorType cursor);
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
    GLFWwindow* window, monitor;
    bool previousMouseStateVisible;
    long cursorNormal, cursorCanPoint, cursorIsPoint, cursorCanHold, cursorIsHold;
    CursorType cursorTypeSelected;
    bool focused;

    void updateWithinWindow(int currWidth);
    long createCursor(char32_t *path, float xPercent);
    GLFWimage createGLFWImage(char32_t *path);
    long getCurrentMonitor();

};


#endif //UNTITLED3_WINDOW_H
