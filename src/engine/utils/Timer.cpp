#include "Timer.h"
#include <chrono>
using namespace std::chrono;

long lastLoopTime;

long timerNowMillis() {
    return duration_cast< milliseconds >(
            system_clock::now().time_since_epoch()
    ).count();
}

double timerNowDelta() {
    long time = timerNowMillis();
    double delta = time - lastLoopTime;
    lastLoopTime = time;
    return delta;
}