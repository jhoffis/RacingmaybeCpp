package scenes.game.racing_subscenes;

import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_style_push_vec2;

import java.awt.Color;

import engine.math.Vec2;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryStack;

import audio.AudioRemote;
import communication.Communicator;
import elem.Animation;
import elem.Font;
import elem.interactions.Gearbox;
import elem.objects.Camera;
import elem.objects.GameObject;
import elem.objects.Sprite;
import elem.objects.SpriteNumeric;
import elem.ui.UIFont;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import elem.ui.UIScrollable;
import elem.ui.UIWindowInfo;
import engine.graphics.Renderer;
import engine.graphics.Texture;
import engine.io.Window;
import engine.math.Vec3;
import main.Features;
import main.ResourceHandler;
import player_local.Player;
import player_local.Car.Car;
import player_local.Car.Rep;
import scenes.Scenes;
import scenes.adt.Visual;
import scenes.game.Race;

public class RaceVisual extends Visual {

	private final int whiteBound = 20000;
	
	private Race race;
	private Camera perspCamera;
	private Car opponent;
	private Sprite goal;
	private Sprite myCarBase;
	private Texture myCarLighting;
	private Vec2 baseMyCarPos;

	private Vec3 tintColor;
	private float tintAlpha;
	private Animation background;
	private Animation nitros;
	private Sprite fastness;

	// top left and right info
	private UIFont infoFont;
	private UILabel currentDistance;
	private UILabel lapsedDistance;
	private UILabel lapsedTime;
	private UILabel extraGamemodeInfo;
	private float infoSpacingY;
	private float infoPaddingX, infoPaddingY;
	private float infoRowHeight;

	// bottom right info
	private SpriteNumeric tachoSpeed;
	private SpriteNumeric tachoGear;
	private SpriteNumeric turboBlow;
	
	private Sprite tachoBase;
	private Sprite tachoPointer;
	private Sprite tachoDotSmall, tachoDotLarge, tachoDotRedline;
	private Vec2 tachoPointerOrigo;
	private Vec2 tachoPointerProperPosition;
	
	private Sprite turbo;
	private Vec2 turboPointerProperPosition;
	
	
	
	private Gearbox gearbox;
	private UIScrollable gearboxTimeShift;

	// middle info
	private int ballcount;
	private Sprite racelight; // multiple
	private Sprite tireboost;
	private UIWindowInfo tireboostWindow;
	private UILabel tireboostInfoLabel;

	// bottom left info
	private Sprite nosbottle; // multiple
	private Vec3 nosStrengthColor;
	private float blurShake;
	private float frameScale;
	private boolean tireboostInfoShow;
	private long tireboostInfoShowTime;
	private float templight = 10;
	private float turboPointerScaleup;
	private float turboPointerScaledown;
	private float whiteify;
	
