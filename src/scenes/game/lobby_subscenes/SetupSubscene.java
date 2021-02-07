package scenes.game.lobby_subscenes;

import static org.lwjgl.nuklear.Nuklear.nk_end;

import java.util.function.Consumer;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import audio.SfxTypes;
import communication.GameInfo;
import elem.ui.UIButton;
import elem.ui.UILabel;
import elem.ui.UIWindowInfo;
import engine.graphics.Renderer;
import engine.io.Window;
import game_modes.GameModes;
import main.Features;
import main.ResourceHandler;
import player_local.Player;
import scenes.adt.Subscene;
import scenes.game.Lobby;

/**
 * 
 * Shows upgrades for the engine. Have a different one for boost and fuel
 * 
 * @author Jens Benz
 *
 */

public class SetupSubscene extends Subscene {

	private UIWindowInfo window;
	private float rowHeight, rowSpacingY;

	private GameInfo info;
	private Player player;

	private UIButton<Integer> prevGamemodeBtn, nextGamemodeBtn;
	private UILabel[] gameModeInformation;

	public SetupSubscene(Features features, int sceneIndex) {
		super(features, sceneIndex, "Setup");
		
		prevGamemodeBtn = new UIButton<>("Previous gamemode");
		nextGamemodeBtn = new UIButton<>("Next gamemode");
		
		add(prevGamemodeBtn);
		add(nextGamemodeBtn);
	}

	@Override
	protected void initDown(Lobby lobby, UIButton outNavigationBottom, UIButton outNavigationSide) {

		prevGamemodeBtn.setConsumerValue(-1);
		nextGamemodeBtn.setConsumerValue(+1);
		
		add(prevGamemodeBtn);
		add(nextGamemodeBtn);
		
		prevGamemodeBtn.setNavigations(null,() ->  nextGamemodeBtn, null, () -> outNavigationBottom);
		nextGamemodeBtn.setNavigations(() -> prevGamemodeBtn, () -> outNavigationSide, null, () -> outNavigationBottom);
	}

	public void initGameModeManipulation(GameInfo info, Player player) {
		this.info = info;
		this.player = player;
		
		Consumer<Integer> gamemodeChange = (i) -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			info.init(i, -1);
			for(Player p : info.getPlayers()) {
				p.getCar().completeReset();
			}
			player.upgrades.resetTowardsCar(player, info.getGamemode());
			lobby.updateStore();
		};
		
		prevGamemodeBtn.setPressedAction(gamemodeChange);
		nextGamemodeBtn.setPressedAction(gamemodeChange);
	}

	@Override
	public void updateGenerally() {
		var gm = info.getGamemode();
		if (gm != null)
			gameModeInformation = gm.getGameModeInformation();
	}
	
	@Override
	public void updateResolution() {
		rowSpacingY = Window.HEIGHT / 192.75f;
		rowHeight = Window.HEIGHT / 18f;
	}
	
	@Override
	public void createWindowsWithinBounds(float x, float y, float w, float h) {
		window = createWindow(x, y, w, h);
	}

	@Override
	public void keyInput(int keycode, int action) {
	}
	
	@Override
	public void mouseScrollInput(float x, float y) {
	}

	@Override
	public void mousePositionInput(float x, float y) {
		// System.out.println("rot: " + camera.getRotation().toString());
	}

	@Override
	public void tick(float delta) {
	}

	/**
	 * Use me to render the engine
	 */
	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
	}

	/**
	 * Use me to render the buttons and stuff
	 */
	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		if(window.begin(ctx, stack, 0, rowSpacingY / 2, rowSpacingY, 0)) {
			/*
			 * Settings if null then you are not the host!
			 */
			if(player != null && player.isHost()) {
				if(info.getGamemode().isCanSwitchBetweenGamemodes()) {
				
				Nuklear.nk_layout_row_dynamic(ctx, rowHeight, 3);
				prevGamemodeBtn.layout(ctx, stack);
				Nuklear.nk_label(ctx, info.getGamemode().getName(),
						Nuklear.NK_TEXT_ALIGN_CENTERED
						| Nuklear.NK_TEXT_ALIGN_MIDDLE);
				nextGamemodeBtn.layout(ctx, stack);
				}
			}
		
			if(gameModeInformation != null) {
				for(UILabel infoLabel : gameModeInformation) {
					Nuklear.nk_layout_row_dynamic(ctx, rowHeight, 1);
					infoLabel.layout(ctx, stack);
				}
			}
		}
		nk_end(ctx);
	}

	@Override
	public void createBackground() {
		ResourceHandler.LoadSprite("./images/back/lobbyy.png", "background", (sprite) -> backgroundImage = sprite);
	}

	@Override
	public UIButton intoNavigationSide() {
		if(info.getGamemode().getGameModeEnum().equals(GameModes.SINGLEPLAYER))
			return null;
		return nextGamemodeBtn;
	}

	@Override
	public UIButton intoNavigationBottom() {
		if(info.getGamemode().getGameModeEnum().equals(GameModes.SINGLEPLAYER))
			return null;
		return nextGamemodeBtn;
	}

	@Override
	public void destroy() {
	}
	
	@Override
	public void setVisible(boolean visible) {
		window.visible = visible;
	}
	
}
