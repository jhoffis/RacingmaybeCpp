package game_modes;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import adt.IAction;
import communication.Translator;
import elem.ui.UILabel;
import elem.upgrades.Upgrades;
import player_local.Player;

public abstract class GameMode {

	protected ConcurrentHashMap<Byte, Player> players;
	protected ArrayList<Player> winners;

	protected int[] upgradePrices;
	protected Random r;
	protected boolean allFinished;
	protected boolean racing;
	protected int started;
	protected int amountFinished;
	protected int amountInTheRace;
	protected int length;
	protected long raceStartedTime;
	protected final long waitTime = 1000;
	protected long regulatingWaitTime = -1;
	protected boolean endGame;
	protected boolean canSwitchBetweenGamemodes = true;
	protected byte goldCostStandard = 2; // override if necessary
	protected byte normalGainStandard = 1;
	protected int type = -1;
	
	private UILabel[] info;

	public void init(ConcurrentHashMap<Byte, Player> players, Random r) {
		this.r = r;
		this.players = players;
		this.endGame = false;
		createPrices();
	}

	/**
	 * Based on the gamemode rules - where does the asker stand?
	 */
	public String getPodiumPosition(Player asker) {
		int place = 0;
		for (Entry<Byte, Player> otherEntry : players.entrySet()) {

			if (otherEntry.getValue() != asker) {

				int otherPoints = otherEntry.getValue().getPoints();
				if (asker.getPoints() < otherPoints) {
					place++;
				}
			}
		}
		return String.valueOf(place);
	}

	public abstract boolean isGameBegun();

	public abstract boolean isGameOverPossible();

	public abstract boolean isGameOver();

	/**
	 * @return has the server run "endGame()"?
	 */
	public boolean isGameExplicitlyOver() {
		return endGame;
	}

	/**
	 * Resets and closes down most - if not all - values
	 */
	public void endGame() {
		endGame = true;
	}

	/**
	 * Ends a single race and resets values used to determine status of ended race
	 */
	public void stopRace() {
		amountInTheRace = 0;
		amountFinished = 0;
		started = 0;
		racing = false;
	}

	/**
	 * Sets everything up as if race has started
	 */
	public abstract void startNewRace();

	/**
	 * Creates a new racetrack somewhere in the world and with a race type of
	 * choice. For instance regular 1000 m or first to 200 km/h
	 * 
	 * @return type of race
	 */
	public abstract int getRandomRaceType();

	/**
	 * Checks type of race and determines the length. For instance if it's 1000 m or
	 * 2000 m.
	 * 
	 * @return length of current type of race
	 */
	public abstract int getNewRaceGoal();

	/**
	 * Is it first to 20 points or one with most points after 18 races?
	 */
	public abstract void newEndGoal(int gameLength);

	public abstract int getEndGoalStandard();

	/**
	 * @return A text that shows the players what the goal of the game is
	 */
	public abstract String getEndGoalText();

	/**
	 * This is run at the end of the game. It looks at points and such to determine
	 * who won based on the rules of the gamemode. This is only run once to not lose
	 * information about players who leave.
	 */
	public void determineWinner() {
		winners = new ArrayList<Player>();

		for (Entry<Byte, Player> entry : players.entrySet()) {
			Player other = entry.getValue();
			if (winners.size() == 0 || other.getPoints() == winners.get(0).getPoints()) {
				winners.add(other);
			} else if (other.getPoints() > winners.get(0).getPoints()) {
				winners.clear();
				winners.add(other);
			}
		}

	}

	public boolean isWinner(Player player) {
		for(Player winner : winners) {
			if(winner.equals(player))
				return true;
		}
		
		return false;
	}
	
	/**
	 * @param asker - client
	 * @return Winnerstring to show in "WinnerVisual" based on who is asking
	 */
	public String getDeterminedWinnerText(Player asker) {
		String winnerText = null;

		if (asker.getPoints() == winners.get(0).getPoints())
			winnerText = youWinnerText(asker);
		else if (winners.size() == 1)
			winnerText = otherSingleWinnerText(asker);
		else
			winnerText = otherMultiWinnerText(asker);

		return winnerText;
	}

	/**
	 * You are the winner
	 */

	public String youWinnerText(Player asker) {
		String winnerText = "";
		winnerText += "You won";

		// Are you the only winner?
		if (winners.size() > 1) {
			winnerText += " along with: ";
			for (Player player : winners) {
				winnerText += "#" + player.getName() + " who drove a " + player.getCarName();
			}
		} else {
			winnerText += "!!!";
		}
		winnerText += "#You have " + asker.getPoints() + " points!";

		return winnerText;
	}

	/**
	 * One other player won, how are the stats of that player compared to you?
	 */

	public String otherSingleWinnerText(Player asker) {
		String winnerText = "";
		winnerText = winners.get(0).getName() + " won!!!##" + "He drove a " + winners.get(0).getCarName() + "!#"
				+ winners.get(0).getName() + " has " + winners.get(0).getPoints() + " points!#";

		winnerText += "You drove a " + asker.getCarName() + " and you only have " + asker.getPoints() + " points!";
		return winnerText;
	}

	/**
	 * Multiple other players have won. How are their stats compared to yours?
	 */
	public String otherMultiWinnerText(Player asker) {
		String winnerText = "";
		winnerText = "The winners are: ";

		for (Player player : winners) {
			winnerText += "#" + player.getName() + " who drove a " + player.getCarName();
		}

		winnerText += "!#" + "They won with " + winners.get(0).getPoints() + " points!";
		return winnerText;
	}

