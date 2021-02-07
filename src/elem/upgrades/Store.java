package elem.upgrades;

import java.util.ArrayList;
import java.util.HashMap;

import communication.Communicator;
import communication.Translator;
import elem.interactions.Tile;
import elem.interactions.TileUpgrade;
import elem.objects.Sprite;
import elem.ui.modal.UIBonusModal;
import engine.io.Window;
import engine.math.Vec2;
import game_modes.GameMode;
import main.Game;
import player_local.Bank;
import player_local.BankType;
import player_local.Player;
import scenes.game.lobby_subscenes.UpgradesSubscene;

/**
 * 
 * @author jhoffis
 * 
 *         Returns plain text and whatever that the actual scene just shows. The
 *         FixCar scene should not deal with figuring out text, just printing.
 */

public class Store {
	
	public static ITileAction tileInit;
	private final TileUpgrade[] storeTiles; // TODO Skal disse være i upgrades i stedet 
	                   //eller peke mot upgrades siden de er avhengig av hvilken player som spør
	
	private UpgradesSubscene visualizer;
	private UIBonusModal bonusModal;
	
	public Store() {
		storeTiles = new TileUpgrade[Upgrades.UPGRADE_NAMES.length];
	}
	
	public void resetAllTowardsPlayer(Player player, GameMode gm) {
		player.upgrades.resetTowardsCar(player, gm);
		for(int i = 0; i < storeTiles.length; i++)
			storeTiles[i] = null;
		createNeededTiles(player);
	}
	
	public void resetTowardsPlayer(Player player) {
		for(int i = 0; i < storeTiles.length; i++)
			storeTiles[i] = null;
		createNeededTiles(player);
	}
	
	public synchronized void removeTile(TileUpgrade tile) {
		for(int i = 0; i < storeTiles.length; i++) {
			final var t = storeTiles[i];
			if (t != null && t.equals(tile)) {
				storeTiles[i] = null;
//				Upgrade upgrade = tile.getUpgrade();
//				if (upgrade.getMaxLVL() <= upgrade.getLVL()) {
//					
//				}
				break;
			}
		}
	}
	
	public void createNeededTiles(Player player) {
		float nNulls = 0;
		for (int i = 0; i < storeTiles.length; i++) {
			if (storeTiles[i] == null) {
				if ((storeTiles[i] = createTile(player, i)) == null)
					nNulls++;
			} else if (player.upgrades.getUpgrade(i).isFullyUpgraded()) {
				storeTiles[i] = null;
				nNulls++;
			}
		}
		
		float newTop = 
				(nNulls / (float) storeTiles.length) * FromY() + 
				(TileUpgrade.size() * nNulls / (2f * (float) storeTiles.length / (float) Layer.h));
		int n = 0;
		for (int i = 0; i < storeTiles.length; i++) {
			if (storeTiles[i] != null) {
				storeTiles[i].setPosY(newTop + (TileUpgrade.size() * n));
				n++;
			}
		}
	}
	
	private TileUpgrade createTile(Player player, int i) {
		Upgrade upgrade = player.upgrades.instantiateTile(i);
		if (upgrade == null)
			return null;
		var tile = new TileUpgrade(player, new Vec2(FromX(),0), upgrade,
				(visualizer != null ? UpgradesSubscene.TileSprites[i] : null));
		if (tileInit != null)
			tileInit.init(tile, player);
		return tile;
	}

	public void connectSubscene(UpgradesSubscene visualizer) {
		this.visualizer = visualizer;
	}
	
	public Player getSelectedUpgradeCarRep(Player player, Upgrade upgrade, Vec2 pos) {
		return player.upgrades.upgradeClone(player, upgrade, pos, true);
	}

	public int getSelectedUpgradeCost(Bank bank) {
		return visualizer.CurrentUpgrade.getUpgrade().getCostMoney(bank);
	}

	public int canAffordCurrent(Bank bank) {
		return visualizer.CurrentUpgrade.getUpgrade().canAfford(bank);
	}
	
	private boolean buy(Player player, Upgrade upgrade) {
		var bank = player.getBank();
		return bank.buy(upgrade.getCostMoney(bank), BankType.MONEY);
	}
	
	public boolean isBonusToChooseFirst(Player player, Upgrade checkUpgrade, boolean checkBonusAfterUpgrade) {
		if (bonusModal == null) {
			System.out.println("There is no bonus modal to push upgrades");
			return false;
		}
		
		boolean res = false;
		int upgradeID = 0;

		// -2 betyr sjekk mer, 
		// -1 betyr at det er ingenting mer å sjekke, 
		// ellers oppgraderingsID-en.
		do {
			Upgrade u = null;
			upgradeID = player.upgrades.pollLastFocusedUpgrade();
			
			if (upgradeID == checkUpgrade.getNameID()) {
				u = checkUpgrade;
				var uRep = player.upgrades.getUpgrade(upgradeID);
				if (u.equals(uRep))
					u = uRep;
				else
					continue;
			} else if (upgradeID != -1) {
				u = player.upgrades.getUpgrade(upgradeID);
			}

			if (u != null && u.hasBonusReady(checkBonusAfterUpgrade)) {
				bonusModal.pushUpgrade(u);
				res = true;
			}

		} while (upgradeID >= 0);

		return res;
	}
	
