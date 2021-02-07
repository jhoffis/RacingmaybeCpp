package elem.ui.modal;

import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_style_pop_vec2;
import static org.lwjgl.nuklear.Nuklear.nk_style_push_vec2;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import elem.ColorBytes;
import elem.ui.IUIObject;
import elem.ui.IUIPressable;
import elem.ui.UIButton;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import engine.io.Window;
import main.Features;
import main.Texts;
import scenes.Scenes;

public class UIMessageModal implements IUIObject, IUIPressable {

	private Features features;
	private UIWindowInfo window;
	private String label;
	private UIButton okBtn;

	public UIMessageModal(Features features) {
		this.features = features;
		label = "";

		// Buttons
		okBtn = new UIButton(Texts.exitOKText);

		okBtn.setPressedAction(() -> hide());

		window = UISceneInfo.createWindowInfo(Scenes.GENERAL_NONSCENE,
				0, 
				0, 
				Window.WIDTH, 
				Window.HEIGHT);
		window.visible = false;
		window.z = 3;

		UISceneInfo.addPressableToScene(Scenes.GENERAL_NONSCENE, this);
		UISceneInfo.setChangeHoveredButtonAction(Scenes.GENERAL_NONSCENE, okBtn);

	}

	@Override
	public void layout(NkContext ctx, MemoryStack stack) {
		// Create a rectangle for the window
		features.pushBackgroundColor(ctx, new ColorBytes(0x00, 0x00, 0x00, 0x66));

		if(window.begin(ctx)) {
			// Set own custom styling
			NkVec2 spacing = NkVec2.mallocStack(stack);
			NkVec2 padding = NkVec2.mallocStack(stack);

			float sp = Window.WIDTH / 30f;
			spacing.set(sp, 0);
			padding.set(sp * 2f, sp);

			nk_style_push_vec2(ctx, ctx.style().window().spacing(), spacing);
			nk_style_push_vec2(ctx, ctx.style().window().group_padding(), padding);

			int height = Window.HEIGHT * 2 / 5;
			int heightElements = height / 4;

			// Move group down a bit
			nk_layout_row_dynamic(ctx, height / 2, 1);

			// Height of group
			nk_layout_row_dynamic(ctx, height, 1);

			features.pushBackgroundColor(ctx, new ColorBytes(0x00, 0x00, 0x00, 0xFF));

			if (nk_group_begin(ctx, "MessageGroup", UIWindowInfo.OPTIONS_STANDARD)) {

				String[] lines = label.split("\n");
				for (String label : lines) {
					nk_layout_row_dynamic(ctx, heightElements, 1);
					nk_label(ctx, label, NK_TEXT_ALIGN_LEFT);
				}

				nk_layout_row_dynamic(ctx, heightElements, 1);
				okBtn.layout(ctx, stack);

				// Unlike the window, the _end() function must be inside
				// the if() block
				nk_group_end(ctx);
			}

			features.popBackgroundColor(ctx);

			// Reset styling
			nk_style_pop_vec2(ctx);
			nk_style_pop_vec2(ctx);

		}
		nk_end(ctx);

		features.popBackgroundColor(ctx);
	}

	@Override
	public void release() {
		okBtn.release();
	}
	
	@Override
	public void press() {
		okBtn.press();
	}

	public boolean isVisible() {
		return window.visible;
	}

	public void show(String message) {
		if (message != null) {
			this.label = message;
			window.visible = true;
			press();
		}
	}

	public void hide() {
		window.visible = false;
	}

	public void input(int keycode, int action) {
		if (action == 1) {
			if (keycode == GLFW.GLFW_KEY_ENTER) {
				okBtn.runPressedAction();
			}
		}
	}

}
