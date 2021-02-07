package communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.codedisaster.steamworks.SteamID;

import audio.AudioRemote;
import audio.SfxTypes;
import elem.upgrades.Store;
import elem.upgrades.Upgrade;
import elem.upgrades.Upgrades;
import game_modes.GameMode;
import game_modes.GameModes;
import game_modes.GolfMode;
import game_modes.LeadoutMode;
import game_modes.SinglePlayerMode;
import main.Features;
import main.Game;
import player_local.Bank;
import player_local.Player;
import player_local.Car.Car;
import scenes.SceneHandler;
import scenes.game.GameRemote;

/**
 * Holds info about who is a part of this game. Also holds info about the cars
 * when racing.
 * 
 * Finish gamemodes
 * 
 * 
 * @author jonah
 *
 */

public class GameInfo implements Communicator {

	public static final int countdown_std = 180;
	public static final int JOIN_TYPE_VIA_CLIENT_NEW_HOST = 0, JOIN_TYPE_VIA_CREATOR = 1, JOIN_TYPE_VIA_CLIENT = 2;
	
	private Store storeHandler; // pointer to the same as in lobby
	private ConcurrentHashMap<Byte, Player> players;
	private HashMap<SteamID, Player> lostPlayers;
	private ConcurrentHashMap<Byte, Long> ping;
	private HashMap<Player, Mail> mail;
	private List<Player> sortedPlayers;
	private long[] raceLights;
	private String raceLobbyString;
	private String lobbyString;
	private GameMode gm;
	private byte nextID;
	private boolean raceLobbyStringFinalized;
	public boolean countdownPaused;
	private long countdown;
	private AudioRemote audio;
	private int gameID;
	private final Features features;
	private int gamemodeScrollingIndex;
	private Consumer<Car> finishPlayerAnimationAction;
	private final GameRemote game;

	public GameInfo(Features features, Store storeHandler, GameRemote game) {
		this.features = features;
		this.storeHandler = storeHandler;
		this.game = game;
		players = new ConcurrentHashMap<Byte, Player>();
		ping = new ConcurrentHashMap<Byte, Long>();
		mail = new HashMap<Player, Mail>();
		lostPlayers = new HashMap<SteamID, Player>();
		gameID = -1;
		clearRaceCountdown();
	}
	
	public void createGameID() {
		gameID = Math.abs(Features.ran.nextInt());
		System.out.println("GameInfo - gameID: " + gameID);
	}

	// TODO when everyone has a gameinfo play audio from here for everyone.
	public void setAudio(AudioRemote audio) {
		this.audio = audio;
	}

	public void endGame() {
		gm.endGame();
	}

	private byte generateID() {
		return nextID++;
	}

	@Override
	public void finishRace(Player player, long time) {
		player.setFinished(1);
		if (finishPlayerAnimationAction != null)
			finishPlayerAnimationAction.accept(player.getCar());
		Player opponent = players.get(player.getOpponent());
		if (opponent != null) {
			opponent.setOpponent((byte) -1);
		}
		player.getCar().reset();

		if (raceLights != null && System.currentTimeMillis() >= raceLights[3]
				&& time != -1) {
			player.setTime(time);
		} else {
			player.setTime(-1);
		}

		if (gm != null)
			gm.anotherPlayerFinished();
		finishControl();
	}

	@Override
	public void setInTheRace(Player player, boolean in) {
		if (in && !player.isIn() && gm != null)
			gm.playerInTheRace(); // increments number of players, not dependent
		player.setIn(in);
		// on any player.
	}

	public void addHost(SteamID steamID) {
		join(new Player("Host", (byte) -200, Player.HOST)
				.setSteamID(steamID), Game.VERSION, GameInfo.JOIN_TYPE_VIA_CLIENT_NEW_HOST, null, null);
	}

	public Player getHost() {
		for (Entry<Byte, Player> entry : players.entrySet()) {
			Player player = entry.getValue();
			if (player.isHost())
				return player;
		}
		return null;
	}

