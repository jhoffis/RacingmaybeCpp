package elem.ui.modal;

import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_style_pop_vec2;
import static org.lwjgl.nuklear.Nuklear.nk_style_push_vec2;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import elem.ColorBytes;
import elem.ui.IUIObject;
import elem.ui.IUIPressable;
import elem.ui.UIButton;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import engine.io.Window;
import main.Features;
import main.Texts;
import scenes.Scenes;

public class UIExitModal implements IUIObject, IUIPressable {

	private Features features;
	private UIWindowInfo window;
	private final UILabel exitLabel;
	private final UIButton okBtn, cancelBtn;

	public UIExitModal(Features features) {
		this.features = features;
		
		exitLabel = new UILabel(Texts.exitLabelText);
		
		// Buttons
		okBtn = new UIButton(Texts.exitOKText);
		cancelBtn = new UIButton(Texts.exitCancelText);
		
		window = UISceneInfo.createWindowInfo(Scenes.GENERAL_NONSCENE,
				0, 
				0, 
				Window.WIDTH, 
				Window.HEIGHT);
		window.visible = false;
		window.z = 4;

		UISceneInfo.addPressableToScene(Scenes.GENERAL_NONSCENE, this);
		UISceneInfo.setChangeHoveredButtonAction(Scenes.GENERAL_NONSCENE, okBtn);
		UISceneInfo.setChangeHoveredButtonAction(Scenes.GENERAL_NONSCENE, cancelBtn);
	
	}
	
	public void setButtonActions(IAction okAction, IAction cancelAction) {
		okBtn.setPressedAction(okAction);
		cancelBtn.setPressedAction(cancelAction);		
	}


	@Override
	public void layout(NkContext ctx, MemoryStack stack) {

		features.pushBackgroundColor(ctx,
				new ColorBytes(0x00, 0x00, 0x00, 0x66));

		if(window.begin(ctx)) {
			// Set own custom styling
			NkVec2 spacing = NkVec2.mallocStack(stack);
			NkVec2 padding = NkVec2.mallocStack(stack);

			float sp = Window.WIDTH / 30f;
			spacing.set(sp, 0);
			padding.set(sp * 2f, sp);

			nk_style_push_vec2(ctx, ctx.style().window().spacing(), spacing);
			nk_style_push_vec2(ctx, ctx.style().window().group_padding(),
					padding);

			int height = Window.HEIGHT * 2 / 5;
			int heightElements = height / 4;

			// Move group down a bit
			nk_layout_row_dynamic(ctx, height / 2, 1);

			// Height of group
			nk_layout_row_dynamic(ctx, height, 1);

			features.pushBackgroundColor(ctx,
					new ColorBytes(0x00, 0x00, 0x00, 0xFF));

			if (nk_group_begin(ctx, "ExitGroup", UIWindowInfo.OPTIONS_STANDARD)) {

				nk_layout_row_dynamic(ctx, heightElements, 1);
				exitLabel.layout(ctx, stack);

				nk_layout_row_dynamic(ctx, heightElements, 2);
				okBtn.layout(ctx, stack);
				cancelBtn.layout(ctx, stack);

				// Unlike the window, the _end() function must be inside the
				// if() block
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
		cancelBtn.release();
	}

	@Override
	public void press() {
		okBtn.press();
		cancelBtn.press();
	}

	public boolean isVisible() {
		return window.visible;
	}

	public void setVisible(boolean visible) {
		window.visible = visible;

		if (visible) {
			features.getWindow().mouseStateHide(false);
			press();
		} else {
			features.getWindow().mouseStateToPrevious();
		}
	}

	public void input(int keycode) {
		switch (keycode) {
			case GLFW.GLFW_KEY_UP :
			case GLFW.GLFW_KEY_LEFT :
			case GLFW.GLFW_KEY_A :
			case GLFW.GLFW_KEY_W :
				okBtn.hover();
				break;
			case GLFW.GLFW_KEY_DOWN :
			case GLFW.GLFW_KEY_RIGHT :
			case GLFW.GLFW_KEY_D :
			case GLFW.GLFW_KEY_S :
				cancelBtn.hover();
				break;
			case GLFW.GLFW_KEY_ENTER :
				UIButton hoveredButton = UISceneInfo.getHoveredButton(Scenes.GENERAL_NONSCENE);
				if(hoveredButton != null) {
					hoveredButton.runPressedAction();
					UISceneInfo.clearHoveredButton(Scenes.GENERAL_NONSCENE);
				}
				break;
		}
	}

}
