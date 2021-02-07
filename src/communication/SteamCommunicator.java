package communication;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamNetworking;
import com.codedisaster.steamworks.SteamNetworking.P2PSend;
import com.codedisaster.steamworks.SteamNetworking.P2PSessionError;
import com.codedisaster.steamworks.SteamNetworkingCallback;

import elem.upgrades.Store;
import game_modes.GameMode;
import main.Features;
import player_local.Bank;
import player_local.Player;
import player_local.Car.Car;
import scenes.SceneHandler;
import scenes.game.GameRemote;
import scenes.game.Lobby;

/**
 * Responses are handled async and get methods gets from its local version -
 * like a host. The async responses therefore manipulate this local version.
 * 
 * Når du gjør noe send det til alle! F eks om du bytter bil eller bytter
 * gamemode - alle har egt tilgang, men praktisk talt er det bare host som kan
 * endre gamemode.
 * 
 * 
 * 
 */
public class SteamCommunicator
		implements
			Communicator,
			SteamNetworkingCallback,
			Runnable {

	private final Features features;
	private final GameRemote game;
	private final GameInfo info;
	private final Translator translator;
	private final Thread respondThread;
	private final SteamNetworking net;

	private Player player;
	private boolean running;

	public SteamCommunicator(Features features, Store store, GameRemote game, GameInfo info, Lobby lobby) {
		this.features = features;
		this.game = game;
		this.info = info;
		translator = new Translator(info, store, lobby, this);
		net = new SteamNetworking(this);

		respondThread = new Thread(this);
		respondThread.start();
	}

	private void sendSpecific(SteamID steamID, String message) {
		try {
			ByteBuffer buffer = ByteBuffer.allocateDirect(message.length());
			buffer.clear();
			buffer.put(message.getBytes("UTF-8"));
			buffer.flip();

			if (net.sendP2PPacket(steamID, buffer, P2PSend.Reliable, 0)) { // player.getChannel()
				System.out.println("SUCC_" + message);
			} else {
				System.out.println("NOT_" + message);
			}

		} catch (SteamException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void sendAllButSelf(String message) {
		for (Player player : info.getPlayers()) {
			if (!player.equals(this.player)) {
				sendSpecific(player.getSteamID(), message);
			}
		}
	}

	private void sendRequest(String message) {
		sendAllButSelf(Translator.req + message);
	}

	private void sendRequestSpecific(SteamID steamID, String message) {
		sendSpecific(steamID, Translator.req + message);
	}

	private void sendRequestHost(String message) {
		sendRequestSpecific(info.getHost().getSteamID(), message);
	}

	@Override
	public void onP2PSessionConnectFail(SteamID steamIDRemote,
			P2PSessionError sessionError) {
		System.out.println("onP2PSessionConnectFail: " + sessionError + " with "
				+ steamIDRemote.getAccountID());
		Player player = info.getPlayer(steamIDRemote);

		if (player != null) {
			info.leave(player, false);
		}
	}

	@Override
	public void onP2PSessionRequest(SteamID steamIDRemote) {

		// add the player to recieveable peeps
		net.acceptP2PSessionWithUser(steamIDRemote);
		System.out.println("accepted session with "
				+ features.getSteamHandler().getUsername(steamIDRemote));
		System.out.println(
				"accepted session with " + steamIDRemote.getAccountID());
		System.out.println("accepted session with " + steamIDRemote);
		if (player.isHost()) {
			info.addJoiner(steamIDRemote);
		}
	}

	/*
	 * Used to retrive packets from outsiders. Legal outsiders.
	 */
	@Override
	public void run() {
		ByteBuffer bb;
		running = true;
		while (running) {
			// check with all players untill the game is over
			// is there a message?

			int size = net.isP2PPacketAvailable(0);// player.getChannel()
			while (size != 0) {
				// read the message
				bb = ByteBuffer.allocateDirect(size);

				SteamID requesterID = new SteamID();
				try {
					net.readP2PPacket(requesterID, bb, 0); // player.getChannel()
				} catch (SteamException e) {
					e.printStackTrace();
				}

				// convert the message
				String converted = null;
				try {
					byte[] bytes = new byte[bb.limit()];
					for (int i = 0; i < bytes.length; i++) {
						bytes[i] = bb.get(i);
					}
					converted = new String(bytes, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				System.out.println(converted);

				if (converted != null) {
					// use the message locally
					if (translator.isResponse(converted)) {
						// WAS A RESPONSE
						translator.understandResponse(converted, requesterID);
					} else if (translator.isRequest(converted)) {
						// WAS A REQUEST - respond to the player
						String responseToOther = translator
								.understandRequest(converted, requesterID);
						System.out.println(responseToOther);

						if (responseToOther != null) {
							String intro = responseToOther.substring(0,
									Translator.resAll.length());
							if (intro.equals(Translator.resAll))
								sendAllButSelf(responseToOther);
							else
								sendSpecific(requesterID, responseToOther);
						}
					} else {
						// ERROR MESSAGESS
						switch (converted) {
							case Response.NO_SERVER_RESPONSE :
								System.out.println("no answer from server");
								SceneHandler.showMessage(
										"No answer from the server. Is the server running?");
							case Response.CLOSED_SOCKET :
							case Response.END_ALL_CLIENT_STRING :
								System.out.println(
										"END_ALL_CLIENT_STRING response from server");
								running = false;
								game.endAll();
							case Response.NOT_RELEVANT :
								System.out.println(
										"NOT_RELEVANT response from server");
								break;
						}
					}

				} else {
					size = 0;
					System.out.println("ERROR IN  STEAM COMMUNICATOR - BB array has no array");
				}

				// Repeat
				size = net.isP2PPacketAvailable(0); // player.getChannel()
			}
			try {
				if (this.player != null) {
					for (Player player : info.getPlayers()) {
						if (player.getNameID()
								.equals(this.player.getNameID()) == false) {
							Mail mail = info.getMail(player);
							if (mail != null) {
								while (mail.hasMail()) {
									sendSpecific(player.getSteamID(),
											mail.getMail());
								}
							}
						}
					}
				}
			} catch (NullPointerException e) {
				System.out.println("Lost player while using him >:) sadly...");
			}
		}

		game.endAll();
	}

	@Override
	public void finishRace(Player player, long time) {

		// FIXME double for 2nd player - kanskje noe med request bank stats?
		// FIXME oppdater aheadby elns
		// si til alle andre at du er ferdig
		sendRequest(Translator.finish + "#" + player.getID() + "#"
				+ player.getGameID() + "#" + time);
		// fullfør på din egen.
		info.finishRace(player, time);
	}

	@Override
	public void setInTheRace(Player player, boolean in) {
		sendRequest(Translator.inTheRace + "#" + player.getID() + "#"
				+ player.getGameID() + "#" + in);
		info.setInTheRace(player, in);
		player.setIn(in);
	}

	@Override
	public Player join(Player player, String checksum, int typeJoin,
			Consumer<Player> afterJoined, SteamID host) {

		this.player = player;

		if (player.isHost()) {
			translator.setAfterJoined(player, null);
			return info.join(player, checksum, typeJoin, null, null)
					.setSteamID(features.getSteamHandler().getMySteamID());
		} else {
			translator.setAfterJoined(player, afterJoined);
			info.addHost(host);
			sendRequestSpecific(host, Translator.join + "#" + player.getName()
					+ "#" + player.getRole() + "#" + checksum);
		}

		return null;
	}

	@Override
	public void leave(Player player, boolean outsideForce) {
		if(player.equals(this.player)) {
			running = false;
			features.leave();
		}
		if(!outsideForce) {
			sendRequest(Translator.leave + "#" + player.getID() + "#"
					+ player.getGameID());
		}
		info.leave(player, outsideForce);
	}

	@Override
	public void ready(Player player, byte val) {
		sendRequest(Translator.ready + "#" + player.getID() + "#"
				+ player.getGameID() + "#" + val);
		info.ready(player, val);
	}

	@Override
	public String updateLobby(Player player) {
		return info.updateLobby(player);
	}

	@Override
	public String updateRaceLobby(Player player, boolean force) {
		return info.updateRaceLobby(player, force);
	}

	/*
	 * Host will update all clients directly with new racelight times. Check
	 * your info if it has been changed by the host
	 */
	@Override
	public boolean updateRaceLights(Player host) {
		boolean res = info.updateRaceLights(host);

		if (res && host.isHost()) {
			// update my clients about changing
			sendAllButSelf(Translator.resAll + Translator.raceLightsAndOpponents
					+ "#" + info.getRaceLightsString() + "#"
					+ info.createOpponents());
		}

		return res;
	}

	@Override
	public long[] getRaceLights() {
		return info.getRaceLights();
	}

	@Override
	public long getRaceCountdown() {
		return info.countdownPaused ? -1 : info.getRaceCountdown();
	}

	@Override
	public void clearRaceCountdown() {
		info.clearRaceCountdown();
	}

	@Override
	public void updateRaceCountdown(Player host, boolean force) {
		if (host != null && host.isHost() && (force || info.isRaceCountdownNotUpdated())) {
			info.updateRaceCountdown(host, false);
			// update my clients about changing!
			sendAllButSelf(Translator.resAll + Translator.raceCountdown + "#"
					+ info.getRaceCountdown() + "#" + (info.getRaceCountdown() - (GameInfo.countdown_std * 1000))); // send med tidsreferanse.
		}
	}

	@Override
	public void startRace() {
		info.startRace();
	}

	@Override
	public int getTrackLength() {
		int len = -1;

		if (player.isHost()) {
			len = info.getTrackLength();
			// update my clients about changing!
			sendAllButSelf(
					Translator.resAll + Translator.trackLength + "#" + len);
		}

		return len;
	}

	@Override
	public void setBankStats(Player player, Bank bank) {
		sendRequest(Translator.setBankStats + "#" + player.getID() + "#"
				+ player.getGameID() + "#" + bank.getPoints() + "#"
				+ bank.getMoney() + "#" + bank.getGold());
		info.setBankStats(player, bank);
	}

	@Override
	public String getBankStatsInfo(Player player) {
		return info.getBankStatsInfo(player);
	}

	@Override
	public void createNewGame(Player player) {
		info.createNewGame(player);

		// TODO tell everyone after joining
	}

	@Override
	public String getWinner(Player player) {
		return info.getWinner(player);
	}

	@Override
	public void sendChat(Player player, String text) {
		info.sendChat(player, text);
		
		if(player.isHost()) {
			String[] args = text.split(" ");
			if(args.length > 2) {
				// kick the player most above
				if(args[0].toLowerCase().equals("!k")) {
					
					// who downwards from top - players can have the same name you know.
					String who = args[args.length - 1];
					int whoInt = 0;
					if(who.matches("[0-9]+")) {
						whoInt = Integer.valueOf(who);
					} else {
						return;
					}
					
					// gather the name in case there are spaces in the name
					String kickName = "";
					for(int i = 1; i < args.length - 1; i++) {
						kickName += args[i] + " ";
					}
					kickName = kickName.trim();
					
					// Find him
					int occurences = 0;
					for(Player kickPlayer : info.getSortedPlayers()) {
						if(kickPlayer.getName().equals(kickName)) {
							if(occurences == whoInt) {
								//KICK!
								leave(kickPlayer, false);
								return;
							}
							
							// maybe next one..
							occurences++;
						}
					}
				}
			}
		}
	}

	@Override
	public String getChat(Player player) {
		return info.getChat(player);
	}

	@Override
	public void setPrices(Player player, Store store) {
		// FIXME this one has to be synced to the host as well. Maybe some kind
		// of check
		// for -1 and
		// a Action that will run after you actually get the prices.
		if (player.isHost()) {
			info.setPrices(player, store);
		} else {
			getPrices(player);
		}
	}

	@Override
	public String getPrices(Player player) {
		sendRequestHost(Translator.price + "#" + player.getID() + "#"
				+ player.getGameID());
		return null;
	}

	@Override
	public void setGoldCosts(Player player, Store store) {
		info.setGoldCosts(player, store);
	}

	@Override
	public String getGoldCosts(Player player) { // TODO use me for join
		sendRequestHost(Translator.goldCosts + "#" + player.getID() + "#"
				+ player.getGameID());
		return null;
	}

	@Override
	public void upgradeGold(Player player, int id, int bonusLVL, boolean gold) {
		sendRequest(Translator.changeGoldValue + "#" + player.getID() + "#"
				+ player.getGameID() + "#" + id + "#" + bonusLVL + "#" + gold);
		info.upgradeGold(player, id, bonusLVL, gold);
	}

	@Override
	public void updateCloneToServer(Player player, String cloneString, int replaceLast) {
		sendRequest(Translator.updateCarRep + Translator.splitterStd + cloneString + Translator.splitterStd + replaceLast);
		info.updateCloneToServer(player, cloneString, replaceLast);
	}
	
	@Override
	public void undoHistory(Player player) {
		sendRequest(Translator.undo + Translator.splitterStd + player.getID() + Translator.splitterStd + player.getGameID());
	}

	@Override
	public void updateCarCloneToServer(Player player, String[] cloneArray, int fromIndex) {
		System.out.println(
				"DO NOT USE ME updateCarCloneToServer() in SteamCommunicator");
	}

	@Override
	public byte isGameOverPossible() {
		return info.isGameOverPossible();
	}

	@Override
	public byte isGameOver() {
		return info.isGameOver();
	}

	@Override
	public byte isGameStarted() {
		return info.isGameStarted(); // may be broken
	}

	@Override
	public void ping(Player player) {
	}

	@Override
	public void checkPings() {
	}

	@Override
	public boolean isSteam() {
		return true;
	}

	@Override
	public boolean doubleCheckStartedRace() {
		return info.doubleCheckStartedRace();
	}

	@Override
	public void setChosenCarModels() {
		info.setChosenCarModels();
	}

	@Override
	public void setChosenCarAudio() {
		info.setChosenCarAudio();
	}

	@Override
	public void addFinishPlayerAnimationAction(Consumer<Car> finishPlayerAnimationAction) {
		info.addFinishPlayerAnimationAction(finishPlayerAnimationAction);
	}

	@Override
	public void raceInformation(Player player, float distance, int speed, long raceTime) {
		sendRequest(Translator.raceInformation + "#" + player.getID() + "#"
				+ player.getGameID() + "#" + distance + "#" + speed + "#" + raceTime);
	}

	@Override
	public Car getOpponent(Player player) {
		return info.getOpponent(player);
	}

	@Override
	public GameMode getGamemode() {
		return info.getGamemode();
	}

	@Override
	public void getGamemodeAllInfo() {
		if (!player.isHost())
			sendRequestHost(Translator.getGamemodeAllInfo + "#" + player.getID()
					+ "#" + player.getGameID());
	}

	@Override
	public boolean isEveryoneDone() {
		return info.isEveryoneDone();
	}

	@Override
	public Player[] getPlayers() {
		return info.getPlayers();
	}

	@Override
	public boolean isSingleplayer() {
		return false;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public void close() {
		running = false;
		features.leave();		
	}

}
