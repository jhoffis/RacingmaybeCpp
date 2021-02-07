package player_local;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import adt.ICloneStringable;
import elem.upgrades.Upgrades;
import main.Game;

public class Bank implements ICloneStringable {


	public final ArrayList<ArrayList<Double>> sale = new ArrayList<>();
	private final String saleSplit = ";";

	public void resetSales() {
		sale.clear();
		for (int i = 0; i < Upgrades.UPGRADE_NAMES.length; i++) {
			sale.add(new ArrayList<>());
		}
	}
	
	/**
	 * @param byId is who is GIVING a sale to THIS price.
	 * -10% off is 0.9, +10% is 1.1
	 */
	public void addSale(double value, int toId, int byId) {
		while (sale.get(toId).size() <= byId)
			sale.get(toId).add(1.0);
		sale.get(toId).set(byId, sale.get(toId).get(byId) * value);
	}
	
	/**
	 * -10% off is 0.9, +10% is 1.1
	 */
	public void replaceSale(double value, int toId, int byId) {
		while (sale.get(toId).size() <= byId)
			sale.get(toId).add(1.0);
		sale.get(toId).set(byId, value);
	}
	
	private float points;
	private float money;
	private float gold;

	private int moneyAchived;
	private int pointsAchived;
	private int goldAchived;

	private int pointsAdded;
	private int moneyAdded;
	private int goldAdded;

	public Bank() {
		reset();
	}

	public void reset() {
		gold = 2.332302f;
		goldAchived = (int) gold;
		money = Game.DEBUG ? 1111111 : 0.142451f;
		points = 0.22221f;
		resetSales();
	}

	@Override
	public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		outString.append(getMoney() + splitter + getPoints() + splitter + getGold());
		for (int i = 0; i < sale.size(); i++) {
			outString.append(splitter);
			if (sale.get(i).size() > 0) {
				for (var sale : sale.get(i)) {
					outString.append(sale + saleSplit);
				}
			} else {
				outString.append("x");
			}
		}
	}

	@Override
	public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
		money = Float.valueOf(cloneString[fromIndex.getAndIncrement()]) + 0.123239f;
		points = Float.valueOf(cloneString[fromIndex.getAndIncrement()]) + 0.14539f;
		gold = Float.valueOf(cloneString[fromIndex.getAndIncrement()]) + 0.223239f;
		
		int len = sale.size();
		sale.clear();
		for (int i = 0; i < len; i++) {
			String str = cloneString[fromIndex.getAndIncrement()];
			ArrayList<Double> list = new ArrayList<>();
			if (!str.equals("x")) {
				for (String saleStr : str.split(saleSplit))
					list.add(Double.parseDouble(saleStr));
			}
			sale.add(list);
		}
	}
	
	public boolean buy(int amount, BankType type) {
		boolean res = false;

		switch (type) {
			case MONEY :
				if (amount <= money) {
					money -= amount;
					res = true;
				}

				break;
			case GOLD :
				if (amount <= gold) {
					gold -= amount;
					res = true;
				}
				break;
			case POINT :
				if (amount <= points) {
					points -= amount;
					res = true;
				}
				break;
		}

		return res;
	}

	public void add(int amount, BankType type) {
		switch (type) {
			case MONEY :
				money += amount;
				moneyAchived += amount;
				moneyAdded = amount;

				break;
			case GOLD :
				gold += amount;
				goldAchived += amount;
				goldAdded = amount;
				break;
			case POINT :
				points += amount;
				pointsAchived += amount;
				pointsAdded = amount;
				break;
		}
	}

	public boolean canAfford(int cost, BankType type) {

		switch (type) {
			case MONEY :
				return cost <= money;
			case GOLD :
				return cost <= gold;
			case POINT :
				return cost <= points;
		}
		return false;
	}

	public void set(int amount, BankType type) {
		switch (type) {
			case MONEY :
				if (amount - this.money > 0)
					moneyAchived += amount - this.money;
				this.money = amount;
				break;
			case GOLD :
				if (amount - this.gold > 0)
					goldAchived += amount - this.gold;
				this.gold = amount;
				break;
			case POINT :
				if (amount - this.points > 0)
					pointsAchived += amount - this.points;
				this.points = amount;
				break;
		}
	}

	public int getPoints() {
		return (int) points;
	}
	
	public int getMoney() {
		return (int) money;
	}

	public int getGold() {
		return (int) gold;
	}

	public int getMoneyAchived() {
		return moneyAchived;
	}

	public void setMoneyAchived(int moneyAchived) {
		this.moneyAchived = moneyAchived;
	}

	public int getPointsAchived() {
		return pointsAchived;
	}

	public void setPointsAchived(int pointsAchived) {
		this.pointsAchived = pointsAchived;
	}

	public int getGoldAchived() {
		return goldAchived;
	}

	public void setGoldAchived(int goldAchived) {
		this.goldAchived = goldAchived;
	}

	public int getPointsAdded() {
		return pointsAdded;
	}

	public int getMoneyAdded() {
		return moneyAdded;
	}

	public int getGoldAdded() {
		return goldAdded;
	}

}
