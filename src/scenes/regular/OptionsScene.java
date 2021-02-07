package scenes.regular;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_INPUT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_style_pop_vec2;
import static org.lwjgl.nuklear.Nuklear.nk_style_push_vec2;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import audio.AudioMaster;
import audio.AudioTypes;
import audio.SfxTypes;
import elem.ColorBytes;
import elem.interactions.RegularTopbar;
import elem.ui.UIButton;
import elem.ui.UISceneInfo;
import elem.ui.UISlider;
import elem.ui.UIWindowInfo;
import engine.graphics.Renderer;
import engine.io.Window;
import main.Features;
import main.Texts;
import scenes.SceneHandler;
import scenes.Scenes;
import scenes.adt.Scene;
import settings_and_logging.ControlsSettings;
import settings_and_logging.RegularSettings;

public class OptionsScene extends Scene {

	private final String[] explaination = {
		"Tutorial:", 
		"1. Ready and start race.",
		"2. Press throttle as green lights turn on!",
		"3. Use NOS as quickly as possible.",
		"4. Change gear by releasing throttle, ", 
		"   and selecting next gear.", 
		"5. Profit or WIN/LOSE",
		"6. Buy upgrades and return to step 1!"
	};

	private final String[] controls = {
			"Controls:", 
			"Throttle: W",
			"NOS: E",
			"Turbo Blow: Q",
			"Gearbox layout:",
			"1 3 5     -",
			"|-|-|  or |",
			"2 4 6     +"
	};
	
	private UIWindowInfo window;
	private UIButton gobackBtn, fullscreenBtn, vsyncBtn;
	private UISlider[] sliders = {new UISlider("Master-Volume"),
			new UISlider("SFX-Volume"), new UISlider("Music-Volume")};
	private RegularSettings settings;
//	private ControlsSettings controlsSettings;
	private AudioMaster audio;

	private int btnHeight;
	private int hPadding;

