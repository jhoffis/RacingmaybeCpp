package scenes.regular;


import java.util.Stack;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamLeaderboardEntriesHandle;
import com.codedisaster.steamworks.SteamLeaderboardEntry;
import com.codedisaster.steamworks.SteamLeaderboardHandle;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUserStats;
import com.codedisaster.steamworks.SteamUserStats.LeaderboardDataRequest;
import com.codedisaster.steamworks.SteamUserStats.LeaderboardUploadScoreMethod;
import com.codedisaster.steamworks.SteamUserStatsCallback;

import adt.IAction;
import audio.SfxTypes;
import elem.interactions.TransparentTopbar;
import elem.ui.IUIObject;
import elem.ui.UIButton;
import elem.ui.UILeaderboardPlayerButton;
import elem.ui.UISceneInfo;
import elem.ui.UIScrollable;
import elem.ui.UIWindowInfo;
import engine.graphics.Renderer;
import engine.io.Window;
import main.Features;
import main.Game;
import main.Texts;
import scenes.Scenes;
import scenes.adt.Scene;

public class LeaderboardScene extends Scene implements SteamUserStatsCallback {

    private static SteamUserStats userStats;
    private static SteamLeaderboardHandle[] leaderboardHandles, carHandles;

    // top part
    private float topRowHeight;
    private UIButton goBackBtn, refreshBtn;
    private UIWindowInfo topWindow;
    // rest
    private UIScrollable leaderboardList;
	private int type, pageFrom = 0, pageTo = 50;

	private final static Stack<IAction> getterMultiLeaderboard = new Stack<>();
	private static int car;

    public LeaderboardScene(Features features, TransparentTopbar topbar) {
        super(features, topbar, Scenes.LEADERBOARD);
        
        leaderboardHandles = new SteamLeaderboardHandle[Texts.singleplayerModes.length];
        carHandles = new SteamLeaderboardHandle[Texts.singleplayerModes.length];
        
        if (features.getSteamHandler() != null) {
	        userStats = new SteamUserStats(this);
	        findLeaderboard(0, false);
        }

        goBackBtn = new UIButton(Texts.gobackText);
        refreshBtn = new UIButton(Texts.refreshText);

        float width = Window.WIDTH / 2f;
        topWindow = createWindow(Window.WIDTH / 2 - width / 2, topbar.getHeight(), width, Window.HEIGHT / 10f);
        leaderboardList = new UIScrollable(sceneIndex, Window.WIDTH / 2 - width / 2, topWindow.getYHeight(), width, Window.HEIGHT - topWindow.getYHeight());

        goBackBtn.setPressedAction(() -> {
            sceneChange.change(Scenes.PREVIOUS, true);
            audio.get(SfxTypes.REGULAR_PRESS).play();
        });

        refreshBtn.setPressedAction(() -> {
            getLeaderboard(type);
            audio.get(SfxTypes.REGULAR_PRESS).play();
        });

        add(goBackBtn);
        add(refreshBtn);
    }

    public void findLeaderboard(int i, boolean car) {
    	if (!car) {
	    	if (i != 1)
	    		userStats.findLeaderboard("Challenge" + i);
	    	else
	    		userStats.findLeaderboard("Score Challenge");
    	} else {
    		userStats.findLeaderboard("ChallengeCar" + i);
    	}
	}
    
    public void setLeaderboard(int i) {
    	this.type = i;
        getLeaderboard(type);
    }

	@Override
    public void updateGenerally() {
    	GL11.glClearColor(0, 0, 0, 1);
    	getLeaderboard(type);
    }

    @Override
    public void updateResolution() {
        topRowHeight = Window.HEIGHT / 16f;
    }

    @Override
    public void keyInput(int keycode, int action) {
    }

    @Override
    public void mouseScrollInput(float x, float y) {
        leaderboardList.scroll(y);
    }

    @Override
    public void mousePositionInput(float x, float y) {
    }

    @Override
    public void tick(float delta) {
    }

    @Override
    public void renderGame(Renderer renderer, long window, float delta) {
    }

    @Override
    public void renderUILayout(NkContext ctx, MemoryStack stack) {
        if (topWindow.begin(ctx)) {
            Nuklear.nk_layout_row_dynamic(ctx, topRowHeight, 2);
            goBackBtn.layout(ctx, stack);
            refreshBtn.layout(ctx, stack);
        }
        Nuklear.nk_end(ctx);

        leaderboardList.layout(ctx, stack);
    }