	public boolean addJoiner(SteamID steamID) {
		return join(
				new Player("Joining...", (byte) -200, Player.UNKNOWN)
						.setSteamID(steamID),
						Game.VERSION, GameInfo.JOIN_TYPE_VIA_CREATOR, null, null) != null;
	}
	
	/**
	 * @param afterJoined and host
	 *            not used
	 * 
	 *            TODO add car model and audio if game already started and it
	 *            does not exist.
	 */
	@Override
	public Player join(Player player, String checksum, int typeJoin, 
			Consumer<Player> afterJoined, SteamID host) {
		boolean jump = false;

		// On a different version
		if (!checksum.equals(Game.VERSION)) {
			return null;
		}
		SteamID steamID = player.getSteamID();

		// Have key?
		if (lostPlayers.containsKey(steamID)) {
			player = lostPlayers.remove(steamID);

			players.put(player.getID(), player);
			ping.put(player.getID(), System.currentTimeMillis());
			mail.put(player, new Mail());
			jump = true;
		}

		// Joined, but is still in? Perhaps lost connection and connected before
		// server
		// noticed?
		if (!jump) {
			for (Entry<Byte, Player> entry : players.entrySet()) {
				Player other = entry.getValue();
				// same discID or SteamID?
				if (steamID != null && other.getSteamID() != null
						&& steamID.getAccountID() == other.getSteamID()
								.getAccountID()) {
					other.setName(player.getName());
					other.setRole(player.getRole());
					player = other;

					jump = true;
				}
			}
		}

		if (!jump) {
				// new player
			if(typeJoin != JOIN_TYPE_VIA_CLIENT)
				player.setID(generateID());
			players.put(player.getID(), player);
			ping.put(player.getID(), System.currentTimeMillis());
			mail.put(player, new Mail());
		}

		if (player != null) {
			if (gm == null) {
				init(GameModes.GOLF, player, -1);
			}
			
			if (typeJoin != JOIN_TYPE_VIA_CLIENT && typeJoin != JOIN_TYPE_VIA_CLIENT_NEW_HOST) {
				player.setGameID(this.gameID);
				sendChat(null, player.getName() + " joined the game.");
				audio.get(SfxTypes.JOINED).play();

				// Tell about players
				for (Entry<Byte, Player> entry2 : players.entrySet()) {
					// dont tellhimself
					if (entry2.getKey() == player.getID()) continue;
					
					// Tell of every player to newcomer
					mail.get(player).addMail(Translator.getPlayerAllInfo + Translator.splitterStd + Translator.getCloneString(entry2.getValue(), false, true));

					// Tell everyone about the newcomer
					if (!entry2.getValue().isHost()) {
						mail.get(entry2.getValue()).addMail(Translator.getPlayerAllInfo + Translator.splitterStd + Translator.getCloneString(player, false, true));
					}
				}
			}
			updateSortedPlayers();
			
			countdownPaused = !lostPlayers.isEmpty(); 
		}

		return player;
	}

	@Override
	public void leave(Player player, boolean notUsed) {
		if (player == null || !players.containsKey(player.getID()))
			return;

		boolean startedGame = gm.isGameBegun() || gm.isRacing(); 
		
		for (var p : getPlayers()) {
			p.setReady(0);
		}
		
		mail.remove(player);
		players.remove(player.getID());
		if (sortedPlayers != null)
			sortedPlayers.remove(player);
		ping.remove(player.getID());
		sendChat(null, player.getName() + " left the game.");
		audio.get(SfxTypes.LEFT).play();

		if (player.isHost()) {
			if (!Game.DEBUG) {
				if (!startedGame) {
					game.endAll();
					return;
				}
			}
			
			Player replacement = getPlayer(features.getLobbyOwner());
			if (replacement != null) {
				replacement.setRole(Player.HOST);
				System.out.println("NEW HOST: " + replacement.getName());
			}
		}

		if (startedGame) {
			lostPlayers.put(player.getSteamID(), player);
			countdownPaused = true;
		}

		if (gm.isRacing()) {
			gm.disconnectedFinish();
			finishControl();
			gm.rewardPlayer(-1, -1, 0, player);
		}

		updateLobbyString();
	}

