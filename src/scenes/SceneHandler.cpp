#include <vector>
#include <iostream>
#include "SceneHandler.h"
#include "Scenes.h"

SceneHandler::SceneHandler(std::vector<SceneEnvir> &scenes, TopbarInteraction &topbarTransparent) :
    scenes(scenes), topbarTransparent(topbarTransparent) {

}

void SceneHandler::updateGenerally() {
    scenes.at(Scenes::CURRENT).updateGenerally();
}


void SceneHandler::updateResolution() {
//    UISceneInfo.updateResolution();

    for (int i = 0; i < sizeof(scenes); i++) {
        scenes[i].updateResolution();
    }
}

void SceneHandler::changeScene(int scenenr, bool logCurrent) {
    if (logCurrent) {
        if (scenenr == Scenes::PREVIOUS)
        {
            do {
                scenenr = Scenes::HISTORY.top();
                Scenes::HISTORY.pop();
            } while (!Scenes::HISTORY.empty() && (Scenes::HISTORY.top() == scenenr || scenenr == Scenes::CURRENT));
        }
        Scenes::HISTORY.push(Scenes::CURRENT);
    }
    Scenes::CURRENT = scenenr;

    // Weird previous ik.
    if (Scenes::CURRENT < Scenes::OPTIONS)
        Scenes::PREVIOUS_REGULAR = Scenes::CURRENT;

}

void SceneHandler::tick(double delta) {

}

void SceneHandler::renderGame(Renderer renderer, Window window, Camera cam3d, Camera cam2d) {
}

void SceneHandler::renderUILayout(NkContext ctx) {
}

void SceneHandler::keyInput(int keycode, int action) {
    std::cout << keycode <<  std::endl;
}

bool SceneHandler::mouseButtonInput(int button, int action, double x, double y) {
//    if(topbarTransparent != null && features != null && !features.getWindow().isFullscreen()) {
        if (action != GLFW_RELEASE) {
            topbarTransparent.press(x, y);
        } else {
            topbarTransparent.release();
        }
//    }
    return false;
}

void SceneHandler::mousePosInput(double x, double y) {
//    if(topbar != null && features != null && !features.getWindow().isFullscreen())
    topbarTransparent.move(x, y);
//    mousePositionInput(x, y);
}

void SceneHandler::mouseScrollInput(double x, double y) {

}
