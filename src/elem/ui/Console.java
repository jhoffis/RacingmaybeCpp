package elem.ui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import elem.ColorBytes;
import engine.graphics.Renderer;
import engine.io.Window;
import main.Features;
import scenes.Scenes;
import scenes.adt.ISceneManipulation;
import scenes.adt.SceneChangeAction;

public class Console implements IUIObject, ISceneManipulation {

	private final UITextField textfield;
	private SceneChangeAction sceneChange;
	
	public Console(Features features) {
		textfield = new UITextField(features, "", false, false, -1, Scenes.GENERAL_NONSCENE, 0, 0, Window.WIDTH, Window.HEIGHT / 32);
		textfield.getWindow().visible = false;
		textfield.getWindow().z = 4;
		textfield.background = new ColorBytes(0x00, 0x00, 0x00, 0xff);
	}
	
	public void init(SceneChangeAction sceneChange) {
		this.sceneChange = sceneChange;
	}
	
	@Override
	public void layout(NkContext ctx, MemoryStack stack) {
		textfield.layout(ctx, stack);
	}

	@Override
	public void updateResolution() {
	}

	public boolean isBlocking() {
		return textfield.getWindow().visible && textfield.isFocused();
	}
	
	@Override
	public void keyInput(int keycode, int action) {
		if (action != GLFW.GLFW_RELEASE) {
			if (keycode == GLFW.GLFW_KEY_ENTER) {// keycode = | 
				runCommand(textfield.getText());
				
				textfield.reset();
				textfield.getWindow().visible = false;
				textfield.focus(false);
				return;
			}

			if (keycode == 96) {// keycode = | 
				boolean open = !textfield.getWindow().visible;			
				if (!open)
					textfield.reset();
				textfield.getWindow().visible = open;
				textfield.focus(open);
				return;
			}
		}
		
		textfield.input(keycode, action);
	}
	
	private void runCommand(String command) {
		String[] cmds = command.toLowerCase().split(" ");
		if (cmds.length == 2 && cmds[1].matches("-?\\d+")) {
			if (cmds[0].equals("mp") ) {
	 			int amountPlayers = Integer.valueOf(cmds[1]);
				System.out.println("create game with " + amountPlayers);
			} else if (cmds[0].equals("goto") ) {	
				int scenenr = Integer.valueOf(cmds[1]);
				if (scenenr >= 0 && scenenr <= Scenes.AMOUNT)
					sceneChange.change(scenenr, false);
			}	
		}
	}

	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		textfield.tryFocus(x, y, false);
		return false;
	}

	@Override
	public void mousePosInput(float x, float y) {
	}

	@Override
	public void mouseScrollInput(float x, float y) {
	}

	@Override
	public void tick(float delta) {
	}

	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		
	}

}
