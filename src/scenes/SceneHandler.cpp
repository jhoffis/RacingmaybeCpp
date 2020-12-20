#include <vector>
#include "SceneHandler.h"

SceneHandler::SceneHandler(std::vector<SceneEnvir> &scenes) : scenes(scenes) {

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

void SceneHandler::tick(double delta) {

}

void SceneHandler::renderGame(Renderer renderer, Window window, Camera cam3d, Camera cam2d) {
}

void SceneHandler::renderUILayout(NkContext ctx) {
}

void SceneHandler::keyInput(int keycode, int action) {

}

void SceneHandler::mouseButtonInput(int button, int action, double x, double y) {

}

void SceneHandler::mousePosInput(double x, double y) {

}

void SceneHandler::mouseScrollInput(double x, double y) {

}
