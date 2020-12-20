
#include <src/engine/io/Window.h>
#include <src/engine/graphics/Renderer.h>
#include <src/engine/graphics/Camera.h>
#include "NkContext.h"

class ISceneManipulator {
public:
    // run me first before any init under (except finalizeInit)
    virtual void updateGenerally() = 0;
    virtual void updateResolution() = 0;
    virtual void tick(double delta) = 0;
    virtual void renderGame(Renderer renderer, Window window, Camera cam3d, Camera cam2d) = 0;
    virtual void renderUILayout(NkContext ctx) = 0;
    virtual void keyInput(int keycode, int action) = 0;
    virtual void mouseButtonInput(int button, int action, double x, double y) = 0;
    virtual void mousePosInput(double x, double y) = 0;
    virtual void mouseScrollInput(double x, double y) = 0;

};
