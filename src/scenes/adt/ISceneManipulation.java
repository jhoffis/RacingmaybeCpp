package scenes.adt;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import engine.graphics.Renderer;

public interface ISceneManipulation {

	// run me first before any init under (except finalizeInit)
	public void updateResolution();
	
	public void keyInput(int keycode, int action);

	public boolean mouseButtonInput(int button, int action, float x, float y);

	public void mousePosInput(float x, float y);

	public void mouseScrollInput(float x, float y);

	public void tick(float delta);
	
	public abstract void renderGame(Renderer renderer, long window, float delta);
	
	public abstract void renderUILayout(NkContext ctx, MemoryStack stack); 

}
