package engine.utils;

public class Timer {

	private long lastLoopTime;
	
	public void init() {
		lastLoopTime = getTime();
	}
	
	private long getTime() {
		return System.nanoTime();
	}
	
	public float getDelta() {
		long time = getTime();
		float delta = time - lastLoopTime;
		lastLoopTime = time;
		 // Base a tick around 40 000 000 ns (40 ms) == 25 ticks per sec
        return delta / 40000000f;
	}
	
	public double getLastLoopTime() {
		return lastLoopTime;
	}
}
