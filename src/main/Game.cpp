//#define NK_IMPLEMENTATION
//#define NK_INCLUDE_FIXED_TYPES
//#define NK_INCLUDE_DEFAULT_ALLOCATOR
//#define NK_INCLUDE_STANDARD_IO
//#define NK_INCLUDE_VERTEX_BUFFER_OUTPUT
////#define NK_INCLUDE_FONT_BAKING
//#define NK_INCLUDE_DEFAULT_FONT
//#include "nuklear.h"

#include <iostream>
#include <src/engine/io/Window.h>
#include <src/engine/utils/Timer.h>
#include <src/scenes/SceneHandler.h>
#include <vector>
#include <src/engine/io/InputHandler.h>


//void processInput(GLFWwindow *window)
//{
//    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
//        glfwSetWindowShouldClose(window, true);
//}

int main() {
    // Setup
    Window window(false, false);

    /*
     * Topbars
     */
    TopbarInteraction topbarTransparent(window);
    topbarTransparent.heightRatio = 18;

//    RegularTopbar regularTopbar = new RegularTopbar(features, minimizeButton, closeButton, topbar);
//    LobbyTopbar lobbyTopbar = new LobbyTopbar(features, minimizeButton, closeButton, new TopbarInteraction(window));

    std::vector<SceneEnvir> scenes;
    SceneHandler sceneHandler(scenes, topbarTransparent);

    SetupInputs(sceneHandler, window.getWindow());

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
