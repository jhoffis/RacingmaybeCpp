#include <iostream>
//#include <GLFW\glfw3.h>
#include <src/engine/io/Window.h>

//void processInput(GLFWwindow *window)
//{
//    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
//        glfwSetWindowShouldClose(window, true);
//}

int main() {
    Window window(false, false);

    bool running = true;
    while(running)
    {
        if (glfwWindowShouldClose(window.getWindow())) {
            running = false;
            break;
        }
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
