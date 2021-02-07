package audio;

import java.util.Random;

import engine.math.Vec3;

/**
 * For each race load up the sounds for the cars and whatever is needed with
 * them here. Maybe have the buffers ready somewhere else, but you create the
 * carsounds here at the start of each race, and you delete them here after a
 * race. The same carsound is used when someone crosses the line. So that means,
 * actually, that you need sounds for every player. And then base the sound on
 * carstats and how powerful the engine is. But this can be implemented later. I
 * was the car to sound CRAAAAZY if you have tons of power. Maybe have like a
 * buzzing sound from the tireboost and a pssssst sound from the nos bottles.
 * For atmosphere, ya know. And then wind, rocks, shaking, quiet down everything
 * before hitting soundbarrier, etc.
 * 
 * I think that there should be nothing but atmosphere when actually racing.
 * Finish, win, lobby, whatever, there it is good to have music. Actually, it
 * would fit good to have a beat come in after you finish a race.. huh. Nothing
 * crazy there.
 * 
 * @author Jens Benz
 *
 */

public class CarAudio {

	private Source motor, turboBlowoff, turbospool, straightcutgears, redline,
			tireboost, grind, nos, nosDown, soundbarrier, gear, clutch, backfire;
	private int[] gears;
	private int motorAcc, motorDcc;
	private int clutchIn, clutchOut;
	private int turboBlowoffLow, turboBlowoffHigh;

	private float motorOverallVolume = 1;
	private float wavgain, turboBlowoffVolume;
	private AudioRemote audio;
	private Random r;

	public CarAudio(int motorAcc, int motorDcc, AudioRemote audio) {
		this.audio = audio;
		gear = new Source();
		r = new Random();
		setMotorAcc(motorAcc, motorDcc);
	}

	public void updateVolume() {
		float volume = audio.getVolume(AudioTypes.MASTER)
				* audio.getVolume(AudioTypes.SFX);

		float gain = (float) (volume * 2.5f);
		wavgain = gain / 2;
		if (wavgain > 1)
			wavgain = 1;

		turboBlowoffVolume = volume;
		turboBlowoff.volume(volume);
		gear.volume(volume);
		redline.volume(volume);
		nos.volume(volume);
		nosDown.volume(volume);
		tireboost.volume(volume);
		grind.volume(volume);
		soundbarrier.volume(volume);
		clutch.volume(volume);
		backfire.volume(volume);
	}

	public void motorPitch(double rpm, double totalRPM, double maxValue,
			float gain) {
		double value;
		rpm = maxValue * rpm;

		if (rpm > totalRPM * maxValue)
			value = maxValue;
		else if (rpm < 0)
			value = 0;
		else
			value = rpm / totalRPM;

		value = -0.05 * Math.pow(2, value) + 0.8 * value;
		motor.pitch((float) value);

		if (value > 1.0) {
			motorOverallVolume = 1;
		} else {
			motorOverallVolume = (float) value;
		}

		motorOverallVolume = motorOverallVolume * gain;

		updateMotorVolume();
	}

	private void updateMotorVolume() {
		motor.volume(wavgain * motorOverallVolume);
	}

	public void motorAcc(boolean hasTurbo) {
		motor.play(motorAcc);

		if (hasTurbo && !turbospool.isPlaying()) {
			turbospool.play();
			turboBlowoff.stop();
		}

		redlineStop();
	}

	public void motorDcc() {
		redlineStop();
		turbospool.stop();
		motor.play(motorDcc);
	}
	
	public void turboBlowoff(double barspool) {
		float volume = (float) barspool / 2f;
		if(volume > 1)
			volume = 1;
		turboBlowoff.volume(turboBlowoffVolume * volume);
		
		if (barspool < 1.6)
			turboBlowoff.play(turboBlowoffLow);
		else
			turboBlowoff.play(turboBlowoffHigh);
	}

	public void turbospoolPitch(float spool, float turboKw, float gain) {
		float value;

		float affect = turboKw / 800f;
		if (affect < 1)
			spool = spool * affect;

		if (spool > 1.0f)
			value = 1;
		else
			value = spool;

		turbospool.pitch(value);
		turbospool.volume(value * audio.getVolume(AudioTypes.MASTER)
				* audio.getVolume(AudioTypes.SFX) * gain);

	}

