package game_modes;

import java.util.Map.Entry;
import player_local.BankType;
import player_local.Player;
import player_local.Car.Rep;

public class GolfMode extends GameMode {

	private int racesLeft;
	private int totalRaces;

	public GolfMode() {
		String info = "Get the most points within " + getEndGoalStandard() + " races.\n"
				+ "If you're behind and win a race you\n"
				+ "might get extra points!";

		setGameModeInformation(info.split("\n"));
	}

	@Override
	public boolean isGameOver() {
		return racesLeft <= 0;
	}

	@Override
	public void startNewRace() {
		racesLeft--;

		for (Entry<Byte, Player> entry : players.entrySet()) {
			entry.getValue().newRace();
		}

		raceStartedTime = System.currentTimeMillis();
		regulatingWaitTime = waitTime * 3;
	}

	@Override
	public int getRandomRaceType() {
		return 0;
	}

	@Override
	public int getNewRaceGoal() {
		return 240 * (r.nextInt(totalRaces - racesLeft + 1)
				+ (int) Math.exp((totalRaces - racesLeft) / 3.5f));
	}

	@Override
	public void newEndGoal(int gameLength) {
		totalRaces = gameLength;
		racesLeft = totalRaces;

		prepareNextRace();
	}

	@Override
	public int getEndGoalStandard() {
		return 12; 
	}

	@Override
	public String getEndGoalText() {
		return getName() + ";" + "Races left: " + String.valueOf(racesLeft);
	}

	@Override
	public String getName() {
		return String.valueOf(GameModes.GOLF);
	}

	@Override
	public void rewardPlayer(final int place, final int amountOfPlayers, int behindBy, Player player) {

		Rep rep = player.getCar().getRep();
		final int racesDone = Math.abs(totalRaces - racesLeft);
		final float inflation = (racesDone + 1f) / 2f;
		final float stdPrice = 100f;
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
			} else if (behindBy > 6) {
				if (behindBy > 9)
					pointsAdded++;
				pointsAdded++;
			}
		}

		moneyAdded += rep.get(Rep.vmoney);
		goldAdded += rep.get(Rep.vgold);

		player.getBank().add(pointsAdded, BankType.POINT);
		player.getBank().add(moneyAdded, BankType.MONEY);
		player.getBank().add(goldAdded, BankType.GOLD);

	}

	@Override
	public boolean isGameBegun() {
		return racesLeft < totalRaces;
	}

	@Override
	public boolean isGameOverPossible() {
		return false;
	}

	@Override
	protected void setAllInfoDown(String[] input, int index) {
		racesLeft = Integer.valueOf(input[index]);
		index++;
		totalRaces = Integer.valueOf(input[index]);
	}

	@Override
	protected String getAllInfoDown() {
		return racesLeft + "#" + totalRaces;
	}

	@Override
	public GameModes getGameModeEnum() {
		return GameModes.GOLF;
	}

	@Override
	public String getExtraGamemodeRaceInfo() {
		return null;
	}

}
