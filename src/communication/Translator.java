package communication;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.codedisaster.steamworks.SteamID;

import adt.ICloneStringable;
import elem.upgrades.Store;
import game_modes.GameModes;
import main.Game;
import player_local.Bank;
import player_local.BankType;
import player_local.Player;
import scenes.game.Lobby;

public class Translator {
	private Consumer<Player> afterJoined;
	private Player player;
	private final GameInfo info;
	private final Store store;
	private final Lobby lobby;
	private final SteamCommunicator com;
	public static final String 
		req = "REQ", 
		resAll = "RESA",
		resRequester = "RESR";
	public static final String 
		join = "0", 
		getPlayerAllInfo = "1", 
		finish = "2",
		inTheRace = "3", 
		raceInformation = "4", 
		leave = "5",
		raceLightsAndOpponents = "6", 
		raceCountdown = "7",
		trackLength = "8", 
		setBankStats = "9", 
		getWinner = "a", 
		addChat = "b", 
		price = "c", 
		updateCarRep = "d",
		ready = "e", 
		isGameOver = "f", 
		isGameOverPossible = "g",
		isGameStartedAlready = "h", 
		changeGoldValue = "i", 
		goldCosts = "j",
		changeGamemode = "k", 
		getGamemodeAllInfo = "l",
		undo = "m";
	public static final String splitterStd = "#";

	public Translator(GameInfo info, Store store, Lobby lobby, SteamCommunicator com) {
		this.info = info;
		this.lobby = lobby;
		this.store = store;
		this.com = com;
	}

	public boolean isRequest(String request) {
		return separatorChecker(request, req);
	}

	public boolean isResponse(String response) {
		return separatorChecker(response, resAll)
				|| separatorChecker(response, resRequester)
				|| separatorChecker(response, "MAIL");
	}

	private boolean separatorChecker(String message, String compared) {
		String temp = message.substring(0, compared.length());
		return temp.equals(compared);
	}

	/*
	 * REQUEST
	 */

	/**
	 * take the first word and run the rest to its responsible function. Like
	 * SQL. If res (response) is null then a return call won't be made to the requester. 
	 *
	 * Old example but ye:
	 * JOIN#name+id#host-boolean#carname LEAVE#name+id CLOSE
	 * UPDATELOBBY#name+id#ready UPDATERACE#name+id#mysitsh
	 * 
	 */
	public String understandRequest(String request, SteamID requesterID) {
		String[] input = request.split("#");
		// System.out.println(request);

		String res = null;
		boolean all = true;
		boolean failed = false;
		int index = 0;

		if (input.length > 0) {
			String identifier = input[index].substring(req.length());
			index++;
			if (identifier.equals(join) && player.isHost()) {
				res = join(input, index, requesterID);
				all = false;
			} else {

				Player player = info.getPlayer(input, index);
				index += 2;

				if (player != null) {

					info.ping(player);

					switch (identifier) {
						case finish :
							info.finishRace(player, Long.valueOf(input[index]));
							break;
						case inTheRace :
							info.setInTheRace(player,
									Boolean.valueOf(input[index]));
							break;
						case leave :
							com.leave(player, true);
							break;
						case trackLength :
							res = trackLength + "#"
									+ String.valueOf(info.getTrackLength());
							break;
						case raceInformation :
							info.raceInformation(player,
									Float.valueOf(input[index]),
									Integer.valueOf(input[++index]),
									Long.valueOf(input[++index]));
							break;
						case setBankStats :
							setBankStats(player, input);
							break;
						case getWinner :
							res = getWinner + "#" + info.getWinner(player);
							break;
						case price :
							res = price + info.getPrices(player);
							break;
						case updateCarRep :
							info.updateCarCloneToServer(player, input, 1);
							break;
						case undo :
							player.undoHistory();
							break;
						case ready :
							info.ready(player, Byte.valueOf(input[index]));
							break;
						case isGameStartedAlready :
							res = isGameStartedAlready + "#"
									+ String.valueOf(info.isGameStarted());
							break;
						case changeGoldValue :
							info.upgradeGold(player, Integer.valueOf(input[index]),
									Integer.valueOf(input[++index]),
									Boolean.valueOf(input[++index]));
							break;
						case goldCosts :
							res = goldCosts + "#" + info.getGoldCosts(this.player);
							break;
						case getGamemodeAllInfo :
							res = getGamemodeAllInfo + "#"
									+ info.getGamemode().getAllInfo();
							all = false;
							break;
						default :
							System.out.println("false start of request");
							failed = true;
							break;

					}
				} else {
					System.out.println("no such player");
					failed = true;
				}
			}
		} else {
			System.out.println("wrong request overall");
			failed = true;
		}

		if (failed && this.player != null && this.player.isHost()) {
			res = Response.END_ALL_CLIENT_STRING;
		} else if (res != null) {
			if (all)
				res = Translator.resAll + res;
			else
				res = Translator.resRequester + res;
		}

		return res;
	}

