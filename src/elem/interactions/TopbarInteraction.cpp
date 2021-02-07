
#include "TopbarInteraction.h"

void TopbarInteraction::press(double x, double y) {
    if (y < getHeight()) {
        pressedWithin(x, y);
    }
}

void TopbarInteraction::release() {
    held = false;
    window.setFullscreen(window.isFullscreen());
}

void TopbarInteraction::move(double toX, double toY) {
    if (held) {
        int x, y;
        glfwGetWindowPos(window.getWindow(), &x, &y);

        x = x + (int) (toX - xTb);
        y = y + (int) (toY - yTb);

        glfwSetWindowPos(window.getWindow(), x, y);
    }
}

double TopbarInteraction::getHeight() const {
    return Window::HEIGHT / heightRatio;
}

void TopbarInteraction::pressedWithin(double X, double Y) {
    // Move window
    xTb = X;
    yTb = Y;
    held = true;
}
