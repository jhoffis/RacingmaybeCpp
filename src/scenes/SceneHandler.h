#ifndef RACINGMAYBE_SCENEHANDLER
#define RACINGMAYBE_SCENEHANDLER

#include "SceneEnvir.h"

class SceneHandler : ISceneManipulator {
    std::vector<SceneEnvir> &scenes;
    //UIUsernameModal usernameModal;
    //UIExitModal exitModal;
    //static UIMessageModal messageModal;
    //final NkColor white;
public:
    SceneHandler(std::vector<SceneEnvir> &scenes);
    void updateGenerally() override;
    void updateResolution() override;
    void tick(double delta) override;
    void renderGame(Renderer renderer, Window window, Camera cam3d, Camera cam2d) override;
    void renderUILayout(NkContext ctx) override;
    void keyInput(int keycode, int action) override;
    void mouseButtonInput(int button, int action, double x, double y) override;
    void mousePosInput(double x, double y) override;
    void mouseScrollInput(double x, double y) override;

private:
     void changeScene(int scenenr, bool logCurrent);
};

#endif