#include "timer.h"
#include <chrono>
using namespace std::chrono;

uint64_t lastLoopTime;

uint64_t Timer::nowMillis() {
    return duration_cast< milliseconds >(
            system_clock::now().time_since_epoch()
    ).count();
}

double Timer::nowDelta() {
    auto time = nowMillis();
    auto delta = static_cast<double>(time - lastLoopTime);
    lastLoopTime = time;
    return delta;
}

