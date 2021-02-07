package scenes.regular;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_INPUT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import main.ResourceHandler;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

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
import main.Texts;
import scenes.Scenes;
import scenes.adt.Scene;

/**
 * 
 * @author Jens Benz
 *
 */
public class MainMenuScene extends Scene {

	private final UIButton singleplayerBtn, multiplayerBtn, optionsBtn, exitBtn;
	private final UILabel[] reminder;

	private final UIWindowInfo window;
	private int btnHeight;
	private int hPadding;
	private Sprite backgroundImage;
	
	public MainMenuScene(Features features, RegularTopbar topbar) {
		super(features, topbar, Scenes.MAIN_MENU);

		features.pushBackgroundColor(new ColorBytes(0, 0, 0, 0));
		ResourceHandler.LoadSprite("./images/back/lobby.png", "main", (sprite) -> backgroundImage = sprite);

		window = createWindow(0, topbar.getHeight(), Window.WIDTH, Window.HEIGHT - topbar.getHeight());
		
		singleplayerBtn = new UIButton(Texts.singleplayerText);
		multiplayerBtn = new UIButton(Texts.multiplayerText);
		optionsBtn = new UIButton(Texts.optionsControlsText);
		exitBtn = new UIButton(Texts.exitText);

		singleplayerBtn.setPressedAction(() -> {
			sceneChange.change(Scenes.SINGLEPLAYER, true);
			audio.get(SfxTypes.REGULAR_PRESS).play();
		});
//		leaderboardBtn = new UIButton(Texts.leaderboardText);
//		leaderboardBtn
//		.setHoverAction(() -> audio.get(SfxTypes.REGULAR_HOVER).play());
//		leaderboardBtn.setPressedAction(() -> {
//			sceneChange.change(Scenes.LEADERBOARD, true);
//			audio.get(SfxTypes.REGULAR_PRESS).play();
//		});
//		add(leaderboardBtn);
//		leaderboardBtn.setNavigations(null, null, () -> singleplayerBtn, () -> multiplayerBtn);
		multiplayerBtn.setPressedAction(() -> {
			sceneChange.change(Scenes.MULTIPLAYER, true);
			audio.get(SfxTypes.REGULAR_PRESS).play();
		});
		optionsBtn.setPressedAction(() -> {
			sceneChange.change(Scenes.OPTIONS, true);
			audio.get(SfxTypes.REGULAR_PRESS).play();
		});
		exitBtn.setPressedAction(() -> {
			GLFW.glfwSetWindowShouldClose(features.getWindow().getWindow(), true);
		});

		reminder = UILabel.split(
				"""
						This game is in Early Access!
						Please read Controls before you begin!
						If you encounter any bugs, please email me: jhoffiscreates@gmail.com""", "\n");
		
		/*
		 * Add to a specific window
		 */

		add(singleplayerBtn);
		add(multiplayerBtn);
		add(optionsBtn);
		add(exitBtn);

		singleplayerBtn.setNavigations(null, null, null, () -> multiplayerBtn);
		multiplayerBtn.setNavigations(null, null, () -> singleplayerBtn, () -> optionsBtn);
		optionsBtn.setNavigations(null, null,() ->  multiplayerBtn, () -> exitBtn);
		exitBtn.setNavigations(null, null, () -> optionsBtn, null);

	}

	@Override
	public void updateGenerally() {
		((RegularTopbar) topbar).setTitle(Texts.mainMenu);
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
			generalHoveredButtonNavigation(singleplayerBtn, keycode);
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
		spacing.set(0, btnHeight / 2);

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

			features.pushBackgroundColor(ctx,
					new ColorBytes(0x00, 0x00, 0x00, 0x00));

			if (nk_group_begin(ctx, "My Group", NK_WINDOW_NO_SCROLLBAR)) {

				nk_layout_row_dynamic(ctx, btnHeight, 1);
				singleplayerBtn.layout(ctx, stack); 
				nk_layout_row_dynamic(ctx, btnHeight, 1);
				multiplayerBtn.layout(ctx, stack);  
				nk_layout_row_dynamic(ctx, btnHeight, 1);
				optionsBtn.layout(ctx, stack);  
				nk_layout_row_dynamic(ctx, btnHeight, 1);
				exitBtn.layout(ctx, stack);
				
				group_padding.set(0, 0);
				spacing.set(0, 0);
				
				for(UILabel l : reminder) {
					nk_layout_row_dynamic(ctx, btnHeight / 8, 1);
					l.layout(ctx, stack);
				}
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
