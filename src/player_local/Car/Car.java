package player_local.Car;

import audio.CarAudio;
import elem.objects.Camera;
import engine.graphics.Renderer;
import main.Game;

public class Car {

	public static final String[] CAR_TYPES = {"Decentra"
			, "Oldsroyal", "Fabulvania", "Thoroughbred"
			},
			DESCRIPTION = {
					"Has turbo that scales well!",
					"Strongest Tireboost beast", "Got an extra NOS bottle!", "+100% kW from Beefy Block"
			};
	private CarModel model;
	private final CarStats stats;
	private final CarFuncs funcs;
	private Rep rep;
	private CarAudio audio;

	private Car(boolean user) {
		stats = new CarStats();
		model = new CarModel();
		if (user)
			funcs = new CarFuncs();
		else
			funcs = null;
	}

	public Car() {
		this(false);
		rep = new Rep();
		reset();
	}
	
	public Car(int carNameIndex, boolean user) {
		this(user);
		switchTo(carNameIndex);
		reset();
	}

	public void switchTo(int index) {
		if(rep != null && rep.getNameID() == index)
			return;
		model.setModel(index);
		
		int nosTimeStandard= 1000;
		int nosBottleAmountStandard= 0;
		double nosStrengthStandard= 0;
		double hp = 0;
		double weight= 0;
		double speedTop= 0;
		int rpmIdle= 0;
		int rpmTop= 0;
		int gearTop= 0;
		int tbTimeStandard = 900;
		double tbStrengthStandard= 0;
		double tbArea= 25;
		double bar = 0;

		switch (index) {
			case 0 -> {
				rpmIdle = 1000;
				hp = 210;
				weight = Game.DEBUG ? 2 : 1400;
				gearTop = 5;
				rpmTop = 7800;
				speedTop = Game.DEBUG ? 2050 : 205;
				bar = 1.6;
				tbStrengthStandard = Game.DEBUG ? 10 : 0;
//				rpmIdle = 1000;
//				hp = 200;
//				weight = 1000; //Game.DEBUG ? 2 : 1400;
//				gearTop = 5;
//				rpmTop = 7800;
//				speedTop = 200;
////				bar = 1.6;
////				tbStrengthStandard = 0Game.DEBUG ? 10 : 0;
			}
			case 1 -> {
				rpmIdle = 300;
				hp = 380;
				weight = 3207;
				gearTop = 4;
				rpmTop = 2500;
				speedTop = 202;
				tbTimeStandard = 1800;
				tbStrengthStandard = 0.5;
				tbArea = -1;
			}
			case 2 -> {
				rpmIdle = 800;
				hp = 80;
				weight = 740;
				gearTop = 6;
				rpmTop = 5500;
				speedTop = 204;
				nosBottleAmountStandard = 1;
				nosStrengthStandard = 0.6;
			}
			case 3 -> {
				rpmIdle = 1200;
				hp = 320;
				weight = 1629;
				gearTop = 6;
				rpmTop = 9200;
				speedTop = 210;
				tbTimeStandard = 700;
			}
		}

		rep = new Rep(index, nosTimeStandard, nosBottleAmountStandard,
				nosStrengthStandard, hp, weight, speedTop, rpmIdle, rpmTop,
				gearTop, tbTimeStandard, tbStrengthStandard,
				tbArea, bar);
		reset();
	}

	public void completeReset() {
		switchTo(rep.getNameID());
		reset();
	}
	
	public void reset() {
		stats.reset(rep);
		if (audio != null)
			audio.reset();
	}

	public void updateSpeed(float tickFactor) {
		if (funcs.updateSpeed(stats, rep, tickFactor)) {

			audio.soundbarrier();
			if (!stats.soundBarrierBroken) {
				stats.soundBarrierBroken = true;
				if (rep.is(Rep.nosSoundbarrier))
					stats.nosBottleAmountLeft++;
			}

		}

		audio.motorPitch(stats.rpm, rep.get(Rep.rpmTop), 
				2,
				(stats.NOSON || stats.tireboostON) ? 3 : 1);
		audio.turbospoolPitch((float) stats.spool, (float) rep.getTurboKW(),
				stats.getTurboBlow() ? 3 : 1);
		audio.straightcutgearsPitch(stats.speed, rep.get(Rep.spdTop));

		if (shouldRedline())
			audio.redline();
		else
			audio.redlineStop();
	}
	
	private boolean shouldRedline() {
		return stats.rpm >= rep.get(Rep.rpmTop) - 100;
	}
	
	public boolean startBoost(int timeDiff) {
		boolean res = false;
		
		if(stats.throttle && !stats.hasDoneStartBoost) {
			stats.hasDoneStartBoost = true;
			
			res = tryTireboost(timeDiff);
	
			if (rep.is(Rep.nosAuto))
				nos(false);
		}
		
		return res;
	}

	private boolean tryTireboost(int timeDiff) {
		if (hasTireboost()) {
			funcs.tireboost(stats, rep, System.currentTimeMillis(), 1, timeDiff);
			audio.tireboost();
			return true;
		}
		return false;
	}

	public int tireboostLoss(int timeDiff) {
		return 100 - (int) (100f * funcs.tireboostLoss(rep, timeDiff));
	}

