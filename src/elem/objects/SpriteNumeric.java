package elem.objects;

import engine.graphics.Renderer;
import engine.math.Vec2;

public class SpriteNumeric extends GameObject {

	private final Sprite[] numbers;
	
	// for later creation
	private Vec2 topleft;
	
	// what is shown
	private int number;
	private boolean leanRight;
	private Sprite[] representation;
	
	public SpriteNumeric(Vec2 topleft, float height, int number, boolean leanRight) {
		this.topleft = topleft;
		this.leanRight = leanRight;
		
		numbers = new Sprite[10];
		
		for (int i = 0; i < 10; i++) {
			numbers[i] = new Sprite(
					new Vec2(0),
					height, "./images/numbers/" + i + ".png", "main");
			numbers[i].create();
		}
		
		setNumber(number);
	}
	
	public void render(Renderer renderer) {
		if(representation == null)
			return;
		
		for (int i = 0; i < representation.length; i++) {
			int a = leanRight ? representation.length - (1 + i) : i;
			if (representation[a] == null)
				continue;
			representation[a].setPosition(new Vec2(topleft.x - (i != 0 ? (representation[i - 1].getWidth() * 1.1f) * i : 0), topleft.y));
			renderer.renderOrthoMesh(representation[a]);
		}
	}
	
	// create the sprites
	public void setNumber(int number) {
		if(number == this.number)
			return;
		
		this.number = number;
		
		String stringRep = String.valueOf(number);
		int numOfChars = stringRep.length();
		representation = new Sprite[numOfChars];
		for (int i = 0; i < numOfChars; i++) {
			int numberIndex = Integer.valueOf(stringRep.charAt(i) - 48);
			if (numberIndex >= 0)
				representation[i] = numbers[numberIndex];
		}
	}
	
	public void destroy() {
		if(numbers != null) {
			for (Sprite s : numbers) {
				s.destroy();
			}
		}
	}

	public float getWidth() {
		return numbers[0].getWidth();
	}
 }