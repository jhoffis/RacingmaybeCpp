#include <iostream>
//#include <src/engine/io/Window.h>
#include <src/engine/utils/Timer.h>
#include <src/scenes/SceneHandler.h>
#include <vector>

//void processInput(GLFWwindow *window)
//{
//    if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
//        glfwSetWindowShouldClose(window, true);
//}
static std::vector<SceneEnvir> scenes;
static SceneHandler sceneHandler(scenes);

int main() {
    // Setup
  Window window(false, false);


    glfwSetKeyCallback(window.getWindow(), [](auto window, auto key, auto scancode, auto action, auto mods) {
        sceneHandler.keyInput(key, action);

    });
    
//    glfwSetMouseButtonCallback(myWindow, GLFWMouseButtonCallback
//            .create((window, button, action, mods) -> {
//        MOUSEBUTTON = button;
//        MOUSEACTION = action;
//        this.currentScene.mouseButtonInput(button, action, x, y);
//        try (MemoryStack stack = stackPush()) {
//            DoubleBuffer cx = stack.mallocDouble(1);
//            DoubleBuffer cy = stack.mallocDouble(1);
//
//            glfwGetCursorPos(window, cx, cy);
//
//            int x = (int) cx.get(0);
//            int y = (int) cy.get(0);
//
//            int nkButton;
//            switch (button) {
//                case GLFW_MOUSE_BUTTON_RIGHT :
//                    nkButton = NK_BUTTON_RIGHT;
//                    break;
//                case GLFW_MOUSE_BUTTON_MIDDLE :
//                    nkButton = NK_BUTTON_MIDDLE;
//                    break;
//                default :
//                    nkButton = NK_BUTTON_LEFT;
//            }
//            nk_input_button(ctx, nkButton, x, y,
//                            action != GLFW.GLFW_RELEASE);
//        }
//    }));
//
//    glfwSetCursorPosCallback(myWindow,
//                             GLFWCursorPosCallback.create((window, xpos, ypos) -> {
//
//        nk_input_motion(ctx, (int) xpos, (int) ypos);
//
//        x = (float) xpos;
//        y = (float) ypos;
//        this.currentScene.mousePosInput(x, y);
//    }));
//
//    glfwSetScrollCallback(myWindow,
//                               GLFWScrollCallback.create((window, xoffset, yoffset) -> {
//        float x = (float) xoffset;
//        float y = (float) yoffset;
//
//        this.currentScene.mouseScrollInput(x, y);
//    }));
    
    // Run the game
    bool running = true;
    while(running)
    {
        if (glfwWindowShouldClose(window.getWindow())) {
            running = false;
            break;
        }
//        steam.update();
        //sceneHandler.tick(Timer::nowDelta());
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
