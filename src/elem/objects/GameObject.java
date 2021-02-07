package elem.objects;

import engine.graphics.Mesh;
import engine.graphics.Shader;
import engine.math.Matrix4f;
import engine.math.Vec3;

public class GameObject implements IGameObject {

	private Matrix4f actualTransformation, tempTransformation;
	private GameObject owner;
	protected Vec3 position, rotation, scale;
	protected Mesh mesh;
	protected Shader shader;

	protected GameObject() {}
	
	public GameObject(Mesh mesh, Shader shader) {
		init (new Vec3(0, 0, 0), new Vec3(0, 0, 0), new Vec3(1, 1, 1), mesh, shader);
	}

	public GameObject(Vec3 position, Vec3 rotation, Vec3 scale, Mesh mesh, Shader shader) {
		init(position, rotation, scale, mesh, shader);
	}
	
	protected void init(Vec3 position, Vec3 rotation, Vec3 scale, Mesh mesh, Shader shader) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		this.mesh = mesh;
		this.shader = shader;
		tempTransformation = Matrix4f.identity();
	}
	
	public Mesh getMesh() {
		return mesh;
	}
//
//	public void setMeshes(Mesh[] meshes) {
//		this.meshes = meshes;
//	}

	public Shader getShader() {
		return shader;
	}

	public void setShader(Shader shader) {
		this.shader = shader;
	}

	public Vec3 rotation() {
		return rotation;
	}

	public void setRotation(Vec3 rotation) {
		this.rotation = rotation;
		tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.rotation(rotation));
	}
	
	// 360 is a whole circle
	public void rotateZ(float z) {
		rotation.z = z;
		tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.rotation(new Vec3(0, 0, z)));
	}

	public Vec3 getScale() {
		return scale;
	}

	public void setScale(Vec3 scale) {
		this.scale = scale;
		tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.scale(scale));
	}
	
	public void scale(float f) {
		scale.mul(f);
	}
	
	public Vec3 position() {
		return position;
	}

	public void setPosition(Vec3 position) {
		this.position = position;
		tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.translate(position));
	}
	
	public void setPositionX(float x) {
		position.x = x;
		tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.translate(new Vec3(x, 0, 0)));
	}
	
	public void setPositionY(float y) {
		position.y = y;
		tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.translate(new Vec3(0, y, 0)));
	}
	
	public void setPositionZ(float z) {
		position.z = z;
		tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.translate(new Vec3(0, 0, z)));
	}

	public void setRotationY(float y) {
		rotation.y = y;
		tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.rotation(new Vec3(0, y, 0)));
	}

	public Matrix4f getTransformation() {
		if(owner != null) {
			var tempTransformation = Matrix4f.multiply(this.tempTransformation, Matrix4f.translate(owner.position()));
			tempTransformation = Matrix4f.multiply(tempTransformation, Matrix4f.rotation(owner.rotation()));

			updateTransformation();
			actualTransformation = Matrix4f.multiply(actualTransformation, tempTransformation);
		}
		
		return (actualTransformation != null ? actualTransformation : updateTransformation());
	}
	
	/**
	 * Updates the temp matrix and combines it into the actual one.
	 * Translation, rotation and scaling happens seperately otherwise.
	 */
	public Matrix4f updateTransformation() {
		if(actualTransformation != null)
			actualTransformation = Matrix4f.multiply(actualTransformation, tempTransformation);
		else
			actualTransformation = Matrix4f.transform(position, rotation, scale);
		tempTransformation = Matrix4f.identity();
		
		return actualTransformation;
	}
	
	public void resetTransformation() {
		actualTransformation = null;
		tempTransformation = Matrix4f.identity();
	}

	public GameObject getOwner() {
		return owner;
	}

	public GameObject setOwner(GameObject owner) {
		this.owner = owner;
		return this;
	}

	public void destroy() {
		if (mesh != null)
			mesh.destroy();
		if(shader != null)
			shader.destroy();
	}

}
