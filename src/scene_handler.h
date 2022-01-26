#pragma once
#include "window.h"

namespace  SceneHandler {


	void createSceneHandler();
	void tick(double delta);
	void render();
	/*
	void updateGenerally() override;
	void updateResolution() override;
	void tick(double delta) override;
	void renderGame() override;
	void renderUILayout(NkContext ctx) override;
	void keyInput(int keycode, int action) override;
	void mouseButtonInput(int button, int action, double x, double y) override;
	void mousePosInput(double x, double y) override;
	void mouseScrollInput(double x, double y) override;
	*/
}