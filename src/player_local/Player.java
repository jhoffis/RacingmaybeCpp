package player_local;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.codedisaster.steamworks.SteamID;

import adt.ICloneStringable;
import communication.Translator;
import elem.interactions.TileUpgrade;
import elem.ui.UILabel;
import elem.upgrades.Layer;
import elem.upgrades.Upgrades;
import engine.math.Vec2;
import main.Features;
import player_local.Car.Car;
import player_local.Car.Rep;

public class Player implements ICloneStringable {
	
	// temp vals
	private byte ready;
	private byte opponent;
	private long timeLapsedInRace;
	private byte finished;
	private int podium;
	private int aheadByPoints;
	private boolean inTheRace;

	// 0 == player, 1 == spectator, 2 == hostPlayer, 3 == hostSpectator
	public static byte UNKNOWN = -1, PLAYER = 0, HOST = 2, COMMENTATOR = 3;
	private String name;
	private SteamID steamID;
	private byte role, id; // also used as channel
	private int gameID;

	private Layer layer;
	private Bank bank;
	private Car car;
	private final String format1 = "%.1f";
	
	public Upgrades upgrades;
	private final ArrayList<String> history = new ArrayList<>();
	private int historyIndex;
	private boolean canUndoHistory;

	public Player(String name, byte id, byte role, Car car) {
		this.name = name;
		this.id = id;
		this.role = role;
		this.car = car;
		this.opponent = -1;
		this.bank = new Bank();
		layer = new Layer();
		upgrades = new Upgrades();
	}


	public Player(String name, byte id, byte role) {
		this(name, id, role, new Car());
		car.switchTo(0);
	}

	public Player() {
		this("", (byte) 0, (byte) 0);
	}
//	public String getShortLobbyInfo() {
//		if (car != null)
//			return role + "#" + name + "#" + car.getRep().getName() + "#" + ready;
//		else
//			return "Joining...";
//	}


	/**
	 * @return name#ready#host#points
	 */
	public String getLobbyInfo() {
		if (car != null)
			return podiumConversion() + " - " + name + "#" + car.getRep().getName() + "#" + ready + "#" + bank.getPoints() + "#" + getCarRep().getInt(Rep.vmoney) + "#" + id;		
		else
			return "Joining...";
	}
	

	private String podiumConversion() {
		int podiumActual = (podium + 1);
		String res = String.valueOf(podiumActual);
		switch (podiumActual) {
			case 1 -> res += "st";
			case 2 -> res += "nd";
			case 3 -> res += "rd";
			default -> res += "th";
		}
		return res;
	}

	public UILabel[] getPlayerInfo() {
		Rep rep = getCarRep();
		return UILabel.split(
				"    " + podiumConversion() + " - " + name + "\n" +
				"" + bank.getPoints() + " points\n" +
				"$" + bank.getMoney() + " + v$" + rep.getInt(Rep.vmoney) +  "\n" +
				bank.getGold() + "g + " + rep.getInt(Rep.vgold) + "vg\n\n" +
				"    " + rep.getName() + "\n" +
				rep.getInt(Rep.kW) + " kW\n" +
				rep.getInt(Rep.kg) + " kg\n" +
				rep.getInt(Rep.spdTop) + " km/h x" + rep.getInt(Rep.gearTop) + "\n" +
				String.format("%.1f", rep.get(Rep.nos)) + " nos x" + rep.getInt(Rep.nosSize) + "\n" + 
				String.format("%.1f", rep.get(Rep.tb)) + " tb, " + rep.getInt(Rep.tbMs) + "ms"
				, "\n");
	}

	public String getCarInfo() {
		String res = "";
		if (car != null)
			res = car.getRep().getInfo();
		return res;
	}
	
	public String getCarInfoDiff(Player player) {
		String res = "";
		if (car != null) {
			res = car.getRep().getInfoDiff(player.getCarRep());
		}
		return res;
	}

	@Override
	public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		outString.append(id + splitter + 
				gameID + splitter + 
				(steamID != null ? SteamID.getNativeHandle(steamID) : "x") + splitter + 
				name + splitter + 
				podium + splitter + 
				aheadByPoints);
		