	public RaceVisual(Features features, Race race) {
		super(features);
		this.race = race;

		perspCamera = new Camera();

		background = new Animation("road", "background", 6, 0, Window.HEIGHT);
		nitros = new Animation("nitros", "main", 4, 0, Window.HEIGHT);
		
		currentDistance = new UILabel();
		lapsedDistance = new UILabel();
		lapsedTime = new UILabel();
		lapsedTime.setOptions(Nuklear.NK_TEXT_ALIGN_RIGHT | Nuklear.NK_TEXT_ALIGN_MIDDLE);
		extraGamemodeInfo = new UILabel();
		extraGamemodeInfo.setOptions(Nuklear.NK_TEXT_ALIGN_RIGHT | Nuklear.NK_TEXT_ALIGN_MIDDLE);

		blurShake = 3.0f;
		
		tireboostInfoLabel = new UILabel("");
		tireboostInfoLabel.setOptions(Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_MIDDLE);
		
		ResourceHandler.LoadSprite("./images/fastness.png", "fastness", (sprite) -> fastness = sprite);

		final float raceLightSize = Window.HEIGHT / 5.5f;
		ResourceHandler.LoadSprite(new Vec2(Window.WIDTH / 2 - raceLightSize / 2, raceLightSize), raceLightSize, "./images/racelight.png", "racelight", (sprite) -> {
			racelight = sprite;
	
			float tbSize = raceLightSize * 0.4f;
			ResourceHandler.LoadSprite(new Vec2(0, racelight.position().y + racelight.getHeight()), tbSize, "./images/tireboost.png", "tireboost", (sprite2) -> {
				tireboost = sprite2;
				
				tireboost.setPositionX(Window.WIDTH / 2 - tireboost.getWidth() / 2);
				float tbWindowWidth = tireboost.getWidth();
				tireboostWindow = UISceneInfo.createWindowInfo(Scenes.RACE, 
						Window.WIDTH / 2 - tbWindowWidth / 2, tireboost.position().y + tireboost.getHeight(), tbWindowWidth, tireboost.getHeight());
			});
		});
		// have to place nos bottle every time as it is based on camera pos.
		float size = Window.HEIGHT / 4;
		ResourceHandler.LoadSprite(new Vec2(0, Window.HEIGHT - size * 1.1f), size, "./images/nosbottle.png", "nosbottle", (sprite) -> nosbottle = sprite);
		
		ResourceHandler.LoadSprite(Window.HEIGHT / 2.5f, "./images/tachometer.png", "tachometer", (sprite) -> {
			tachoBase = sprite;
			
			tachoBase.setPositionX(Window.WIDTH - tachoBase.getWidth());
			tachoBase.setPositionY(Window.HEIGHT - tachoBase.getHeight());
			
			ResourceHandler.LoadSprite(tachoBase.getHeight() / 10.1f, "./images/tachometerPointer.png", "tachometer", (s) -> tachoPointer = s);
			
			// dots
			Vec2 pos = new Vec2((float) Window.WIDTH / 2f - tachoBase.getHeight() / 2.7f, (float) Window.HEIGHT / 2f);
			ResourceHandler.LoadSprite(pos, tachoBase.getHeight() / 85.33f, "./images/tachometerDotSmall.png", "main", (s) -> tachoDotSmall = s);
			ResourceHandler.LoadSprite(pos, tachoBase.getHeight() / 42.67f, "./images/tachometerDotLarge.png", "main", (s) -> tachoDotLarge = s);
			ResourceHandler.LoadSprite(pos, tachoBase.getHeight() / 32f, "./images/tachometerDotRedline.png", "main", (s) -> tachoDotRedline = s);
		});
		ResourceHandler.LoadSprite(Window.HEIGHT / 4.2f, "./images/turbometer.png", "tachometer", (sprite) -> turbo = sprite);
		float excessSize = Window.HEIGHT;
		
		ResourceHandler.LoadSprite(new Vec2(-excessSize * 16f / 9f, -excessSize), Window.HEIGHT + excessSize, "./images/goal.png", "goal", (sprite) -> {
			goal = sprite;
			
			goal.setRotation(new Vec3(0, 94f, 0)); // + (4f * ((float) Window.HEIGHT / (float) Window.WIDTH) * 16f / 9f), 0));
			goal.setPositionXReal(2);//* -1.5f); // - 150%
		});
		
//		goal.setPositionYReal(goal.getHeightReal() / 2);

		// tachometerPointer.setPositionX(tachometerPointX + distanceX / 1.6f -
		// tachometerPointerRotationX);
//		tachometerPointer.setPositionX(tachometerPointX + distanceX / 1.6f);
//		tachometerPointer.setPositionY(tachometerPointY - distanceY / 2);
		// tachometerBase.scale(8f);
		// tachometerBase.setPositionX(orthoCamera.getRight() / 10);
		// tachometerBase.setPositionY(10f);
		// tachometerBase.setPositionX(1.5f);
		// tachometerBase.setPositionX(orthoCamera.getRight() -
		// tachometerBase.getScale().x());

		
		infoFont = new UIFont(Font.REGULAR, Window.HEIGHT / 38);

	}
	
