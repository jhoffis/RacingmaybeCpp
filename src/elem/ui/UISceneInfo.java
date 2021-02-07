package elem.ui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.Nuklear;

import scenes.Scenes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

public class UISceneInfo {

	// Lists of buttons to press and unpress
	private final static HashMap<Integer, ArrayList<IUIPressable>> scenePressables = new HashMap<>();
	
	// Holds a reference to one button per scene. May be null ( nothing hovered ).
	private final static HashMap<Integer, UIButton> hoveredButtons = new HashMap<>();
	
	// scenename, list of windows, given integer when adding into here.
	private final static HashMap<Integer, ArrayList<UIWindowInfo>> uiWindowMap = new HashMap<>();

	private final static ArrayList<UIFont> uiFonts = new ArrayList<>();
	private static int genName;

	/*
	 * HOVERED BUTTON
	 */
	
	/**
	 * Adds object like normal, but since it is a button it designates extra
	 * actions for changes in hoverstate
	 */
	public static void addPressableToScene(int sceneIndex, IUIPressable pressable) {
		try {
			validateSceneIndex(sceneIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(pressable.getClass().equals(UIButton.class))
			setChangeHoveredButtonAction(sceneIndex, (UIButton) pressable);

		ArrayList<IUIPressable> pressableList;
		if (!scenePressables.containsKey(sceneIndex)) {
			pressableList = new ArrayList<>();
			scenePressables.put(sceneIndex, pressableList);
		} else {
			pressableList = scenePressables.get(sceneIndex);
		}
		pressableList.add(pressable);
	}

	public static ArrayList<IUIPressable> getScenePressables(int sceneIndex) {
		if (!scenePressables.containsKey(sceneIndex))
			scenePressables.put(sceneIndex, new ArrayList<>());
		return scenePressables.get(sceneIndex);
	}

	public static void setChangeHoveredButtonAction(int sceneIndex, UIButton button) {
		if (button.hasChangeHoverButtonAction())
			return;

		button.setChangeHoverButtonAction(() -> {
			if (hoveredButtons.containsKey(sceneIndex)) {
				UIButton lastButton = hoveredButtons.get(sceneIndex);

				// Run actions
				if(lastButton != null) {
					lastButton.unhover();

					hoveredButtons.remove(sceneIndex);
				}
			}

			hoveredButtons.put(sceneIndex, button);
		});
	}

	public static UIButton getHoveredButton(int sceneIndex) {
		return hoveredButtons.get(sceneIndex);
	}

	public static void clearHoveredButton(int sceneIndex) {
		UIButton prevHoveredButton = getHoveredButton(sceneIndex);
		if(prevHoveredButton != null) {
			prevHoveredButton.unhover();
			hoveredButtons.put(sceneIndex, null);
		}
	}

	/*
	 * WINDOWS
	 */
	public static void addWindowToScene(int sceneIndex, UIWindowInfo window) {
		try {
			validateSceneIndex(sceneIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		ArrayList<UIWindowInfo> windowList;
		
		if (!uiWindowMap.containsKey(sceneIndex)) {
			windowList = new ArrayList<>();
			uiWindowMap.put(sceneIndex, windowList);
		} else {
			windowList = uiWindowMap.get(sceneIndex);
		}
		
		windowList.add(window);
	}
	
	/**
	 * Adds a focusable window if the object does not already exist.
	 * @throws Exception if unvalid scene id
	 */
	private static UIWindowInfo createWindowInfo(int sceneIndex, String windowName, int options, double x, double y, double width, double height) {
		UIWindowInfo window = new UIWindowInfo(windowName, options, (float) x, (float) y, (float) width, (float) height);
		addWindowToScene(sceneIndex, window);
		return window;
	}

	public static UIWindowInfo createWindowInfo(int sceneIndex, double x, double y, double w, double h) {
		var res = createWindowInfo(sceneIndex, sceneIndex + "genName" + genName, UIWindowInfo.OPTIONS_STANDARD, x, y, w, h);
		genName++;
		return res;
	}

	
	public static void removeWindowInfoReference(int sceneIndex, UIWindowInfo window) {
		try {
			validateSceneIndex(sceneIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(uiWindowMap.containsKey(sceneIndex))
			uiWindowMap.get(sceneIndex).remove(window);
	}
	
	public static void decideFocusedWindow(long window) {
		double[] xpos = new double[1];
		double[] ypos = new double[1];
		GLFW.glfwGetCursorPos(window, xpos, ypos);
		decideFocusedWindow((float) xpos[0], (float) ypos[0]);
	}

	public static void decideFocusedWindow(float x, float y) {
		var windowStack = new Stack<UIWindowInfo>();
		var z = 0;
		var allWindowsToCheck = new ArrayList<>(uiWindowMap.get(Scenes.GENERAL_NONSCENE));
		if(uiWindowMap.containsKey(Scenes.CURRENT))
			allWindowsToCheck.addAll(uiWindowMap.get(Scenes.CURRENT));

		for (UIWindowInfo window : allWindowsToCheck) {
			if (window.visible) {
				if (window.z > z) {

					// clear out lower windows
					while (!windowStack.isEmpty()) {
						var badWindow = windowStack.pop();
						badWindow.focus = false;
					}

					z = window.z;
					windowStack.push(window);
					continue;
				} else if (window.z == z) {
					windowStack.push(window);
					continue;
				}
			}

			window.focus = false;
		}
		
		while(!windowStack.isEmpty()) {
			var window = windowStack.pop();
//			if(
					window.setInFocus(x, y);
//					)
//				System.out.println("Window focused " + window.getName());
		}
//		System.out.println("");
	}
	

	public static void updateResolution() {
		for (Entry<Integer, ArrayList<UIWindowInfo>> windowList : uiWindowMap.entrySet()) {
			for(UIWindowInfo window : windowList.getValue()) {
				window.updateResolution();
			}
		}
		for(UIFont font : uiFonts) {
			font.updateResizeFont();
		}
	}

	private static void validateSceneIndex(int sceneIndex) throws Exception {
		if(sceneIndex < 0 || sceneIndex >= Scenes.AMOUNT) 
			throw new Exception("TRIED TO ADD UIWINDOW WITH WRONG SCENEINDEX with " + sceneIndex);
	}

	public static void pushFont(UIFont uiFont) {
		uiFonts.add(uiFont);
	}

}