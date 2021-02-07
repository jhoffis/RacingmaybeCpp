package scenes.regular;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import elem.Font;
import elem.interactions.TransparentTopbar;
import elem.ui.UIButton;
import elem.ui.UIFont;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import engine.graphics.Renderer;
import engine.io.Window;
import main.Texts;
import scenes.Scenes;
import scenes.adt.Scene;
import scenes.game.Lobby;

public class JoiningScene extends Scene {
	
	private UIButton cancelBtn;
	private UIFont joiningFont;
	private UILabel joiningLabel;
	private UIWindowInfo window;

	public JoiningScene(Lobby lobby, TransparentTopbar topbar) {
		super(null, topbar, Scenes.JOINING);
		
		window = createWindow(0, 0, Window.WIDTH, Window.HEIGHT);
		
		joiningFont = new UIFont(Font.BOLD_REGULAR, 0);
		joiningLabel = new UILabel();
		joiningLabel.setText(Texts.joining);
		joiningLabel.setOptions(Nuklear.NK_TEXT_ALIGN_MIDDLE | Nuklear.NK_TEXT_ALIGN_CENTERED);
		
		cancelBtn = new UIButton(Texts.gobackText);
		cancelBtn.setPressedAction(() -> {
			lobby.getGame().gameOver(true);
			lobby.tick(0);
			sceneChange.change(Scenes.MULTIPLAYER, false);
		});
		
		add(cancelBtn);
	}

	@Override
	public void updateGenerally() {
	}

	@Override
	public void updateResolution() {
		joiningFont.resizeFont(Window.HEIGHT / 20);
	}

	@Override
	public void keyInput(int keycode, int action) {
	}

	@Override
	public void mouseScrollInput(float x, float y) {
	}

	@Override
	public void mousePositionInput(float x, float y) {
	}

	@Override
	public void tick(float delta) {
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
	}

	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		if(window.begin(ctx)) {
			Nuklear.nk_style_push_font(ctx, joiningFont.getFont());
			Nuklear.nk_layout_row_dynamic(ctx, Window.HEIGHT * 0.9f, 1);
			joiningLabel.layout(ctx, stack);
			Nuklear.nk_style_pop_font(ctx);

			Nuklear.nk_layout_row_dynamic(ctx, Window.HEIGHT * 0.08f, 3);
			Nuklear.nk_label(ctx, "", 0);
			cancelBtn.layout(ctx, stack);
		}
		Nuklear.nk_end(ctx);
	}

	@Override
	public void destroy() {
		joiningFont.destroy();
	}

}
