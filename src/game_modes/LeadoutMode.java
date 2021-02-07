package game_modes;

import java.util.Map.Entry;

import player_local.BankType;
import player_local.Player;
import player_local.Car.Rep;

/**
 * If you're behind the leader by 3 points you get knocked out.
 * 
 * @author Jens Benz
 *
 */
public class LeadoutMode extends GameMode {

	private int endGoal;
	private int races;
	
	@Override
	public boolean isGameBegun() {
		return races > 0;
	}

	@Override
	public boolean isGameOverPossible() {
		return false;
	}

	@Override
	public boolean isGameOver() {

		for (Entry<Byte, Player> entry : players.entrySet()) {
			Player player = entry.getValue();

			if (player.getAheadBy() >= endGoal)
				return true;

		}

		return false;
	}

	@Override
	public void startNewRace() {
		races++;

		for (Entry<Byte, Player> entry : players.entrySet()) {
			entry.getValue().newRace();
		}

		raceStartedTime = System.currentTimeMillis();
		regulatingWaitTime = waitTime * 3;
	}

	@Override
	public int getRandomRaceType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNewRaceGoal() {
		return 240 * (r.nextInt(races + 1) + (int) Math.exp(races / 3.5f));
	}

	@Override
	public void newEndGoal(int gameLength) {
		endGoal = gameLength;

		String info = "In this gamemode you win by being ahead with at least one more than " + endGoal + " points!";
		setGameModeInformation(info.split("\n"));

		prepareNextRace();
	}

	@Override
	public int getEndGoalStandard() {
		return (players.size() + 1); // TODO gjør om til (n + 1 | n = players.size)
	}

	@Override
	public String getEndGoalText() {
		return getName() + ";" + "Lead by more than: " + endGoal + " points";
	}

	@Override
	public String getName() {
		return String.valueOf(GameModes.LEADOUT);
	}

	@Override
	public void rewardPlayer(int place, int amountOfPlayers, int behindBy, Player player) {

		Rep rep = player.getCar().getRep();
		int racesDone = races;
		float inflation = (racesDone + 1f) / 2f;
		float stdPrice = 100f;
		int pointsAdded = 0;
		int goldAdded = 0;
		int moneyAdded = (int) (stdPrice * inflation);

		if (!(amountOfPlayers == -1 || place == -1)) {
			if (amountOfPlayers > 1)
				pointsAdded = (amountOfPlayers - (place + 1));
			else
				pointsAdded = 1;

			if (place > 0) {
				moneyAdded += (int) (stdPrice * (0.30 * place / (amountOfPlayers - 1)) * inflation);
			}
		}

		moneyAdded += rep.get(Rep.vmoney);
		goldAdded += rep.get(Rep.vgold);
		
		player.getBank().add(pointsAdded, BankType.POINT);
		player.getBank().add(moneyAdded, BankType.MONEY);
		player.getBank().add(goldAdded, BankType.GOLD);

	}

	@Override
	protected void setAllInfoDown(String[] input, int index) {
		this.endGoal = Integer.valueOf(input[index]);
		index++;
		this.races = Integer.valueOf(input[index]);
	}

	@Override
	protected String getAllInfoDown() {
		return endGoal + "#" + races;
	}

	@Override
	public GameModes getGameModeEnum() {
		return GameModes.LEADOUT;
	}

	@Override
	public String getExtraGamemodeRaceInfo() {
		return null;
	}
}
