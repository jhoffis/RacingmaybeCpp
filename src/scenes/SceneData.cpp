//
// Created by Jens Benz on 20.12.2020.
//

#include "SceneData.h"
#include <iostream>

void render(const SceneData* scene)
{
}

void tick(const SceneData* scene, auto delta)
{
}

void keyInput(const SceneData* scene, auto key, auto action)
{
}

void mouseButtonInput(const SceneData* scene, auto button, auto action, auto x, auto y)
{
}

void mousePosInput(const SceneData* scene, auto x, auto y)
{
	std::cout << x << ' ' << y << std::endl;
}