	public boolean isTireboostRight() {
		return stats.tireboostON && stats.throttle;
	}

	public boolean isTireboostRunning() {
		return stats.tireboostTimeLeft >= System.currentTimeMillis();
	}

	public boolean throttle(boolean down, boolean safe) {
		if(down != stats.throttle && (safe || stats.gear == 0)) {
			clutch(!down);
			stats.throttle = down;
			if (down) {
				audio.motorAcc(hasTurbo());
			} else {
				tryTurboBlowoff();
				audio.motorDcc();
				
			}
			return true;
		}
		return false;
	}

	public void clutch(boolean down) {
		if (down) {
			if (!stats.clutch) {
				stats.clutch = true;
				stats.resistance = 1.0f;
				audio.clutch(true);
			}
		} else {
			if (stats.clutch) {
				stats.clutch = false;
				audio.clutch(false);
				if (stats.gear > 0)
					stats.resistance = 0.0f;
			}
		}
	}

	/**
	 * @return shifted into a new gear
	 */
	public int shift(int gear, double delta) {
		int res = 0;
		System.out.println("gear: " + gear);
		if (gear != stats.gear && (gear <= rep.get(Rep.gearTop) && gear >= 0)) {
			if (gear == 0 || stats.clutch || rep.is(Rep.throttleShift)) {
				stats.gear = gear;
				
				if (!stats.clutch) {
					if (gear == 0)
						stats.resistance = 1.0f;
					else
						stats.resistance = 0.0f;
				}
				

				if (gear != 0) {
					audio.gear();
					res = 2; // TODO gjør om til enums for lettere lesing
				} else {
					audio.gearNeutral();
					res = 1;
				}
			} else {
				audio.grind();
				funcs.brake(stats, delta);
				res = -1;
			}
			
			if(!shouldRedline())
				audio.redlineStop();
		}
		return res;
	}

	public void shiftUp(double delta) {
		if (shift(stats.gear + 1, delta) > 0 && stats.rpm > rep.get(Rep.rpmTop) * 0.7f)
			audio.backfire();
	}

	public void shiftDown(double delta) {
		shift(stats.gear - 1, delta);
	}

	public void nos(boolean down) {
		if (stats.nosBottleAmountLeft > 0) {
			if (!down)
				funcs.nos(stats, rep, System.currentTimeMillis(), 1);
			audio.nos(down);
		}
	}

	public float getTurbometer() {
		return (float) (stats.spool * rep.get(Rep.bar) * 45.0) - 180f;
	}
	
	/**
	 * @return radian that represents rpm from -180 to ca. 35 - 40 idk yet
	 */
	public float getTachometer() {
		return 235f * (float) ((stats.rpm + 1.0) / rep.get(Rep.rpmTop))
				- 203f;
	}

	public void blowTurbo(boolean down) {
		// dont gain, lose.
		stats.setTurboBlow(hasTurboBlow() && down);
	}

	private void tryTurboBlowoff() {
		if (hasTurbo())
			audio.turboBlowoff(stats.spool * rep.get(Rep.bar));
	}

	public void renderCar(Renderer renderer, Camera camera) {
		model.getModel().render(renderer, camera);
	}
	
	public void regenTurboBlow() {
		if (hasTurbo() && rep.get(Rep.turboblow) < 100f) {
			rep.add(Rep.turboblow, rep.get(Rep.turboblowRegen));
			if (rep.get(Rep.turboblow) > 100f) {
				rep.set(Rep.turboblow, 100f);
				rep.set(Rep.turboblowRegen, 0);
			}
			
			if (stats.usedTurboBlow) {
				rep.set(Rep.turboblowRegen, (100f - rep.get(Rep.turboblow)) / 2f);
				stats.usedTurboBlow = false;
			}
		}
	}

	public void pushModelPosition(float distance, int speed, long raceTime) {
		model.pushInformation(distance, speed, raceTime);
	}

	public double calcPowerloss() {
		double loss = 1f - (stats.rpm - rep.get(Rep.rpmIdle)) / (rep.get(Rep.rpmTop) - rep.get(Rep.rpmIdle));
		return (int) (loss * 1000f) / 10.0;
	}
	
	/*
	 * GETTERS AND SETTERS
	 */

	public CarStats getStats() {
		return stats;
	}

	public Rep getRep() {
		return rep;
	}

	public void setRep(Rep rep) {
		this.rep = rep;
	}

	public CarAudio getAudio() {
		return audio;
	}

	public void setAudio(CarAudio audio) {
		this.audio = audio;
	}

	public void updateVolume() {
		audio.updateVolume();
	}

	public int getSpeed() {
		return (int) stats.speed;
	}

	public int getDistance() {
		return (int) stats.distance;
	}
	
	public String getDistanceOnline() {
		return model.isFinished() ? "finished" : (int) -model.getPositionDistance() + "m";
	}

	public boolean hasTurbo() {
		return rep.hasTurbo();
	}

	public boolean hasNOS() {
		return rep.hasNOS();
	}

	public boolean hasTireboost() {
		return rep.hasTireboost();
	}

	public boolean hasTurboBlow() {
		return rep.get(Rep.turboblow) > 0;
	}

	public CarModel getModel() {
		return model;
	}

}