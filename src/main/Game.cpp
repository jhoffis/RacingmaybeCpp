#include <iostream>
//#include <GLFW\glfw3.h>
#include <src/engine/io/Window.h>
#include <src/engine/utils/Timer.h>

//void processInput(GLFWwindow *window)
//{
//    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
//        glfwSetWindowShouldClose(window, true);
//}

int main() {
    Window window(false, false);

    bool running = true;
    double delta = 0;
    while(running)
    {
        if (glfwWindowShouldClose(window.getWindow())) {
            running = false;
            break;
        }
        delta = Timer::nowDelta();
//        steam.update();
//        sceneHandler.tick(delta);
//        audio.checkMusic();

//        processInput(window.getWindow());
//
        glfwPollEvents();
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
//
        glfwSwapBuffers(window.getWindow());
    }

    return 0;
}