		bank.getCloneString(outString, lvlDeep, splitter, test, all);
		layer.getCloneString(outString, lvlDeep, splitter, test, all);
		getCarRep().getCloneString(outString, lvlDeep, splitter, test, all);		
		upgrades.getCloneString(outString, lvlDeep, splitter, test, all);
	}
	
	@Override
	public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
		id = Byte.parseByte(cloneString[fromIndex.getAndIncrement()]);
		gameID = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);
		String steamIDStr = cloneString[fromIndex.getAndIncrement()];
		if (!steamIDStr.equals("x"))
			steamID = SteamID.createFromNativeHandle(Long.parseLong(steamIDStr));
		name = cloneString[fromIndex.getAndIncrement()];
		podium = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);
		aheadByPoints = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]);

		bank.setCloneString(cloneString, fromIndex);
		layer.setCloneString(cloneString, fromIndex);
		layer.initTiles(this);
		getCarRep().setCloneString(cloneString, fromIndex);
		upgrades.setCloneString(cloneString, fromIndex);
	}
	
	public void setClone(Upgrades upgrades, Bank bank, Layer layer, Rep rep) {
		this.upgrades = upgrades;
		this.bank = bank;
		setLayer(layer);
		this.car.setRep(rep);
	}
	
	public Player getClone() {
		Player clone = new Player(name, id, role);
		clone.setClone(upgrades, bank, layer, getCarRep().getClone());
//		FIXME Alt blir ikke klonet!!! clone.setClone(bank.clone(), layer.clone(), getCarRep().getClone());
		return clone;
	}

	/**
	 * @return name#ready#car#...
	 */
	public String getRaceInfo(boolean allFinished, boolean full) {
		String carName;
		if (car != null)
			carName = car.getRep().getName();
		else
			carName = "NO_NAME";

		int point = full ? bank.getPoints() : bank.getPointsAdded();

		if (!allFinished)
			return name + "#" + finished + "#" + timeLapsedInRace + "#" + car.getDistanceOnline() + "#x#" + carName;
		else
			return name + "#" + finished + "#" + timeLapsedInRace + "#" + (full ? "" : (point < 0 ? "- " : "+ ")) + Math.abs(point) + " point" + (Math.abs(point) != 1 ? "s" : "")
					+ (full ? ", $" + getMoney() : ",") + " +$" + bank.getMoneyAdded() + "#" + (full ? getGold() + " gold " : "")
					+ (bank.getGoldAdded() > 0 ? bank.getGoldAdded() + " gold" : "") + "#"
					+ carName;
	}

	public void newRace() {
		finished = 0;
		timeLapsedInRace = 0;
		inTheRace = false;
	}

	/**
	 * fra og med input[3] input[3] finished input[4] timecurrently
	 */
	public void updateRaceResults(byte finished, long time) {
		setFinished(finished);
		timeLapsedInRace = time;
	}

	public int getFinished() {
		return finished;
	}

	public void setFinished(int i) {
		this.finished = (byte) i;
	}

	public int getPoints() {
		return bank.getPoints();
	}

	public void setPoints(int points) {
		bank.set(points, BankType.POINT);
	}

	public int getMoney() {
		return bank.getMoney();
	}

	public void setMoney(int money) {
		bank.set(money, BankType.MONEY);
	}

	public void setGold(int gold) {
		bank.set(gold, BankType.GOLD);
	}

	public int getGold() {
		return bank.getGold();
	}

	public String getName() {
		return name;
	}

	public String getNameID() {
		return name + id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCarName() {
		return car.getRep().getName();
	}

	public long getTime() {
		return timeLapsedInRace;
	}

	public void setTime(long time) {
		this.timeLapsedInRace = time;
	}

	public byte getID() {
		return id;
	}

	public void setID(byte id) {
		this.id = id;
	}

	public void setIn(boolean in) {
		inTheRace = in;
	}

	public boolean isIn() {
		return inTheRace;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
	}

	public byte getReady() {
		return ready;
	}

	public void setReady(byte ready) {
		this.ready = ready;
	}

	public void setReady(int i) {
		this.ready = (byte) i;
	}

	public void setPodium(int podium) {
		this.podium = podium;
	}

	public byte getRole() {
		return role;
	}

	public void setRole(byte role) {
		this.role = role;
	}

	public void setFinished(byte finished) {
		this.finished = finished;
	}

	public boolean isHost() {
		return role >= HOST;
	}

	public String getInfo(double comparedCost, Player comparedStats, int x, int y) {
		String res = "";
		Rep rep = car.getRep();
		boolean check = comparedStats != null;
		switch (x) {
		case 0:
			switch (y) {
				case 0 -> res = getName() + ", " + getPoints() + "p, #" + (podium + 1) + "0";
				case 1 -> res = "Money: $" + getMoney() + " +" + rep.getInt(Rep.vmoney) + "v$" + (check ? extraPlus(false, comparedCost, getMoney()) : "0");
				case 2 -> res = "Gold: " + getGold() + "g +" + rep.getInt(Rep.vgold) + " vgold" + "0";
			}

			break;
		case 1:
			switch (y) {
				case 0 -> {
					String turboBar = extra(true, rep.get(Rep.bar), comparedStats == null ? 0 : comparedStats.getCarRep().get(Rep.bar));
					res = (int) rep.getTurboKW() + " turbo kW (" + String.format(format1, rep.get(Rep.bar)) + " bar"
							+ turboBar + ", " + (int) rep.get(Rep.turboblow) + "% +" + (int) rep.get(Rep.turboblowRegen) + ")0";
				}
				case 1 -> res = rep.getInt(Rep.nosMs) + " nos ms" + (rep.is(Rep.nosAuto) ? ", auto" : "") + "0";
				case 2 -> res = rep.getInt(Rep.tbMs) + " tb ms" + (rep.getInt(Rep.tbArea) == -1 ? " Guarenteed" : "") + "0";
			}

			break;
		}

		return res;
	}

	private String extraPlus(boolean that, boolean other) {
		return extra(that, other) + hasInfoCompareDifference(that, other);
	}

	private String extraPlus(boolean sprickle, double that, double other) {
		return extra(sprickle, that, other) + hasInfoCompareDifference(that, other);
	}

	private String extra(boolean that, boolean other) {
		return (hasInfoCompareDifference(that, other) != 0 ? " (" + other + ")" : "");
	}

	private String extra(boolean sprickle, double that, double other) {

		double combi = other - that;
		String res = "";
		if (sprickle) {

			if (combi >= 0) {
				res += "+";
			}

			if ((combi == Math.floor(combi)) && !Double.isInfinite(combi)) {
				res += (int) combi;
			} else {
				res += String.format(format1, combi);
			}
		} else {
			res += String.format(format1, combi);

		}

		return (hasInfoCompareDifference(that, other) != 0 ? " (" + res + ")" : "");
	}

	private int hasInfoCompareDifference(String that, String other) {
		return (that.equals(other) ? 0 : 2);
	}

	private int hasInfoCompareDifference(double that, double other) {
		if (that < other)
			return 2;
		else if (that > other)
			return 1;
		return 0;
	}

	private int hasInfoCompareDifference(boolean that, boolean other) {
		if (that != other)
			return other ? 1 : 2;
		return 0;
	}

	public int getGameID() {
		return gameID;
	}

	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

	/**
	 * used for determining if you are for instance leading with 3 points and will thereby win.
	 */
	public int getAheadBy() {
		return aheadByPoints;
	}

	public void setAheadBy(int aheadBy) {
		this.aheadByPoints = aheadBy;
	}

	public SteamID getSteamID() {
		return steamID;
	}

	public Player setSteamID(SteamID steamID) {
		this.steamID = steamID;
		return this;
	}

	public byte getOpponent() {
		return opponent;
	}

	public void setOpponent(byte opponent) {
		this.opponent = opponent;
	}
	
	public String[] getInfoWin() {
		ArrayList<String> info = new ArrayList<>();
		info.add("Achived: $" + bank.getMoneyAchived() + ", " + bank.getPointsAchived() + " points, " + bank.getGoldAchived() + " gold");
		getCarRep().getInfoWin(info);
//		String upgrades = "Upgrades: ";
//		for(int u : upgradeLVLs) {
//			upgrades += u + ", ";
//		}
//		upgrades = upgrades.substring(0, upgrades.length() - 2);
//		res[index] = upgrades;
		
		return info.toArray(new String[0]);
	}

	public Rep getCarRep() {
		return car.getRep();
	}

	public void addTile(TileUpgrade t, Vec2 pos) {
		layer.set(t, pos);
	}

	/**
	 * @return vec2 if new modifier tile must replace this one.
	 */
	public void removeTile(Vec2 pos) {
		layer.remove(pos);
	}

	public void createModifierTiles() {
		int modifierTilePool = 10;
		int min = 2;
		do {
			int modifier;
			if (modifierTilePool > 3) {
				int percent = Features.ran.nextInt(100);
				if (percent < 60) {
					modifier = 2;
				} else if (percent < 92) {
 					modifier = 3;
				} else if (percent < 97) {
					modifier = 4; 
				} else if (percent < 99) {
					modifier = 5;
				} else {
					modifier = 6;
				}
			} else {
				modifier = modifierTilePool;
			}
			modifierTilePool -= modifier;
			
			int x, y; 
			do {
				x = Features.ran.nextInt(Layer.w);
				y = Features.ran.nextInt(Layer.h);
			} while (layer.hasTimesMod(x, y));

			layer.setTimesMod(modifier, x, y);
		} while (modifierTilePool >= min);

	}

	public void setLayer(Layer layer) {
		this.layer = layer;
	}

	public Layer getLayer() {
		return layer;
	}

	public int getCarNameID() {
		var rep = getCarRep();
		if (rep != null)
			return rep.getNameID();
		return -1;
	}
	
	public void addHistory(String cloneString) {
		history.add(cloneString);
		historyIndex = history.size() - 1;
		canUndoHistory = true;
		
		System.out.println("HISTORY: " + cloneString);
	}

	public void addHistory(String[] input, int fromIndex) {
		StringBuilder cloneString = new StringBuilder(input[fromIndex]);
		for (int i = fromIndex + 1; i < input.length - 1; i++) {
			cloneString.append(Translator.splitterStd + input[i]);
		}
		int replaceLast = Integer.parseInt(input[input.length - 1]); 
		if (replaceLast != 0 && history.size() > 0) {
			history.remove(history.size() - 1);
		}
		String str = cloneString.toString();
		addHistory(str);
		Translator.setCloneString(this, str);
	}
	
	public boolean historyForward() {
		if (isHistoryNow())
			return false;
		Translator.setCloneString(this, history.get(++historyIndex));
		return true;
	}

	public boolean historyBack() {
		if (historyIndex <= 0)
			return false;
		Translator.setCloneString(this, history.get(--historyIndex));
		return true;
	}

	public boolean historyBackHome() {
		if (historyIndex == 0)
			return false;
		historyIndex = 0;
		Translator.setCloneString(this, history.get(historyIndex));
		return true;
	}
	
	public boolean setHistoryNow() {
		if (isHistoryNow()) return false;
		
		historyIndex = history.size() - 1;
		Translator.setCloneString(this, history.get(historyIndex));
		return true;
	}

	public boolean isHistoryNow() {
		return historyIndex >= history.size() - 1;
	}
	
	public void resetHistory() {
		historyIndex = 0;
		history.clear();
		canUndoHistory = false;
	}

	public void redoLastHistory() {
		if (history.size() > 0)
			history.remove(history.size() - 1);
		addHistory(Translator.getCloneString(this, false, true));
	}
	
	public void undoHistory() {
		if (history.size() <= 0) return;
		history.remove(history.size() - 1);
		historyIndex = history.size() - 1;
		Translator.setCloneString(this, history.get(historyIndex));
		canUndoHistory = false;
	}

	public boolean canUndoHistory() {
		return canUndoHistory;
	}


	public int getHistoryIndex() {
		return historyIndex;
	}


	public String peekHistory() {
		int size = history.size();
		if (size > 0)
			return history.get(size - 1);
		return null;
	}

}
