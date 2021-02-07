//
// Created by jhoffis on 2/7/2021.
//

#ifndef RACINGMAYBE_TOPBARINTERACTION_H
#define RACINGMAYBE_TOPBARINTERACTION_H

#include <src/engine/io/Window.h>

class TopbarInteraction {
    Window window;
public:
    double xTb, yTb, heightRatio;
    bool held;

    explicit TopbarInteraction(const Window& window): window(window) {
        xTb = 0;
        yTb = 0;
        held = false;
    }
    void press(double x, double y);
    void release();
    void move(double toX, double toY);
    double getHeight() const;

private:
    void pressedWithin(double x, double y);
};


#endif //RACINGMAYBE_TOPBARINTERACTION_H
