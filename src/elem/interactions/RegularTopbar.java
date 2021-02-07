package elem.interactions;

import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_label;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import elem.ui.IUIObject;
import elem.ui.UIButton;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import engine.io.Window;
import main.Features;
import main.Game;
import scenes.Scenes;

public class RegularTopbar extends TransparentTopbar implements IUIObject {

	private UIWindowInfo window;
	private String title;
	private UIButton minimizeButton, closeButton;

	public RegularTopbar(Features features, UIButton minimizeButton, UIButton closeButton, TopbarInteraction topbar) {
		super(topbar, 13);
		
		this.minimizeButton = minimizeButton;
		this.closeButton = closeButton;
		
		window = UISceneInfo.createWindowInfo(Scenes.GENERAL_NONSCENE,
				0, 
				0, 
				Window.WIDTH, 
				topbar.getHeight());
		window.visible = false;
	}

	public void setTitle(String title) {
		this.title = Game.NAME + " " + Game.VERSION + " - " + title;
	}

	public void layout(NkContext ctx, MemoryStack stack) {
		int height = getHeight() * 3 / 4;
		if(window.begin(ctx)) { //, stack, height, (int) (height / 3 * 0.65), height, 0)) {
			// Layout
			Nuklear.nk_layout_row_begin(ctx, Nuklear.NK_DYNAMIC, height, 3);
			Nuklear.nk_layout_row_push(ctx, 0.8f);
			nk_label(ctx, title, NK_TEXT_ALIGN_LEFT);

			// Empty space
			Nuklear.nk_layout_row_push(ctx, 0.1f);
			minimizeButton.layout(ctx, stack);
			Nuklear.nk_layout_row_push(ctx, 0.1f);
			closeButton.layout(ctx, stack);
		}
		nk_end(ctx);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if(window != null)
			window.visible = visible;
	}

}
