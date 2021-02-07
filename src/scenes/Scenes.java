package scenes;

import java.util.Stack;

public class Scenes {
	/*
	 * REGULAR indexes
	 */
	public static final int 
		PREVIOUS = -1, 
		MAIN_MENU = 0, 
		SINGLEPLAYER = 1, 
		MULTIPLAYER = 2, 
		OPTIONS = 3, 
		JOINING = 4, 
		LEADERBOARD = 5, 
		LOBBY = 6,
		RACE = 7, 
		GENERAL_NONSCENE = 8, 
		AMOUNT = 9;
	public static int PREVIOUS_REGULAR = MAIN_MENU,
			CURRENT = MAIN_MENU;
	public static final Stack<Integer> HISTORY = new Stack<>();
}
