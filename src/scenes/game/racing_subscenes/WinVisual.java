package scenes.game.racing_subscenes;

import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_style_push_vec2;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import communication.Communicator;
import elem.ColorBytes;
import elem.Font;
import elem.ui.IUIObject;
import elem.ui.UIButton;
import elem.ui.UIFont;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import elem.ui.UIScrollable;
import elem.ui.UIWindowInfo;
import elem.upgrades.Store;
import engine.graphics.Renderer;
import engine.io.Window;
import main.Features;
import player_local.Player;
import scenes.Scenes;
import scenes.adt.Visual;
import scenes.game.lobby_subscenes.UpgradesSubscene;

public class WinVisual extends Visual {

	private UIFont statsFont;
	private UIScrollable raceLobbyLabel, winStats;
	
	private boolean singleplayer;
	private UIWindowInfo leaderboardWindow;
	private UIButton leaderboardBtn;
	private UILabel leaderboardScoreLabel1, leaderboardScoreLabel2;
	
	private UIButton tryAgainBtn;
	
//	private Store store;
	private int currentPlayerIndex; 
	private Player[] players;
	private UpgradesSubscene upgradesVisualization;
	private UIWindowInfo upgradesControlPanel;
	private UIButton flipToUpgradesBtn, nextPlayerBtn;
	private UILabel[] playerInfos;

	public WinVisual(Features features, UIScrollable raceLobbyLabel) {
		super(features);
		this.raceLobbyLabel = raceLobbyLabel;
		statsFont = new UIFont(Font.BOLD_REGULAR, Window.WIDTH / 45);
		leaderboardScoreLabel1 = new UILabel();
		leaderboardScoreLabel2 = new UILabel();
		
		upgradesControlPanel = UISceneInfo.createWindowInfo(Scenes.RACE, Window.WIDTH * 0.7, 0, Window.WIDTH * 0.3, Window.HEIGHT);
		flipToUpgradesBtn = new UIButton("See upgrade history");
		nextPlayerBtn = new UIButton("Next player");
		
		flipToUpgradesBtn.setPressedAction(() -> {
			if (player == null) {
				flipToUpgradesBtn.setTitle("Go back to the overview");
				playerInfos = players[currentPlayerIndex].getPlayerInfo();
				player = players[currentPlayerIndex];
				upgradesVisualization.setPlayer(player);
		        GL11.glClearColor(0.5f, 0.5f, 0.5f, 1);
			} else {
				flipToUpgradesBtn.setTitle("See upgrade history");
				player = null;
		        GL11.glClearColor(0, 0, 0, 1);
			}
		});
		
		nextPlayerBtn.setPressedAction(() -> {
			currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
			player = players[currentPlayerIndex];
			playerInfos = player.getPlayerInfo();
			upgradesVisualization.setPlayer(player);
		});
		
		UISceneInfo.addPressableToScene(Scenes.RACE, flipToUpgradesBtn);
		UISceneInfo.addPressableToScene(Scenes.RACE, nextPlayerBtn);
	}
	
	public void initRest(boolean singleplayer) {
		this.singleplayer = false;
		if (winStats != null) {
			UISceneInfo.removeWindowInfoReference(Scenes.RACE, winStats.getWindow());
		}
		
		winStats = new UIScrollable(statsFont, Scenes.RACE,
				0, 
				0, 
				Window.WIDTH / 2, 
				singleplayer ? Window.HEIGHT - Window.HEIGHT / 5 : Window.HEIGHT);
		float paddingAmount = Window.HEIGHT / 60;
		winStats.setPadding(paddingAmount / 2f, paddingAmount);
		
		if (singleplayer && leaderboardWindow == null) {
			
			leaderboardWindow = UISceneInfo.createWindowInfo(Scenes.RACE, 0, winStats.getWindow().getYHeight(), winStats.getWindow().width, Window.HEIGHT - winStats.getWindow().getYHeight());
		}
		this.singleplayer = singleplayer;
	}

	@Override
	public void updateResolution() {
		upgradesVisualization.updateResolution();
	}
	
	@Override
	public void mouseScrollInput(float x, float y) {
		if (player != null)
			upgradesVisualization.mouseScrollInput(x, y);
		else
			winStats.scroll(y);
	}
	
	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		if (player != null)
			upgradesVisualization.mouseButtonInput(button, action, x, y);