	@Override
	public void ready(Player player, byte val) {
		player.setReady(val);
		updateLobbyString();
	}

	public void updateLobbyString() {
		boolean fullPlayerInformation = true; //gm.isGameBegun();
		String result = "#" + (fullPlayerInformation ? "1" : "0");

//		if (fullPlayerInformation) {

			for (int i = 0; i < sortedPlayers.size(); i++) {
				Player entry = sortedPlayers.get(i);
				if (entry.getRole() != Player.COMMENTATOR || sortedPlayers.size() < 2)
					result += "#" + entry.getLobbyInfo() + "#" + entry.getCarInfo();
			}
//
//		} else {
//
//			for (int i = 0; i < sortedPlayers.size(); i++) {
//				Player entry = sortedPlayers.get(i);
//				result += "#" + entry.getShortLobbyInfo();
//			}
//
//		}

		lobbyString = result;
	}

	@Override
	public String updateLobby(Player player) {

		String result = gm.getEndGoalText();

//		if (lobbyString == null) {
			updateLobbyString();
//		}

		return result + lobbyString;
	}

	@Override
	public String updateRaceLobby(Player player, boolean force) {
		if (!raceLobbyStringFinalized) {
			raceLobbyString = updateRaceLobby(false, force);
		}

		return raceLobbyString;
	}

	/**
	 * @return name#ready#car#...
	 */
	private String updateRaceLobby(boolean allFinished, boolean full) {
		String result = "";

		if (!allFinished) {
			// Hent spillere i hvilken som helst rekkefølge og sett de inn i
			// returnstrengen
			result += 0;
			allFinished = true;
			for (Entry<Byte, Player> entry : players.entrySet()) {
				if (entry.getValue().getRole() != Player.COMMENTATOR) {
					result += "#" + entry.getValue().getRaceInfo(false, full);
					if (entry.getValue().getFinished() == 0) {
						allFinished = false;
					}
				}
			}
			
			if (allFinished) {
				gm.forceAllFinished();
				finishControl();
				result = "";
			} else {
				return result;
			}
		}

		result += 1;

		LinkedList<Player> sortedByTime = new LinkedList<Player>();

		// Sorter alle spillere etter alle har fullført racet
		sortedByTime.addAll(players.values());
		Collections.sort(sortedByTime, new Comparator<Player>() {
			@Override
			public int compare(Player o1, Player o2) {

				int result = 0;
				if (!full) {
					if (o1.getTime() < o2.getTime()) {
						if (o1.getTime() != -1)
							result = -1;
						else
							result = 1;

					} else if (o1.getTime() > o2.getTime()) {
						if (o2.getTime() != -1)
							result = 1;
						else
							result = -1;
					}
				} else {
					if (o1.getBank().getPoints() < o2.getBank()
							.getPoints()) {
						result = 1;

					} else if (o1.getBank().getPoints() > o2.getBank()
							.getPoints()) {
						result = -1;
					}
				}
				return result;
			}
		});

		// Legg de inn i strengen
		int n = 0;
		for (int i = 0; i < sortedByTime.size(); i++) {
			if (sortedByTime.get(i).getRole() == Player.COMMENTATOR)
				continue;

			String str = sortedByTime.get(i).getRaceInfo(true, full);
			n++;
			result += "#" + (n) + ": " + str;
		}
		return result;
	}

	public void setRaceLights(long[] timesResult) {
		this.raceLights = timesResult;
	}

	@Override
	public long[] getRaceLights() {
		return raceLights;
	}

	public String getRaceLightsString() {
		String res = "";

		if (raceLights != null) {
			for (long time : raceLights) {
				res += time + "#";
			}
			res = res.substring(0, res.length() - 1);
		}

		return res;

	}

