#ifndef RACINGMAYBE_SCENEHANDLER
#define RACINGMAYBE_SCENEHANDLER

#include <src/elem/interactions/TopbarInteraction.h>
#include "SceneEnvir.h"

class SceneHandler : ISceneManipulator {
    //final NkColor white;
    //static UIMessageModal messageModal;
    //UIExitModal exitModal;
    //UIUsernameModal usernameModal;
    std::vector<SceneEnvir> &scenes;
    TopbarInteraction &topbarTransparent;
//    Console console;
public:
    SceneHandler(std::vector<SceneEnvir> &scenes, TopbarInteraction &topbarTransparent);
    void updateGenerally() override;
    void updateResolution() override;
    void tick(double delta) override;
    void renderGame() override;
    void renderUILayout(NkContext ctx) override;
    void keyInput(int keycode, int action) override;
    bool mouseButtonInput(int button, int action, double x, double y) override;
    void mousePosInput(double x, double y) override;
    void mouseScrollInput(double x, double y) override;
private:
    void changeScene(int scenenr, bool logCurrent);
};

#endif