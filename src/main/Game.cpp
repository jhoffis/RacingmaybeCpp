#include <iostream>
#include <engine/utils/Timer.h>
#include <scenes/SceneHandler.h>
#include <vector>
#include <thread>
#include <audio/AudioMaster.h>

//void processInput(GLFWwindow *window)
//{
//    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
//        glfwSetWindowShouldClose(window, true);
//}

int main() {
    createAudio();
    Window::createWindow(false, false);
    SceneHandler::createSceneHandler();
    
    static bool running = true;
    std::thread tickThread([]() {
        std::cout << "Starting tick thread" << std::endl;
        while (running)
        {
            //        steam.update();
            SceneHandler::tick(Timer::nowDelta());
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
        glfwPollEvents(); // maybe flytt denne over til tick?

        SceneHandler::render();

        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        glfwSwapBuffers(Window::getWindow());
        
    }
    
    return 0;
}