	public void initRest(Player myPlayer, AudioRemote audio) {
		this.player = myPlayer;
		if(myCarBase != null) {
			myCarBase.destroy();
			myCarLighting.destroy();
			gearbox.destroy();
		}
		// TODO flytt disse ut siden de tar veldig lite plass i minnet. Bare noen kb.
		String carname = player.getCar().getRep().getName();
		myCarBase = new Sprite(Window.HEIGHT,"./images/" + carname + ".png", "carinterior");
		myCarBase.create();
		myCarLighting = new Texture("./images/" + carname + "Light.png");
		myCarLighting.create();
		baseMyCarPos = myCarBase.position();
		gearbox = new Gearbox(player.getCar(), features, audio);
		
		Sprite gearboxSprite = this.gearbox.getGearbox();
		float height = gearboxSprite.getHeight(), width = height;
		gearboxTimeShift = new UIScrollable(Scenes.RACE, 
				(float) gearboxSprite.position().x - width, (float) gearboxSprite.position().y, width, height);
		gearboxTimeShift.setScrollable(false);
		
	}

	public void initBeforeNewRace(Car opponent) {

		float nosStrengthBased = (float) player.getCarRep().get(Rep.nos) / 12f;
		if (nosStrengthBased > 1)
			nosStrengthBased = 1;

		nosStrengthColor = new Vec3(Color.getHSBColor(nosStrengthBased, 1, 1));

		// TODO perhaps create the camera and update whenever you're told to
		// update,
		// instead of every time.

		perspCamera.setRotation(new Vec3(0, -90f, 0));
		if (opponent != null) {
			this.opponent = opponent;
			opponent.getModel().setPositionSide(4.5f);
			opponent.getModel().setRotation(183.5);
			opponent.getModel().setRotationZ(-0.7);
		}
		if (gearbox != null)
			gearbox.resetAndUpdateGearTop();
		
		// TODO find the sprites and the model for your car and the model for
		// the opponent
		// You only show the model of the other car and your model is used for
		// cinematics at the beginning, for burnout and for finishing.
		// But I guess for finishing you need all cars. Eh, the car models arnt
		// that expensive.
		ballcount = 0;
		tireboostInfoLabel.setText("");
		
		templight = 10;
		if (turboBlow == null)
			createTurboBlowPercentage();
		//		backgroundImage.getShader().setUniform//new Vector3f(0.05f, 0.05f, 0.1f));
		
		tintColor = new Vec3(Features.ran.nextFloat(), Features.ran.nextFloat() / 2f, Features.ran.nextFloat());
		tintAlpha = Features.ran.nextFloat() / 15f + 0.03f;
		
	}
	
