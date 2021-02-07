package scenes.adt;

import audio.AudioRemote;
import elem.objects.Sprite;
import elem.ui.UIButton;
import engine.graphics.Renderer;
import main.Features;
import scenes.game.GameRemote;
import scenes.game.Lobby;

public abstract class Subscene extends Scene {

	private String name;
	protected GameRemote game;
	protected Lobby lobby;
	protected Sprite backgroundImage;
	private int subsceneIndex;

	public Subscene(Features features, int sceneIndex, String name) {
		super(features, null, sceneIndex);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setAudio(AudioRemote audio) {
		this.audio = audio;
	}

	public void setSceneChangeAction(SceneChangeAction sceneChange) {
		this.sceneChange = sceneChange;
	}

	public void setIndex(int index) {
		this.subsceneIndex = index;
	}
	
	public int getIndex() {
		return subsceneIndex;
	}
	

	public void init(Lobby lobby, UIButton outNavigationBottom, UIButton outNavigationSide) {
		this.lobby = lobby;
		initDown(lobby, outNavigationBottom, outNavigationSide);
	}
	protected abstract void initDown(Lobby lobby, UIButton outNavigationBottom, UIButton outNavigationSide);
	
	public abstract void createWindowsWithinBounds(float x, float y, float width, float height);

	public void renderBackground(Renderer renderer) {
		renderer.renderOrthoMesh(backgroundImage);
	}
	
	public abstract void createBackground();
	
	public abstract UIButton intoNavigationSide();

	public abstract UIButton intoNavigationBottom();
	
	public abstract void setVisible(boolean visible);
}