	private String join(String[] input, int fromIndex, SteamID requesterID) {
		boolean paused = info.countdownPaused;
		
		Player player = info.join(
				new Player(input[fromIndex], (byte) -200,
						Byte.valueOf(input[fromIndex + 1]))
								.setSteamID(requesterID),
				input[fromIndex + 2],
				(this.player == null || this.player.isHost()
						? GameInfo.JOIN_TYPE_VIA_CREATOR
						: GameInfo.JOIN_TYPE_VIA_CLIENT),
				null, null);
		
		boolean stillPaused = info.countdownPaused;
		
		if (paused && !stillPaused)
			com.updateRaceCountdown(this.player, true);

		if (player != null) {
			StringBuilder sb = new StringBuilder(join + splitterStd);
			player.getCloneString(sb, 0, splitterStd, false, true);
			sb.append(splitterStd).append(info.getRaceCountdown())
			.append(splitterStd).append(stillPaused ? 1 : 0)
			.append(splitterStd).append(info.getRaceLightsString())
			.append(splitterStd).append(info.getGamemode().getJoinerInfo());
//			.append(splitterStd).append(com.isGameStarted());
			
			return sb.toString();
			// add important info about the game
		} else {
			return Response.END_ALL_CLIENT_STRING;
		}
	}

	private void setBankStats(Player player, String[] input) {
		Bank bank = new Bank();
		bank.set(Integer.valueOf(input[2]), BankType.POINT);
		bank.set(Integer.valueOf(input[3]), BankType.MONEY);
		bank.set(Integer.valueOf(input[4]), BankType.GOLD);
		info.setBankStats(player, bank);
	}

	/*
	 * RESPONSE
	 */