	@Override
	public void updateResolution() {
		infoRowHeight = infoFont.getHeight() * 1.1f;
		infoSpacingY = Window.HEIGHT / 60;
		infoPaddingX = Window.HEIGHT / 33.75f;
		infoPaddingY = Window.HEIGHT / 25.71f;
		
		float rotX = 5.1f, rotY = 2;
		float tachometerPointerRotationX = tachoPointer.getWidth() / rotX;
		float tachometerPointerRotationY = tachoPointer.getHeight() / rotY;
		tachoPointerOrigo= new Vec2(Window.WIDTH / 2 - tachometerPointerRotationX, Window.HEIGHT / 2 - tachometerPointerRotationY);
		tachoPointerProperPosition = new Vec2(tachoBase.position().x + tachoBase.getWidth() * 0.5f //* 0.63f
				- tachoPointerOrigo.x - tachometerPointerRotationX,
					tachoBase.position().y + tachoBase.getHeight() / 1.95f - tachoPointerOrigo.y - tachometerPointerRotationY);
		
		float turbometerDistanceX = 0.8f;
		float turbometerDistanceY = 1.1f;
		
		float scale = turbo.getWidth() * 0.95f;
		turboPointerScaledown = scale / tachoBase.getWidth();
		turboPointerScaleup = tachoBase.getWidth() / scale;
		
		float turbometerPointerRotationX = tachoPointer.getWidth() * turboPointerScaledown / rotX;
		float turbometerPointerRotationY = tachoPointer.getHeight() * turboPointerScaledown / rotY;
		Vec2 turbometerPointerOrigo = new Vec2(Window.WIDTH / 2 - turbometerPointerRotationX, Window.HEIGHT / 2 - turbometerPointerRotationY);
		turbo.setPosition(new Vec2(tachoBase.position().x - turbo.getWidth() * turbometerDistanceX,
				tachoBase.getHeight() + tachoBase.position().y - turbo.getHeight() * turbometerDistanceY));
		turboPointerProperPosition = new Vec2(turbo.position().x + turbo.getWidth() * 0.5f - turbometerPointerOrigo.x - turbometerPointerRotationX,
				turbo.position().y + turbo.getHeight() / 2f - turbometerPointerOrigo.y - turbometerPointerRotationY);
		
		frameScale = ((float) Window.WIDTH / (float) Window.HEIGHT) * (9f / 16f);
		float frameMoveX = -((float) Window.WIDTH - ((float) Window.WIDTH * frameScale)) / 2f; 
		for(Sprite frame : background.getFrames()) {
			frame.setScale(new Vec3(frameScale, 1f, 1f));
			frame.setPositionX(frameMoveX);
		}
		
		goal.setScale(new Vec3(1f, 1f + (1f - frameScale), 1f));
		
		if(tachoSpeed != null) {
			tachoGear.destroy();
			tachoSpeed.destroy();
		}
		
		Vec2 tachpoint = tachoBase.position();
		float textHeight = Window.HEIGHT / 18f;
		float x = tachpoint.x + tachoBase.getWidth() / 2f + textHeight / 3.8f;
		float height = Window.HEIGHT / 10.5f + textHeight / 2f; // was 5.5f
		// width of tachometer size. Height is text height
		tachoSpeed = new SpriteNumeric(new Vec2(x, Window.HEIGHT - height), textHeight, 490, true);

		textHeight *= 0.8f;
		x = Window.WIDTH - textHeight;
		height = Window.HEIGHT / 9.5f - textHeight / 2f;
		tachoGear =  new SpriteNumeric(new Vec2(x, Window.HEIGHT - height), textHeight, 0, false);

		createTurboBlowPercentage();
		
		addGameObject(tachoGear);
		addGameObject(tachoSpeed);
		
		
		tachoDotSmall.setPositionX(- tachoBase.getWidth() / 2f);
		tachoDotLarge.setPositionX(- tachoBase.getWidth() / 2f); 
		tachoDotRedline.setPositionX(- tachoBase.getWidth() / 2f);
	}

	private void createTurboBlowPercentage() {
		if (turboBlow != null)
			turboBlow.destroy();
		if (player != null && player.getCar().hasTurbo()) {
			float margin = 0.6f;
			float textHeight = Window.HEIGHT / 36f;
			float x = turbo.position().x + turbo.getWidth() * margin * 1.1f,
				  y = turbo.position().y + turbo.getHeight() * margin;
			turboBlow = new SpriteNumeric(new Vec2(x, y), textHeight, 490, true);
			addGameObject(turboBlow);
		}
	}
	
	/**
	 * if <= 3 then red balls otherwise green.
	 * 
	 * @param i
	 */
	public void setBallCount(int i) {
		ballcount = i;
	}

