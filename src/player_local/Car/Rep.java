package player_local.Car;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import adt.ICloneStringable;
import elem.ui.UILabel;
import elem.upgrades.RegVals;

/**
 * Used to store stats about Car and more easily communicate Cars with the
 * server. Also used to restore a players car after they have lost connection
 * and rejoined.
 * 
 * @author jonah
 *
 */

public class Rep implements ICloneStringable {
	private int nameid = -1;

	private double[] stats;
	private boolean[] changed;

	public static final int 
	nosSize = 0,
	nosMs = 1,
	nos = 2,
	kW = 3,
	kg = 4,
	spdTop = 5,
	rpmIdle = 6,
	rpmTop = 7,
	gearTop = 8,
	tbMs = 9,
	tb = 10,
	tbArea = 11,
	turboblow = 12,
	vgold = 13,
	vmoney = 14,
	fuel = 15,
	bar = 16,
	health = 17,
	displacement = 18,
	highestSpdAchived = 19,
	stickyclutch = 20,
	spool = 21,
	sequential = 22,
	nosSoundbarrier = 23,
	nosAuto = 24,
	throttleShift = 25,
	vmoneyAlways = 26,
	gearbalance = 27,
	turboblowRegen = 28;

	public Rep(int nameid, int nosTimeStandard, int nosBottleAmountStandard, double nosStrengthStandard, double kW,
			double weight, double speedTop, int rpmIdle, int rpmTop, int gearTop, int tireGripTimeStandard,
			double tireGripStrengthStandard, double tireGripArea, double bar) {

		init();

		this.nameid = nameid;

		Arrays.fill(changed, true);

		set(Rep.nosMs, nosTimeStandard);
		set(Rep.nosSize, nosBottleAmountStandard);
		set(Rep.nos, nosStrengthStandard);
		set(Rep.kW, kW);
		set(Rep.kg, weight);
		set(Rep.spdTop, speedTop);
		set(Rep.rpmIdle, rpmIdle);
		set(Rep.rpmTop, rpmTop);
		set(Rep.gearTop, gearTop);
		set(Rep.tbMs, tireGripTimeStandard);
		set(Rep.tb, tireGripStrengthStandard);
		set(Rep.tbArea, tireGripArea);
		set(Rep.turboblow, 100);
		set(Rep.bar, bar);
		set(Rep.gearbalance, 220f / speedTop);
		set(spool, 1f);
	}

	public Rep() {
		init();
	}

	private void init() {
		stats = new double[29];
		changed = new boolean[stats.length];
	}

	

		/*
		 * Upgrade ids
		 */
//		String[] toConvertIds = values[fromFromIndex + fromIndex].split(UPGRADELVL_REGEX);
//		for (int x = 0; x < upgrades.length; x++) {
//			for (int y = 0; y < upgrades[x].length; y++ ) {
//				int possibleId = Integer.parseInt(toConvertIds[x + y]);
//				if ((upgrades[x][y] == null && possibleId != -1) || upgrades[x][y].compareId(possibleId)) {
////					TODO upgrades[x][y] = Upgrades.instantiateTile(possibleId);
//				}
//			}
//		}
//		
//		/*
//		 * Bonus lvl
//		 */
//		String[] toConvertBonusesX = values[fromFromIndex + fromIndex].split(UPGRADELVL_REGEX);
//		for (int x = 0; x < toConvertBonusesX.length; x++) {
//			String[] toConvertBonusesXY = toConvertBonusesX[x].split(";");
//			for (int y = 0; y < upgrades.length; y++) {
//				upgrades[x][y].setBonusLVL(Integer.parseInt(toConvertBonusesXY[y].trim()));
//			}
//		}
//		fromFromIndex++;
//
//		/*
//		 * Upgrade lvls
//		 */
//		String[] toConvertLVLs = values[fromFromIndex + fromIndex].split(UPGRADELVL_REGEX);
//		for (int x = 0; x < upgrades.length; x++) {
//			for (int y = 0; y < upgrades[x].length; y++ ) {
//				upgrades[x][y].setLVL(Integer.parseInt(toConvertLVLs[x + y].trim()));
//			}
//		}
//		fromFromIndex++;
//		/*
//		 * Bank
//		 */
//		bank.setAsString(values[fromFromIndex + fromIndex], UPGRADELVL_REGEX);
//		fromFromIndex++;
		/*
		 * Carstats
		 */