	public void straightcutgearsPitch(double speed, double speedTop) {
		double value;
		double maxValue = 0.2;
		speed = maxValue * speed;

		if (speed > speedTop * maxValue)
			value = maxValue;
		else if (speed < 0)
			value = 0;
		else
			value = speed / speedTop;

		value = -0.05 * Math.pow(2, value) + 4 * value;
		straightcutgears.pitch((float) value);
		straightcutgears.volume((float) (value * wavgain));
	}

	public void redline() {
		if (!redline.isPlaying())
			redline.play();
	}
	
	public void redlineStop() {
		if (redline != null && redline.isPlaying()) {
			redline.stop();
		}
	}

	public void tireboost() {
		tireboost.play();
	}
	
	public void backfire() {
		backfire.play();
	}

	public void gear() {
		int nextSfx = 0;
		nextSfx = r.nextInt(7);
		gear.play(gears[nextSfx]);
	}
	
	public void gearNeutral() {
		int nextSfx = 7;
		nextSfx += r.nextInt(gears.length - nextSfx);
		gear.play(gears[nextSfx]);
	}

	public void grind() {
		grind.play();
	}

	public void nos(boolean down) {
		if (down)
			nosDown.play();
		else
			nos.play();
	}

	public void soundbarrier() {
		soundbarrier.play();
	}

	public void clutch(boolean in) {
		if (in)
			clutch.play(clutchOut);
		else
			clutch.play(clutchIn);
	}

	public void reset() {
		motor.stop();
		nosDown.stop();
		turboBlowoff.stop();
		turbospool.stop();
		straightcutgears.stop();
		redline.stop();
		tireboost.stop();
		grind.stop();
		nos.stop();
		gear.stop();
		soundbarrier.stop();
		clutch.stop();
		setMotorPosition(new Vec3(0));
	}

	public void destroy() {
		motor.delete();
		turboBlowoff.delete();
		turbospool.delete();
		straightcutgears.delete();
		redline.delete();
		tireboost.delete();
		grind.delete();
		nos.delete();
		nosDown.delete();
		gear.delete();
		soundbarrier.delete();
		clutch.delete();
		backfire.delete();
	}

	public void setTurboBlowoff(int turboBlowoffLow, int turboBlowoffHigh) {
		turboBlowoff = new Source();
		this.turboBlowoffLow = turboBlowoffLow;
		this.turboBlowoffHigh = turboBlowoffHigh;
	}

	public void setTurbospool(Source turbospool) {
		this.turbospool = turbospool;
		turbospool.loop(true);
	}

	public void setStraightcut(Source straightcut) {
		this.straightcutgears = straightcut;
		straightcut.loop(true);
	}

	public void setRedline(Source redline) {
		this.redline = redline;
		redline.loop(true);
	}

	public void setTireboost(Source tireboost) {
		this.tireboost = tireboost;
	}

	public void setGrind(Source grind) {
		this.grind = grind;
	}

	public void setNos(Source nos) {
		this.nos = nos;
	}

	public void setGears(int[] gears2) {
		this.gears = gears2;
	}

	public void setMotorAcc(int motorAcc, int motorDcc) {
		motor = new Source();
		this.motorAcc = motorAcc;
		this.motorDcc = motorDcc;
		motor.loop(true);
	}

	public void setSoundbarrier(Source soundbarrier) {
		this.soundbarrier = soundbarrier;
	}

	public void setClutch(Source clutch, int clutchIn, int clutchOut) {
		this.clutch = clutch;
		this.clutchIn = clutchIn;
		this.clutchOut = clutchOut;
	}

	public Source getMotor() {
		return motor;
	}

	public void setMotorPosition(Vec3 pos) {
		motor.position(pos);
		turbospool.position(pos);
	}

	public void setBackfire(Source source) {
		backfire = source;
	}

	public void setNosDown(Source nosDown) {
		this.nosDown = nosDown;
	}
	
	

}