	public int upgrade(Player player, Upgrade checkUpgrade, Vec2 pos, boolean checkBonusAfterUpgrade) {
		// check if there is a bonus to adhere first.
		boolean foundBonus = isBonusToChooseFirst(player, checkUpgrade, checkBonusAfterUpgrade);
		if (foundBonus)
			return 2;
		return buy(player, checkUpgrade) &&
				checkUpgrade.upgrade(
						player,
						pos,
						false) ? 1 : 0;
	}
	
	public final IBuyAction buyTileAction = (player, upgrade, pos) -> {
		int currentLVL = upgrade.getLVL();
		int maxLVL = upgrade.getMaxLVL();
		if (currentLVL >= maxLVL && maxLVL != -1) {
			return 0;
		}

		float amount = upgrade.canAfford(player.getBank());

		setLastFocusedUpgrade(player, upgrade.getNameID());
//		TODO storeHandler.setCurrentCost(amount);

		int failed = 0;
		if (amount != -1) {
			failed = upgrade(player, upgrade, pos, false);
		}
		
		// Code ends here unless successful

//		String[] output = com.getBankStatsInfo(player).split("#");
//		player.setPoints(Integer.parseInt(output[0]));
//		player.setMoney(Integer.parseInt(output[1]));
//		player.setGold(Integer.parseInt(output[2]));

		return failed;
	};
	
	/**
	 * @return 0 om feil, 1 om kjøpt, 2 om den skal kjøpe etter man har valgt de ledige bonusene
	 * TODO kanskje gjør om til enum? Kanskje..
	 */
	public int attemptBuyTile(Player player, TileUpgrade tile, Vec2 pos) {
		var ogUp = tile.getUpgrade();
		var newUpt = ogUp.clone();
		tile.setUpgrade(player, newUpt);

		int boughtOrBonus = buyTileAction.buy(player, newUpt, pos);
		if (boughtOrBonus != 0) {
			// setup new tile
			ogUp.setLVL(newUpt.getLVL());
		} else {
			// failed to buy
			tile.setUpgrade(player, ogUp);
		}
		
		return boughtOrBonus;
	}

	public void setPrices(Player player, int[] prices) {
		for (int i = 0; i < player.upgrades.getUpgrades().length; i++) {
			player.upgrades.getUpgrade(i).setPrice(new UpgradePrice(prices[i]));
		}
	}
	
	public void setGoldCostsToBuffers(Player player) {
		for(Upgrade upgrade : player.upgrades.getUpgrades()) {
			upgrade.setGoldCostBuffers();
		}
	}

	public void setGoldCosts(Player player, String string) {
		Upgrade upgrade = null;
		String[] costs = string.split(":"); // split is for each upgrade
		for (int i = 0; i < Upgrades.UPGRADE_NAMES.length; i++) {
			upgrade = player.upgrades.getUpgrade(i);
			int n = 0;
			String[] goldNormal = costs[i].split(";"); // split is between gold cost and normal gain
			if (goldNormal[0].length() == 0) continue;
			for (String gn : goldNormal) {
				upgrade.setGoldCost(gn.charAt(0) - 48, n);
				upgrade.setNormalGain(gn.charAt(1) - 48, n);
				n++;
			}

		}
	}

	public void setGoldCosts(Player player, GameMode gamemode) {
		Upgrade upgrade = null;
		for (int x = 0; x < Upgrades.UPGRADE_NAMES.length; x++) {
			upgrade = player.upgrades.getUpgrade(x);

			for (int y = 0; y < Upgrades.UPGRADE_HEIGHTS[x]; y++) {
				upgrade.setGoldCost(gamemode.getGoldCostStandard(), y);
				upgrade.setNormalGain(gamemode.getNormalGainStandard(), y);
			}
		}
	}

	public void setLastFocusedUpgrade(Player player, byte upgrade) {
		player.upgrades.setLastFocusedUpgrade(upgrade);
	}

	public ArrayList<TileUpgrade> getStoreTiles() {
		ArrayList<TileUpgrade> res = new ArrayList<>();
		for (var tile : storeTiles) {
			if (tile != null)
				res.add(tile);
		}
		return res;
	}
	
	public ArrayList<Tile> getAllTiles(Player player) {
		ArrayList<Tile> all = new ArrayList<>(getStoreTiles());
		if (player != null) {
			for (var tile : player.getLayer().getLinArr()) {
				if (tile != null)
					all.add(tile);
			}
		}
		return all;
	}

	public static float FromX() {
		return TileUpgrade.size() * (Upgrades.UPGRADE_NAMES.length / 10f) * 4f;
	}
	
	public static float FromY() {
		return TileUpgrade.size() * (Upgrades.UPGRADE_NAMES.length / 10f) / 2f;
	}

	public void setBonusModal(UIBonusModal bonusModal) {
		this.bonusModal = bonusModal;
	}

}