	@Override
    public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		outString.append(nameid);
		
		for (int i = 0; i < stats.length; i++) {
			outString.append(splitter);
			if (changed[i] || all) {

				// Check if stat has a decimal
				if ((stats[i] % 1) != 0)
					outString.append(stats[i]);
				else
					outString.append((int) stats[i]);

				if (!test)
					changed[i] = false;
			} else {
				outString.append("x");
			}
		}
	}

	@Override
	public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
		/*
		 * Names
		 */
		nameid = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);
		for (int i = 0; i < stats.length; i++) {
			var clonedVal = cloneString[fromIndex.getAndIncrement()];
			if (!clonedVal.equals("x")) {
				double newValue = Double.parseDouble(clonedVal);
				stats[i] = newValue;
			}
		}
	}
	
	public Rep getClone() {
		var clone = new Rep();
		String split = "#";
		StringBuilder sb = new StringBuilder();
		getCloneString(sb, 0, split, true, true);
		clone.setCloneString(sb.toString().split(split), new AtomicInteger(0));
		
		return clone;
	}

//	private String getBonusesString() {
//		String r = "";
//
//		for (int[] x : bonuses) {
//			for (int xy : x) {
//				r += xy + ";";
//			}
//			r += UPGRADELVL_REGEX;
//		}
//
//		return r;
//	}
//
//	private String getUpgradeLVLsString() {
//		String r = "";
//
//		for (int lvl : upgradeLVLs) {
//			r += lvl + UPGRADELVL_REGEX;
//		}
//
//		return r;
//	}
//
//	public int[] getUpgradeLVLs() {
//		return upgradeLVLs;
//	}
//
//	public void setUpgradeLVLs(int[] upgradeLVLs) {
//		this.upgradeLVLs = upgradeLVLs;
//	}

