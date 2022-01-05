#ifndef RACINGMAYBE_WINDOW
#define RACINGMAYBE_WINDOW

#define STB_IMAGE_IMPLEMENTATION
#include <string>
//#include <glad\gl.h>
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

#endif
