package player_local.Car;

public class CarFuncs {

	private final int soundBarrierSpeed;

	public CarFuncs() {
		soundBarrierSpeed = 1234;
	}

	/*
	 * returns true if sound barrier is broken
	 */
	public boolean updateSpeed(CarStats stats, Rep rep, double tickFactor) {

		double speedChange;

		// MOVEMENT
		if (stats.throttle && !stats.clutch && isGearCorrect(stats, rep))
			speedChange = boostCar(stats, rep, speedInc(stats, rep, tickFactor), System.currentTimeMillis());
		else {
			speedChange = decelerateCar(stats);
			stats.NOSON = false;
			stats.tireboostON = false;			
		}
		
		// RPM
		updateRPM(stats, rep, tickFactor);

		stats.spdinc = speedChange * tickFactor;
		stats.speed += speedIncCheck(stats, rep, stats.spdinc);

		updateHighestSpeed(stats, rep);
		calculateDrag(stats, rep);
		calculateDistance(stats, tickFactor);

		return soundBarrierSpeed < stats.speed;
	}

	private void updateHighestSpeed(CarStats stats, Rep rep) {
		if (stats.speed > rep.get(Rep.highestSpdAchived))
			rep.set(Rep.highestSpdAchived, (int) stats.speed);
	}

	private double speedInc(CarStats stats, Rep rep, double tickFactor) {
		double w = rep.get(Rep.kg);
		double rpmCalc = 1;
		if (!rep.is(Rep.stickyclutch))
			rpmCalc = stats.rpm / rep.get(Rep.rpmTop);

		double kw = rep.get(Rep.kW);

		if (rep.hasTurbo()) {
			double tkw = rep.getTurboKW() * stats.spool;
			
			if (stats.getTurboBlow() && stats.throttle) {
				if (rep.get(Rep.turboblow) >= 0) {
					tkw = tkw * 5;
					rep.add(Rep.turboblow, -1f * tickFactor);
				}
			}
			
			kw += tkw;
		} else {
			stats.setTurboBlow(false);
		}
		
		return (kw * rpmCalc / w) * stats.drag
				* (stats.gear > 0
				? rep.get(Rep.gearbalance) * (rep.get(Rep.gearTop) / stats.gear)
				: 1);
	}

	private boolean isGearCorrect(CarStats stats, Rep rep) {
		return stats.speed < gearMax(stats, rep);
	}

	private double gearMax(CarStats stats, Rep rep) {
		return stats.gear * (rep.get(Rep.spdTop) / rep.get(Rep.gearTop));
	}

	private double boostCar(CarStats stats, Rep rep, double speedInc, long comparedTimeLeft) {

		if (rep.hasNOS()) {
			int nosAmount = 0;
			
			for (int i = 0; i < rep.get(Rep.nosSize); i++) {
				if (stats.getNosTimeLeft(i) > comparedTimeLeft) {
					nosAmount++;
					speedInc += rep.get(Rep.nos) / nosAmount;
				}
			}
			stats.NOSON = nosAmount > 0;
		}

		if (stats.tireboostTimeLeft > comparedTimeLeft) {
			speedInc += rep.get(Rep.tb);
			stats.tireboostON = true;
		} else {
			stats.tireboostON = false;
		}

		return speedInc;
	}
	
	private double speedIncCheck(CarStats stats, Rep rep, double spdinc) {
		double topspd = rep.get(Rep.spdTop);
		if (stats.speed + spdinc > topspd)
			spdinc = topspd - stats.speed;
		return spdinc;
	}

	private double decelerateCar(CarStats stats) {
		double dec = 0;

		if (stats.speed > 0)
			dec = -0.5f;
		else
			stats.speed = 0;

		return dec;
	}

