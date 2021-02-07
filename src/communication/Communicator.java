package communication;

import java.util.function.Consumer;

import com.codedisaster.steamworks.SteamID;

import elem.upgrades.Store;
import game_modes.GameMode;
import player_local.Bank;
import player_local.Player;
import player_local.Car.Car;
import scenes.game.GameRemote;

public interface Communicator {

	/**
	 * JOIN#name+id#host-boolean
	 * @param afterJoined 
	 * @param myIp 
	 */
	public Player join(Player player, String checksum, int typeJoin, Consumer<Player> afterJoined, SteamID host);

	public void finishRace(Player player, long time);

	public void setInTheRace(Player player, boolean in);


	/**
	 * LEAVE#name+id
	 */
	public void leave(Player player, boolean outsideForce);

	public void ready(Player player, byte val);

	public String updateLobby(Player player);

	public String updateRaceLobby(Player player, boolean force);

	/**
	 * For host to check whether or not you can create the race lights. Clients check if it is null
	 */
	public boolean updateRaceLights(Player host);

	public long[] getRaceLights();
	
	// if -1 then not ready yet
	public long getRaceCountdown();

	// for the host to create a new countdown.
	public void updateRaceCountdown(Player host, boolean force);

	public void startRace();

	public int getTrackLength();

	public void setBankStats(Player player, Bank bank);

	public String getBankStatsInfo(Player player);

	/**
	 * Must be a host
	 */
	public void createNewGame(Player player);

	public String getWinner(Player player);

	public void sendChat(Player player, String text);

	/**
	 * La serveren kommunisere med clienten ved å si om 
	 * det er en bonus oppdatering, om noen joina eller
	 * stakk og så spille av lyder til det osv. 
	 */
	public String getChat(Player player);

	public void setPrices(Player player, Store store);

	public String getPrices(Player player);

	public void setGoldCosts(Player player, Store store);
	
	public String getGoldCosts(Player player);

	public void upgradeGold(Player player, int id, int bonusLVL, boolean gold);

	public void updateCloneToServer(Player player, String cloneString, int replaceLast);
	
	public void updateCarCloneToServer(Player player, String[] cloneArray, int fromIndex);
	
	/**
	 * @return boolean 1 : 0
	 */
	public byte isGameOverPossible();
	
	/**
	 * @return boolean 1 : 0
	 */
	public byte isGameOver();

	/**
	 * @return boolean 1 : 0 Sjekker gamemode om den er startet
	 */
	public byte isGameStarted();

	public void ping(Player player);
	
	public void checkPings();
	
	public boolean isSteam();

	public void clearRaceCountdown();

	public boolean doubleCheckStartedRace();

	public void setChosenCarModels();

	public void setChosenCarAudio();

	public void addFinishPlayerAnimationAction(Consumer<Car> finishPlayerAnimationAction);

	public void raceInformation(Player player, float distance, int speed, long raceTime);

	public Car getOpponent(Player player);
	
	public GameMode getGamemode();
	
	public boolean isSingleplayer();

	public void getGamemodeAllInfo();

	public boolean isEveryoneDone();

	public Player[] getPlayers();

	public void undoHistory(Player player);

	public void close();

}
