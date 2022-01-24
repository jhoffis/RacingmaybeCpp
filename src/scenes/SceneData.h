#pragma once
#include <vector>
#include <engine/graphics/GameObject.h>

struct SceneData {
    const std::vector<GameObject> gameObjects;
    int sceneIndex;
};

void render(const SceneData* scene);
void tick(const SceneData* scene, auto delta);
void keyInput(const SceneData* scene, auto key, auto action);
void mouseButtonInput(const SceneData* scene, auto button, auto action, auto x, auto y);
void mousePosInput(const SceneData* scene, auto x, auto y);