	/**
	 * take the first word and run the rest to its responsible function. Like
	 * SQL.
	 * 
	 * JOIN#name+id#host-boolean#carname LEAVE#name+id CLOSE
	 * UPDATELOBBY#name+id#ready UPDATERACE#name+id#mysitsh
	 * 
	 * @param text
	 *            input from client
	 * @return answer based upon request
	 */
	public void understandResponse(String response, SteamID requester) {
		String[] input = response.split("#");

		// System.out.println(request);

		int index = 0;
		if (response != null && input.length > 1) {
			String identifier = input[index].substring(resAll.length());
			index++;

			switch (identifier) {
				case join :
					// TRANSLATE
					player = info.join(player, Game.VERSION,
							GameInfo.JOIN_TYPE_VIA_CLIENT, null, null);
					if (player != null) {
						AtomicInteger atomIndex = new AtomicInteger(index);
						player.setCloneString(input, atomIndex);
						info.setRaceCountdown(Long.parseLong(input[atomIndex.getAndIncrement()]));
						info.countdownPaused = Integer.parseInt(input[atomIndex.getAndIncrement()]) != 0;
						
						String raceLights = input[atomIndex.get()];
						if (raceLights.length() > 0) {
							long[] timesResult = new long[4];
							for (int i = 0; i < timesResult.length; i++) {
								timesResult[i] = Long.parseLong(input[atomIndex.getAndIncrement()]);
							}
							info.setRaceLights(timesResult);
						} else {
							atomIndex.getAndIncrement();
						}
						
						var gm = info.init(GameModes.valueOf(input[atomIndex.getAndIncrement()]), player, Integer.parseInt(input[atomIndex.getAndIncrement()]));
						gm.setAllInfo(input, atomIndex.get());
						lobby.setCurrentLength(gm.getRaceGoal());
						info.setGameID(player.getGameID());
						info.updateIndex(player);
					}

					com.setPlayer(player);

					if (afterJoined != null)
						afterJoined.accept(player);
					afterJoined = null;
					break;
				case getPlayerAllInfo :
					Player playerToUpdate = info.getPlayer(input, index);
					if (playerToUpdate == null) {
						playerToUpdate = new Player("", (byte) -200,
						(byte) Player.UNKNOWN);
						playerToUpdate.setCloneString(input, new AtomicInteger(index));
						info.join(playerToUpdate, Game.VERSION,
								GameInfo.JOIN_TYPE_VIA_CLIENT, null, null);
					} else {
						playerToUpdate.setCloneString(input, new AtomicInteger(index));
						info.updateIndex(playerToUpdate);
					}

					info.updateLobbyString();
					break;
				case raceCountdown : // send med tidsreferanse.
					long countdown = Long.valueOf(input[index]);
					index++;
					long timeReferance = Long.valueOf(input[index]);
					countdown += System.currentTimeMillis() - timeReferance;
					System.out.println("time diff " + (System.currentTimeMillis() - timeReferance));
					info.setRaceCountdown(countdown);
					break;
				case raceLightsAndOpponents :
					long[] timesResult = new long[4];
					for (int i = 0; i < timesResult.length; i++) {
						timesResult[i] = Long.valueOf(input[i + index]);
					}
					info.setRaceLights(timesResult);
					index += timesResult.length;
					info.setOpponents(input, index);
					break;
				case trackLength :
					int len = Integer.valueOf(input[index]);
					info.setTrackLength(len);
					lobby.setCurrentLength(len);
					break;
				case getGamemodeAllInfo :
					info.getGamemode().setAllInfo(input, index);
					break;
				case changeGamemode :
					info.init(GameModes.valueOf(input[index]), player, Integer.parseInt(input[++index]));
					index++;
					for(Player player : info.getPlayers()) {
						player.getCar().completeReset();
					}
					player.upgrades.resetTowardsCar(player, com.getGamemode());
				case price :
					int[] prices = new int[input.length - index];
					for (int i = 0; i < prices.length; i++) {
						prices[i] = Integer.valueOf(input[i + index]);
					}
					store.setPrices(player, prices);
					break;
				// case "OVER":
				// res = String.valueOf(info.isGameOver(player));
				// break;
				// case "OVERMAYBE":
				// res = String.valueOf(info.isGameOverPossible(player));
				// break;
				// case "STARTED":
				// res = String.valueOf(info.isGameStarted(player));
				// break;
				case goldCosts :
					store.setGoldCosts(player, input[index]);
					break;
				default :
					String newText = input[index];
					index++;
					for (int i = index; i < input.length; i++)
						newText += "#" + input[i];
					info.addChat(player, newText);
					break;

			}
		}

		// return res;
	}

	public void setAfterJoined(Player joiner,
			Consumer<Player> afterJoined) {
		this.player = joiner;
		this.afterJoined = afterJoined;
	}
	
	public static String getCloneString(ICloneStringable clone, boolean test, boolean all) {
		StringBuilder sb = new StringBuilder();
		clone.getCloneString(sb, 0, Translator.splitterStd, test, all);
		return sb.toString();
	}
	
	public static void setCloneString(ICloneStringable clone, String cloneString) {
		clone.setCloneString(cloneString.split(Translator.splitterStd), new AtomicInteger());
	}

}
