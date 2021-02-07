package elem.ui;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import elem.ColorBytes;
import main.Features;

public class UITextField implements IUIObject {

	private boolean focused, firstTimeFocused, removeAtFirstTime, justNumbers;
	private String text;
	private String preText;
	private Consumer<Integer> specialInput;
	private IAction specialUnfocused;

	private UIWindowInfo window;

	private int maxLength;
	private String cursor;
	private boolean shift, caps;
	private Features features;
	public ColorBytes background;

	/**
	 * Creates Textfield to be used in a window. It does not create its own.
	 * But it cannot be selected with mouse
	 */
	public UITextField(Features features, String preText, boolean removeAtFirstTime,
			boolean justNumbers, int maxLength) {
		this.features = features;
		this.preText = preText;
		this.text = preText;
		this.maxLength = maxLength;
		cursor = "";
		firstTimeFocused = true;
		this.justNumbers = justNumbers;
		this.removeAtFirstTime = removeAtFirstTime;
		background = new ColorBytes(0x00, 0x00, 0x00, 0x66);
	}

	/**
	 * Creates Textfield to be used outside of a window. It does create its own.
	 */
	public UITextField(Features features, String preText, boolean removeAtFirstTime,
			boolean justNumbers, int maxLength, int sceneIndex,
			float x, float y, float width, float height) {
		this(features, preText, removeAtFirstTime, justNumbers, maxLength);

		window = UISceneInfo.createWindowInfo(sceneIndex, x, y, width, height);
	}

	@Override
	public void layout(NkContext ctx, MemoryStack stack) {
		if (window != null) {
			features.pushBackgroundColor(ctx, background);
			if(window.begin(ctx)) {
				nk_layout_row_dynamic(ctx, window.height, 1); // nested row
				layoutTextfieldItself(ctx);
			}
			nk_end(ctx);
			features.popBackgroundColor(ctx);
		} else {
			layoutTextfieldItself(ctx);
		}
		
	}

	public void layoutTextfieldItself(NkContext ctx) {
		Nuklear.nk_label(ctx, text + cursor, Nuklear.NK_TEXT_ALIGN_LEFT | Nuklear.NK_TEXT_ALIGN_CENTERED);
	}

	public void setSpecialInputAction(Consumer<Integer> specialInput) {
		this.specialInput = specialInput;
	}

	public void setUnfocuedAction(IAction specialUnfocused) {
		this.specialUnfocused = specialUnfocused;
	}

	public void input(int keycode, int action) {
		if (focused && (window == null || window.visible)) {

			switch (keycode) {
				case GLFW_KEY_LEFT_SHIFT :
				case GLFW_KEY_RIGHT_SHIFT :
					shift = !shift;
					return;
			}

			if (action != GLFW.GLFW_RELEASE) {
				if (specialInput != null)
					specialInput.accept(keycode);

				if (keycode == GLFW.GLFW_KEY_DELETE
						|| keycode == GLFW.GLFW_KEY_BACKSPACE) {
					if (text.length() > 0)
						text = text.substring(0, text.length() - 1);
			} else if (maxLength == -1 || text.length() < maxLength) {

					if (keycode == GLFW.GLFW_KEY_CAPS_LOCK) {
						caps = !caps;
						return;
					}

					if (keycode >= (justNumbers ? 48 : 32)
							&& keycode <= (justNumbers ? 57 : 126)) {
						String key = String.valueOf((char) keycode);

						if (shift || caps) {
							// key
							if(keycode == 49)
								key = "!";
							else
								key = key.toUpperCase();
						} else {
							key = key.toLowerCase();
						}

						text += key;
					} else {
						String specialKey = null;
						switch(keycode) {
							case 320:
								specialKey = "0";
								break;
							case 321:
								specialKey = "1";
								break;
							case 322:
								specialKey = "2";
								break;
							case 323:
								specialKey = "3";
								break;
							case 324:
								specialKey = "4";
								break;
							case 325:
								specialKey = "5";
								break;
							case 326:
								specialKey = "6";
								break;
							case 327:
								specialKey = "7";
								break;
							case 328:
								specialKey = "8";
								break;
							case 329:
								specialKey = "9";
								break;
							case 330:
								specialKey = ".";
								break;
						}
						
						if(specialKey != null)
						text += specialKey;
					}

				}
			}

		}
	}

	public String getText() {
		return (String) text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean tryFocus(double x, double y, boolean remove) {
		if(window != null) {
			if ((window.x <= x && window.y <= y)
					&& (window.x + window.width >= x && window.y + window.height >= y)) {
				focus(remove);
			} else {
				unfocus(remove);
			}
		}

		return focused;
	}

	public boolean isFocused() {
		return focused;
	}

	public void reset() {
		text = preText;
		firstTimeFocused = true;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setPretext(String text) {
		preText = text;
	}

	public void unfocus(boolean remove) {
		/// Unfocused
		if (remove)
			text = preText;
		if (specialUnfocused != null)
			specialUnfocused.run();
		cursor = "";
		focused = false;
		if (window != null)
			window.focus = false;
	}

	public void focus(boolean remove) {
		// Focused
		if (!focused) {
			if ((removeAtFirstTime && firstTimeFocused) || remove) {
				text = "";
				firstTimeFocused = false;
			}
			cursor = "|";
			focused = true;
			if (window != null)
				window.focus = true;
		}
	}

	public UIWindowInfo getWindow() {
		return window;
	}

}
