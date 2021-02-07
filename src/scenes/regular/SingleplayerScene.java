package scenes.regular;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_INPUT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import java.util.function.Consumer;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import audio.SfxTypes;
import elem.ColorBytes;
import elem.interactions.RegularTopbar;
import elem.objects.Sprite;
import elem.ui.UIButton;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import engine.graphics.Renderer;
import engine.io.Window;
import main.Features;
import main.ResourceHandler;
import main.Texts;
import scenes.Scenes;
import scenes.adt.Scene;

public class SingleplayerScene extends Scene {

	private UIButton<Void> gobackBtn;
	private UILabel title;
	private UIButton<Integer>[] modesBtns, leaderboardBtns;

	private UIWindowInfo window;
	private int btnHeight;
	private int hPadding;
	private Sprite backgroundImage;
	
	public SingleplayerScene(Features features, Consumer<Integer> createNewSingleplayerGameAction, RegularTopbar topbar) {
		super(features, topbar, Scenes.SINGLEPLAYER);

		features.pushBackgroundColor(new ColorBytes(0, 0, 0, 0));
		ResourceHandler.LoadSprite("./images/back/lobby.png", "background", (sprite) -> backgroundImage = sprite);

		window = createWindow(0, topbar.getHeight(), Window.WIDTH, Window.HEIGHT - topbar.getHeight());
		
		title = new UILabel(Texts.difficultyChoose);
		
		gobackBtn = new UIButton<>(Texts.gobackText);
		gobackBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			sceneChange.change(Scenes.MAIN_MENU, true);
		});
		add(gobackBtn);
		gobackBtn.setNavigations(null, null, null, () -> modesBtns[0]);
		
		Consumer<Integer> leaderboardAction = (i) -> {
			var scene = (LeaderboardScene) sceneChange.change(Scenes.LEADERBOARD, true);
			scene.setLeaderboard(i);
			audio.get(SfxTypes.REGULAR_PRESS).play();
		};
		int len = Texts.singleplayerModes.length;
		modesBtns = new UIButton[len];
		leaderboardBtns = new UIButton[len];
		for (int i = 0; i < len; i++) {
			modesBtns[i] = new UIButton<>(Texts.singleplayerModes[i]);
			modesBtns[i].setPressedAction(createNewSingleplayerGameAction);
			modesBtns[i].setConsumerValue(i);
			add(modesBtns[i]);

			leaderboardBtns[i] = new UIButton<>(Texts.singleplayerModes[i] + " Leaderboard");
			leaderboardBtns[i].setPressedAction(leaderboardAction);
			leaderboardBtns[i].setConsumerValue(i);
			add(leaderboardBtns[i]);
		}

	}

	@Override
	public void updateGenerally() {
		((RegularTopbar) topbar).setTitle(Texts.singleplayerText);
//		TODO for (int i = 1; i < modesBtns.length; i++) {
//			boolean enabled = features.getAllowedChallenges() >= i;
//			modesBtns[i].setEnabled(enabled);
//			leaderboardBtns[i].setEnabled(enabled);
//		}
	}
	
	@Override
	public void updateResolution() {
		btnHeight = Window.HEIGHT / 15;
		hPadding = Window.WIDTH / 8;
	}
	
	@Override
	public void tick(float delta) {
	}

	@Override
	public void keyInput(int keycode, int action) {
		if (action == 1) {
			// Downstroke for quicker input
			generalHoveredButtonNavigation(gobackBtn, keycode);
		}
	}
	
	@Override
	public void mouseScrollInput(float x, float y) {
	}

	@Override
	public void mousePositionInput(float x, float y) {
	}

	/*
	 * ========= VISUALIZATION ==========
	 */
	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		// Begin the window
		renderUIBackground(renderer, backgroundImage);
	}

	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {

		// Set the padding of the group
		NkVec2 group_padding = NkVec2.mallocStack(stack);
		NkVec2 spacing = NkVec2.mallocStack(stack);

		group_padding.set(hPadding, btnHeight);
		spacing.set(btnHeight / 10, btnHeight / 2);

		Nuklear.nk_style_push_vec2(ctx, ctx.style().window().group_padding(),
				group_padding);
		Nuklear.nk_style_push_vec2(ctx, ctx.style().window().spacing(),
				spacing);

		features.setBackgroundColor(ctx);
		/*
		 * MAIN SHIT
		 */
		if(window.begin(ctx)) {
			/*
			 * GROUP OF MAIN BUTTONS
			 */

			nk_layout_row_dynamic(ctx,
					Window.HEIGHT - topbar.getHeight(), 1);

			// Groups have the same options available as windows
			int options = NK_WINDOW_NO_SCROLLBAR;

			features.pushBackgroundColor(ctx,
					new ColorBytes(0x00, 0x00, 0x00, 0x00));

			if (nk_group_begin(ctx, "My Group", options)) {

				nk_layout_row_dynamic(ctx, btnHeight, 1);
				gobackBtn.layout(ctx, stack);
				
				nk_layout_row_dynamic(ctx, btnHeight, 1);
				title.layout(ctx, stack);
				
				for (int i = 0; i < modesBtns.length; i++) {
					nk_layout_row_dynamic(ctx, btnHeight, 2);
					modesBtns[i].layout(ctx, stack);
					leaderboardBtns[i].layout(ctx, stack);
				}
				
				group_padding.set(0, 0);
				spacing.set(0, 0);
				
				nk_group_end(ctx);
			}

			features.popBackgroundColor(ctx);
		}
		Nuklear.nk_end(ctx);

		Nuklear.nk_style_pop_vec2(ctx);
		Nuklear.nk_style_pop_vec2(ctx);
	}

	@Override
	public void destroy() {
		removeGameObjects();
	}

}