	/**
	 * Is it safe to gather the times of racelights?
	 */
	@Override
	public boolean updateRaceLights(Player host) {

		if (host != null && host.isHost()) {
			// Everyone in the race
			if (doubleCheckStartedRace()) {
				gm.setStarted(0);
				// Wait for 3 secounds before the race starts && wait for each
				// racelight
				raceLights = gm.waitTimeRaceLights();
			}
		}

		return raceLights != null;
	}

	public void setRaceCountdown(long countdown) {
		this.countdown = countdown;
	}

	@Override
	public long getRaceCountdown() {
		return countdown;
	}

	@Override
	public void updateRaceCountdown(Player host, boolean force) {
		countdown = System.currentTimeMillis() + countdown_std * 1000;
	}

	public boolean isRaceCountdownNotUpdated() {
		return countdown < System.currentTimeMillis();
	}

	@Override
	public void clearRaceCountdown() {
		countdown = -1;
	}

	@Override
	public void startRace() {
		// if (player == null || player.isHost()) {
		gm.startNewRace();
		gm.setStarted(1);
		if (!gm.isRacing())
			gm.setRacing(true);
		raceLobbyStringFinalized = false;
	}

	public void stopRace(Player player) {
		if (player == null || player.isHost()) {
			System.out.println("stop race on the server");
			gm.setStarted(0);
			gm.setRacing(false);
			raceLights = null;
			gm.stopRace();
		}
	}

	public void setTrackLength(int length) {
		gm.prepareNextRaceManually(length);
	}

	@Override
	public int getTrackLength() {
		return gm.getRaceGoal();
	}

	@Override
	public void setBankStats(Player player, Bank bank) {
		player.setPoints(bank.getPoints());
		player.setMoney(bank.getMoney());
		player.setGold(bank.getGold());
		updateLobbyString();
	}

	@Override
	public String getBankStatsInfo(Player player) {
		String res = null;
		try {
			res = player.getPoints() + "#" + player.getMoney() + "#"
					+ player.getBank().getGold();
		} catch (NullPointerException e) {
			System.out.println("Player timed out");
			checkPings();
		}
		return res;
	}

	@Override
	public void createNewGame(Player player) {
		if (player.isHost())
			gm.newEndGoal(gm.getEndGoalStandard());
	}

	@Override
	public String getWinner(Player player) {
		if(gm.isWinner(player)) {
			audio.get(SfxTypes.WON).play();
		} else {
			audio.get(SfxTypes.LOST).play();
		}
		
		String res =  gm.getDeterminedWinnerText(player);
		
		return res;
	}

	public void addChat(Player player, String text) {
		if(player != null && mail.containsKey(player))
			mail.get(player).addChat(text);
	}

	@Override
	public void sendChat(Player player, String text) {
		text = (player != null ? player.getName() + ": " : "") + text;
		
		for (Mail box : mail.values()) {
			box.addChat(text);
		}
	}

	@Override
	public String getChat(Player player) {
		String chatText = null;

		if (player != null) {
			var mailbox = mail.get(player);
			if (mailbox != null)
				chatText = mailbox.getChat();
		}
		return chatText;
	}

	@Override
	public void setPrices(Player player, Store store) {
		store.setPrices(player, gm.getPrices());
	}

	/**
	 * Starts with a "#" !!!
	 */
	@Override
	public String getPrices(Player player) {
		int[] upgradePrices = gm.getPrices();
		String res = "";

		for (int i = 0; i < upgradePrices.length; i++) {
			res += "#" + upgradePrices[i];
		}

		return res;
	}

	@Override
	public void setGoldCosts(Player player, Store store) {
		store.setGoldCosts(player, getGoldCosts(player));
	}