//	public String[] generateHudPartsStrings() {
//		String parts = "Main#" + "Wheel";
//		return parts.split("#");
//	}
	
	// TODO use me in add and such below.
	public void setSpeedTop(double speedTop) {
		// first update the gear balance
		double prevSpeedTop = stats[Rep.spdTop];
		set(gearbalance, Math.abs(stats[27] * (1 - ((speedTop - prevSpeedTop) / prevSpeedTop))));
		
		// set the actual top speed
		set(Rep.spdTop, Math.round(speedTop));
	}

	public void guarenteeTireboostArea() {
		set(tbArea, -1);
	}

	public static String getInfoTitles() {
		return "kW;kg;km/h gears;nos n;tb";
	}

	public String getInfo() {
		return getTotalKW() + ";"
				+ getInt(kg) + ";" + getInt(spdTop) + "x" + getInt(gearTop) + ";"
				+ String.format("%.1f", get(nos)) + "x" + getInt(nosSize) + ";"
				+ String.format("%.1f", get(tb));
	}

	public String getInfoDiff(Rep rep) {
		return calcDiff(getTotalKW(), rep.getTotalKW())
			 + calcDiff(rep, kg)
			 + calcDiff(rep, spdTop)
			 + calcDiff(rep, nos)
			 + calcDiff(rep, tb, true);
	}
	
	private String calcDiff(Rep rep, int i) {
		var res = calcDiff(get(i), rep.get(i));
		//Special case
		if (i == kg && res.length() > 2) {
			int last = res.length() - 2;
			byte[] chars = res.getBytes();
			if (chars[last] == 'G')
				chars[last] = 'R';
			else
				chars[last] = 'G';
			
			res = new String(chars);
		}
		return res;
	}

	private String calcDiff(Rep rep, int i, boolean cutLast) {
		var res = calcDiff(get(i), rep.get(i));
		res = res.substring(0, res.length() - 1);
		return res;
	}

	private String calcDiff(double x1, double x2) {
		String res = " ";
		if(x1 != x2) {
			double diff = x1 - x2;

			String format = "%." +
					(RegVals.hasDecimals(diff) ? "1" : "0") +
					"f";
			res =  String.format(format, diff);
			if (diff > 0)
				res = "+" + res + "#G";
			else
				res += "#R";
		}
		return res + ";";
	}
	
	public UILabel[] getCarChoiceInfo() {
		String[] res = {
				getTotalKW() + " kW (" + getInt(kW) + " kW + " + String.format("%.1f",get(bar)) + " bar)",
				getInt(kg) + " kg",
				getInt(spdTop) + "km/h x " + getInt(gearTop) + ", " + getInt(rpmIdle) + "-" + getInt(rpmTop) + " rpm",
				String.format("%.1f", get(nos)) + " nos x " + getInt(nosSize) + " bottles, " + getInt(nosMs) + " ms",
				String.format("%.1f", get(tb)) + " tb, " + getInt(tbMs) + " ms" + (getInt(tbArea) == -1 ? " Guarenteed!" : ""),
				Car.DESCRIPTION[this.nameid] + "#LBEIGE",
		};
		
		return UILabel.create(res);
	}
	
	public void getInfoWin(ArrayList<String> info) {
		info.add(getInt(vmoney) + " v$" + ", " + getInt(vgold) + " vGold");
		info.add(getTotalKW() + " kW / " + getInt(kg) + " kg");		
		info.add(getInt(spdTop) + " km/h x " + getInt(gearTop) + (is(sequential) ? " sequential" : "") + ", record: " + getInt(highestSpdAchived) + " km/h");		
		info.add(String.format("%.1f", get(nos)) + " nos x " + getInt(nosSize) + " x " + getInt(nosMs) + " ms" + (is(nosSoundbarrier) ? ", soundbarrier-NOS" : "") + (is(nosAuto) ? ", auto-NOS" : ""));
		info.add(String.format("%.1f", get(tb)) + " tb, " + (getInt(tbArea) == -1 ? "guarenteed" : String.format("%.1f", get(tbArea)) + " %") + " area, " + getInt(tbMs) + " ms");
		info.add(String.format("%.1f", get(bar)) + " bar (" +  getInt(kW) + " + " + ((int) getTurboKW()) + " kW), " + (String.format("%.2f", get(spool)) + " spool "));
		info.add((is(throttleShift) ? "Has" : "No") + " throttle-shift and " + (is(stickyclutch) ? "got" : "no") + " Sticky-Clutch"); 
	}

	// public void turbospoolPitch(double rpm, int rpmTop) {
	// double value;
	// double maxValue = 2;
	// rpm = maxValue * rpm;
	//
	// if (rpm > rpmTop * maxValue)
	// value = maxValue;
	// else if (rpm < 0)
	// value = 0;
	// else
	// value = rpm / rpmTop;
	//
	// turbospooling += 0.1 * rpm / rpmTop;
	// if (turbospooling > 1) {
	// turbospooling = 1;
	// }
	//
	// value = -0.05 * Math.pow(2, value) + 0.8 * value;
	// turbospool.pitch((double) value);
	// turbospool.volume((double) ((value / maxValue) * turbospooling
	// * audio.getVolume(AudioTypes.MASTER)
	// * audio.getVolume(AudioTypes.SFX)));
	//
	// }
	
	public void set(int i, double val) {
		stats[i] = val;
		changed[i] = true;
	}

	public void setBool(int i, boolean val) {
		stats[i] = val ? 1 : 0;
		changed[i] = true;
	}
	
	public void add(int i, double val) {
		stats[i] += val;
		changed[i] = true;
	}
	
	public void mul(int i, double val) {
		stats[i] *= val;
		changed[i] = true;
	}
	
	public void div(int i, double val) {
		stats[i] /= val;
		changed[i] = true;
	}
	
	public double get(int i) {
		return stats[i];
	}

	public double getTurboKW() {
		return get(kW) / 4 * get(bar);
	}

	public long getTotalKW() {
		return (long) (get(kW) + getTurboKW());
	}
	
	public boolean is(int i) {
		return stats[i] != 0;
	}
	
	public long getInt(int i) {
		return (long) stats[i];
	}

	public String getName() {
		return Car.CAR_TYPES[nameid];
	}
	
	public int getNameID() {
		return nameid;
	}

	public boolean hasTurbo() {
		return is(Rep.bar);
	}

	public boolean hasNOS() {
		return is(Rep.nos);
	}

	public boolean hasTireboost() {
		return is(Rep.tb);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.valueOf(nameid));
		for (double val : stats) {
			sb.append(", ").append(val);
		}
		return sb.toString();
	}
	
}
