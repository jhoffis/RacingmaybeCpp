package elem.upgrades;

public class ChosenBonus {

	private int nameID;
	private int bonusLVL;
	private boolean gold;
	
	public ChosenBonus(int nameID, int bonusLVL, boolean gold) {
		super();
		this.nameID = nameID;
		this.bonusLVL = bonusLVL;
		this.gold = gold;
	}
	
	public int getNameID() {
		return nameID;
	}
	public void setNameID(int nameID) {
		this.nameID = nameID;
	}
	public int getBonusLVL() {
		return bonusLVL;
	}
	public void setBonusLVL(int bonusLVL) {
		this.bonusLVL = bonusLVL;
	}
	public boolean isGold() {
		return gold;
	}
	public void setGold(boolean gold) {
		this.gold = gold;
	}

}
