package elem.upgrades;

import elem.interactions.Tile;
import player_local.Player;

public interface ITileAction {
	void init(Tile tile, Player player);
}
