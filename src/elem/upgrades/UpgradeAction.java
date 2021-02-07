package elem.upgrades;

import player_local.Player;
import player_local.Car.Rep;

public interface UpgradeAction {
	void upgrade(RegVals regularValues, Player player, Rep rep, boolean gold, boolean test);
}
