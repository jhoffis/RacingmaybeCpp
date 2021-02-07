package elem.upgrades;

import java.util.concurrent.atomic.AtomicInteger;

import adt.ICloneStringable;
import player_local.Bank;

public class UpgradePrice implements ICloneStringable {

	private double money, originalPrice;

	public UpgradePrice(double money) {
		this.money = money;
		this.originalPrice = money;
	}
	
	public UpgradePrice() {
	}

	public UpgradePrice clone() {
		return new UpgradePrice(money);
	}

	@Override
	public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		outString.append(money).append(splitter).append(originalPrice);
	}

	@Override
	public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
		money = Double.parseDouble(cloneString[fromIndex.getAndIncrement()]);
		originalPrice = Double.parseDouble(cloneString[fromIndex.getAndIncrement()]);
	}

	public int getMoney(double priceFactor, Bank bank, int upgradeId) {
		double money = this.money * getSale(bank, upgradeId) * priceFactor;
		return (int) money;
	}

	public void setMoneyFactored(double priceFactor, double money) {
		this.money = money / priceFactor;
	}

	public double getSale(Bank bank, int upgradeId) {
		var endSale = 1f;
		
		for (var sale : bank.sale.get(upgradeId))
			endSale *= sale;
		
		return endSale;
	}

	public double getOriginalPrice() {
		return originalPrice;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpgradePrice other = (UpgradePrice) obj;
		if (Double.doubleToLongBits(money) != Double
				.doubleToLongBits(other.money))
			return false;
		if (Double.doubleToLongBits(originalPrice) != Double
				.doubleToLongBits(other.originalPrice))
			return false;
		return true;
	}

}
