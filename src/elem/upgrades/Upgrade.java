package elem.upgrades;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import adt.IAction;
import adt.ICloneStringable;
import communication.Communicator;
import communication.Translator;
import elem.interactions.Tile;
import elem.interactions.TileUpgrade;
import elem.ui.IUIObject;
import elem.ui.UIButton;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import engine.math.Vec2;
import engine.math.Vec3;
import game_modes.GameMode;
import main.Texts;
import player_local.Bank;
import player_local.BankType;
import player_local.Player;
import player_local.Car.Rep;
import scenes.Scenes;

public class Upgrade implements ICloneStringable {

	private UpgradeAction regularAction;
	private UpgradeAction[] bonuses;
	private RegVals regularValues, neighbourModifier;
	private UpgradePrice price;
	private byte nameID, maxLVL;
	private byte[] normalGain;
	private byte[] bonusCost;
	private byte[] bonusCostBuffer;
	private int bonusLVL;
	private byte[] bonusLVLs;
	private byte[] bonusesTaken;
	private int lvl, fromLVL = -1;
	private final double priceFactor = 0.75f;
	
	public Upgrade() {}

	public Upgrade(byte nameID, byte maxLVL, byte[] bonusLVLs2, RegVals upgrade, RegVals neighbourModifier) {
		this.nameID = nameID;
		this.maxLVL = maxLVL;
		this.regularValues = upgrade;
		this.bonusLVLs = bonusLVLs2;
		normalGain = new byte[bonusLVLs2.length];
		bonusCost = new byte[bonusLVLs2.length];
		bonusCostBuffer = new byte[bonusLVLs2.length];
		bonusesTaken = new byte[bonusLVLs2.length];
		bonusLVL = 0;
		this.neighbourModifier = neighbourModifier;
	}
	
	public Upgrade clone() {
		
		var bonusLVLs = new byte[this.bonusLVLs.length];
		System.arraycopy(this.bonusLVLs, 0, bonusLVLs, 0, bonusLVLs.length);
		var regularValues = this.regularValues.clone();
		
		Upgrade res = new Upgrade(nameID, maxLVL, bonusLVLs, regularValues, neighbourModifier);
		res.setPrice(price.clone());
		res.setUpgrade(regularAction);
		res.setBonuses(bonuses);
		res.setLVL(lvl);
		res.setBonusLVL(bonusLVL);
		for (int i = 0; i < bonusCost.length; i++) {
			res.setGoldCost(bonusCost[i], i);
			res.setBonusChoice(i, bonusesTaken[i]);
			res.setNormalGain(normalGain[i], i);
		}
		
		return res;
	}

