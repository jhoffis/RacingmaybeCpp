package elem.interactions;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import adt.ICloneStringable;
import elem.objects.Sprite;
import elem.ui.IUIPressable;
import elem.ui.UIFont;
import elem.upgrades.ICheckPositionAction;
import elem.upgrades.Upgrades;
import engine.graphics.Renderer;
import engine.io.InputHandler;
import engine.io.Window;
import engine.math.Vec2;
import player_local.Bank;

public abstract class Tile implements IUIPressable , ICloneStringable  {
	
	public final static String tileUpgradeId = "0";

	public boolean placed;
	// x og y er basert p� normal posisjon, men diffx og y flytter den litt og.
	protected float diffX, diffY, diffXBased, diffYBased;
	protected boolean pressable, mouseDown, mouseAbove;
	protected Sprite normalSprite;
	protected IAction pressedAction, pressedActionRight, hoveredExitAction, hoveredAction;
	protected ICheckPositionAction moveTileBuyAction, mouseAboveAction, uiUpdateAction;
	protected Consumer<IUIPressable> pressedActionSelf;

	public Tile() {}
	
	public Tile(float diffX, float diffY, Tile other) {
		setPos(diffX, diffY);
		
		this.normalSprite = other.getNormalSprite();
	}
	
	public void render(Renderer renderer) {
		
		float typeScheme = 0;
		
		// hover or pressed
		if (mouseDown && mouseAbove)
			typeScheme = 1;
		else if (mouseAbove)
			typeScheme = 2;
		
		normalSprite.getShader().setUniform("mouseTypeScheme", typeScheme);
		normalSprite.setPositionX(diffX);
		normalSprite.setPositionY(diffY);
		renderer.renderOrthoMesh(normalSprite);
	}
	
	public void renderSelected(Renderer renderer, Sprite selected, boolean pressedTile) {
		selected.getShader().setUniform("mouseTypeScheme", pressedTile ? 1f : 0f);
		selected.setPositionX(diffX);
		selected.setPositionY(diffY);
		renderer.renderOrthoMesh(selected);
	}

	public void renderUILayout(NkContext ctx, MemoryStack stack, Bank bank, UIFont immediateFont, UIFont saleFont) { }

	public boolean mousePosInput(float x, float y) {
		boolean prevMouseAbove = mouseAbove;
		Vec2 tilePos = new Vec2(diffX, diffY);
		mouseAbove = normalSprite.above(tilePos, x, y);
		
		if(!prevMouseAbove && mouseAbove) {
			runHoveredAction();
			if (mouseAboveAction != null) {
				mouseAboveAction.check(this, tilePos);
			}
		} else if (prevMouseAbove && !mouseAbove) {
			if (hoveredExitAction != null) {
				hoveredExitAction.run();
			}
		}
		return prevMouseAbove != mouseAbove;
	}
	
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		mouseDown = action != GLFW.GLFW_RELEASE;
		
//		System.out.println(x + ", " + y + " - " + diffX + ", " + diffY);
		
		if(!mouseDown && pressable && mouseAbove) {
			press();
			runPressedAction();
		} else if (mouseDown) {
			release();
		}
		
		return mouseAbove;
	}

	@Override
	public void press() {
		pressable = false;
	}

	@Override
	public void release() {
		pressable = true;
	}
	
	public void runPressedAction() {
		runPressedAction(GLFW.GLFW_MOUSE_BUTTON_LEFT);
	}
	
	private boolean runPressedAction(int button) {
		boolean res = false;

		if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			if (pressedAction != null) {
				pressedAction.run();
				res = true;
			}
			
			if (pressedActionSelf != null) {
				pressedActionSelf.accept(this);
				res = true;
			}
			
		} else if (pressedActionRight != null && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && InputHandler.MOUSEACTION == GLFW.GLFW_RELEASE) {
			pressedActionRight.run();
			res = true;
		}
		
		return res;
	}
	
	public void runHoveredAction() {
		// Play hover sfx
		if (hoveredAction != null) {
			hoveredAction.run();
		}
	}
	
	public void setHoverAction(IAction action) {
		this.hoveredAction = action;
	}

	public IAction getHoverAction() {
		return hoveredAction;
	}

	public void setHoverExitAction(IAction action) {
		this.hoveredExitAction = action;
	}
	
	public void setPressedAction(IAction action) {
		this.pressedAction = action;
	}
	
	public void setPressedAction(Consumer<IUIPressable> action) {
		this.pressedActionSelf = action;
	}
	
	public void setPressedActionRight(IAction action) {
		this.pressedActionRight = action;
	}

	public void setMouseAboveAction(ICheckPositionAction mouseAboveAction) {
		this.mouseAboveAction = mouseAboveAction;
	}

	public void setMovedAction(ICheckPositionAction action) {
		this.moveTileBuyAction = action;
	}
	
	public void setUpdateUI(ICheckPositionAction uiUpdate) {
		this.uiUpdateAction = uiUpdate;
	}
	
	public Sprite getNormalSprite() {
		return normalSprite;
	}

	public boolean isHovered() {
		return mouseAbove;
	}
	
	public void updateResolution() {
		diffX = Window.WIDTH * diffXBased;
		diffY = Window.HEIGHT * diffYBased;
	}

	public void setPos(float x, float y) {
		diffX = x;
		diffY = y;
		
		diffXBased = diffX / Window.WIDTH;
		diffYBased = diffY / Window.HEIGHT;
	}
	
	public void setPosY(float y) {
		diffY = y;
		diffYBased = diffY / Window.HEIGHT;
	}

	@Override
	public Tile clone() {
		return this;
	}

	public abstract String getTileTypeId();

}
