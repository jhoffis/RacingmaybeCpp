package elem.interactions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import engine.math.Vec2;
import org.lwjgl.nuklear.Nuklear;

import audio.AudioRemote;
import audio.SfxTypes;
import elem.CursorType;
import elem.objects.Sprite;
import elem.ui.UILabel;
import engine.graphics.Renderer;
import engine.io.Window;
import main.Features;
import player_local.Car.Car;
import player_local.Car.Rep;

public class Gearbox {

	private Car car;
	private Features features;
	private AudioRemote audio;
	private Sprite gearbox;
	private Sprite gearboxLever;
	// all from whatever position of the gearbox.
	private float fromX, yNeutral, xUnit, yUnit;
	private float leverXTarget, leverYTarget;
	private int gearColumn, gearSlots, held;
	
	private float xMouse, yMouse, xMousePrev, yMousePrev;
	private boolean playedWhoosh;
	
	private boolean doTimeShifted;
	private long fromTimeShifted;
	private LinkedList<String> timeShifted;
	private Queue<Long> timeShiftedAtTime;
	
	private long fromTimePowerloss;
	private String powerlossText;
	private boolean releaseThrottle;

	public Gearbox(Car car, Features features, AudioRemote audio) {
		this.car = car;
		this.features = features;
		this.audio = audio;
		
		timeShifted = new LinkedList<>();
		timeShiftedAtTime = new LinkedList<>();
		gearSlots = -69;
		resetAndUpdateGearTop();
	}
	
	public void resetAndUpdateGearTop() {
		int newGearTop = (int) (car.getRep().is(Rep.sequential) ? 2 : car.getRep().getInt(Rep.gearTop));

		if(newGearTop != gearSlots) {
			float lastGBPosX = -1; 
			if (gearbox != null)
				lastGBPosX = gearbox.position().x;
			setGearTop(newGearTop);
			if(newGearTop == 2) {
				if (lastGBPosX != -1) {
					float thisGBPosX = gearbox.position().x;
					fromX = Math.abs(thisGBPosX - lastGBPosX);
				} else {
					fromX = 0;
				}
			}
		}

		timeShifted.clear();
		timeShiftedAtTime.clear();
		held = 0;
		xMouse = 0;
		yMouse = 0; 
		xMousePrev = 0;
		yMousePrev = 0;
		leverXTarget = 0;
		leverYTarget = 0;
	}
	
	private void setGearTop (int gears) {
		boolean alreadySet = gearbox != null; 
		if(alreadySet)
			gearbox.destroy();
		
		this.gearSlots = gears;
//		System.out.println("gears: " + gears);
		gearbox = new Sprite(Window.HEIGHT / 3f, "./images/gearbox" + gears + ".png", "main");
		gearbox.create();
		gearbox.setPosition(new Vec2(Window.WIDTH - gearbox.getWidth(), gearbox.getHeight() / 2));
//		System.out.println("gearposx " + gearbox.position().x + ", " + (Window.WIDTH - gearbox.getWidth()));
		if(!alreadySet) {
			float size = gearbox.getHeight() / (53f / 11f);
			gearboxLever = new Sprite(new Vec2(gearbox.position().x, this.gearbox.position().y), size, "./images/gearboxlever.png", "main");
			gearboxLever.create();
		}
		updateResolution();
	}
	
	public void updateResolution() {
		yNeutral = gearbox.position().y + gearbox.getHeight() / 2;
		xUnit = gearboxLever.getWidth() * 13 / 11;
		yUnit = gearboxLever.getHeight() * 7 / 11;
	}
	
	// Used with sequential shifting
	public void press(float x, float y) {
		if(held == 0) {				
			
			xMouse = x;
			yMouse = y;
			xMousePrev = xMouse;
			yMousePrev = yMouse;
			
			if (gearSlots > 2) {
				if (gearboxLever.above(x, y)) {
					held = 1;
					// change mouse
					features.getWindow().setCursor(CursorType.cursorIsHold);
				} 
			} else {
				if (gearbox.above(x, y)) {
					features.getWindow().setCursor(CursorType.cursorIsPoint);
					
					if (y < yNeutral) {
						held = 1;
						car.shiftDown(1);
					} else {
						held = 2;
						car.shiftUp(1);
					}
				}
			}
		}
	}