	public OptionsScene(Features features, RegularTopbar topbar) {
		super(features, topbar, Scenes.OPTIONS);
		// TODO fullscreen checks?
		
		window = createWindow(0, topbar.getHeight(), Window.WIDTH, Window.HEIGHT - topbar.getHeight());
		
		gobackBtn = new UIButton(Texts.gobackText + " and update");
		gobackBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();

			settings.setMasterVolume(sliders[0].getValue());
			settings.setSfxVolume(sliders[1].getValue());
			settings.setMusicVolume(sliders[2].getValue());

			audio.setVolume(AudioTypes.MASTER, (float) sliders[0].getValue());
			audio.setVolume(AudioTypes.SFX, (float) sliders[1].getValue());
			audio.setVolume(AudioTypes.MUSIC, (float) sliders[2].getValue());

			audio.updateVolumeSfx();
			audio.updateVolumeMusic();

			sceneChange.change(Scenes.PREVIOUS, true);
		});
		
		fullscreenBtn = new UIButton("");
		vsyncBtn = new UIButton("");
		
		/*
		 * Add to a specific window
		 */

		add(gobackBtn);
		add(fullscreenBtn);
		add(vsyncBtn);

	}
	
	public void initOptions(RegularSettings settings,
			ControlsSettings controlsSettings, AudioMaster audio, SceneHandler sceneHandler) {
		this.settings = settings;
		this.audio = audio;

		sliders[0].setValue(settings.getMasterVolume());
		sliders[1].setValue(settings.getSfxVolume());
		sliders[2].setValue(settings.getMusicVolume());
		
		fullscreenBtn.setPressedAction(() -> {
			boolean full = !settings.getFullscreen();
			settings.setFullscreen(full);
			fullscreenBtn.setTitle(full ? "Windowed?" : "Fullscreen?");
			features.getWindow().setFullscreen(full);
			sceneHandler.updateResolution();
			press();
		});

		fullscreenBtn.setTitle(settings.getFullscreen() ? "Windowed?" : "Fullscreen?");
		
		vsyncBtn.setPressedAction(() -> {
			boolean vsync = !settings.getVsync();
			settings.setVsync(vsync);
			vsyncBtn.setTitle(vsync ? "Turn off vsync?" : "Turn on vsync?");
			GLFW.glfwSwapInterval(vsync ? 1 : 0);			
			sceneHandler.updateResolution();
			press();
		});

		vsyncBtn.setTitle(settings.getVsync() ? "Turn off vsync?" : "Turn on vsync?");
	}
	
	@Override
	public void updateGenerally() {
		GL11.glClearColor(0.1f, 0.1f, 0.1f, 1);

		gobackBtn.hover();
		((RegularTopbar) topbar).setTitle(Texts.optionsText);
		press();
	}
	
	@Override
	public void updateResolution() {
		btnHeight = Window.HEIGHT / 16;
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
	public void mousePositionInput(float  x, float  y) {
	}

	/*
	 * ========= VISUALIZATION ==========
	 */

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
	}

	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {

		// Set the padding of the group

		NkVec2 spacing = NkVec2.mallocStack(stack);
		NkVec2 padding = NkVec2.mallocStack(stack);

		spacing.set(0, btnHeight / 2);
		padding.set(hPadding, btnHeight);

		nk_style_push_vec2(ctx, ctx.style().window().spacing(), spacing);
		nk_style_push_vec2(ctx, ctx.style().window().group_padding(), padding);
		
		features.setBackgroundColor(ctx);
		/*
		 * MAIN SHIT
		 */
		if(window.begin(ctx)) {
			/*
			 * GROUP OF MAIN BUTTONS
			 */

			nk_layout_row_dynamic(ctx,
					(Window.HEIGHT - topbar.getHeight()) - Window.HEIGHT * 0.45f, 1);

			// Groups have the same options available as windows
			int options = NK_WINDOW_NO_SCROLLBAR;

			features.pushBackgroundColor(ctx,
					new ColorBytes(0x00, 0x00, 0x00, 0x00));

			if (nk_group_begin(ctx, "My Group", options)) {

				//
				// The group contains rows and the rows contain widgets, put
				// those here.
				//
				nk_layout_row_dynamic(ctx, btnHeight, 1); // nested row
				gobackBtn.layout(ctx, stack);

				nk_layout_row_dynamic(ctx, btnHeight, 2);
				fullscreenBtn.layout(ctx, stack);
				vsyncBtn.layout(ctx, stack);

				nk_style_pop_vec2(ctx);
				nk_style_pop_vec2(ctx);

				spacing.set(0, 0);
				padding.set(hPadding, 0);

				nk_style_push_vec2(ctx, ctx.style().window().group_padding(),
						padding);
				nk_style_push_vec2(ctx, ctx.style().window().spacing(),
						spacing);

				for (UISlider slider : sliders) {

					nk_layout_row_dynamic(ctx, btnHeight / 2, 1);
					Nuklear.nk_label(ctx, slider.getName(),
							Nuklear.NK_TEXT_ALIGN_CENTERED
									| Nuklear.NK_TEXT_ALIGN_MIDDLE);
					Nuklear.nk_layout_row_begin(ctx, Nuklear.NK_DYNAMIC,
							btnHeight / 2, 2);
					Nuklear.nk_layout_row_push(ctx, 0.9f);
					slider.layout(ctx, stack);
					Nuklear.nk_layout_row_push(ctx, 0.1f);
					Nuklear.nk_label(ctx,
							String.valueOf(slider.getValueActual()),
							Nuklear.NK_TEXT_ALIGN_CENTERED
									| Nuklear.NK_TEXT_ALIGN_MIDDLE);

				}

				// Unlike the window, the _end() function must be inside the
				// if() block
				
				nk_group_end(ctx);
			}
			nk_style_pop_vec2(ctx);
			spacing.set(Window.WIDTH * 0.27f, 0);
			nk_style_push_vec2(ctx, ctx.style().window().spacing(),
					spacing);
			
			int length = explaination.length > controls.length ? explaination.length : controls.length;
			
			for (int i = 0; i < length; i++) {
				nk_layout_row_dynamic(ctx, btnHeight / 2f, 2);
				String s = "";
				if(i < controls.length)
					s = controls[i];
				Nuklear.nk_label(ctx, s, Nuklear.NK_TEXT_ALIGN_CENTERED
						| Nuklear.NK_TEXT_ALIGN_MIDDLE);

				if(i < explaination.length)
					s = explaination[i];
				else
					s = "";
				Nuklear.nk_label(ctx, s, Nuklear.NK_TEXT_ALIGN_LEFT
						| Nuklear.NK_TEXT_ALIGN_MIDDLE);
			}

			nk_style_pop_vec2(ctx);
			nk_style_pop_vec2(ctx);
			features.popBackgroundColor(ctx);
		}
		Nuklear.nk_end(ctx);
	}
	
	@Override
	public void destroy() {
		removeGameObjects();
	}
}
