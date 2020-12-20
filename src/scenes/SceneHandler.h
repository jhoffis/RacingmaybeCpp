#include <src/engine/io/Window.h>
#include "SceneEnvir.h"
#include "Scenes.h"

class SceneHandler : ISceneManipulator {
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
    std::vector<SceneEnvir> &scenes;
//UIUsernameModal usernameModal;
//UIExitModal exitModal;
//static UIMessageModal messageModal;
//final NkColor white;

     void changeScene(int scenenr, bool logCurrent) {
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

};