	@Override
	public void mouseScrollInput(float x, float y) {
	}
	
	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		// TODO Auto-generated method stub
		// if (action == GLFW.GLFW_RELEASE) {
		// this.x += 1f / 4f;
		// if (this.x > 10f)
		// this.x = -10f;
		// tachometerBase.setPositionX((float) this.x);
		// System.out.println(this.x);
		// }
		if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			if (action != GLFW.GLFW_RELEASE)
				gearbox.press(x, y);
			else
				gearbox.release(x, y);
		}
		return false;
	}

	@Override
	public void mousePosInput(float x, float y) {
//		 perspCamera.rotateCameraMouseBased(x - Window.WIDTH / 2,
//		 y - Window.HEIGHT / 2);
		gearbox.move(x, y);
	}

	@Override
	public void tick(float tickFactor) {
		if (race != null) {
			currentDistance.setText("Distance: " + race.getCurrentLength() + "m");
			if (System.currentTimeMillis() >= race.getStartTime()) {
				lapsedTime.setText("Time: " + String.format("%.3f",
						Float.valueOf(System.currentTimeMillis() - race.getStartTime()) / 1000) + " sec");
			} else {
				lapsedTime.setText("Waiting");
			}
			
			gearboxTimeShift.setText(gearbox.getTimeShifted());

			if (player != null) {
				goal.setPositionZ((float) (-(race.getCurrentLength() - player.getCar().getStats().distance) * 8f)); // why the fuck do i have to time this by 3 to make it look right?
				float distanceY = 3f * 240f / race.getCurrentLength();
				float distanceDivide =  (float) (player.getCar().getStats().distance / (float)race.getCurrentLength());
				float y =  (1f - distanceDivide) * (goal.getHeightReal() / distanceY); // + (-goal.getHeightReal() / 6.8f * distanceDivide) 
				goal.setPositionYReal(y);

//				float scale = (player.getCar().getStats().distance / (float)race.getCurrentLength()) + 0.5f;
//				goal.setScale(new Vector3f(scale, scale, scale));

				lapsedDistance.setText("Distance covered: " + player.getCar().getDistance() + "m");
			
				double speed = player.getCar().getStats().speed;
				tachoSpeed.setNumber((int) speed);
				
				if (speed > whiteBound) {
					whiteify = (float) ((speed - whiteBound + ((whiteBound / 10f) * Features.ran.nextFloat())) / (whiteBound * 4f));
				}
				
//				String gear = null;
//				if (player.getCar().getStats().gear == 0)
//					gear = "N";
//				else
//					gear = String.valueOf(player.getCar().getStats().gear);
				tachoGear.setNumber(player.getCar().getStats().gear);
				if(player.getCar().hasTurbo() && turboBlow != null) {
					turboBlow.setNumber((int) player.getCarRep().getInt(Rep.turboblow));
				}
				
				
//				System.out.println(gearbox.getTimeShifted());
			}
		}


//		perspCamera.setPosition(new Vector3f((float) player.getCar().getStats().distance, 0, 1f));

		// x += delta / 8;
		// if (x > 10)
		// x = -10;
		// tachometerBase.setPositionX((float) x);

		// TODO change me into generated 3d elements.
		background.setCurrentFrame((background.getCurrentFrame() + (player.getCar().getStats().speed / 100) * tickFactor)
				% background.getFramesAmount());
		
		gearbox.tick(tickFactor);
		
	}

	@Override
	public boolean hasAnimationsRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		var backFrame = background.getFrame();
		var actualTint = new Vec3(tintColor);
		var alphaBound = 0.5f;
		var actualAlpha = tintAlpha > whiteify ? tintAlpha : (whiteify < alphaBound ? whiteify : alphaBound);
		for (int i = 0; i < 3; i++) {
			float oldTint = actualTint.get(i) * (1f - (4f * whiteify));
			if (oldTint < 0)
				oldTint = 0;
			float newTint = oldTint + whiteify;
			if (newTint > 1)
				newTint = 1;
			actualTint.set(i, newTint);
		}
		backFrame.getShader().setUniform("tint", actualTint, actualAlpha);
		renderer.renderOrthoMesh(backFrame);

		if (opponent != null) {
			opponent.renderCar(renderer, perspCamera); // FIXME baklengs distance
		}

		myCarBase.resetTransformation();
		
		goal.getShader().setUniform("tint", actualTint, actualAlpha);
		renderer.renderMesh(goal, perspCamera);

		boolean moving = player.getCar().getStats().speed > 1;
		double speed = player.getCar().getStats().speed;

		zoomMyCar(moving);
		if (!moving) {
			float comparedValue = (float) (player.getCar().getStats().rpm / player.getCarRep().get(Rep.rpmTop));
			rotateIdle(myCarBase, comparedValue, blurShake);
		} else {
			shakeHighSpeed(speed);
		}

		/*
		 * dashboard lights
		 */
		myCarBase.getShader().setUniform("lightingTexture", 1);
		float tillNext = (float) (templight - player.getCar().getStats().distance);
		// la oss si at det g�r fra 10 fram til -10 bak.
		if (Math.abs(tillNext) <= 10f) {
			tillNext += 10f; // 0 til 20
			tillNext = -(tillNext - 20f) / 20f;
		} else {
			tillNext = 0.0f;
			templight += 20;
		}
		tillNext = tillNext * 1.5f - 0.25f;
		
