package main;

public class Texts {

	public static final String 
	mainMenu = "Main Menu",
	readyText = "Ready? (R)",
	optionsText = "Options",
	gobackText = "Go back",
	leaveText = "Leave",
	singleplayerText = "Singleplayer",
	multiplayerText = "Multiplayer",
	exitText = "G T F O",
	exitOKText = "Hell yeah!",
	exitCancelText = "C A N C E L  T H A T  S H I T",
	createOnlineText = "Create new lobby",
	joinOnlineText = "Join selected lobby",
	usernameText = "Whats your username?",
	refreshText = "Refresh",
	minimizeText = "Iconify",
	lobbiesText = "Online lobbies:",
	exitLabelText = "Sure you wanna exit?",
	joining = "Joining...",
	goldBonus = "overnight parts",
	normalBonus = "normal",
	optionsControlsText = optionsText + " and Controls",
	leaderboardText = "The Leaderboard",
	tryAgain = "Try again?",
	privateText = "Private?",
	publicText = "Public?",
	nos = "nos",
	tireboost = "tb",
	weeklyText = "Weekly Challenge",
	difficultyChoose = "Choose a difficulty:",
	spectator = "Spectator",
	player = "Player",
	improveUpgrade = "Improve Tile (SPACE)",
	historyFwd = "Next",
	historyBck = "Previous",
	historyHome = "First",
	historyEnd = "Last",
	undo = "Undo";
	
	public static final String[] 
			tags = {
				Texts.nos + " bottle", Texts.nos + " ms", Texts.nos, "kW", "kg", "km/h",
				"idle-rpm", "rpm", "gears", Texts.tireboost + " ms", Texts.tireboost, Texts.tireboost + " area", "Turbo Blow",
				"vgold", "v$", "fuel", "bar", "hp", "l"
			},
			singleplayerModes = {
				"Normal", "Tough", "Nightmare" 
			};


	public static String leaderboardScoreName(int type) {
		return singleplayerModes[type] + " Score Challenge";
	}

}