	@Override
	public String getGoldCosts(Player player) {
		String res = "";

		Upgrade upgrade = null;
		for (int i = 0; i < Upgrades.UPGRADE_NAMES.length; i++) {

			upgrade = player.upgrades.getUpgrade(i);

			for (int n = 0; n < Upgrades.UPGRADE_HEIGHTS[i]; n++) {
				res += upgrade.getGoldCost(n);
				res += upgrade.getNormalGain(n);
				res += ";";
			}

			res += ":";
		}

		return res;
	}

	@Override
	public void upgradeGold(Player player, int id, int bonusLVL, boolean gold) {
		player.upgrades.getUpgrade(id).addGoldCostBuffer(gold ? +1 : -1, bonusLVL);
	}

	@Override
	public void updateCloneToServer(Player player, String cloneString, int replaceLast) {
//		TODO Skal denne v�re her i multiplayer? if (!isSingleplayer())
//			Translator.setCloneString(player, cloneString);
		player.addHistory(cloneString);
		updateLobbyString();
	}

	@Override
	public void updateCarCloneToServer(Player player, String[] input, int fromIndex) {
		player.addHistory(input, fromIndex);
		updateLobbyString();
	}

	@Override
	public byte isGameOverPossible() {
		return (byte) (gm.isGameOverPossible() ? 1 : 0);
	}

	@Override
	public byte isGameOver() {
		return (byte) (gm.isGameExplicitlyOver() ? 1 : 0);
	}

	@Override
	public byte isGameStarted() {
		return (byte) (gm.isGameBegun() ? 1 : 0);
	}

	@Override
	public void ping(Player player) {
		ping.put(player.getID(), System.currentTimeMillis());
	}

	@Override
	public void checkPings() {
		for (Entry<Byte, Long> entry : ping.entrySet()) {
			Player player = getPlayer(entry.getKey());

			if (player == null) {
				ping.remove(entry.getKey());
				return;
			}

			// If isnt host and ping is too high
			if (player.getRole() < 2 && !validPing(entry.getValue())) {
				sendChat(null, player.getName() + " has too high ping!");
				leave(player, false); // TODO leaves only locally, must leave globally!!!
			}

		}
	}

	public boolean validPing(long ping) {
		return ping > System.currentTimeMillis() - 10000;
	}

	public long getPing(Player player) {
		return System.currentTimeMillis() - ping.get(player.getID());
	}

	private void updateSortedPlayers() {
		sortedPlayers = new CopyOnWriteArrayList<Player>();
		//
		// // Sorter alle spillere etter alle har fullført racet
		sortedPlayers.addAll(players.values());
		if (sortedPlayers.size() > 0) {
			Collections.sort(sortedPlayers, new Comparator<Player>() {
				@Override
				public int compare(Player o1, Player o2) {

					int result = 0;
					if (o1.getBank().getPoints() < o2.getBank().getPoints()) {
						result = 1;

					} else if (o1.getBank().getPoints() > o2.getBank()
							.getPoints()) {
						result = -1;
					}

					return result;
				}
			});
		}

		updateLobbyString();
	}

	private void finishControl() {
		if (gm != null && gm.getAllFinished()) {
			stopRace(null);
			determinePositioningFinishedRace();
			updateSortedPlayers();

			if (gm.controlGameAfterFinishedPlayer()) {
				raceLobbyString = updateRaceLobby(true, true);
				endGame();
			} else {
				raceLobbyString = updateRaceLobby(true, false);
				gm.noneFinished();
			}
			raceLobbyStringFinalized = true;
		}
	}

