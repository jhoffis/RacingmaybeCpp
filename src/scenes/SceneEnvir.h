
#include <src/scenes/adt/ISceneManipulator.h>
#include <vector>
#include <src/engine/graphics/GameObject.h>

class SceneEnvir : public ISceneManipulator {
protected:
    const std::vector<GameObject> gameObjects;
    int sceneIndex;
public:

};

