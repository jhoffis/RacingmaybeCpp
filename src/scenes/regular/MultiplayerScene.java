package scenes.regular;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import java.util.function.Consumer;

import main.ResourceHandler;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import com.codedisaster.steamworks.SteamMatchmaking;

import adt.IAction;
import audio.SfxTypes;
import elem.ColorBytes;
import elem.interactions.RegularTopbar;
import elem.objects.Sprite;
import elem.ui.UIButton;
import elem.ui.UIButtonLobby;
import elem.ui.UIWindowInfo;
import engine.graphics.Renderer;
import engine.io.Window;
import main.Features;
import main.Texts;
import scenes.Scenes;
import scenes.adt.Scene;

public class MultiplayerScene extends Scene {


	private Consumer<Integer> initMovingIntoALobby;
	private UIButton joinOnlineBtn, createOnlineBtn, gobackBtn, refreshBtn;
	private RegularTopbar topbar;

	private UIWindowInfo window;
	private int btnHeight;
	private String lobbiesTitle;
	private Sprite backgroundImage;

	public MultiplayerScene(Features features, RegularTopbar topbar, Consumer<Integer> initMovingIntoALobby) {
		super(features, topbar, Scenes.MULTIPLAYER);

		ResourceHandler.LoadSprite("./images/back/titlebackground.png", "main", (sprite) -> backgroundImage = sprite);

		window = createWindow(0, topbar.getHeight(), Window.WIDTH,Window.HEIGHT - topbar.getHeight());

		this.topbar = topbar;
		this.initMovingIntoALobby = initMovingIntoALobby;

		lobbiesTitle = Texts.lobbiesText;

		joinOnlineBtn = new UIButton(Texts.joinOnlineText);
		createOnlineBtn = new UIButton(Texts.createOnlineText);
		gobackBtn = new UIButton(Texts.gobackText);
		refreshBtn = new UIButton(Texts.refreshText);

		joinOnlineBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			if (features.getSelectedLobby() != null	&& features.getSelectedLobby().isSelected()) {
				join();
			}
		});
		joinOnlineBtn.setEnabled(false);
		createOnlineBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			host();
		});
		gobackBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			sceneChange.change(Scenes.MAIN_MENU, true);
		});
		refreshBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			updateGenerally();
		});

		add(joinOnlineBtn);
		add(createOnlineBtn);
		add(gobackBtn);
		add(refreshBtn);

		joinOnlineBtn.setNavigations(null, () -> createOnlineBtn, null, () -> gobackBtn);
		createOnlineBtn.setNavigations(() -> joinOnlineBtn, null, null, () -> refreshBtn);
		gobackBtn.setNavigations(null, () -> refreshBtn, () -> joinOnlineBtn, null);
		refreshBtn.setNavigations(() -> gobackBtn, null, () -> createOnlineBtn, null);

		features.createLobbyBtnAction(joinOnlineBtn);
	}
	
	@Override
	public void updateGenerally() {
		topbar.setTitle(Texts.multiplayerText);

		features.setLobbiesInnerText("searching...");
		new SteamMatchmaking(features).requestLobbyList();
		features.clearLobbies();
		joinOnlineBtn.setEnabled(false);
	}
	
	@Override
	public void updateResolution() {
		btnHeight = Window.HEIGHT / 12;
	}

	@Override
	public void tick(float delta) {
	}

	@Override
	public void keyInput(int keycode, int action) {

		if (action == 1) {
			// Downstroke for quicker input
			generalHoveredButtonNavigation(joinOnlineBtn, keycode);
		}

	}

	@Override
	public void mouseScrollInput(float x, float y) {
	}
	
	@Override
	public void mousePositionInput(float x, float y) {
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		renderUIBackground(renderer, backgroundImage);
	}

	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {

		// Set the padding of the group
//		ctx.style().window().spacing().set(0, btnHeight / 2);

		features.setBackgroundColor(ctx);

		/*
		 * MAIN SHIT
		 */
		if(window.begin(ctx)) {

			NkVec2 spacing = NkVec2.mallocStack(stack);
			NkVec2 padding = NkVec2.mallocStack(stack);

			/*
			 * Lobbies
			 */

			spacing.set(0, btnHeight / 10);
			padding.set(0, 0);

			Nuklear.nk_style_push_vec2(ctx, ctx.style().window().spacing(),
					spacing);
			Nuklear.nk_style_push_vec2(ctx, ctx.style().window().padding(),
					padding);

			nk_layout_row_dynamic(ctx, Window.HEIGHT * 2 / 3 - btnHeight / 1.5f, 1);

			// Groups have the same options available as windows
			int options = Nuklear.NK_WINDOW_BORDER
					| Nuklear.NK_WINDOW_TITLE;

			features.pushBackgroundColor(ctx,
					new ColorBytes(0x00, 0x00, 0x00, 0x66));

			if (Nuklear.nk_group_begin(ctx, lobbiesTitle, options)) {

				if (!features.getLobbiesInnerText().isEmpty()) {
					nk_layout_row_dynamic(ctx, btnHeight, 1); // nested
					// row
					Nuklear.nk_label(ctx, features.getLobbiesInnerText(), Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_MIDDLE);
				}

				for (UIButtonLobby btn : features.getLobbies()) {
					nk_layout_row_dynamic(ctx, btnHeight / 2, 1); // nested row
					btn.layout(ctx, stack);
				}

				Nuklear.nk_group_end(ctx);
			}

			features.popBackgroundColor(ctx);

			/*
			 * Buttons
			 */

			nk_layout_row_dynamic(ctx, btnHeight / 2, 1);

			Nuklear.nk_style_pop_vec2(ctx);
			Nuklear.nk_style_pop_vec2(ctx);

			spacing.set(btnHeight / 2, btnHeight / 2);
			padding.set(btnHeight / 2, 0);

			Nuklear.nk_style_push_vec2(ctx, ctx.style().window().spacing(),
					spacing);
			Nuklear.nk_style_push_vec2(ctx, ctx.style().window().padding(),
					padding);

			nk_layout_row_dynamic(ctx, btnHeight, 2);
			joinOnlineBtn.layout(ctx, stack);
			createOnlineBtn.layout(ctx, stack);

			nk_layout_row_dynamic(ctx, btnHeight, 2);
			gobackBtn.layout(ctx, stack);
			refreshBtn.layout(ctx, stack);

			Nuklear.nk_style_pop_vec2(ctx);
			Nuklear.nk_style_pop_vec2(ctx);
		}
		Nuklear.nk_end(ctx);
	}

	private void join() {
		initMovingIntoALobby.accept(0);
	}

	private void host() {
		initMovingIntoALobby.accept(1);
	}

	@Override
	public void destroy() {
		removeGameObjects();
	}

}
