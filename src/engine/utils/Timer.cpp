#include "Timer.h"
#include <chrono>
using namespace std::chrono;

long lastLoopTime;

long timerGetTime() {
    return duration_cast< milliseconds >(
            system_clock::now().time_since_epoch()
    ).count();
}

double timerGetDelta() {
    long time = timerGetTime();
    double delta = time - lastLoopTime;
    lastLoopTime = time;
    return delta;
}