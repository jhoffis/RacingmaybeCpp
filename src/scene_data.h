#pragma once
#include <vector>

struct SceneData {
    int sceneIndex;
    //const std::vector<GameObject> gameObjects;

    SceneData(int sceneIndex)
        : sceneIndex(sceneIndex)
    {}
};

void render(const SceneData* scene);
void tick(const SceneData* scene, auto delta);
void keyInput(const SceneData* scene, int key, int action);
void mouseButtonInput(const SceneData* scene, int button, int action, int x, int y);
void mousePosInput(const SceneData* scene, int x, int y);