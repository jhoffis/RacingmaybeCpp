package engine.io;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.nglfwGetClipboardString;
import static org.lwjgl.nuklear.Nuklear.NK_BUTTON_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_BUTTON_MIDDLE;
import static org.lwjgl.nuklear.Nuklear.NK_BUTTON_RIGHT;
import static org.lwjgl.nuklear.Nuklear.nk_input_button;
import static org.lwjgl.nuklear.Nuklear.nk_input_motion;
import static org.lwjgl.nuklear.Nuklear.nk_input_unicode;
import static org.lwjgl.nuklear.Nuklear.nnk_strlen;
import static org.lwjgl.nuklear.Nuklear.nnk_textedit_paste;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memCopy;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import scenes.adt.ISceneManipulation;
import settings_and_logging.ControlsSettings;

public class InputHandler {

	public static int MOUSEBUTTON, MOUSEACTION;
	
	private float x, y;
	private ISceneManipulation currentScene;
	private ControlsSettings keys;

	public InputHandler(Window win, NkContext ctx) {
		keys = new ControlsSettings();

		long myWindow = win.getWindow();

//		glfwSetCursorEnterCallback(myWindow,
//				GLFWCursorEnterCallback.create((window, entered) -> {
//					this.currentScene.mouseEnterWindowInput(entered);
//				}));

		glfwSetKeyCallback(myWindow, GLFWKeyCallback
				.create((window, key, scancode, action, mods) -> 
					this.currentScene.keyInput(key, action)
				));

		glfwSetMouseButtonCallback(myWindow, GLFWMouseButtonCallback
				.create((window, button, action, mods) -> {
					MOUSEBUTTON = button;
					MOUSEACTION = action;
					this.currentScene.mouseButtonInput(button, action, x, y);
					try (MemoryStack stack = stackPush()) {
						DoubleBuffer cx = stack.mallocDouble(1);
						DoubleBuffer cy = stack.mallocDouble(1);

						glfwGetCursorPos(window, cx, cy);

						int x = (int) cx.get(0);
						int y = (int) cy.get(0);

						int nkButton;
						switch (button) {
							case GLFW_MOUSE_BUTTON_RIGHT :
								nkButton = NK_BUTTON_RIGHT;
								break;
							case GLFW_MOUSE_BUTTON_MIDDLE :
								nkButton = NK_BUTTON_MIDDLE;
								break;
							default :
								nkButton = NK_BUTTON_LEFT;
						}
						nk_input_button(ctx, nkButton, x, y,
								action != GLFW.GLFW_RELEASE);
					}
				}));

		glfwSetCursorPosCallback(myWindow,
				GLFWCursorPosCallback.create((window, xpos, ypos) -> {

					nk_input_motion(ctx, (int) xpos, (int) ypos);

					x = (float) xpos;
					y = (float) ypos;
					this.currentScene.mousePosInput(x, y);
				}));

		GLFW.glfwSetScrollCallback(myWindow,
				GLFWScrollCallback.create((window, xoffset, yoffset) -> {
					
					float x = (float) xoffset;
					float y = (float) yoffset;
					
					this.currentScene.mouseScrollInput(x, y);
				}));

		glfwSetCharCallback(myWindow,
				(window, codepoint) -> nk_input_unicode(ctx, codepoint));

		ctx.clip().copy((handle, text, len) -> {
			if (len == 0) {
				return;
			}

			try (MemoryStack stack = stackPush()) {
				ByteBuffer str = stack.malloc(len + 1);
				memCopy(text, memAddress(str), len);
				str.put(len, (byte) 0);

				glfwSetClipboardString(myWindow, str);
			}
		}).paste((handle, edit) -> {
			long text = nglfwGetClipboardString(myWindow);
			if (text != NULL) {
				nnk_textedit_paste(edit, text, nnk_strlen(text));
			}
		});

	}

	public void destroy(long win) {
		glfwFreeCallbacks(win);
	}

	public void setCurrent(ISceneManipulation scene) {
		currentScene = scene;
	}

	public ControlsSettings getKeys() {
		return keys;
	}
}
