package elem.objects;

import org.lwjgl.glfw.GLFW;

import engine.io.Window;
import engine.math.Matrix4f;
import engine.math.Vec3;

public class Camera {

	public static final float ORTHO_SIZE = 20f, ORTHO_HALFSIZE = ORTHO_SIZE / 2f, ORTHO_HALFSIDES = ORTHO_HALFSIZE * 16f / 9f, ORTHO_SIDES = ORTHO_HALFSIDES * 2;
	
	private Vec3 position, rotation, moveState;
	private float movespeed = 0.05f, mouseSensitivity = 0.1f;
	private boolean fwd, bck, lft, rgt, up, dwn;
	private double lastMouseX, lastMouseY, newMouseX, newMouseY;
	private Matrix4f projection;
	
	//ortho
	private float top, bottom, left, right;

	public Camera(boolean perspective) {

		float near = 0;
		float far = 0;

		if (perspective) {
			near = 0.1f;
			far = 10000f;
		} else {
			near = -1f;
			far = 1f;
		}

		this.init(new Vec3(0, 0, 0), new Vec3(0, 0, 0), perspective,
				70f, near, far);
	}

	public Camera() {
		this(true);
	}

	public Camera(Vec3 position, Vec3 rotation,
                  float fov, float near, float far) {
		this.init(position, rotation, true, fov, near, far);
	}

	private void init(Vec3 position, Vec3 rotation, boolean perspective,
                      float fov, float near, float far) {
		this.position = position;
		this.rotation = rotation;
		moveState = new Vec3(0, 0, 0);
		setProjection(perspective, fov, near, far);
	}

	public void update() {
		if (fwd || bck || lft || rgt) {
			float x = (float) Math.sin(Math.toRadians(rotation.y)) * movespeed;
			float z = (float) Math.cos(Math.toRadians(rotation.y)) * movespeed;

			// Forward and backwards + side to side
			position.x += (x * moveState.z) + (z * moveState.x);
			position.z += (z * moveState.z) - (x * moveState.x);
		}

		if (up || dwn)
			position.y += moveState.y * movespeed;

	}

	public void move(int keycode, int action) {
		if (action != GLFW.GLFW_RELEASE) {
			move(keycode);
		} else {
			moveHalt(keycode);
		}
	}

	public void move(int keycode) {
		switch (keycode) {
			case GLFW.GLFW_KEY_W :
				if (!fwd) {
					moveState.z--;
					fwd = true;
				}
				break;
			case GLFW.GLFW_KEY_A :
				if (!lft) {
					moveState.x--;
					lft = true;
				}
				break;
			case GLFW.GLFW_KEY_S :
				if (!bck) {
					moveState.z++;
					bck = true;
				}
				break;
			case GLFW.GLFW_KEY_D :
				if (!rgt) {
					moveState.x++;
					rgt = true;
				}
				break;
			case GLFW.GLFW_KEY_SPACE :
				if (!up) {
					moveState.y++;
					up = true;
				}
				break;
			case GLFW.GLFW_KEY_LEFT_SHIFT :
				if (!dwn) {
					moveState.y--;
					dwn = true;
				}
				break;
		}
	}

	public void moveHalt(int keycode) {
		switch (keycode) {
			case GLFW.GLFW_KEY_W :
				moveState.z++;
				fwd = false;
				break;
			case GLFW.GLFW_KEY_A :
				moveState.x++;
				lft = false;
				break;
			case GLFW.GLFW_KEY_S :
				moveState.z--;
				bck = false;
				break;
			case GLFW.GLFW_KEY_D :
				moveState.x--;
				rgt = false;
				break;
			case GLFW.GLFW_KEY_SPACE :
				moveState.y--;
				up = false;
				break;
			case GLFW.GLFW_KEY_LEFT_SHIFT :
				moveState.y++;
				dwn = false;
				break;
		}
	}

	public void rotateCameraMouseBased(double x, double y) {
		newMouseX = x;
		newMouseY = y;

		float dx = (float) (newMouseX - lastMouseX) * mouseSensitivity;
		float dy = (float) (newMouseY - lastMouseY) * mouseSensitivity;

		lastMouseX = newMouseX;
		lastMouseY = newMouseY;

		rotation.add(-dy, -dx);
	}

	public Vec3 getPosition() {
		return position;
	}

	public Vec3 getRotation() {
		return rotation;
	}

	public Matrix4f getProjection() {
		return projection;
	}

	/**
	 * 0 = perspective in client, 1 = perspective in game, 2 = orthographic in
	 * client, 3 = orthographic in game rest: perspective in current
	 */
	public void setProjection(boolean perspective, float fov, float near,
			float far) {
		float width = Window.WIDTH;
		float height = Window.HEIGHT;
		float aspect = width / height;

		if (perspective) {
			projection = Matrix4f.perspectiveProjection(fov, aspect, near, far);
		} else {
			top = ORTHO_HALFSIZE; 
			bottom = -ORTHO_HALFSIZE;
			right = ORTHO_HALFSIDES;
			left = -ORTHO_HALFSIDES;

			projection = Matrix4f.orthographicProjection(top, bottom, right,
					left, near, far);
		}

	}

	public void setPosition(Vec3 position) {
		this.position = position;
	}

	public void setRotation(Vec3 rotation) {
		this.rotation = rotation;
	}

	public float getTop() {
		return top;
	}

	public float getBottom() {
		return bottom;
	}

	public float getLeft() {
		return left;
	}

	public float getRight() {
		return right;
	}

	public float getWidth() {
		return right * 2;
	}

	public float getHeight() {
		return top * 2;
	}

}
