package scenes;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import audio.AudioRemote;
import elem.ui.Console;
import elem.ui.IUIObject;
import elem.ui.IUIPressable;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import elem.ui.modal.UIExitModal;
import elem.ui.modal.UIMessageModal;
import elem.ui.modal.UIUsernameModal;
import engine.graphics.Renderer;
import main.Features;
import main.Game;
import scenes.adt.ISceneManipulation;
import scenes.adt.Scene;
import scenes.adt.SceneChangeAction;

public class SceneHandler implements ISceneManipulation {

	private final ArrayList<Scene> scenes;
	private final UIUsernameModal usernameModal;
	private final UIExitModal exitModal;
	private static UIMessageModal messageModal;
	private final Console console;
	private final NkColor white;
	private float slowmotion = 1;

	public SceneHandler(UIExitModal exitModal, UIUsernameModal usernameModal, Console console) {
		this.usernameModal = usernameModal;
		this.exitModal = exitModal;
		this.console = console;
		
		scenes = new ArrayList<Scene>();
		white = NkColor.malloc().set((byte) 240, (byte) 240,
				(byte) 240, (byte) 255);
	}

	public void init(Features features, Scene[] scenes, AudioRemote audio) {
		IAction sceneUpdate = () -> {
			getCurrentScene().updateGenerally();  // update points
			getCurrentScene().press();
		};

		SceneChangeAction sceneChange = (scenenr, logCurrent) -> {
			changeScene(scenenr, logCurrent);

			getCurrentScene().getTopbarInteraction().select();
			for(IUIPressable pressable : UISceneInfo.getScenePressables(Scenes.GENERAL_NONSCENE)) {
				pressable.press();
			}
			
			sceneUpdate.run();

			return getCurrentScene();
		};

		for (Scene scene : scenes) {
			this.scenes.add(scene);
			scene.finalizeInit(audio, sceneChange, sceneUpdate);
		}

		usernameModal.setVisible(false, false);
		usernameModal.setStandardInputText(features.getUsername());

		messageModal = new UIMessageModal(features);
		
		if (console != null)
			console.init(sceneChange);

		sceneChange.change(Scenes.MAIN_MENU, true);
	}

	@Override
	public void updateResolution() {
		
		UISceneInfo.updateResolution();
		
		for(Scene scene : scenes) {
			scene.updateResolution();
		}
		
		usernameModal.updateResolution();
	}

	/**
	 * Destroy all the meshes and shaders etc
	 */
	public void destroy() {
		for (Scene scene : scenes) {
			scene.destroy();
		}
		white.free();
	}

	public void changeScene(int scenenr, boolean logCurrent) {
		if (logCurrent) {
			if (scenenr == Scenes.PREVIOUS)
			{
				do {
					scenenr = Scenes.HISTORY.pop();
				} while (!Scenes.HISTORY.isEmpty() && (Scenes.HISTORY.peek() == scenenr || scenenr == Scenes.CURRENT));
			}
			Scenes.HISTORY.push(Scenes.CURRENT);
		}
		Scenes.CURRENT = scenenr;

		// Weird previous ik.
		if (Scenes.CURRENT < Scenes.OPTIONS)
			Scenes.PREVIOUS_REGULAR = Scenes.CURRENT;
	}

	public Scene getCurrentScene() {
		return scenes.get(Scenes.CURRENT);
	}

	public Scene getLastScene() {
		return scenes.get(Scenes.PREVIOUS);
	}

	/*
	 * =========== SCENE MANIPULATION ===========
	 */

	@Override
	public void tick(float delta) {
		getCurrentScene().tick(delta * (Game.DEBUG ? slowmotion : 1));
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		getCurrentScene().renderGame(renderer, window, delta * (Game.DEBUG ? slowmotion : 1));
	}

	/**
	 * TODO set global theme here... If it has to be done each cycle
	 */
	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		Nuklear.nk_style_push_color(ctx, ctx.style().text().color(), white);
		Nuklear.nk_style_push_color(ctx, ctx.style().button().text_normal(), white);
		Nuklear.nk_style_push_color(ctx, ctx.style().button().text_active(), white);
		Nuklear.nk_style_push_color(ctx, ctx.style().button().text_hover(), white);

		/*
		 * MODALS
		 */
		if (console != null)
			console.layout(ctx, stack);
		usernameModal.layout(ctx, stack);
		messageModal.layout(ctx, stack);
		exitModal.layout(ctx, stack);

		/*
		 * SCENE
		 */
		getCurrentScene().renderUILayout(ctx, stack);

		/*
		 * TOPBAR
		 */
		IUIObject topbar = getCurrentScene().getTopbarRenderable();
		if (topbar != null)
			topbar.layout(ctx, stack);

		Nuklear.nk_style_pop_color(ctx);
		Nuklear.nk_style_pop_color(ctx);
		Nuklear.nk_style_pop_color(ctx);
		Nuklear.nk_style_pop_color(ctx);

	}

	@Override
	public void keyInput(int keycode, int action) {
//		if (keycode == GLFW.glfwkey)
		if (console != null) {
			boolean wasBlocking = console.isBlocking();
			console.keyInput(keycode, action);
			if (wasBlocking || console.isBlocking()) return;
		}
		
		if (exitModal.isVisible()) {
			exitModal.input(keycode);
		} else {
			if (usernameModal.isVisible())
				usernameModal.input(keycode, action);
			else if (messageModal.isVisible()) {
				messageModal.input(keycode, action);
			} else {
				getCurrentScene().keyInput(keycode, action);
			}
		}
		
		if (Game.DEBUG && action != GLFW.GLFW_RELEASE) {
			switch (keycode) {
			case GLFW.GLFW_KEY_RIGHT -> {
				slowmotion *= 2;
				System.out.println("Slowmotion: " + slowmotion);
			}
			case GLFW.GLFW_KEY_DOWN -> {
				slowmotion = 1;
				System.out.println("Slowmotion: " + slowmotion);
			}
			case GLFW.GLFW_KEY_LEFT -> {
				slowmotion  /= 2;
				System.out.println("Slowmotion: " + slowmotion);
			}
			}
		}

	}
	
	@Override
	public void mouseScrollInput(float x, float y) {
		getCurrentScene().mouseScrollInput(x, y);
	}

	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		getCurrentScene().mouseButtonInput(button, action, x, y);
		for(IUIPressable pressable : UISceneInfo.getScenePressables(Scenes.GENERAL_NONSCENE)) {
			pressable.release();
		}
		getCurrentScene().release();
		exitModal.release();
		usernameModal.release();
		messageModal.release();

		if (console != null)
			console.mouseButtonInput(button, action, x, y);
		if (usernameModal.isVisible())
			usernameModal.mouseButtonInput(button, action, x, y);
		return false;
	}

	@Override
	public void mousePosInput(float x, float y) {
		getCurrentScene().mousePosInput(x, y);
		UISceneInfo.decideFocusedWindow(x, y);
	}
	
	public static void showMessage(String message) {
		messageModal.show(message);
	}

	public Scene getScene(int sceneID) {
		return scenes.get(sceneID);
	}

}