    @Override
    public void onUserStatsReceived(long gameId, SteamID steamIDUser,
                                    SteamResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUserStatsStored(long gameId, SteamResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUserStatsUnloaded(SteamID steamIDUser) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUserAchievementStored(long gameId, boolean isGroupAchievement,
                                        String achievementName, int curProgress, int maxProgress) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard,
                                        boolean found) {
        if (found && leaderboard != null) {
        	String name = userStats.getLeaderboardName(leaderboard).toLowerCase();
        	int type = 0;
        	try {
        		type = Integer.parseInt(name.substring(name.length() - 1));
        	} catch (Exception e) {
        		type = 1;
        	}
        	if (name.substring(name.length() - 4, name.length() - 1).equals("car")) {
        		LeaderboardScene.carHandles[type] = leaderboard;
        	} else {
        		LeaderboardScene.leaderboardHandles[type] = leaderboard;
        	}
        	for (int i = 0; i < Texts.singleplayerModes.length; i++) {
        		if (leaderboardHandles[i] == null) {
        			findLeaderboard(i, false);
        			break;
        		} else if (carHandles[i] == null) {
        			findLeaderboard(i, true);
        			break;
        		}
        	}
        }
    }
    
    private void getLeaderboard(int type) {
        leaderboardList.setText("Loading...");

        if (leaderboardHandles[type] == null || carHandles[type] == null)
            return;

        userStats.downloadLeaderboardEntries(leaderboardHandles[type], LeaderboardDataRequest.Global, pageFrom, pageTo);
        leaderboardList.getWindow().name = "Leaderboard: " + Texts.leaderboardScoreName(type);
    }
    
    private static void getMulti() {
    	if (!getterMultiLeaderboard.isEmpty())
    		getterMultiLeaderboard.pop().run();
    }
    
    public static void newScore(int type, int score, int car) {
    	LeaderboardScene.car = car;
    	score = Game.DEBUG ? 200 : score;
    	userStats.uploadLeaderboardScore(leaderboardHandles[type], Game.DEBUG ? LeaderboardUploadScoreMethod.ForceUpdate : LeaderboardUploadScoreMethod.KeepBest, score, new int[0]);
    }


    @Override
    public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard,
                                              SteamLeaderboardEntriesHandle entries,
                                              int numEntries) {

        System.out.println("Leaderboard scores downloaded: handle=" + leaderboard.toString() +
                ", entries=" + entries.toString() + ", count=" + numEntries);

        int[] details = new int[16];
        int handleIndex = -1; 
        for (int i = 0; i < leaderboardHandles.length; i++) {
        	if (leaderboardHandles[i].equals(leaderboard)) {
        		handleIndex = i;
        		break;
        	}
        }
        
        if (handleIndex != -1) {
        	//player score
        	leaderboardList.setText("");
		    if (numEntries == 0) {
		        leaderboardList.addText("No entries in this leaderboard",
		                Nuklear.NK_TEXT_ALIGN_MIDDLE | Nuklear.NK_TEXT_ALIGN_CENTERED);
		    } else {
		        resetButtons();
		
		        for (int i = 0; i < numEntries; i++) {
		
		            SteamLeaderboardEntry entry = new SteamLeaderboardEntry();
		            if (userStats.getDownloadedLeaderboardEntry(entries, i, entry, details)) {
		                var btn = new UILeaderboardPlayerButton(features, entry);
		                leaderboardList.addObject(btn);
		                
		                if (entry.getSteamIDUser().equals(features.getSteamHandler().getMySteamID())) {
		                	features.setAllowedChallenges(handleIndex + 1);
		                }
		            }
		
		        }
		        userStats.downloadLeaderboardEntries(carHandles[type], LeaderboardDataRequest.Global, pageFrom, pageTo);
		    }
        } else {
        	// car
        	IUIObject[] list = leaderboardList.getList();
            for (int i = 0; i < numEntries; i++) {
	            SteamLeaderboardEntry entry = new SteamLeaderboardEntry();
	            if (userStats.getDownloadedLeaderboardEntry(entries, i, entry, details)) {
	            	
	            	for (IUIObject elem : list) {
	            		if (!elem.getClass().equals(UILeaderboardPlayerButton.class)) continue;
	            		var actualElem = (UILeaderboardPlayerButton) elem;
	            		if (actualElem.getSteamID().equals(entry.getSteamIDUser())) {
	            			actualElem.setCarID(entry.getScore());
	            			break;
	            		}
	            	}
	            	
	            }
	        }
        }
        getMulti();
    }

    private void resetButtons() {
        removePressables();
        add(goBackBtn);
        add(refreshBtn);
    }

    @Override
    public void onLeaderboardScoreUploaded(boolean success,
                                           SteamLeaderboardHandle leaderboard, int score, boolean scoreChanged,
                                           int globalRankNew, int globalRankPrevious) {
    	if (scoreChanged && !leaderboard.equals(carHandles[type]))
            userStats.uploadLeaderboardScore(carHandles[type], LeaderboardUploadScoreMethod.ForceUpdate, car, new int[0]);
    }

    @Override
    public void onGlobalStatsReceived(long gameId, SteamResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
    	if (userStats != null)
    		userStats.dispose();
    }

}