//		System.out.println(tillNext);
		
		myCarLighting.bind(GL13.GL_TEXTURE1);
		myCarBase.getShader().setUniform("lightDistanceNext", tillNext);
		myCarBase.getShader().setUniform("lightDistancePrev", 0);
		renderer.renderOrthoMesh(myCarBase);
		myCarLighting.unbind();
		if (player.getCar().getStats().NOSON) {
			nitros.setCurrentFrame(Features.ran.nextInt(nitros.getFramesAmount()));
			renderer.renderOrthoMesh(nitros.getFrame());
		}
		
		float fastnessBoundry = 250;
		
		if (speed > fastnessBoundry) {
			float speedPercentage = ((float) speed - fastnessBoundry) / 1000f;
			if (speedPercentage > 1f)
				speedPercentage = 1f;
				
			fastness.getShader().setUniform("speedPercentage", speedPercentage);
			renderer.renderOrthoMesh(fastness);
		}
		
		/*
		 * middle
		 */
		racelights(renderer);
		tireboost(player.getCar(), renderer);
		
		/*
		 * nos
		 */
		nosbottles(player.getCar(), renderer);

		/*
		 * turbo
		 */
		if(player.getCar().hasTurbo()) {
			renderer.renderOrthoMesh(turbo);
			if (turboBlow != null)
				turboBlow.render(renderer);
			tachoPointer.scale(turboPointerScaledown);
			meterPointerRotation(tachoPointer, tachoPointerOrigo, turboPointerProperPosition, player.getCar().getTurbometer());
			tachoPointer.scale(turboPointerScaleup);
			renderer.renderOrthoMesh(tachoPointer);
		}
		
		/*
		 * tachometer
		 */
		renderer.renderOrthoMesh(tachoBase);
		
		int len = (int) (player.getCarRep().get(Rep.rpmTop) / 1000f) * 5;
		float degree = 180 / ((len + 5) * 6 / 8); // det er alltid 5 i mellom hver 1000rpm og 6 / 8 er innenfor 180 grader. Hvor mange grader er det i mellom hver.
		for (int i = 0; i <= len + 5; i++) {
			float diff = tachoBase.getWidth() - tachoBase.getHeight();
			diff /= 6f;
			Vec3 rot = new Vec3(0, 0, i * degree -30);
			Vec2 pos = new Vec2(-Window.WIDTH / 2f + tachoBase.position().x + tachoBase.getWidth() / 2f + diff / 2f,
					-Window.HEIGHT / 2f + tachoBase.position().y + tachoBase.getHeight() / 2f + diff);
			Sprite chosenDot = null;
			if (rot.z < 170) {
				if (i % 5 == 0)
					chosenDot = tachoDotLarge;
				else
					chosenDot = tachoDotSmall;
			} else {
				if (i % 5 == 0)
					chosenDot = tachoDotRedline;
				else
					continue;
			}
			
			chosenDot.setRotation(rot);
			chosenDot.updateTransformation();
			chosenDot.setPosition(pos);
			chosenDot.updateTransformation();
			
			renderer.renderOrthoMesh(chosenDot);
			chosenDot.setPosition(new Vec2(0));
		}
		
		meterPointerRotation(tachoPointer, tachoPointerOrigo, tachoPointerProperPosition, player.getCar().getTachometer());
		renderer.renderOrthoMesh(tachoPointer);

		tachoSpeed.render(renderer);
		tachoGear.render(renderer);

		/*
		 * gearbox
		 */
		gearbox.render(renderer);
		
	}
	
	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		raceInfo(ctx, stack);
		
		gearboxTimeShift.layout(ctx, stack);

		if(tireboostWindow.begin(ctx)) {
			nk_layout_row_dynamic(ctx, infoRowHeight, 1);
			tireboostInfoLabel.layout(ctx, stack);
		}
		nk_end(ctx);

		if (tireboostInfoShow && tireboostInfoShowTime < System.currentTimeMillis()) {
			tireboostInfoShow = false;
			tireboostInfoLabel.setText("");
		}
		
	}

	private void zoomMyCar(boolean moving) {

//		float zoom = 1;
		float spdInc = (float) player.getCar().getStats().spdinc;
		float zoom = 2.3f;
		if (moving) {
			zoom = zoom / (spdInc * 0.1f
					* (1 - (float) player.getCar().getStats().resistance) + 1);
			if (zoom < 1.05f)
				zoom = 1.05f;
			// 4 -> 1
		}
		myCarBase.setScale(new Vec3(zoom, zoom, 0));
//		System.out.println(baseMyCar.getScale().toString() + ", spdinc " + player.getCar().getStats().spdinc);
		myCarBase.setPosition(new Vec2(baseMyCarPos.x, baseMyCarPos.y - spdInc * 10f));
	}

	private void shakeHighSpeed(double speed) {
		Vec2 ogPos = myCarBase.position();
		double shakeBoundry = 0.4;
		float x = shakeValue(speed, -shakeBoundry, shakeBoundry);
		float y = shakeValue(speed, -shakeBoundry, shakeBoundry);
		myCarBase.setPosition(new Vec2(ogPos.x + x, ogPos.y + y));
//		System.out.println(baseMyCarPos.y() + ", " + y);
	}

	/**
	 * shakes values to a value from 0 to 1.
	 */
	private float shakeValue(double comparedValue, double fromValue, double tillValue) {

		double scaleForDecimals = 1000;
		int maxRandomBoundryScaled = (int) ((Math.abs(fromValue) + Math.abs(tillValue)) * scaleForDecimals);
		double res = Features.ran.nextInt(maxRandomBoundryScaled) / scaleForDecimals - fromValue;

		double significanceShake = comparedValue / 8000;

		if (significanceShake > 1) {
			significanceShake = 1;
		}

		res *= significanceShake;

		return (float) res;
	}

	private void rotateIdle(GameObject go, float comparedValue, float shake) {
		double finetuneShake = 16.0;
		comparedValue = comparedValue * comparedValue;

		int shakeFrom = (int) (shake * 100 * comparedValue);
		if (shakeFrom < 1)
			shakeFrom = 1;

		double ranShake = Features.ran.nextInt(shakeFrom) / (100 * finetuneShake);
		double degrees = ranShake - (shake / (2 * finetuneShake));

		go.rotateZ((float) degrees);
	}

	private void raceInfo(NkContext ctx, MemoryStack stack) {
		NkVec2 spacing = NkVec2.mallocStack(stack);
		NkVec2 padding = NkVec2.mallocStack(stack);

		spacing.set(0, infoSpacingY);
		padding.set(infoPaddingX, infoPaddingY);

		nk_style_push_vec2(ctx, ctx.style().window().spacing(), spacing);
		nk_style_push_vec2(ctx, ctx.style().window().padding(), padding);

		NkRect rect = NkRect.mallocStack(stack);
		rect.x(0).y(0).w(Window.WIDTH).h(2 * infoRowHeight + infoSpacingY + infoPaddingY);

		Nuklear.nk_window_set_focus(ctx, "raceInfo");
		if (nk_begin(ctx, "raceInfo", rect, Nuklear.NK_WINDOW_NO_SCROLLBAR | Nuklear.NK_WINDOW_NO_INPUT)) {
			Nuklear.nk_style_push_font(ctx, infoFont.getFont());

			nk_layout_row_dynamic(ctx, infoRowHeight, 2);
			lapsedDistance.layout(ctx, stack);
			lapsedTime.layout(ctx, stack);
			nk_layout_row_dynamic(ctx, infoRowHeight, 2);
			currentDistance.layout(ctx, stack);
			extraGamemodeInfo.layout(ctx, stack);

			Nuklear.nk_style_pop_font(ctx);
		}
		nk_end(ctx);

		Nuklear.nk_style_pop_vec2(ctx);
		Nuklear.nk_style_pop_vec2(ctx);
	}

	private void meterPointerRotation(GameObject pointer, Vec2 tachometerPointerOrigo, Vec2 tachometerPointerProperPosition, float rotation) {
		pointer.setPosition(new Vec3(tachometerPointerOrigo));
		pointer.setRotation(new Vec3(0, 0, rotation));
		pointer.updateTransformation();

		pointer.setPosition(new Vec3(tachometerPointerProperPosition));
		pointer.updateTransformation();
	}

	private void racelights(Renderer renderer) {
		if (ballcount < 1)
			return;

		int amount = ballcount;
		boolean green = false;

		if (ballcount == 4) {
			// green racelights
			green = true;
			amount--;
		}

		float posX = -racelight.getWidth();

		for (int i = 0; i < amount; i++) {
			racelight.getShader().setUniform("green", green);
			racelight.setPositionX(posX + Math.abs(posX) * i);
			renderer.renderOrthoMesh(racelight);
		}
	}

	private void tireboost(Car car, Renderer renderer) {
		if (!car.hasTireboost() || !car.isTireboostRunning())
			return;
		
		tireboost.getShader().setUniform("hitTB", car.isTireboostRight());
		renderer.renderOrthoMesh(tireboost);
	}

	private void nosbottles(Car car, Renderer renderer) {
		if (!car.hasNOS())	
			return;

		float posX = 1;

		for (int i = 0; i < car.getRep().get(Rep.nosSize); i++) {
			nosbottle.setPositionX(posX);
			float height = nosbottle.getHeight();
			float diff = height * 7f / 40f;
			height = height - diff;
			float top = Window.HEIGHT - nosbottle.position().y - diff;
			float bot = top - height;
			float percent = car.getStats().getNosPercentageLeft(i);
			float nosLevel = top * percent + bot * (1f - percent);

			nosbottle.getShader().setUniform("nosAmountLevelPositionY", nosLevel);

			nosbottle.getShader().setUniform("nosStrength", nosStrengthColor);
			renderer.renderOrthoMesh(nosbottle);
			posX += nosbottle.getWidth() * 1.1f;
		}
	}
	
	public void removeOpponent() {
		opponent = null;
	}
	
	public void removeOpponent(Car car) {
		if (hasOpponent() && opponent.equals(car))
			opponent = null;
	}
	
	public boolean hasOpponent() {
		return opponent != null;
	}
	
	public void updateOpponentDistance(long time, int trackLength, float delta) {
		opponent.getModel().setPositionToModel(-player.getCar().getStats().distance);
		opponent.getModel().updatePositionByInformation(time, trackLength, delta);
	}

	@Override
	public void keyInput(int keycode, int action) {
	}

	public void setStartboostTime(long reactionTime, long timeloss) {
		if(!tireboostInfoShow) {
			tireboostInfoLabel.setText(reactionTime + "ms | - " + Math.abs(timeloss) + "%");
			tireboostInfoShow = true;
			tireboostInfoShowTime = System.currentTimeMillis() + player.getCarRep().getInt(Rep.tbMs) + 2000;
		}
	}

	public void setStartboostTime(int reactionTime) {
		if(!tireboostInfoShow) {
			tireboostInfoLabel.setText(reactionTime + "ms");
			tireboostInfoShow = true;
			tireboostInfoShowTime = System.currentTimeMillis() + 2000;
		}
	}
	
	public void setExtraGamemodeInfoText(Communicator com) {
		extraGamemodeInfo.setText(com.getGamemode().getExtraGamemodeRaceInfo());
	}

	public void setWarning(String string) {
		tireboostInfoLabel.setText(string);
	}

	public Gearbox getGearbox() {
		return gearbox;
	}

	
}
