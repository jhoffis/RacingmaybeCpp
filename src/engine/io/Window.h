#pragma once

#define STB_IMAGE_IMPLEMENTATION
#define GLFW_INCLUDE_VULKAN
//#include <glad/gl.h>
#include <GLFW/glfw3.h>
#include <string>

namespace Window {
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
    void switchFullscreen();
    void mouseStateHide(bool lock);
    void mouseStateToPrevious();
    GLFWwindow* getWindow();
}