	/**
	 * Updates RPM value based on engaged clutch and throttle
	 */
	private void updateRPM(CarStats stats, Rep rep, double tickFactor) {
		double rpmChange = 0;
		double rpm = stats.rpmGoal;
		
		if (stats.resistance < 1) {

			// If clutch engaged
			double change = rpm;
			double gearFactor = stats.speed / (gearMax(stats, rep) + 1);
			rpm = rep.get(Rep.rpmTop) * gearFactor;

			// Turbo spooling
			change = rpm - change;
			if (rep.hasTurbo() && change > 0) {
				double increase = 0.0000045f;

				if (stats.spool > 1)
					increase = increase * 0.005f * Rep.spool;
				stats.spool += rpm * increase * tickFactor;
			} else {
				stats.spool = 0;
			}

			long minimum;

			if (stats.throttle)
				minimum = rep.getInt(Rep.rpmIdle) * 2 / 3;
			else
				minimum = rep.getInt(Rep.rpmIdle);

			if (rpm < minimum)
				rpm = minimum;
		} else if (stats.throttle) {
			if (rpm < rep.get(Rep.rpmTop) - 60) {

				double rpmFactor = (rep.get(Rep.rpmTop) * 0.8f)
						+ (rpm * 0.2f);
				rpmChange = rep.get(Rep.kW)
						* (rpmFactor / rep.get(Rep.rpmTop))
						* stats.resistance;

				stats.spool = 0;
			} else {
				// Redlining
				rpm = rep.get(Rep.rpmTop) - 100;
			}
		} else {

			// Not engaged and throttle not down
			if (rpm > rep.getInt(Rep.rpmIdle))
				rpmChange = -(rep.get(Rep.kW) * 0.5f);
			else
				// Sets RPM to for instance 1000 rpm as standard.
				rpm = rep.getInt(Rep.rpmIdle);
			stats.spool = 0;
		}

		stats.rpmGoal = rpm + rpmChange * tickFactor;
		
		double diff = stats.rpmGoal - stats.rpm;
		if(Math.abs(diff) > 200)
			stats.rpm = stats.rpm + (diff * tickFactor);
		else
			stats.rpm = stats.rpmGoal;
	}

	// FIXME this drag is just wrong
	private void calculateDrag(CarStats stats, Rep rep) {
		double drag = -Math.pow(stats.speed / rep.get(Rep.spdTop), 5) + 1;
		if (drag < 0)
			drag = 0;
		stats.drag = drag;
	}

	// private void CalculateDistance(CarStats stats, double tickFactor) {
	// // 25 ticks per sec. kmh, distance in meters. So, x / 3.6 / 25.
	// stats.AddDistance((stats.GetSpeed() / 90) * tickFactor);
	// }

	public void tireboost(CarStats stats, Rep rep, long comparedTimeValue,
			int divideTime, int timeDiff) {
		stats.tireboostTimeLeft = comparedTimeValue
				+ (long) ((rep.get(Rep.tbMs) / divideTime) * tireboostLoss(rep, timeDiff));
	}
	
	public double tireboostLoss(Rep rep, int timeDiff) {
		if(rep.get(Rep.tbArea) == -1)
			return 1;
		
		double timeloss = 1f - (timeDiff / 1000f);
		if(timeloss < 0)
			timeloss = 0;
		return timeloss;
	}

	public void nos(CarStats stats, Rep rep, long comparedTimeValue,
			int divideTime) {
		if (stats.nosBottleAmountLeft > 0) {
			stats.popNosBottle(comparedTimeValue,
					comparedTimeValue + rep.getInt(Rep.nosMs) / divideTime);
		}
	}

	public int GetSoundBarrierSpeed() {
		return soundBarrierSpeed;
	}

	private void calculateDistance(CarStats stats, double tickFactor) {
		// 25 ticks per sec. kmh, distance in meters. So, x / 3.6 / 25.
		stats.distance += (stats.speed / 90) * tickFactor;
	}

	public void brake(CarStats stats, double tickFactor) {
		if (stats.speed > 0) {
			stats.speed -= 2f * tickFactor;
		}
	}
	
}
