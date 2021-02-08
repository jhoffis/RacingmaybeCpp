//
// Created by jhoffis on 2/8/2021.
//

#ifndef RACINGMAYBE_TESTSCENE_H
#define RACINGMAYBE_TESTSCENE_H


#include <src/scenes/SceneEnvir.h>

class TestScene : SceneEnvir {

public:
    void updateGenerally() override;
    void updateResolution() override;
    void tick(double delta) override;
    void renderGame(Renderer renderer, Window window, Camera cam3d, Camera cam2d) override;
    void renderUILayout(NkContext ctx) override;
    void keyInput(int keycode, int action) override;
    bool mouseButtonInput(int button, int action, double x, double y) override;
    void mousePosInput(double x, double y) override;
    void mouseScrollInput(double x, double y) override;

};


#endif //RACINGMAYBE_TESTSCENE_H
