#include <iostream>
//#include <GLFW\glfw3.h>
#include <src/engine/io/Window.h>
#include <src/engine/utils/Timer.h>
#include <src/scenes/SceneHandler.h>
#include <vector>

//void processInput(GLFWwindow *window)
//{
//    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
//        glfwSetWindowShouldClose(window, true);
//}

int main() {
    // Setup
    Window window(false, false);

    std::vector<SceneEnvir> scenes;
    SceneHandler sceneHandler(scenes);

    // Run the game
    bool running = true;
    while(running)
    {
        if (glfwWindowShouldClose(window.getWindow())) {
            running = false;
            break;
        }
//        steam.update();
        sceneHandler.tick(Timer::nowDelta());
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
