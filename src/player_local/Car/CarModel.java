package player_local.Car;
import java.util.ArrayList;

import elem.objects.GameObject;
import elem.objects.Model;
import engine.math.Vec3;
import scenes.game.lobby_subscenes.CarChoiceSubscene;

public class CarModel {

	private Model model;
	
	private float positionX;
	private float positionZ;

	private double rotation;

	private double rotationZ;
	
	private ArrayList<Float> distances = new ArrayList<>();
	private ArrayList<Integer> speeds = new ArrayList<>();
	private ArrayList<Long> times = new ArrayList<>();

	private boolean finished;

	private int modelIndex;
	
	public static Model createModel(String carname) {
		Model car = new Model(carname + ".obj", new String[] {
				"./images/models/" + carname + "Paint.png",
				"./images/models/Tires.png"
			}, "main");

		car.create();
		return car;
	}
	
	public void reset() {
		positionX = 0;
		positionZ = 0;
		rotation = 0;
		rotationZ = 0;
		distances.clear();
		speeds.clear();
		times.clear();
		setPositionToModel(0);
		finished = false;
		model.resetTransformation();
	}
	
	
	public int getSpeed() {
		for (int i = speeds.size() - 1; i >= 0; i--) {
			if(speeds.get(i) > 0)
				return speeds.get(i);
		}
		return 0;
	}
	
	public void addSpeed(int speed) {
		speeds.add(speed);
	}
	
	public float getPositionDistance() {
		return positionX;
	}

	public void setPositionDistance(float positionX) {
		this.positionX = positionX;
	}
	
	public void addPositionDistance(double d) {
		positionX += d;
		// TODO calc and rotate wheels. Should always be mesh 1 and 2
		
//		car.getMeshes()[1]
	}
	
	public void setPositionSide(float f) {
		positionZ = f;		
	}

	public Model getModel() {
		return model;
	}

	public void setPositionToModel(double distanceFromMyCamera) {
		model.position().x = (float) (positionX - distanceFromMyCamera);
		model.position().z = positionZ;
		model.updateTransformation();
		model.rotation().x = 0;
		model.rotation().y = (float) rotation;
		model.rotation().z = (float) rotationZ;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}
	
	public void setRotationZ(double rotationZ) {
		this.rotationZ = rotationZ;
	}

	public void pushInformation(float distance, int speed, long time) {
		if(distances.size() != 0 && distances.get(distances.size() - 1) == distance)
			return; // FIXME set speeds and time but not add. dont set distance.
		
		speeds.add(speed);
		times.add(time);
		distances.add(distance);
	}
	
	public void updatePositionByInformation(long time, int trackLength, float delta) {
		// TODO set positionZ til forrige distance om etter eller anta distance om f�r
		
		if (positionX >= trackLength || finished)
			return;
		
		int length = distances.size() - 1;
		int found = 0;
		for (int i = length; i >= 0; i--) {
			if (time >= times.get(i)) {
				found = i;
				break;
			}
		}

		if (found == 0) {
			positionX = 0;
		} else { 
			float timeFromFound = times.get(found) - time;

			if (found == length) {
				// calc distance
				float differenceTime = timeFromFound / 1000f;
				float mps = speeds.get(found) / 3.6f;
				float predictedDistance = mps * differenceTime;
				
				positionX = distances.get(found) + predictedDistance;
			} else {
				// interpolate between
				float timeToNext = times.get(found + 1) - times.get(found);
				float interpolation = timeFromFound / timeToNext; // vil v�re 1 om time helt mot next, mens 0 om helt mot found
				
				float distanceFromFoundToNext = distances.get(found + 1) - distances.get(found);
				
				positionX = distances.get(found) - (interpolation * distanceFromFoundToNext);
			}
			rotateWheels(speeds.get(found) * delta);
		}
		
	}

	public void setFinished(boolean b) {
		this.finished = b;
 	}

	public boolean isFinished() {
		return finished;
	}

	public void setModel(int i) {
		this.model = CarChoiceSubscene.CARS[i];
		this.modelIndex = i;
	}
	
	public void rotateWheels(float speed) {
		if (modelIndex == 1 || modelIndex == 3) return;
		
		var gos = model.getGos();
		for (int i = 1; i < gos.size(); i++) {
			var g = gos.get(i);
			float rad = g.getMesh().getSize().x / 2f;
			float rotSpd = speed / (2f * (float) Math.PI * rad);
			
			Vec3 posOG = g.position();
//			System.out.println(posOG.toString());
			Vec3 posNew = new Vec3(0);
			posNew.x = 
					-posOG.x + 
					-g.getMesh().getPositionAvgOg().x;
			posNew.y =
					-posOG.y +
					-g.getMesh().getPositionAvgOg().y;
			g.setPosition(posNew);
			g.updateTransformation();
			g.rotation().z -= rotSpd;
			posNew.x =
					posOG.x + 
					g.getMesh().getPositionAvgOg().x;
			posNew.y =
					posOG.y +
					g.getMesh().getPositionAvgOg().y;
			g.setPosition(posNew);
			g.updateTransformation();
//			System.out.println(posNew.toString());
			g.setPosition(posOG);
			g.updateTransformation();
		}
	}

}
	