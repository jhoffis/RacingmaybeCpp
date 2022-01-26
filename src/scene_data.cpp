#include "scene_data.h"

#include <iostream>

void render(const SceneData* scene)
{
}

void tick(const SceneData* scene, auto delta)
{
}

void keyInput(const SceneData* scene, int key, int action)
{
}

void mouseButtonInput(const SceneData* scene, int button, int action, int x, int y)
{
	std::cout << "Button: " << button << ", Action: " << action << std::endl;
}

void mousePosInput(const SceneData* scene, int x, int y)
{
	std::cout << x << ' ' << y << std::endl;
}
