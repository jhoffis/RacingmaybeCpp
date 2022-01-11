#include <iostream>
#include <src/engine/utils/Timer.h>
#include <src/scenes/SceneHandler.h>
#include <vector>
#include <thread>

//void processInput(GLFWwindow *window)
//{
//    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
//        glfwSetWindowShouldClose(window, true);
//}
static std::vector<SceneEnvir> scenes;
static SceneHandler sceneHandler(scenes);

int main() {
    // Setup
    Window::createWindow(false, false);

    static size_t x = 0;
    static size_t y = 0;

    glfwSetKeyCallback(Window::getWindow(), [](auto window, auto key, auto scancode, auto action, auto mods) {
        sceneHandler.keyInput(key, action);
    });

    glfwSetMouseButtonCallback(Window::getWindow(), [](auto window, auto button, auto action, auto mods) {
        sceneHandler.mouseButtonInput(button, action, x, y);
    });

    glfwSetCursorPosCallback(Window::getWindow(), [](auto window, auto xpos, auto ypos) {
        x = xpos;
        y = ypos;
        sceneHandler.mousePosInput(x, y);
    });
    
    static bool running = true;
    std::thread tickThread([]() {
        std::cout << "Starting tick thread" << std::endl;
        while (running)
        {
            //        steam.update();
            sceneHandler.tick(Timer::nowDelta());
            //        audio.checkMusic();

            //        processInput(window.getWindow());
            //
        }
        std::cout << "Ending tick thread" << std::endl;
    });

    // Run the game
    while(running)
    {
        if (glfwWindowShouldClose(Window::getWindow())) {
            running = false;
            tickThread.join();
            break;
        }

        glfwPollEvents();
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glfwSwapBuffers(Window::getWindow());
        
    }
    
    return 0;
}
