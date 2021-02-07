package scenes.game.lobby_subscenes;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_end;

import main.ResourceHandler;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import audio.SfxTypes;
import communication.Communicator;
import elem.objects.Camera;
import elem.objects.Model;
import elem.objects.Sprite;
import elem.ui.UIButton;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import engine.graphics.Renderer;
import engine.io.Window;
import engine.math.Vec2;
import engine.math.Vec3;
import game_modes.GameMode;
import game_modes.GameModes;
import game_modes.SinglePlayerMode;
import main.Features;
import player_local.Car.Car;
import player_local.Car.CarModel;
import player_local.*;
import scenes.adt.Subscene;
import scenes.game.Lobby;

/**
 * 
 * Shows upgrades for the engine. Have a different one for boost and fuel
 * 
 * @author Jens Benz
 *
 */

public class CarChoiceSubscene extends Subscene {

	public static final Model[] CARS = new Model[Car.CAR_TYPES.length];
	private float rowHeight, rowSpacingY; //FIXME these should be global, most likely

	private Communicator com;

	private int selectedCarIndex;
	private Sprite[] characters;
	private Camera camera;
	private UIButton prevCarBtn, nextCarBtn;
	private Player player;

	private UIWindowInfo window;

	public CarChoiceSubscene(Features features, int sceneIndex) {
		super(features, sceneIndex, "Car Selection");
	}
	
	@Override
	protected void initDown(Lobby lobby, UIButton outNavigationBottom, UIButton outNavigationSide) {
		characters = new Sprite[Car.CAR_TYPES.length];
		selectedCarIndex = 0;
		
		float size = Window.HEIGHT / 2.5f;
		for(int i = 0; i < CARS.length; i++) {
			final int carID = i;
			CARS[i] = CarModel.createModel(Car.CAR_TYPES[i]); // TODO last inn modeller bare en gang
			ResourceHandler.LoadSprite(new Vec2(0, Window.HEIGHT - size), size,"./images/" +  Car.CAR_TYPES[i] + "_character.png", "main", (s) -> characters[carID] = s);
		}

		camera = new Camera(new Vec3(2.2f, 0.4f, 7.4f),
				new Vec3(2.2f, 20.7f, 0), 70f, 0.1f, 1000f);
		
		prevCarBtn = new UIButton("Previous car");
		nextCarBtn = new UIButton("Next car");

		prevCarBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			if(selectedCarIndex == 0)
				selectedCarIndex = CARS.length - 1;
			else
				selectedCarIndex--;
			lobby.carSelectUpdate(selectedCarIndex);
			
			GameMode gm = com.getGamemode();
			if(gm.getGameModeEnum().equals(GameModes.SINGLEPLAYER)) {
				((SinglePlayerMode) gm).giveStarterPoints();
			}
		});
		nextCarBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			selectedCarIndex = (selectedCarIndex + 1) % CARS.length;
			lobby.carSelectUpdate(selectedCarIndex);
			
			GameMode gm = com.getGamemode();
			if(gm.getGameModeEnum().equals(GameModes.SINGLEPLAYER)) {
				((SinglePlayerMode) gm).giveStarterPoints();
			}
		});

		add(prevCarBtn);
		add(nextCarBtn);

		prevCarBtn.setNavigations(null, () -> nextCarBtn, null, () -> outNavigationBottom);
		nextCarBtn.setNavigations(() -> prevCarBtn, () -> outNavigationSide, null, () -> outNavigationBottom);

	}
	
	@Override
	public void updateGenerally() {
		for (var car : CARS) {
			car.resetTransformation();
		}
	}
	
	@Override
	public void updateResolution() {
		rowSpacingY = Window.HEIGHT / 192.75f;
		rowHeight = Window.HEIGHT / 16f;
	}
	
	@Override
	public void createWindowsWithinBounds(float x, float y, float w, float h) {
		window = createWindow(x, y, w, h / 2);
	}

	@Override
	public void keyInput(int keycode, int action) {
//		camera.move(keycode, action);
//		System.out.println("pos: " + camera.getPosition().toString());

	}
	
	@Override
	public void mouseScrollInput(float x, float y) {
	}

	@Override
	public void mousePositionInput(float x, float y) {
//		 camera.rotateCameraMouseBased(x - Window.WIDTH / 2, y-
//		 Window.HEIGHT / 2);
//		 System.out.println("rot: " + camera.getRotation().toString());
	}

	@Override
	public void tick(float delta) {
		CARS[selectedCarIndex].rotation().y += 3 * delta;
//		rotateWheels(CARS[selectedCarIndex], 30 * delta);
		camera.update();
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		 CARS[selectedCarIndex].render(renderer, camera);
		 renderer.renderOrthoMesh(characters[selectedCarIndex]);
	}

	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		if (window.begin(ctx, stack, 0, 0, rowSpacingY, 0)) {
			Nuklear.nk_layout_row_dynamic(ctx, rowHeight, 3);
			prevCarBtn.layout(ctx, stack);
			Nuklear.nk_label(ctx, Car.CAR_TYPES[selectedCarIndex], Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_MIDDLE);
			nextCarBtn.layout(ctx, stack);
			if (this.player != null) {
				for (var info : player.getCarRep().getCarChoiceInfo()) {
					Nuklear.nk_layout_row_dynamic(ctx, rowHeight / 2f, 1);
					info.layout(ctx, stack);
				}
			}
		}
		nk_end(ctx);
	}

	public void setCom(Communicator com) {
		this.com = com;
	}

	@Override
	public void createBackground() {
		ResourceHandler.LoadSprite("./images/back/carselection.png", "background", (sprite) -> backgroundImage = sprite);
	}
	
	@Override
	public UIButton intoNavigationSide() {
		return nextCarBtn;
	}

	@Override
	public UIButton intoNavigationBottom() {
		return nextCarBtn;
	}

	@Override
	public void destroy() {
		for (var m : CARS)
			m.destroy();
		for (Sprite s : characters)
			s.destroy();
	}
	
	@Override
	public void setVisible(boolean visible) {
		window.visible = visible;
	}

	public void afterJoined(Player player) {
		this.player = player;
		selectedCarIndex = 0;
	}

}
