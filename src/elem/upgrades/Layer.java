package elem.upgrades;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import adt.ICloneStringable;
import elem.interactions.Tile;
import elem.interactions.TileUpgrade;
import engine.math.Vec2;
import player_local.Player;

// Pos is mod from that tile
public class Layer implements ICloneStringable {

    public final static int w = 6, h = 9;
    private final Tile[][] tiles;
    private final int[][] timesMod;
    
    public Layer() {
    	tiles = new Tile[w][h];
    	timesMod = new int[w][h];
    }
    
	public void reset() {
		for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                tiles[x][y] = null;
            }
        }
	}

    public Layer clone() {
        var cloneTiles = new Layer();
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                if (tiles[x][y] != null)
                    cloneTiles.set(tiles[x][y].clone(), new Vec2(x, y));
            }
        }
        return cloneTiles;
    }

    @Override
    public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		outString.append(w).append(splitter).append(h);        
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (tiles[x][y] != null) {
                	outString.append(splitter + tiles[x][y].getTileTypeId());
                	tiles[x][y].getCloneString(outString, lvlDeep, splitter, test, all);
                	outString.append(splitter + timesMod[x][y]);
                }
                else if (timesMod[x][y] > 1)
                	outString.append(splitter + "x" + timesMod[x][y]);
                else
                	outString.append(splitter + "x");
            }
        }
    }

    @Override
    public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
    	int w = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);
    	int h = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);
    	
    	for (int x = 0; x < w; x++) {
    		for (int y = 0; y < h; y++) {
    			tiles[x][y] = null;
    			timesMod[x][y] = 0;
    			if (cloneString[fromIndex.get()].charAt(0) != 'x') {
    				Tile newTile = null;
    				switch (cloneString[fromIndex.getAndIncrement()]) {
    					case Tile.tileUpgradeId:
    						newTile = new TileUpgrade();
    						break;
    				}
    				newTile.setCloneString(cloneString, fromIndex);
    				tiles[x][y] = newTile;
    				timesMod[x][y] = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);
    			} else {
    				if (cloneString[fromIndex.get()].length() > 1) {
    					timesMod[x][y] = Character.getNumericValue(cloneString[fromIndex.get()].charAt(1));
    				}
    				
    				fromIndex.incrementAndGet();
    			}
    		}
    	}
    }
    
    public void initTiles(Player player) {
    	for (int x = 0; x < w; x++) {
    		for (int y = 0; y < h; y++) {
    			if (tiles[x][y] != null && tiles[x][y].getTileTypeId() == Tile.tileUpgradeId) {
    				var tile = (TileUpgrade) tiles[x][y]; 
    				tile.setPlayer(player);
    				tile.init(timesMod[x][y]);
    				tile.place();
    				if (Store.tileInit != null)
    					Store.tileInit.init(tile, player);
    			}
    		}
    	}
    }

    public void set(Tile tile, Vec2 pos) {
        tiles[(int) pos.x][(int) pos.y] = tile;
    }

    public void remove(Vec2 pos) {
        tiles[(int) pos.x][(int) pos.y] = null;
    }

    public Tile get(Vec2 pos) {
        return tiles[(int) pos.x][(int) pos.y];
    }

    public Tile get(int x, int y) {
        return tiles[x][y];
    }

    public ArrayList<Tile> getNeighbours(Vec2 pos) {
        ArrayList<Tile> res = new ArrayList<>();
        int x = (int) pos.x, y = (int) pos.y;
        Tile tile;

        if (x > 0 && (tile = tiles[x - 1][y]) != null)
            res.add(tile);
        if (y > 0 && (tile = tiles[x][y - 1]) != null)
            res.add(tile);
        if (x < w - 1 && (tile = tiles[x + 1][y]) != null)
            res.add(tile);
        if (y < h - 1 && (tile = tiles[x][y + 1]) != null)
            res.add(tile);

        return res;
    }

    public ArrayList<Tile> getLinArr() {
        var res = new ArrayList<Tile>();
        for (var x : tiles)
            for (var xy : x)
                if (xy != null)
                    res.add(xy);
        return res;
    }

    public Tile[][] getDobArr() {
        return tiles;
    }

    public boolean isOpen(Vec2 pos) {
        return get(pos) == null;
    }

    public int getTimesMod(Vec2 pos) {
        return timesMod[(int) pos.x][(int) pos.y];
    }

    public int getTimesMod(int x, int y) {
        return timesMod[x][y];
    }

    public boolean hasTimesMod(int x, int y) {
        return timesMod[x][y] > 1;
    }

    public void setTimesMod(int modifier, int x, int y) {
        timesMod[x][y] = modifier;
    }

    public void remove(Tile tile) {
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                if (tiles[x][y] != null && tiles[x][y].equals(tile))
                    tiles[x][y] = null;
    }

}
