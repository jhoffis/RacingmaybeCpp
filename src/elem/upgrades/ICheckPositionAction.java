package elem.upgrades;

import elem.interactions.Tile;
import engine.math.Vec2;

public interface ICheckPositionAction {
	boolean check(Tile tile, Vec2 pos);
}
