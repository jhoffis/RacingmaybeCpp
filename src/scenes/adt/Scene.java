package scenes.adt;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.util.ArrayList;

import elem.ui.*;
import org.lwjgl.glfw.GLFW;

import adt.IAction;
import audio.AudioRemote;
import elem.interactions.TransparentTopbar;
import elem.objects.GameObject;
import elem.objects.Sprite;
import engine.graphics.Renderer;
import main.Features;
import org.lwjgl.nuklear.Nuklear;

public abstract class Scene implements ISceneManipulation, IUIPressable {

	protected SceneChangeAction sceneChange;
	protected AudioRemote audio;
	protected IAction sceneUpdate;

	protected final Features features;
	protected final ArrayList<GameObject> gameObjects = new ArrayList<GameObject>();
	protected int sceneIndex;
	protected TransparentTopbar topbar;
	
	public Scene(Features features, TransparentTopbar topbar, int sceneIndex) {
		this.features = features;
		this.sceneIndex = sceneIndex;

		if(topbar != null) {
			this.topbar = topbar;
			topbar.select();
		}
	}
	
	public void finalizeInit(AudioRemote audio, SceneChangeAction sceneChange, IAction sceneUpdate) {
		this.audio = audio;
		this.sceneChange = sceneChange;
		this.sceneUpdate = sceneUpdate;
	}
	
	public abstract void updateGenerally();

	protected UIWindowInfo createWindow(float x, float y, float w, float h) {
		return UISceneInfo.createWindowInfo(sceneIndex, x, y, w, h);
	}

	protected void generalHoveredButtonNavigation(UIButton defaultButton,
			int keycode) {

		UIButton hoveredButton = UISceneInfo.getHoveredButton(sceneIndex);

		if (hoveredButton == null) {
			hoveredButton = defaultButton;
			hoveredButton.hover();
			return;
		} else if (keycode == GLFW.GLFW_KEY_ENTER) {
			hoveredButton.runPressedAction();
			return;
		}

		switch (keycode) {
			case GLFW.GLFW_KEY_A :
			case GLFW.GLFW_KEY_LEFT :
				hoveredButton.hoverNavigate(ButtonNavigation.LEFT);
				break;
			case GLFW.GLFW_KEY_D :
			case GLFW.GLFW_KEY_RIGHT :
				hoveredButton.hoverNavigate(ButtonNavigation.RIGHT);
				break;
			case GLFW.GLFW_KEY_W :
			case GLFW.GLFW_KEY_UP :
				hoveredButton.hoverNavigate(ButtonNavigation.ABOVE);
				break;
			case GLFW.GLFW_KEY_S :
			case GLFW.GLFW_KEY_DOWN :
				hoveredButton.hoverNavigate(ButtonNavigation.BELOW);
				break;
		}
	}

	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		boolean down = action != GLFW_RELEASE;

		if(topbar != null && features != null && !features.getWindow().isFullscreen()) {
			if (down) {
				topbar.press(x, y);
			} else {
				topbar.release();
			}
		}
		
		return down;
	}
	
	@Override
	public void mousePosInput(float x, float y) {
		if(topbar != null && features != null && !features.getWindow().isFullscreen())
			topbar.move(x, y);
		mousePositionInput(x, y);
	}
	
	public abstract void mousePositionInput(float x, float y);
	
	/**
	 * Pulls the buttons up again so they can be pressed
	 */
	@Override
	public void release() {
		for (IUIPressable uiObj : UISceneInfo.getScenePressables(sceneIndex)) {
			uiObj.release();
		}
	}

	/**
	 * Used to avoid pressing button at same position if for instance changing
	 * scenes
	 */
	@Override
	public void press() {
		for (IUIPressable uiObj : UISceneInfo.getScenePressables(sceneIndex)) {
			uiObj.press();
		}
	}

	public void add(GameObject go) {
		gameObjects.add(go);
	}

	public void add(IUIPressable pressable) {
		try {
			UISceneInfo.addPressableToScene(sceneIndex, pressable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void renderUIBackground(Renderer renderer, Sprite backgroundImage) {
		renderer.renderOrthoMesh(backgroundImage);
	}

	/**
	 * recieve this topbar only to render it
	 */
	public IUIObject getTopbarRenderable() {
		return topbar instanceof IUIObject ? (IUIObject) topbar : null;
	}
	
	public TransparentTopbar getTopbarInteraction() {
		return topbar;
	}

	public void removePressables() {
		UISceneInfo.getScenePressables(sceneIndex).clear();
	}

	public void removeGameObjects() {
		
		for (GameObject go : gameObjects) {
			go.destroy();
		}
		
		gameObjects.clear();
	}
	
	public abstract void destroy();
}
