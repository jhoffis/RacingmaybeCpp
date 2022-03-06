#include "timer.h"
#include "scene_handler.h"
#include "audio.h"
#include "vulkan/pipeline.h"
#include "vulkan/engine_device.h"


#include <iostream>
#include <vector>
#include <thread>


//void processInput(GLFWwindow *window)
//{
//    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
//        glfwSetWindowShouldClose(window, true);
//}

int main() {
    createAudio();

    /*
        Graphics
    */
    Window::createWindow(false, false);
    Graphics::EngineDevice device;
    Graphics::createPipeline(
        //"res/shaders/simple_shader.vert.spv", 
        device,
        "res/shaders/simple_shader.vert.spv", 
        "res/shaders/simple_shader.frag.spv",
        Graphics::defaultPipelineConfig(Window::WIDTH, Window::HEIGHT)
    );
    /*
        Setup scenes and entities
    */
    SceneHandler::createSceneHandler();
    // Run the game
    static bool running = true;
    
    /*
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
    */
    while(running)
    {
        if (glfwWindowShouldClose(Window::getWindow())) {
            running = false;
            //tickThread.join();
            break;
        }
        SceneHandler::tick(Timer::nowDelta());
        glfwPollEvents();

        SceneHandler::render();

        //glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        //glClear(GL_COLOR_BUFFER_BIT);
        //glfwSwapBuffers(Window::getWindow());
        
    }

    Graphics::destroyPipeline();
    
    return 0;
}
