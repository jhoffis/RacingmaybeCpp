package elem.objects;

import engine.graphics.Mesh;
import engine.graphics.Shader;
import engine.graphics.Texture;
import engine.graphics.Vertex;
import engine.io.Window;
import engine.math.Vec2;
import engine.math.Vec3;

public class Sprite extends GameObject {

	private Texture sprite;
	private float width, height;
	private boolean created;
	
	public Sprite(Vec2 topleftPoint, float heightSize, String spriteName, String shaderName) {
		Texture tex = new Texture(spriteName);
		this.init(topleftPoint, heightSize * tex.widthHeightRatio(), heightSize, tex, shaderName);
	}

	public Sprite(float heightSize, String spriteName, String shaderName) {
		this(new Vec2(0), heightSize, spriteName, shaderName);
	}
	
	public Sprite(String spriteName, String shaderName) {
		this(Window.HEIGHT, spriteName, shaderName);
	}

	private void init(Vec2 topleftPoint, float sizeX, float sizeY,
					  Texture sprite, String shaderName) {
		topleftPoint = new Vec2(convertToOrthoSpaceX(topleftPoint.x), convertToOrthoSpaceY(topleftPoint.y));
		sizeX /= Window.WIDTH / Camera.ORTHO_SIDES;
		sizeY /= Window.HEIGHT / Camera.ORTHO_SIZE;
//		topleftPoint.setY(topleftPoint.y() - sizeY);
		
		this.init(new Vec3(topleftPoint.x, topleftPoint.y, 0.0f),
				new Vec3(topleftPoint.x, topleftPoint.y - sizeY, 0.0f),
				new Vec3(topleftPoint.x + sizeX, topleftPoint.y- sizeY, 0.0f),
				new Vec3(topleftPoint.x + sizeX, topleftPoint.y, 0.0f),
				new Vec3(0),
				new Vec3(0),
				new Vec3(1),
				sprite, shaderName);
	}

	private void init(Vec3 topleft, Vec3 botleft, Vec3 botright,
                      Vec3 topright, Vec3 position, Vec3 rotation,
                      Vec3 scale, Texture sprite, String shaderName) {
		super.init(position, rotation, scale, new Mesh(
				new Vertex[]{
						new Vertex(topleft, null,
								new Vec2(0.0f, 0.0f)),
						new Vertex(botleft, null,
								new Vec2(0f, 1f)),
						new Vertex(botright, null,
								new Vec2(1f, 1f)),
						new Vertex(topright, null,
								new Vec2(1f, 0f))},
				new int[]{0, 1, 2, 0, 2, 3}, sprite), new Shader(shaderName));
		// setScale(Vector3f.mulX(getScale(), sprite.widthHeightRatio()));
		this.sprite = sprite;
		
		width  = topright.x - botleft.x - Camera.ORTHO_HALFSIDES;
		height = topright.y - botleft.y + Camera.ORTHO_HALFSIZE;
	}
	
	public void create() {
		if (created) return;
		
		created = true;
		shader.create();
		mesh.create();
	}

	public Texture getSprite() {
		return sprite;
	}
	
	public float getWidth() {
		return convertToPixelSpaceX(width) * scale.x;
	}

	public float getHeight() {
		return Math.abs(convertToPixelSpaceY(height)) * scale.y;
	}
	
	public float getWidthReal() {
		return width;
	}

	public float getHeightReal() {
		return height;
	}

	public void setPositionXReal(float x) {
		super.setPositionX(x);
	}
	
	public void setPositionYReal(float y) {
		super.setPositionY(y);
	}

	@Override
	public void setPositionX(float x) {
		super.setPositionX(convertToOrthoSpaceX(x) + Camera.ORTHO_HALFSIDES);
	}
	
	@Override
	public void setPositionY(float y) {
		super.setPositionY(convertToOrthoSpaceY(y) - Camera.ORTHO_HALFSIZE);
	}

	public void setPosition(Vec2 position) {
		setPositionX(position.x);
		setPositionY(position.y);
	}
	
	@Override
	public void setPosition(Vec3 position) {
		setPosition((Vec2) position);
	}
	
	@Override
	public Vec3 position() {
		Vec3 realPosition = mesh.getVertices()[0].getPosition();
		Vec3 res = new Vec3(realPosition.z);
		
		// g� fra punkt nede til venstre til oppe til venstre.
		res.x = convertToPixelSpaceX(position.x + realPosition.x);
		res.y = convertToPixelSpaceY(position.y + realPosition.y);
		
		return res;
	}
	
	private float convertToOrthoSpaceX(float x) {
		return x * (Camera.ORTHO_SIDES / Window.WIDTH) - Camera.ORTHO_HALFSIDES;
	}
	
	private float convertToOrthoSpaceY(float y) {
		y = Window.HEIGHT - y;
		return y * (Camera.ORTHO_SIZE / Window.HEIGHT) - Camera.ORTHO_HALFSIZE;
	}
	
	private float convertToPixelSpaceX(float x) {
		return (x + Camera.ORTHO_HALFSIDES) * (Window.WIDTH / Camera.ORTHO_SIDES);
	}
	
	private float convertToPixelSpaceY(float y) {
		return Window.HEIGHT - ((y + Camera.ORTHO_HALFSIZE) * (Window.HEIGHT / Camera.ORTHO_SIZE));
	}

	public boolean above(float x, float y) {
		return above(position(), x, y);
	}

	public boolean above(Vec2 p, float x, float y) {
		return (y <= p.y + getHeight() && y >= p.y) &&
		    (x <= p.x + getWidth()  && x >= p.x);
	}

	public boolean isNotCreated() {
		return !created;
	}
}

