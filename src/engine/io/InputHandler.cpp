//
// Created by jhoffis on 2/7/2021.
//

#include "InputHandler.h"

static SceneHandler *_sceneHandler;
static double x, y;

void SetupInputs(SceneHandler &sceneHandler, GLFWwindow *window) {
    _sceneHandler = &sceneHandler;

    glfwSetKeyCallback(window, [](auto window, auto key, auto scancode, auto action, auto mods) {
        _sceneHandler->keyInput(key, action);
    });

    glfwSetMouseButtonCallback(window, [](auto window, auto button, auto action, auto mods) {
//        MOUSEBUTTON = button;
//        MOUSEACTION = action;
        _sceneHandler->mouseButtonInput(button, action, x, y);
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
    });

    glfwSetCursorPosCallback(window, [](auto window, auto xpos, auto ypos) {

//        nk_input_motion(ctx, (int) xpos, (int) ypos);

        x = xpos;
        y = ypos;

        _sceneHandler->mousePosInput(x, y);

//        this.currentScene.mousePosInput(x, y);
    });

//    glfwSetScrollCallback(myWindow,
//                               GLFWScrollCallback.create((window, xoffset, yoffset) -> {
//        float x = (float) xoffset;
//        float y = (float) yoffset;
//
//        this.currentScene.mouseScrollInput(x, y);
//    }));
}