	public void determinePositioningFinishedRace() {
		int amountOfPlayers = players.size() + lostPlayers.size();

		Player[] players = getPlayersIncludingLostOnes();

		// Update game information about players:
		for (Player player : players) {
			int place = 0;
			long thisTime = player.getTime();

			if (lostPlayers.containsValue(player) || thisTime == -1) {
				// Disconnected players and dnfers gain some money:
				gm.rewardPlayer(-1, -1, 0, player);
			} else {
				int behindBy = 0;
				for (Entry<Byte, Player> otherEntry : this.players
						.entrySet()) {
					// not same player
					if (otherEntry.getKey() != player.getID()) {
						long otherTime = otherEntry.getValue().getTime();
						if (thisTime > otherTime && otherTime != -1) {
							place++; // place as in place in this race. About
										// how fast you were now, not where you
										// are in regards to the actual game
						}
						int pointsDifference = otherEntry.getValue().getPoints() - player.getPoints();
						if (pointsDifference > behindBy)
							behindBy = pointsDifference;
					}
				}

				gm.rewardPlayer(place, amountOfPlayers, behindBy, player);
				System.out.println(player.getName() + " ahead by " + player.getAheadBy());
			}
			
			if(gm.getGameModeEnum().equals(GameModes.SINGLEPLAYER)) {
				if(((SinglePlayerMode) gm).lostLife()) 
					audio.get(SfxTypes.LOSTLIFE).play();
			}
			
			player.redoLastHistory();
		}

		for (Player player : players) {
			setPodiumAndAheadBy(player);
		}

	}

	private int setPodiumAndAheadBy(Player player) {
		int place = 0;
		int aheadBy = 2000;

		for (Player other : getPlayersIncludingLostOnes()) {
			if (other != player) {
				int difference = player.getPoints() - other.getPoints();
				if (difference < aheadBy) {
					aheadBy = difference;
				}

				if (difference < 0) {
					place++; // place as in where you are overall.
				}
			}
		}
		player.setAheadBy(aheadBy);
		player.setPodium(place); // skriver bare seg selv og kj�res hver tick!
		return place;
	}

	public Player getPlayer(String[] input, int fromIndex) {
		int othersGameID = Integer.valueOf(input[fromIndex + 1]);
		System.out.println("mine: " + this.gameID + ", " + othersGameID);
		if (this.gameID != -1 && othersGameID != this.gameID) {
			System.out.println("wrong gameID");
			return null;
		}
		Player player = getPlayer(Byte.valueOf(input[fromIndex]));
		return player;
	}

	public Player getPlayer(byte id) {
		return players.get(id);
	}

	public Player getPlayer(SteamID steamID) {
		if (steamID != null) {
			for (Entry<Byte, Player> entry : players.entrySet()) {
				Player other = entry.getValue();
				if (other.getSteamID() != null && steamID
						.getAccountID() == other.getSteamID().getAccountID()) {
					return other;
				}
			}
		}
		return null;
	}

	@Override
	public GameMode getGamemode() {
		return gm;
	}

	public GameMode init(GameModes gamemode, Player player, int type) {
		if (gamemode == null)
			return null;

		switch (gamemode) {
			case GOLF :
				gm = new GolfMode();
				break;
			case LEADOUT :
				gm = new LeadoutMode();
				break;

			case SINGLEPLAYER :
				gm = new SinglePlayerMode(player, type);
				break;
			default :
				return null;
		}

		gm.init(players, Features.ran);
		gm.newEndGoal(gm.getEndGoalStandard());
		if (player != null)
			storeHandler.setGoldCosts(player, gm);
		
		return gm;
	}

	/**
	 * scrolls through gamemodes with +1 || -1
	 */
	public void init(int i, int type) {
		int max = 1;
		GameModes gamemode = null;

		gamemodeScrollingIndex += i;
		if (gamemodeScrollingIndex < 0)
			gamemodeScrollingIndex = max;
		else if (gamemodeScrollingIndex > max)
			gamemodeScrollingIndex = 0;

		switch (gamemodeScrollingIndex) {
			case 0 :
				gamemode = GameModes.GOLF;
				break;
			case 1 :
				gamemode = GameModes.LEADOUT;
				break;
			// case 1 :
			// gamemode = GameModes.TIMEATTACK;
			// break;

		}

		init(gamemode, null, type);

		mailEveryoneButHost(Translator.changeGamemode + "#"
				+ gm.getGameModeEnum() + "#" + gm.getType() + getPrices(null));

	}

	@Override
	public boolean isSteam() {
		return false;
	}