	public void release(float x, float y) {
		held = 0;
		
		if (gearSlots > 2) {
			if (gearboxLever.above(x, y)) {
				features.getWindow().setCursor(CursorType.cursorCanHold);
			} 
		} else {
			if (gearbox.above(x, y)) {
				features.getWindow().setCursor(CursorType.cursorCanPoint);
			}
		}
		
	}

	public void move(float x, float y) {
//		leverXTarget, leverYTarget
		if (gearSlots > 2) {
			if (held != 0) {
				float sizeMoveSpace = gearboxLever.getHeight() / 2;
				
				if(y > yNeutral - sizeMoveSpace && y < yNeutral + sizeMoveSpace) {
					// if inside of neutral area, smooth else lock the x on intervals
					leverXTarget = x - gearbox.position().x - gearboxLever.getWidth() / 2;
					gearColumn = 0;
				} else {
					// if x is within a space, allow for smooth movement in y.
					leverYTarget = y - gearbox.position().y - gearboxLever.getHeight() / 2;
					// what big x space is closest?
					float gearspace = gearbox.getWidth() / 3;
					gearColumn = 0;
					int gearColumnAmount = (gearSlots + gearSlots % 2) / 2;
					for (int i = 1; i < gearColumnAmount; i++) {
						if(x < gearbox.position().x + gearspace * i) {
							// left
							gearColumn = i;
							break;
						}
					}
					if(gearColumn == 0)
						gearColumn = gearColumnAmount;
				}
	//			System.out.println("y:" + y + " x: " + leverXTarget + ", " + yNeutral + ", " + sizeMoveSpace);
			} else if(gearboxLever.above(x, y)) {
				// change mouse grabby
				features.getWindow().setCursor(CursorType.cursorCanHold);
			} else {
				// change mouse pointy or normal
				features.getWindow().setCursor(CursorType.cursorNormal);
			}
		} else if (gearbox.above(x, y)) {
			features.getWindow().setCursor(CursorType.cursorCanPoint);
		} else {
			// change mouse pointy or normal
			features.getWindow().setCursor(CursorType.cursorNormal);
		}
		
		xMousePrev = xMouse;
		yMousePrev = yMouse;
		if (held != 0) {
			xMouse = x;
			yMouse = y;
		}
	}
	
