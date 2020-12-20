
#include <stack>

namespace Scenes {
/*
	 * REGULAR indexes
	 */
    const int
            PREVIOUS = -1,
            MAIN_MENU = 0,
            SINGLEPLAYER = 1,
            MULTIPLAYER = 2,
            OPTIONS = 3,
            JOINING = 4,
            LEADERBOARD = 5,
            LEADERBOARD_ENTRY = 6,
            LOBBY = 7,
            RACE = 8,
            GENERAL_NONSCENE = 9,
            AMOUNT = 10;
    int PREVIOUS_REGULAR = MAIN_MENU,
            CURRENT = MAIN_MENU;
    std::stack<int> HISTORY;

}