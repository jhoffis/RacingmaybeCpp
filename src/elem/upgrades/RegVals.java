package elem.upgrades;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import adt.ICloneStringable;
import main.Texts;
import player_local.Car.Rep;

/*
 * hele tall �ker med hele tall direkte.
 * 20.x �ker med som hele tall, bare fra null. S� liksom 20.2 == 0.2.
 * 10.x �ker med som hele tall, bare at den vises som prosent.
 * < 10.x �ker som prosent.
 */

public class RegVals implements ICloneStringable {

	public static final double specialPercent = 100.0000001, decimals = 200.0000001;
	public double[] values; // Se i Rep.java for hva hver index tilsier.

	public RegVals() {
	}

	public RegVals(double[] values) {
		setValues(values);
	}
	

	public RegVals clone() {
		var values = new double[this.values.length];
		setValuesInto(values, this.values);
		return new RegVals(values);
	}

	@Override
    public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		outString.append(values.length);
		for (var val : values)
			outString.append(splitter + val);
		
	}

	@Override
	public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
		this.values = new double[Integer.parseInt(cloneString[fromIndex.getAndIncrement()])];
		for (int i = 0; i < values.length; i++)
			values[i] = Double.parseDouble(cloneString[fromIndex.getAndIncrement()]);
	}
	
	public void setValues(double[] values) {
		this.values = new double[Texts.tags.length];
		setValuesInto(this.values, values);
	}

	private void setValuesInto(double[] valuesOut, double[] values) {
		System.arraycopy(values, 0, valuesOut, 0, values.length);
	}
	
	public String getUpgradeRepString() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (values[i] != 0) {
				double rmVal = toRemove(values[i]);
				double value = values[i];

				if (res.length() > 0)
					res.append(", ");
				
				boolean isPercent = rmVal == 1 || rmVal == specialPercent;
				if (rmVal == specialPercent)
					rmVal++;

				if (value < rmVal)
					res.append("-");
				else
					res.append("+");

				if (isPercent) {
					res.append(Math.round(Math.abs((value - rmVal) * 100.0)));
					res.append("%");
				} else {
					value = Math.abs(value) - rmVal;
					if (hasDecimals(value)) {
						value = roundDecimals(value);
						res.append(value);
					} else {
						res.append((int) value);
					}
				}

				res.append(" ").append(Texts.tags[i]);
			}
		}

		return res.toString();
	}

	public static boolean hasDecimals(double d) {
		return (d % 1f) != 0f;
	}

	public boolean isPercent(double d) {
		return d < decimals - (specialPercent / 2) && hasDecimals(d);
	}

	public boolean isDecimal(double d) {
		return d > decimals - (specialPercent / 2) && hasDecimals(d);
	}

	public boolean isValuePercent(int i) {
		return isPercent(values[i]);
	}

	public boolean isValueNormal(int i) {
		return !hasDecimals(values[i]);
	}

	public boolean isValueDecimal(int i) {
		return isDecimal(values[i]);
	}	
	
	private double roundDecimals(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	public void multiplyAllValues(double d) {
		for (int i = 1; i < values.length; i++) {
			if (values[i] == 0) continue;
			double value = values[i];
			double rmVal = toRemove(value);
			
			if (isPercent(value)) {
				rmVal = (rmVal != 1 ? rmVal : 0) + 1;
				value = ((value - rmVal) * d) + rmVal;
			} else if (hasDecimals(value))
				value = roundDecimals((value - rmVal) * d) + rmVal;
			else
				value = Math.round(value * d);
			
			values[i] = value;
		}
		
	}

	public void upgrade(Rep rep) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] == 0) continue;
			double value = values[i];
			double rmVal = toRemove(value);
			if (isPercent(value)) {
				rep.set(i, rep.get(i) * roundDecimals(value - (rmVal != 1 ? rmVal : 0)));
			} else {
				if (isDecimal(value))
					value = roundDecimals(value - rmVal);
				rep.set(i, rep.get(i) + value);
			}
		}
	}
	
//	public void upgrade(Rep rep) {
//		for (int i = 0; i < values.length; i++) {
//			if (values[i] == 0) continue;
//			double value = values[i], valueHere = rep.get(i);
//			double rmThere = toRemove(value);
//			if (isPercent(value)) {
//				rep.set(i, valueHere * roundDecimals(value - (rmThere != 1 ? rmThere : 0)));
//			} else {
//				if (isDecimal(value))
//					value = roundDecimals(value - rmThere);
//				rep.set(i, valueHere + value);
//			}
//			if (rmHere == 0 && rmThere != decimals) {
//				values[i] = (int) values[i];
//			}
//		}
//	}
	
	public void combine(RegVals regularValues) {
		for (int i = 0; i < regularValues.values.length; i++) {
			if (regularValues.values[i] == 0) continue;
			if (values[i] == 0) {
				values[i] = regularValues.values[i];
				continue;
			}
			
			double rmHere = toRemove(values[i]), 
				rmThere = toRemove(regularValues.values[i]);
			int typeCombine = 0;
			if (rmThere == 0) {
					switch ((int) rmHere) {
						case 0:
						case (int) decimals:
							typeCombine = 1;
					}
					
			} else if (rmThere == decimals) {
				switch ((int) rmHere) {
					case 0:
						values[i] += decimals;
					case (int) decimals:
						typeCombine = 1;
				}
			} else if (rmThere == 1 || rmThere == specialPercent) {
				typeCombine = 2; // *
			}
			
			if (typeCombine == 1)
				values[i] = ((values[i] - rmHere) + (regularValues.values[i] - rmThere)) + rmHere;
			else if (typeCombine == 2) {
				double thereVal = regularValues.values[i];
				if (rmThere == specialPercent)
					thereVal -= specialPercent;

				if (rmThere == specialPercent && (rmHere == 1 || rmHere == specialPercent))
					values[i] = ((values[i] - rmHere) + (thereVal - 1)) + rmHere;
				else
					values[i] = ((values[i] - rmHere) * thereVal) + rmHere;
			}
			
			if (rmHere == 0 && rmThere != decimals) {
				values[i] = (int) values[i];
			}
		}
	}
	
	private final double toRemove(double val) {
		if (hasDecimals(val)) {
			if (val > decimals - (specialPercent / 2)) {
				return decimals;
			} else if (val > specialPercent / 2) {
				return specialPercent;
			}
			return 1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegVals other = (RegVals) obj;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}
	
}