	/**
	 * Alerts game that a player has finished. Is the game over? Has everyone
	 * finished their single race?
	 */
	public boolean controlGameAfterFinishedPlayer() {

		boolean res = false;

		if (isGameOver()) {
			determineWinner();
			res = true;
		} else if (allFinished) {
			prepareNextRace();
		}

		return res;
	}
	
	public void forceAllFinished() {
		amountFinished = players.size();
		this.allFinished = true;
	}

	public void anotherPlayerFinished() {
		amountFinished++;
		this.allFinished = amountFinished >= players.size();
	}

	public void disconnectedFinish() {
		this.allFinished = amountFinished >= players.size();
	}

	public void prepareNextRace() {
		length = getNewRaceGoal();
	}

	public void prepareNextRaceManually(int length) {
		this.length = length;
	}

	/**
	 * Name to identify which gamemode to host and init
	 */
	public abstract String getName();

	public int getStarted() {
		return started;
	}

	public void setStarted(int started) {
		this.started = started;
	}

	public boolean getAllFinished() {
		return allFinished;
	}

	public boolean everyoneInRace() {
		if (amountInTheRace > players.size())
			System.out.println("AMOUNT IN THE RACE IS HIGHER THAN PLAYERSIZE");

		return amountInTheRace >= players.size();
	}

	/**
	 * Just give the times in between the racelights. Not actual time.
	 * 
	 * 0 = minimum wait time. 0 red -> when done move to these stats when time done
	 * 1 = 1 red -> 2 = 2 red -> 3 = 3 red -> null = all green
	 * 
	 * @return
	 */
	public long[] waitTimeRaceLights() {
		long[] res = new long[4];

//		OLD TYPE
//		res[0] = 2000; 
//		res[1] = res[0] + (waitTime - 300 + r.nextInt(1200));
//		res[2] = res[1] + (waitTime - 300 + r.nextInt(1200));
//		res[3] = res[2] + (waitTime - 300 + r.nextInt(1200));
		
		res[0] = 1000; 
		res[1] = res[0] + (waitTime - 200 + r.nextInt(800));
		res[2] = res[1] + (waitTime - 200 + r.nextInt(800));
		res[3] = res[2] + (waitTime - 200 + r.nextInt(800));

		return res;
	}

	public void resetWaitTimeRaceLights() {
		raceStartedTime = System.currentTimeMillis();
	}

	public void playerInTheRace() {
		amountInTheRace++;
	}

	/**
	 * Rewards money and points based on position in just finished race. If place ==
	 * -1, that means the player DNF-ed
	 * 
	 * @param place.
	 * @param amountOfPlayers
	 * @param player
	 */
	public abstract void rewardPlayer(int place, int amountOfPlayers, int behindBy, Player player);

	public int getRaceGoal() {
		return length;
	}

	public void noneFinished() {
		allFinished = false;
	}

	public boolean isRacing() {
		return racing;
	}

	public void setRacing(boolean racing) {
		this.racing = racing;
	}

	protected void giveNewPrices(int i) {
		if (i < 3)
			upgradePrices[i] = 120;
		else if(i == Upgrades.moneyID)
			upgradePrices[i] = 40;
		else
			upgradePrices[i] = 15 + r.nextInt(70);
	}

	public int[] createPrices() {
		upgradePrices = new int[Upgrades.UPGRADE_NAMES.length];
		for (int i = 0; i < upgradePrices.length; i++) {
			giveNewPrices(i);
		}
		return upgradePrices;
	}

	public int[] getPrices() {
		return upgradePrices;
	}

	public boolean isCanSwitchBetweenGamemodes() {
		return canSwitchBetweenGamemodes;
	}

	public byte getGoldCostStandard() {
		return goldCostStandard;
	}

	public byte getNormalGainStandard() {
		return normalGainStandard;
	}

	public void setAllInfo(String[] input, int index) {
		
		allFinished = Boolean.valueOf(input[index]);
		index++;
		racing = Boolean.valueOf(input[index]);
		index++;
		started = Integer.valueOf(input[index]);
		index++;
		amountFinished = Integer.valueOf(input[index]);
		index++;
		amountInTheRace = Integer.valueOf(input[index]);
		index++;
		length = Integer.valueOf(input[index]);
		index++;
		raceStartedTime = Long.valueOf(input[index]);
		index++;
		regulatingWaitTime = Long.valueOf(input[index]);
		index++;
		endGame = Boolean.valueOf(input[index]);
		index++;
		goldCostStandard = Byte.parseByte(input[index]);
		index++;
		normalGainStandard = Byte.parseByte(input[index]);
		index++;

		setAllInfoDown(input, index);
	}

	public String getAllInfo() {
		String res = allFinished + "#" + racing + "#" + started + "#" + amountFinished + "#" + amountInTheRace + "#"
				+ length + "#" + raceStartedTime  + "#" + regulatingWaitTime + "#" + endGame + "#"
				 + goldCostStandard + "#" + normalGainStandard + "#";
		res += getAllInfoDown();
		return res;
	}

	protected abstract void setAllInfoDown(String[] input, int index);

	protected abstract String getAllInfoDown();

	public void setGameModeInnerChangeMailAction(IAction action) {
		// TODO make me setGameModeInnerChangeMailAction
	}
	
	protected void setGameModeInformation(String[] info) {
		this.info = UILabel.create(info);
	}
	
	public UILabel[] getGameModeInformation() {
		return info;
	}

	public abstract GameModes getGameModeEnum();

	public abstract String getExtraGamemodeRaceInfo();
	
	public String getJoinerInfo() {
		return getGameModeEnum() + Translator.splitterStd + getType() + Translator.splitterStd + getAllInfo();
	}

	public int getType() {
		return type;
	}

}
