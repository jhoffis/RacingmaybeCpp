package game_modes;

import java.util.Map.Entry;

import main.Texts;
import player_local.BankType;
import player_local.Player;
import player_local.Car.Rep;
import scenes.regular.LeaderboardScene;

public class SinglePlayerMode extends GameMode {

	private int lifesStandard, pointsStandard;
	private int lifes;
	private int bigGoalTimeToBeat;
	private int timeToBeat;
	private int races;
	private long prevTime;
	private Player player;
	private boolean lostlife;
	private int endGoalStandard;

	public SinglePlayerMode(Player player, int type) {
		this.player = player;
		super.type = type;
		
		switch (type) {
			case 0:
				lifesStandard = 200;
				pointsStandard = 30;
				bigGoalTimeToBeat = 4000;
				endGoalStandard = 60000;
				break;
			case 1:
				lifesStandard = 10;
				pointsStandard = 30;
				bigGoalTimeToBeat = 3000;
				endGoalStandard = 36000;
				break;
			case 2:
				lifesStandard = 1;
				pointsStandard = 20;
				bigGoalTimeToBeat = 1000;
				endGoalStandard = 25000;
				break;
		}
		
		giveStarterPoints();
		String info = "You have " + (lifesStandard != 200 ? lifesStandard : "infinite") + " lives! \n" 
				+ "Try to beat the time written in the bottom right corner.\n"
				+ "If you do beat it you get money, otherwise you will lose 1 life!\n"
				+ "You always lose 1 point per race, so try to win quickly as you only have " + pointsStandard + " points.\n"
				+ "Game is over if you beat " + (bigGoalTimeToBeat / 1000) + " seconds, run out of points, or die.\n"
				+ "A score will then be set based on your performance.\n\n"
				+ "SCORING ALGORITHM:\n"
				+ scoreAlgo(type);

		setGameModeInformation(info.split("\n"));
		super.canSwitchBetweenGamemodes = false;
	}
	
	public void giveStarterPoints() {
		player.getBank().set(pointsStandard, BankType.POINT);
	}

	@Override
	public boolean isGameOver() {
		
		System.out.println("lifes: " + lifes);
		System.out.println("player: " + player);
		if(player != null)
			System.out.println("getPoints: " + player.getBank().getPoints());
		System.out.println("prevTime: " + prevTime);
		
		return lifes <= 0 || (player != null && player.getBank().getPoints() <= 0)
				|| (prevTime < bigGoalTimeToBeat && prevTime >= 0);
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
		return 0;
	}

	@Override
	public int getNewRaceGoal() {
		return 400;
	}

	@Override
	public void newEndGoal(int gameLength) {
		timeToBeat = gameLength;
		lifes = lifesStandard;

		prepareNextRace();
	}

	@Override
	public int getEndGoalStandard() {
		return endGoalStandard;
	}

	@Override
	public String getEndGoalText() {
		return (lifesStandard != 200 ? "Lifes: " + lifes + ", " : "") + "Points: " + player.getPoints() + " ;" + timeToBeat() + ";Win: " + bigGoalTimeToBeat / 1000 + " sec";
	}
	
	private String timeToBeat() {
		return "Beat: " + String.format("%.3f", timeToBeat / 1000.0) + " sec";
	}

	@Override
	public String getName() {
		return String.valueOf(GameModes.SINGLEPLAYER);
	}

	@Override
	public void rewardPlayer(int place, int amountOfPlayers, int behindBy, Player player) {

		Rep rep = player.getCar().getRep();
		long playerTime = player.getTime();
		int goldAdded = 0;
		int moneyAdded = 0;

		prevTime = playerTime;
		moneyAdded += 100.0 * Math.pow(((double) timeToBeat / (double) playerTime), 1.4);

		if (playerTime != -1 && playerTime < timeToBeat) {
			timeToBeat = (int) playerTime;
			moneyAdded += rep.get(Rep.vmoney);
			goldAdded += rep.get(Rep.vgold);
		} else if (lifesStandard != 200) {			
			lifes--;
			lostlife = true;
		}


		player.getBank().add(-1, BankType.POINT);
		player.getBank().add(moneyAdded, BankType.MONEY);
		player.getBank().add(goldAdded, BankType.GOLD);
	}

	@Override
	public boolean isGameBegun() {
		return races > 0;
	}

	@Override
	public boolean isGameOverPossible() {
		return false;
	}


	// DOES NOT APPLY
	@Override
	protected void setAllInfoDown(String[] input, int index) {
	}

	// DOES NOT APPLY
	@Override
	protected String getAllInfoDown() {
		return "";
	}

	@Override
	public GameModes getGameModeEnum() {
		return GameModes.SINGLEPLAYER;
	}

	@Override
	public boolean isWinner(Player player) {
		return !(lifes <= 0 || player.getPoints() <= 0);
	}
	
	@Override
	public String getDeterminedWinnerText(Player player) {
		return "Game over";
	}

	public boolean lostLife() {
		if(lostlife) {
			lostlife = false;
			return true;
		}
		return false;
	}

	@Override
	public String getExtraGamemodeRaceInfo() {
		return timeToBeat();
	}

	public int getLifes() {
		return lifes;
	}
	
	private final String scoreAlgo(int type) {
		return switch(type) {
        	case 0 -> {
        		yield "points * 1000 + (money + gold) / 10";
        	}
        	case 1 -> {
        		yield "lifes * points * 3400 + money * gold / 10;";
        	}
        	case 2 -> {
        		yield "(lifes * 10) * (points * 4/3) * 3400 + money * gold / 10";
        	}
			default -> throw new IllegalArgumentException("Unexpected value: " + super.type);
        };
	}

	public String[] getCreateScore() {
		int points = player.getPoints();
        int money = player.getBank().getMoneyAchived();
        int gold = player.getBank().getGoldAchived();
        long highSpeed = player.getCarRep().getInt(Rep.highestSpdAchived);

        int score = switch(super.type) {
        	case 0 -> {
        		yield points * 1000 + (money + gold) / 10;
        	}
        	case 1 -> {
        		yield lifes * points * 3400 + money * gold / 10;
        	}
        	case 2 -> {
        		yield (lifes * 10) * (points * 4/3) * 3400 + money * gold / 10;
        	}
			default -> throw new IllegalArgumentException("Unexpected value: " + super.type);
        };
        LeaderboardScene.newScore(type, score, player.getCarNameID());
        
        return new String[]{ score + " SCORE! " + scoreAlgo(type),  
        		"((" + lifes + " * " + points + " * 3400) + ($" + money + " * " + gold + " * " + " / 10))"};
	}
	
}