		return false;
	}

	@Override
	public void mousePosInput(float x, float y) {
		if (player != null)
			upgradesVisualization.mousePosInput(x, y);
	}

	@Override
	public void tick(float delta) {
//		upgradesVisualization.tick(delta);
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		if (player != null) {
			upgradesVisualization.renderGame(renderer, window, delta);
			return;
		}
	}
	
	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		if (player != null) {
			features.pushBackgroundColor(ctx, new ColorBytes(0x00, 0x00, 0x00, 0x66));
			if (upgradesControlPanel.begin(ctx)) {
				nk_layout_row_dynamic(ctx, 100, 1);
				flipToUpgradesBtn.layout(ctx, stack);
				if (!singleplayer) {
					nk_layout_row_dynamic(ctx, 100, 1);
					nextPlayerBtn.layout(ctx, stack);
				}
				nk_layout_row_dynamic(ctx, 30, 1);
				
				for (var playerLabel : playerInfos) {
					nk_layout_row_dynamic(ctx, 30, 1);
					playerLabel.layout(ctx, stack);
				}
			}
			Nuklear.nk_end(ctx);
			features.popBackgroundColor(ctx);
			
			upgradesVisualization.renderUILayout(ctx, stack);
			return;
		}

		features.pushBackgroundColor(ctx, new ColorBytes(255, 255, 255, (int) (255 * 0.8f)));
		raceLobbyLabel.layout(ctx, stack);
		features.popBackgroundColor(ctx);

		float x = raceLobbyLabel.getWindow().x;
		float y = raceLobbyLabel.getWindow().y + raceLobbyLabel.getWindow().height * 1.05f;
		float w = raceLobbyLabel.getWindow().width;
		float h = raceLobbyLabel.getWindow().width / 6; // 2 per knapp som skal være under
		float marginY = 1.05f;
		
		goBackLayout(ctx, stack, x, y, w, h);
		/*
		 * try again
		 */
		NkVec2 spacing = NkVec2.mallocStack(stack);
		NkVec2 padding = NkVec2.mallocStack(stack);
		
		spacing.set(0, 0);
		padding.set(0, 0);
		
		nk_style_push_vec2(ctx, ctx.style().window().spacing(), spacing);
		nk_style_push_vec2(ctx, ctx.style().window().padding(), padding);
		
		NkRect rect = NkRect.mallocStack(stack);
		rect.x(x).y(y + h * marginY).w(w).h(h);
		
		Nuklear.nk_window_set_focus(ctx, "switchToUpgrades");
		if (nk_begin(ctx, "switchToUpgrades", rect, Nuklear.NK_WINDOW_NO_SCROLLBAR | Nuklear.NK_WINDOW_NO_INPUT)) {
			
			nk_layout_row_dynamic(ctx, h, 1);
			flipToUpgradesBtn.layout(ctx, stack);
			
		}
		nk_end(ctx);
		
		Nuklear.nk_style_pop_vec2(ctx);
		Nuklear.nk_style_pop_vec2(ctx);
		
		/*
		 * Stats
		 */
		winStats.layout(ctx, stack);
		
		/*
		 * singleplayer
		 */
		if(singleplayer) {
			/*
			 * try again
			 */
			spacing.set(0, 0);
			padding.set(0, 0);
	
			nk_style_push_vec2(ctx, ctx.style().window().spacing(), spacing);
			nk_style_push_vec2(ctx, ctx.style().window().padding(), padding);
	
			NkRect rect2 = NkRect.mallocStack(stack);
			rect2.x(x).y(y + 2 * h * marginY).w(w).h(h);
	
			Nuklear.nk_window_set_focus(ctx, "tryagain");
			if (nk_begin(ctx, "tryagain", rect2, Nuklear.NK_WINDOW_NO_SCROLLBAR | Nuklear.NK_WINDOW_NO_INPUT)) {
	
				nk_layout_row_dynamic(ctx, h, 1);
				tryAgainBtn.layout(ctx, stack);
	
			}
			nk_end(ctx);
	
			Nuklear.nk_style_pop_vec2(ctx);
			Nuklear.nk_style_pop_vec2(ctx);
	
			/*
			 * leaderboard
			 */
			if (leaderboardWindow.begin(ctx)) {
				float leaderboardScoreHeight = leaderboardWindow.height / 10f;
				Nuklear.nk_layout_row_dynamic(ctx, leaderboardScoreHeight, 1);
				leaderboardScoreLabel1.layout(ctx, stack);
				Nuklear.nk_layout_row_dynamic(ctx, leaderboardScoreHeight, 1);
				leaderboardScoreLabel2.layout(ctx, stack);
				Nuklear.nk_layout_row_dynamic(ctx, leaderboardWindow.height / 3f, 1);
				leaderboardBtn.layout(ctx, stack);
			}
			Nuklear.nk_end(ctx);
		}
		
	}

	@Override
	public boolean hasAnimationsRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setEveryoneDone(boolean everyoneDone) {
		// TODO Auto-generated method stub

	}

	public void claimWinner(Communicator com, String leaderboardScoreText, String leaderboardScoreExplaination) {
		GL11.glClearColor(0, 0, 0, 1);
		leaderboardScoreLabel1.setText(leaderboardScoreText);
		leaderboardScoreLabel2.setText(leaderboardScoreExplaination);

		winStats.setText(UILabel.split(com.getWinner(player), "#"));

		players = com.getPlayers();
		
		ArrayList<UILabel> myStats = new ArrayList<UILabel>();
		for(Player p : players) {
			myStats.add(new UILabel(""));
			myStats.add(new UILabel(p.getName() + ":"));
			myStats.addAll(Arrays.asList(UILabel.create(p.getInfoWin())));
			p.setRole(Player.COMMENTATOR);
		}
		winStats.addText(myStats);

		currentPlayerIndex = player.getID();
		player = null;
	}

	@Override
	public void keyInput(int keycode, int action) {
		
	}

	public void setLeaderboardBtn(UIButton leaderboardBtn) {
		this.leaderboardBtn = leaderboardBtn;
	}

	public void setTryAgainBtn(UIButton tryAgainBtn) {
		this.tryAgainBtn = tryAgainBtn;
	}

	public void setUpgrades(UpgradesSubscene upgradesSubscene) {
		this.upgradesVisualization = upgradesSubscene;
		upgradesSubscene.add(Scenes.RACE, () -> playerInfos = players[currentPlayerIndex].getPlayerInfo());
	}

}