	@Override
    public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		outString.append(nameID);
		outString.append(splitter + lvl);
		outString.append(splitter + maxLVL);
		outString.append(splitter);
		if (bonusLVLs.length > 0) {
			for (int i = 0; i < bonusLVLs.length; i++) {
				if (i != 0)
					outString.append(":");
				outString.append(bonusLVLs[i]).append(":");
				outString.append(bonusCost[i]).append(":");
				outString.append(bonusesTaken[i]).append(":");
				outString.append(normalGain[i]);
			}
		} else {
			outString.append("x");
		}
		regularValues.getCloneString(outString, lvlDeep, splitter, test, all);
		if (price != null)
			price.getCloneString(outString, lvlDeep, splitter, test, all);
		else
			outString.append(splitter + "x");
		neighbourModifier.getCloneString(outString, lvlDeep, splitter, test, all);
		outString.append(splitter + bonusLVL);
	}

	@Override
	public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
		nameID = Byte.parseByte(cloneString[fromIndex.getAndIncrement()]);
		lvl = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);
		maxLVL = Byte.parseByte(cloneString[fromIndex.getAndIncrement()]);
		String[] bonusLVLsStr = cloneString[fromIndex.getAndIncrement()].split(":");
		int len = bonusLVLsStr.length / 4;
		bonusLVLs = new byte[len];
		bonusCost = new byte[len];
		bonusesTaken = new byte[len];
		normalGain = new byte[len];
		for (int i = 0; i < len; i++) {
			bonusLVLs[i] = Byte.parseByte(bonusLVLsStr[(i*4)]);
			bonusCost[i] = Byte.parseByte(bonusLVLsStr[(i*4) + 1]);
			bonusesTaken[i] = Byte.parseByte(bonusLVLsStr[(i*4) + 2]);
			normalGain[i] = Byte.parseByte(bonusLVLsStr[(i*4) + 3]);
		}
		if (regularValues == null)
			regularValues = new RegVals();
		regularValues.setCloneString(cloneString, fromIndex);
		if (!cloneString[fromIndex.get()].equals("x")) {
			if (price == null)
				price = new UpgradePrice();
			price.setCloneString(cloneString, fromIndex);
		} else {
			fromIndex.incrementAndGet();
		}
		if (neighbourModifier == null)
			neighbourModifier = new RegVals();
		neighbourModifier.setCloneString(cloneString, fromIndex);
		bonusLVL = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);
	}
	
	public void reset(double[] regUpgrades, GameMode gm) {
		regularValues.setValues(regUpgrades);
		bonusLVL = 0;
		normalGain = new byte[bonusLVLs.length];
		bonusCost = new byte[bonusLVLs.length];
		for (int i = 0; i < bonusCost.length; i++) {
			bonusCost[i] = gm.getGoldCostStandard();
			normalGain[i] = gm.getNormalGainStandard();
		}
		bonusCostBuffer = new byte[bonusLVLs.length];
		bonusesTaken = new byte[bonusLVLs.length];
		lvl = 0;
	}

	public boolean upgrade(Player player, Vec2 pos, boolean test) {
		if (lvl < maxLVL || maxLVL == -1) {
			
			if (!test) {
				lvl++;
			}
			
			var clonedRegVal = regularValues;
			if (pos != null)
				clonedRegVal = modRegValCloned(clonedRegVal, player.getLayer().getNeighbours(pos), player.getLayer().getTimesMod(pos));
			var rep = player.getCarRep();
			regularAction.upgrade(clonedRegVal, player, rep, true, test);
			return true;
		}
		return false;
	}
	
	private RegVals modRegValCloned(RegVals regularValues, ArrayList<Tile> neighbours, int timesMod) {
		var clonedRegVal = regularValues.clone();
		if (timesMod > 1)
			clonedRegVal.multiplyAllValues(timesMod);

		if (neighbours != null) {
			for (var neighbour : neighbours)
				if (neighbour.getClass().equals(TileUpgrade.class)) 
					((TileUpgrade) neighbour).modifyNeighbour(clonedRegVal);
		}

		return clonedRegVal;
	}

	public int getCostMoney(Bank bank) {
		return (int) (price.getMoney(priceFactor, bank, nameID) * (lvl + 1f));
	}

	public int canAfford(Bank bank) {
		int amount = getCostMoney(bank);
		return bank.canAfford(amount, BankType.MONEY) ? amount : -1;
	}

	public UpgradeAction[] getBonuses() {
		return bonuses;
	}

	public void setBonuses(UpgradeAction[] bonuses) {
		this.bonuses = bonuses;
	}

	public void setUpgrade(UpgradeAction upgrade) {
		regularAction = upgrade;
	}

	public byte getNameID() {
		return nameID;
	}

	public void setNameID(byte nameID) {
		this.nameID = nameID;
	}

	public RegVals getRegVals() {
		return regularValues;
	}

	public void setRegularValues(RegVals regularValues) {
		this.regularValues = regularValues;
	}

	public ArrayList<IUIObject> getInfo(Layer layer, Vec2 pos, boolean hovered, boolean placed) {
		ArrayList<IUIObject> res = new ArrayList<>();
		StringBuilder text = new StringBuilder((hovered ? "HOVERED" : "SELECTED") + ": \"" + Upgrades.GetRealUpgradeName(nameID, fromLVL == -1 ? lvl : fromLVL) + "\"\n"
				+ getRegularValuesString(layer, pos) + "\n");
				
		String neighbours = neighbourModifier.getUpgradeRepString();
		if (neighbours.length() > 0) {
			text.append("Neighbours: " + neighbours + "\n");
		}
		
		if (placed) {
			text.append("LVL: ").append(lvl).append(" / ").append(maxLVL == -1 ? "Infinite" : maxLVL).append("\n");
			Collections.addAll(res, UILabel.split(text.toString(), "\n"));
		} else {
			if (maxLVL != 1)
				text.append("ORIGINAL PRICE: $").append(price.getOriginalPrice() * priceFactor).append("\n");
			text.append("\n").append(Upgrades.info[nameID]).append("\n\n");
			
			for (int i = 0; i < bonuses.length; i++) {
				String color = switch (i) {
				case 0: yield "#WEAKGOLD";
				case 1: yield "#G";
				case 2: yield "#TUR";
				case 3: yield "#BUR";
				default:
					throw new IllegalArgumentException("Unexpected value: " + i);
				};
				text.append("    BONUS LVL ").append(bonusLVLs[i]).append(color).append("\n")
				.append(Texts.goldBonus + " (").append(bonusCost[i]).append(") :").append(bonusesTaken[i] == 2 ? "#G" : (bonusesTaken[i] == 1 ? "#R" : "#LBEIGE")).append("\n")
				.append(" ").append(Upgrades.bonusesTextsGold[nameID][i]).append("\n").append(Texts.normalBonus + ":").append(bonusesTaken[i] == 2 ? "#R" : (bonusesTaken[i] == 1 ? "#G" : "#LBEIGE"))
				.append("\n").append(" ").append(Upgrades.bonusesTexts[nameID][i]).append("\n\n");
			}
			Collections.addAll(res, UILabel.split(text.toString(), "\n"));
		}
 
		return res;
	}
	
	private String getRegularValuesString(Layer layer, Vec2 pos) {
		String res = null;
		if (layer != null && pos != null)
			res = modRegValCloned(regularValues, layer.getNeighbours(pos), layer.getTimesMod(pos)).getUpgradeRepString();
		else
			res = regularValues.getUpgradeRepString();
		if (res.length() == 0)
			res = "--------";
		
		return res + (lvl < maxLVL || maxLVL == -1 ? "#WON" : "#GRAY");
	}

	public int getBonusLVL() {
		return bonusLVL;
	}

	public boolean hasBonusReady(boolean checkBonusAfterUpgrade) {
		return bonusLVL < bonusLVLs.length && bonusLVLs[bonusLVL] <= lvl
				+ (checkBonusAfterUpgrade ? 0 : 1);
	}

	public void upgradeBonus(boolean gain) {
		if (gain)
			bonusLVL++;
		else
			bonusLVL--;
	}

	public void revertBonusLVL() {
		if(bonusLVLs.length > 0 && bonusLVL > 0 && lvl < bonusLVLs[bonusLVL - 1]) {
			bonusLVL--;
			bonusesTaken[bonusLVL] = 0;			
		}
	}

	public byte[] getBonusLVLs() {
		return bonusLVLs;
	}

	public int getGoldCost(int bonusLVL) {
		return bonusCost[bonusLVL];
	}

	public void setGoldCost(int bonusCost, int bonusLVL) {
		if (bonusLVL < this.bonusCost.length)
			this.bonusCost[bonusLVL] = (byte) bonusCost;
	}

	public int getNormalGain(int bonusLVL) {
		return normalGain[bonusLVL];
	}
	
	public void setNormalGain(int gain, int bonusLVL) {
		if (bonusLVL < this.normalGain.length)
			this.normalGain[bonusLVL] = (byte) gain;
	}
	
	public void setGoldCostBuffers() {
		for(int i = 0; i < bonusCost.length; i++) {
			bonusCost[i] += bonusCostBuffer[i];
			bonusCostBuffer[i] = 0;
		}
	}

	public void addGoldCostBuffer(int amount, int bonusLVL) {
		// dont let cost go under 0
		if(amount <= -1 && bonusCost[bonusLVL] + bonusCostBuffer[bonusLVL] <= 0)
			return;
		
		bonusCostBuffer[bonusLVL] += amount;
	}

	public void setBonusLVL(int bonusLVL) {
		this.bonusLVL = bonusLVL;
	}

	public boolean compareId(int possibleId) {
		return nameID == possibleId;
	}

	public int getLVL() {
		return lvl;
	}
	
	public void setLVL(int lvl) {
		this.lvl = lvl;
	}

	public int getMaxLVL() {
		return maxLVL;
	}
	
	public boolean isFullyUpgraded() {
		return maxLVL == lvl;
	}

	public void setPrice(UpgradePrice price) {
		this.price = price;
	}

	public UpgradePrice getPrice() {
		return price;
	}

	public void setBonusChoice(int bonusLVL, int val) {
		if (bonusLVL < bonusesTaken.length)
			bonusesTaken[bonusLVL] = (byte) val;
	}

	public void place(boolean completelyPlaced) {
		fromLVL = lvl + (completelyPlaced ? -1 : 0);
		lvl = 0;
		maxLVL = 5;
		bonuses = new UpgradeAction[0];
		bonusLVLs = new byte[0];
		price.setMoneyFactored(priceFactor, 50);
	}
	
	public boolean hasSale(Bank bank) {
		return price.getSale(bank, nameID) != 1f;
	}

	public RegVals getNeighbourModifier() {
		return neighbourModifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Upgrade other = (Upgrade) obj;
		if (!Arrays.equals(bonusCost, other.bonusCost))
			return false;
		if (!Arrays.equals(bonusCostBuffer, other.bonusCostBuffer))
			return false;
		if (bonusLVL != other.bonusLVL)
			return false;
		if (!Arrays.equals(bonusLVLs, other.bonusLVLs))
			return false;
		if (!Arrays.equals(bonuses, other.bonuses))
			return false;
		if (!Arrays.equals(bonusesTaken, other.bonusesTaken))
			return false;
		if (lvl != other.lvl)
			return false;
		if (maxLVL != other.maxLVL)
			return false;
		if (nameID != other.nameID)
			return false;
		if (neighbourModifier == null) {
			if (other.neighbourModifier != null)
				return false;
		} else if (!neighbourModifier.equals(other.neighbourModifier))
			return false;
		if (!Arrays.equals(normalGain, other.normalGain))
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		if (Double.doubleToLongBits(priceFactor) != Double
				.doubleToLongBits(other.priceFactor))
			return false;
		if (regularAction == null) {
			if (other.regularAction != null)
				return false;
		} else if (!regularAction.equals(other.regularAction))
			return false;
		if (regularValues == null) {
			if (other.regularValues != null)
				return false;
		} else if (!regularValues.equals(other.regularValues))
			return false;
		return true;
	}
	
}