	public void tick(float tickFactor) {
		// check for shift resistance which slows down movement of the lever. So it goes towards a point slowly in tick
		
		float x = 0;
		float y = 0;

		// Checks if your car does not have sequentialshift
		if (gearSlots > 2) {
			int gear = car.getStats().gear;
			int typeShift = 0;
			
			// up down
			// gearcolumn = 0 means that youre in neutral
			if(held == 0 && gear > 0 || held != 0 && gearColumn > 0) {
				if(held == 0) {
					// if you are not holding the lever move the lever to the top and into its slot
					gearColumn = (gear + 1) / 2;
					y = gear % 2 == 1 ? yUnit : gearbox.getHeight() - yUnit - gearboxLever.getHeight();
				} else {
					// holds down and is not in neutral. Smooth drag up and down.
					y = leverYTarget;
					
					float bottomLimit = 0;
					if (gearColumn * 2 <= gearSlots)
						bottomLimit = gearbox.getHeight() - yUnit - gearboxLever.getHeight();
					else
						bottomLimit = gearbox.getHeight() / 2 - gearboxLever.getHeight() / 2;
					
					float joinHeight = gearboxLever.getHeight() / 2;
					int top = -1;
					
					if (y < yUnit + joinHeight) {
						// top
						top = 1;
						y = yUnit;
					} else if (y > bottomLimit - joinHeight) {
						// bottom
						top = 0;
						y = bottomLimit;
					}
					
					if (top != -1)
						typeShift = car.shift(gearColumn * 2 - top, tickFactor);
				}
				
				x = gearColumn * gearboxLever.getWidth() + (gearColumn - 1) * xUnit;
			}
			else {
				//Is in neutral. Drag the lever smooth right to left
				if(held == 0)
					x = gearbox.getWidth() / 2 - gearboxLever.getWidth() / 2;
				else {
					x = leverXTarget;
					float leftLimit = gearboxLever.getWidth();
					float rightLimit = gearbox.getWidth() - gearboxLever.getWidth() * 2;
					if (x < leftLimit)
						x = leftLimit;
					else if (x > rightLimit)
						x = rightLimit;
				}
				typeShift = car.shift(0, tickFactor);
				y = gearbox.getHeight() / 2 - gearboxLever.getHeight() / 2;
			}
			
			
//			if(typeShift != 0)
//				System.out.println("typeShift " + typeShift);
			long now = System.currentTimeMillis();
			
			if (typeShift == 2 && doTimeShifted) {
				timeShifted.add("Time: " + (int) (now - fromTimeShifted) + "ms ");
				timeShiftedAtTime.add(now);
				doTimeShifted = false;
			} else if (typeShift == 1 && !doTimeShifted) {
				fromTimeShifted = now;
				doTimeShifted = true;
			}
			if (typeShift == -1) {
				releaseThrottle = true;
			} else {
				releaseThrottle = false;
			}
			
		} else {

			x = gearboxLever.getWidth();
			
			switch (held) {
				case 0:
					y = gearbox.getHeight() / 2 - gearboxLever.getHeight() / 2;
					break;
				case 1:
					y = yUnit;
					break;
				case 2:
					y = gearbox.getHeight() - gearboxLever.getHeight() - yUnit;
					break;
			}
		}
		
		if(held != 0) {
			float distanceMove = 0;
			Vec2 gbPos = gearbox.position();
			if((xMouse >= gbPos.x || xMousePrev >=gbPos.x) && (xMouse <= gbPos.x + gearbox.getWidth () || xMousePrev <= gbPos.x + gearbox.getWidth () )
			&& (yMouse >= gbPos.y || yMousePrev >=gbPos.y) && (yMouse <= gbPos.y + gearbox.getHeight() || yMousePrev <= gbPos.y + gearbox.getHeight() )) {
				distanceMove = Math.abs(xMouse - xMousePrev);				
				if (Math.abs(yMouse - yMousePrev) > distanceMove)
					distanceMove = Math.abs(yMouse - yMousePrev);
				distanceMove *= tickFactor;
			
			}
			if(!playedWhoosh && distanceMove > 50) {
				audio.get(SfxTypes.WHOOSH).play();
				playedWhoosh = true;
			} else if (playedWhoosh && distanceMove < 4) {
//				audio.get(SfxTypes.WHOOSH).stop();
				playedWhoosh = false;
			}
		}
		
		gearboxLever.setPositionX(fromX + x);
		gearboxLever.setPositionY(y);
	}
	
	public void updatePowerloss(long now) {
		powerlossText = "RPM: -" + car.calcPowerloss() + "% ";
		timeShifted.add(powerlossText);
		timeShiftedAtTime.add(now);
	}

	public void render(Renderer renderer) {
		renderer.renderOrthoMesh(gearbox);
		renderer.renderOrthoMesh(gearboxLever);
	}
	
	public UILabel[] getTimeShifted() {
		if(!timeShiftedAtTime.isEmpty() && System.currentTimeMillis() > timeShiftedAtTime.peek() + 2000) {
			timeShiftedAtTime.poll();
			timeShifted.poll();
		}
		
		int i = (releaseThrottle ? 1 : 0);
		UILabel[] res = new UILabel[timeShifted.size() + i];

		if (releaseThrottle)
			res[0] = new UILabel("Release throttle! #R", Nuklear.NK_TEXT_ALIGN_RIGHT | Nuklear.NK_TEXT_ALIGN_MIDDLE);
		
		Iterator<String> it = timeShifted.descendingIterator();
		
		while (it.hasNext()) {
			res[i] = new UILabel(it.next(), Nuklear.NK_TEXT_ALIGN_RIGHT | Nuklear.NK_TEXT_ALIGN_MIDDLE);
			i++;
		}
		
		return res;
	}

	public Sprite getGearbox() {
		return gearbox;
	}

	public Sprite getGearboxLever() {
		return gearboxLever;
	}
	
	public String getPowerloss() {
		if(System.currentTimeMillis() <= fromTimePowerloss && powerlossText != null)
			return powerlossText;
		return "";
	}

	public void destroy() {
		gearbox.destroy();
		gearboxLever.destroy();
	}
	
}