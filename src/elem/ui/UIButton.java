package elem.ui;

import static org.lwjgl.nuklear.Nuklear.nk_widget_is_hovered;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import elem.ColorBytes;
import engine.io.InputHandler;
import main.Game;
/**
*  Create with text that is displayed in the middle of the button with the externally predefined font. 
*  Then add pressed action (0..x -> void pressed, T pressed, void pressed right)
*  Then add hover action
*  Then add references to buttons left, right, above or below (nav with arrows) (null = nothing there)
*  Then use either add(..) method from Scene.java or UISceneInfo or use pressed() and unpressed() 
*      to add reference to the button so it works as intended. Otherwise it can't be pressed more than once.
*  Then use layout(..) to show the button in a UIWindowInfo.
*/
public class UIButton<T> implements IUIObject, IUIPressable {

	private String title;
	private boolean visible = true;
	private int alignment;
	protected boolean mouseHover, keyHover, pressed, hasRunHover, enabled = true;
	protected NkColor normal, active, hover, disabled;	
	// Actions:
	private Consumer<T> consumerPressedAction;
	private T consumerValue;
	private IAction pressedAction, pressedActionRight, hoveredAction, hoveredExitAction, changeHoverButtonAction;
	private UINavigationAction left, right, above, below;

	public UIButton(String title) {
		this.title = title;

		ColorBytes normal = new ColorBytes(0x20, 0x16, 0x20, 200);
		ColorBytes active = new ColorBytes(0, 0, 0, 0xff);
		ColorBytes hover = new ColorBytes(0x66, 0x66, 0x66, 0xff);
		ColorBytes disabled = new ColorBytes(0x44, 0x44, 0x44, 200);

		this.normal = normal.create();
		this.active = active.create();
		this.hover = hover.create();
		this.disabled = disabled.create();
		alignment = Nuklear.NK_TEXT_ALIGN_MIDDLE | Nuklear.NK_TEXT_ALIGN_CENTERED;
		
		hoveredAction = Game.hoverAction;
	}

	@Override
	public void layout(NkContext ctx, MemoryStack stack) {

		if(!visible) {
			Nuklear.nk_label(ctx, "", 0);
			return;
		}
		
		NkColor figuredNormalColor = null;
		if (enabled) {
			ctx.style().button().hover().data().color().set(hover);
			ctx.style().button().active().data().color().set(active);
		} else {
			ctx.style().button().hover().data().color().set(disabled);
			ctx.style().button().active().data().color().set(disabled);
			ctx.style().button().normal().data().color().set(disabled);
		}
		/*
		 * Deal with hover stuff
		 */
		if (enabled) {
			if (nk_widget_is_hovered(ctx)) {
				if (!mouseHover) {
					hover();
	
					mouseHover = true;
					keyHover = false;
				}
			} else if (mouseHover) {
				mouseHover = false;
	
				if (hoveredExitAction != null)
					hoveredExitAction.run();
			}
		}

		boolean hovered = mouseHover || keyHover;
		if (enabled) {
			if (hovered)
				figuredNormalColor = hover;
			else
				figuredNormalColor = normal;

			ctx.style().button().normal().data().color().set(figuredNormalColor);
		}
		/*
		 * Deal with pressing stuff
		 */
		ctx.style().button().text_alignment(alignment);

		if (Nuklear.nk_button_label(ctx, title) && !pressed) {
			if (runPressedAction(InputHandler.MOUSEBUTTON)) {
				pressed = true;
			}
		}
	}

	public void setTitleAlignment(int alignment) {
		this.alignment = alignment;
	}

	public void runPressedAction() {
		runPressedAction(GLFW.GLFW_MOUSE_BUTTON_LEFT);
	}
	
	private boolean runPressedAction(int button) {
		boolean res = false;

		if (enabled && visible) {
			if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				if (pressedAction != null) {
					pressedAction.run();
					res = true;
				} else if (consumerPressedAction != null) {
					consumerPressedAction.accept(consumerValue);
					res = true;
				}
			} else if (pressedActionRight != null && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && InputHandler.MOUSEACTION == GLFW.GLFW_RELEASE) {
				pressedActionRight.run();
				res = true;
			}
		}
		
		return res;
	}

	public void runHoveredAction() {
		// Play hover sfx
		if (hoveredAction != null) {
			hoveredAction.run();
		} else
			System.out.println(title + " does not have a hover action");
	}

	public void hover() {
		if (keyHover == false) {
			keyHover = true;
			runHoveredAction();
			if (changeHoverButtonAction != null)
				changeHoverButtonAction.run();
		}
	}
	
	public void hoverFake() {
		keyHover = true;
		mouseHover = true;
	}

	public void unhover() {
		keyHover = false;
		mouseHover = false;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPressedAction(IAction action) {
		this.pressedAction = action;
	}
	
	public void setPressedActionRight(IAction action) {
		this.pressedActionRight = action;
	}

	public void setHoverAction(IAction action) {
		this.hoveredAction = action;
	}

	public void setHoverExitAction(IAction action) {
		this.hoveredExitAction = action;
	}

	public void setChangeHoverButtonAction(IAction changeHoverButtonAction) {
		this.changeHoverButtonAction = changeHoverButtonAction;
	}

	public boolean isPressed() {
		return pressed;
	}

	/**
	 * Used to allow the button to be pressed again. The button has to be UP for it
	 * to react to press.
	 */
	@Override
	public void release() {
		pressed = false;
	}

	/**
	 * Manually press the button down: The button has to be UP for it to react to
	 * press. This presses without activating the button below. Used to avoid
	 * pressing button at same position if for instance changing scenes.
	 */
	@Override
	public void press() {
		pressed = true;
	}

	public void setNavigations(UINavigationAction left, UINavigationAction right, UINavigationAction above, UINavigationAction below) {
		this.left = left;
		this.right = right;
		this.above = above;
		this.below = below;
	}

	public void hoverNavigate(ButtonNavigation nav) {
		switch (nav) {
		case LEFT:
			navigate(left);
			break;
		case RIGHT:
			navigate(right);
			break;
		case ABOVE:
			navigate(above);
			break;
		case BELOW:
			navigate(below);
			break;
		default:
			break;
		}
	}

	private void navigate(UINavigationAction action) {
		if (action != null) {
			@SuppressWarnings("rawtypes")
			UIButton btn = action.run();
			if (btn != null)
				btn.hover();
		}
	}

	public void setEnabled(boolean b) {
		enabled = b;
		if(enabled) {
			release();
		}
	}
	
	public void setPressedAction(Consumer<T> consumerPressedAction) {
		this.consumerPressedAction = consumerPressedAction;
	}

	public T getConsumerValue() {
		return consumerValue;
	}

	public void setConsumerValue(T consumerInteger) {
		this.consumerValue = consumerInteger;
	}

	public void setVisible(boolean b) {
		this.visible = b;
	}

	public boolean isVisible() {
		return visible;
	}

    public boolean hasChangeHoverButtonAction() {
		return changeHoverButtonAction != null;
    }
}
