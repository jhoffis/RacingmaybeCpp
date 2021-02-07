package player_local.Car;

import java.util.ArrayList;

public class CarStats {
	public boolean throttle, clutch, NOSON, tireboostON,
			sequentialShift, soundBarrierBroken, usedTurboBlow, hasDoneStartBoost;
	private boolean blowTurboON;
	public int nosBottleAmountLeft, gear;
	public long tireboostTimeLeft;
	public double speed, distance, drag, spool, spdinc, resistance, rpm, rpmGoal;
	private final ArrayList<Long> nosTimesLeft = new ArrayList<>(), nosTimesFrom = new ArrayList<>();

	/**
	 * Resets temporary stats
	 */
	public void reset(Rep rep) {
		blowTurboON = false;
		//Med space
//		clutch = false;
//		resistance = 0f;
		// Uten space
		clutch = true;
		resistance = 1f;
		
		throttle = false;
		NOSON = false;
		soundBarrierBroken = false;
		hasDoneStartBoost = false;
		nosBottleAmountLeft = (int) rep.getInt(Rep.nosSize);
		nosTimesLeft.clear();
		nosTimesFrom.clear();
		speed = 0;
		spdinc = 0;
		distance = 0;
		gear = 1;
		rpm = rep.get(Rep.rpmIdle);
		rpmGoal = rpm;
		tireboostTimeLeft = 0;
		drag = 1;
		sequentialShift = rep.is(Rep.sequential);
	}

	public float getNosPercentageLeft(int i) {
		
		if(i >= nosTimesLeft.size())
			return 1f;
		
		float res = 0.0f;
		
		long timespan = nosTimesLeft.get(i) - System.currentTimeMillis();
		if(timespan > 0) {
			res = (float)timespan / (float)(nosTimesLeft.get(i) - nosTimesFrom.get(i)) ;
		}
		
		return res;
	}

	public void popNosBottle(long fromTime, long tillTime) {
		nosTimesFrom.add(fromTime);
		nosTimesLeft.add(tillTime);
		nosBottleAmountLeft--;
	}
	
	public long getNosTimeLeft(int i) {
		return i < nosTimesLeft.size() ? nosTimesLeft.get(i) : -1;
	}
	
	public void setTurboBlow(boolean blowTurboON) {
		if (this.blowTurboON = blowTurboON)
			usedTurboBlow = true;
	}
	
	public boolean getTurboBlow() {
		return blowTurboON;
	}

}