	public Player[] getPlayers() {
		return players.values().toArray(new Player[players.values().size()]);
	}

	public Player[] getPlayersIncludingLostOnes() {
		ArrayList<Player> res = new ArrayList<Player>();
		res.addAll(players.values());
		res.addAll(lostPlayers.values());
		return res.toArray(new Player[res.size()]);
	}

	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

	public Mail getMail(Player player) {
		return mail.get(player);
	}

	public void mailEveryoneButHost(String letter) {
		for (Entry<Byte, Player> entry2 : players.entrySet()) {
			// dont tellhimself
			if (!entry2.getValue().isHost()) {
				mail.get(entry2.getValue()).addMail(letter);
			}
		}
	}

	@Override
	public boolean doubleCheckStartedRace() {
		return gm.isRacing() || (gm.everyoneInRace() && gm.getStarted() == 1);
	}

	@Override
	public void setChosenCarModels() {
		for (Player player : getPlayersIncludingLostOnes()) {
			player.getCar().getModel().setModel(player.getCarNameID());
		}
	}

	@Override
	public void setChosenCarAudio() {
		for (Player player : getPlayersIncludingLostOnes()) {
			player.getCar().setAudio(audio.getNewCarAudio(player.getCarName()));
			System.out.println("players: " + player.toString());
		}
	}

	@Override
	public void addFinishPlayerAnimationAction(
			Consumer<Car> finishPlayerAnimationAction) {
		this.finishPlayerAnimationAction = finishPlayerAnimationAction;
	}

	@Override
	public void raceInformation(Player player, float distance, int speed, long raceTime) {
		if(raceLights == null)
			return; 
		
		player.getCar().pushModelPosition(distance, speed, raceTime);
	}

	@Override
	public Car getOpponent(Player player) {
		Player opponent = players.get(player.getOpponent());
		return (opponent != null ? opponent.getCar() : null);
	}

	public void setOpponents(String[] input, int fromIndex) {
		for (int i = fromIndex; i < input.length; i++) {
			String[] fighters = input[i].split("x");
			byte p1ID = Byte.valueOf(fighters[0]);
			if (fighters.length > 1) {
				byte p2ID = Byte.valueOf(fighters[1]);
				if (p1ID >= players.size() || p2ID >= players.size())
					return;
				var p1 = players.get(p1ID);
				p1.setOpponent(p2ID);
				var p2 = players.get(p2ID);
				p2.setOpponent(p1ID);
			} else {
				players.get(p1ID).setOpponent((byte) -1);
			}
		}
	}

	public String createOpponents() {
		String res = "";

		ArrayList<Player> players = new ArrayList<Player>(this.players.values());
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getRole() == Player.COMMENTATOR)
				players.remove(i);
		}
		Random r = new Random();
		int opponent;

		// fjern og velg to id-er
		while (players.size() > 0) {
			res += players.remove(0).getID();
			if (players.size() > 0) {
				opponent = r.nextInt(players.size());
				res += "x" + players.remove(opponent).getID();
			}
			res += "#";
		}
		res = res.substring(0, res.length() - 1);

		setOpponents(res.split("#"), 0);

		return res;
	}

	@Override
	public void getGamemodeAllInfo() {
	}
	
	@Override
	public boolean isEveryoneDone() {
		return raceLobbyStringFinalized;
	}

	public List<Player> getSortedPlayers() {
		return sortedPlayers;
	}

	@Override
	public boolean isSingleplayer() {
		return gm.getGameModeEnum().equals(GameModes.SINGLEPLAYER);
	}

	@Override
	public void undoHistory(Player player) {}

	@Override
	public void close() {
	}

	public void updateIndex(Player playerToUpdate) {
		for (Entry<Byte, Player> entry : players.entrySet()) {
			if (playerToUpdate.equals(entry.getValue())) {
				players.remove(entry.getKey());
				players.put(playerToUpdate.getID(), playerToUpdate);
			}
		}
	}

}
