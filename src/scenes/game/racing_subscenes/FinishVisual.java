
package scenes.game.racing_subscenes;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import audio.CarAudio;
import elem.ColorBytes;
import elem.objects.Camera;
import elem.objects.Sprite;
import elem.ui.UIScrollable;
import engine.graphics.Renderer;
import engine.math.Vec3;
import main.Features;
import main.ResourceHandler;
import player_local.Car.Car;
import player_local.Car.CarModel;
import scenes.adt.Visual;

public class FinishVisual extends Visual {

	private Camera perspCamera;
	private List<Car> finishedPlayers;
	private UIScrollable raceLobbyLabel;
	private final float maxDistance = 150;
	private Sprite finishBack;
	private ColorBytes raceLobbyLabelColor;

	public FinishVisual(Features features, UIScrollable raceLobbyLabel) {
		super(features);

		this.raceLobbyLabel = raceLobbyLabel;
		finishedPlayers = new CopyOnWriteArrayList<Car>();
		perspCamera = new Camera(new Vec3(-1f, 0.5f, -3.9f), new Vec3(-0.6f, -145f, 0), 70f, 0.1f, 1000f);
		raceLobbyLabelColor = new ColorBytes(255, 255, 255, (int) (255 * 0.8f));
		
		ResourceHandler.LoadSprite("./images/finishBack.png", "background", (sprite) -> finishBack = sprite);
	}

	@Override
	public void updateResolution() {
	}
	
	public void init() {
		finishedPlayers.clear();
	}

	public void keyInput(int keycode, int action) {
//		camera.move(keycode, action);
//		System.out.println("pos: " + camera.getPosition().toString());
	}
	
	@Override
	public void mouseScrollInput(float x, float y) {
	}
	
	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		return false;
	}

	@Override
	public void mousePosInput(float x, float y) {
	}

	@Override
	public void tick(float delta) {
		for (int i = 0; i < finishedPlayers.size(); i++) {
			Car car = finishedPlayers.get(i);
			CarModel model = car.getModel();
			var speed = model.getSpeed();
//			model.addPositionDistance(-speed / 32f * delta);
//			model.setPositionToModel(0); // this shit is moved and moved and then rendered...... wtf. 
										//  Needs to be moved rendered reset, moved rendered etc.
//			model.rotateWheels(speed * delta);
			
			CarAudio ca = car.getAudio();
			if(!ca.getMotor().isPlaying()) {
				ca.motorPitch(0.9f, 1, 2, 1);
				ca.turbospoolPitch(1, 200, 1);
				ca.motorAcc(car.hasTurbo());
				ca.getMotor().velocity(speed / 2.2f, 0, 0);
			}

			Vec3 carPos = new Vec3(model.getModel().position());
			carPos.invert();
			ca.setMotorPosition(carPos);
			
			if (model.getPositionDistance() < -maxDistance / 1.6f) {
				finishedPlayers.remove(finishedPlayers.get(i));
				model.reset(); // problem om modellen er den samme her.
				ca.reset();
			}

		}
		perspCamera.update();
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		for (Car car : finishedPlayers) {
			
			CarModel model = car.getModel();
			var speed = model.getSpeed();
			model.addPositionDistance(-speed / 32f * delta);
			model.setPositionToModel(0);
			model.rotateWheels(speed * delta);
			car.renderCar(renderer, perspCamera);
		}
		renderer.renderOrthoMesh(finishBack);
	}
	
	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		features.pushBackgroundColor(ctx, raceLobbyLabelColor);
		raceLobbyLabel.layout(ctx, stack);
		features.popBackgroundColor(ctx);
		goBackLayout(ctx, stack, raceLobbyLabel.getWindow().x, 
				raceLobbyLabel.getWindow().y + raceLobbyLabel.getWindow().height * 1.05f, 
				raceLobbyLabel.getWindow().width, raceLobbyLabel.getWindow().width / 4);
	}

	@Override
	public boolean hasAnimationsRunning() {
		return !finishedPlayers.isEmpty();
	}

	public void addFinish(Car car, int speed) {
		CarModel carStats = car.getModel();   //new CarModel(maxDistance)
		carStats.reset();
		carStats.setPositionDistance(maxDistance);
		if(carStats.getSpeed() == 0) {
			carStats.addSpeed(speed);
		}
		carStats.setFinished(true);
		finishedPlayers.add(car);
	}

	public Vec3 getCameraPosition() {
		return perspCamera.getPosition();
	}

}