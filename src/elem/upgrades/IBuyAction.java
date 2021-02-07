package elem.upgrades;

import engine.math.Vec2;
import player_local.Player;

public interface IBuyAction {

	int buy(Player player, Upgrade upgrade, Vec2 pos);
}
