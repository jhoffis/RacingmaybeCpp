package elem.objects;

import java.util.ArrayList;

import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

import engine.graphics.Mesh;
import engine.graphics.Renderer;
import engine.graphics.Shader;
import engine.graphics.Texture;
import engine.graphics.Vertex;
import engine.math.Matrix4f;
import engine.math.Vec2;
import engine.math.Vec3;

// Fyll opp med textures sï¿½ mye som du kan og sï¿½ ta siste mot resten av meshes.
public class Model implements IGameObject {

	// Første i kø eier alle andre.
	private final ArrayList<GameObject> gos = new ArrayList<>();
	private Shader shader;
	
	public Model(String modelPath, String texturePath, String shaderName) {
		this(modelPath, new String[]{texturePath}, shaderName);
	}
	
	public Model(String modelPath, String[] texturePaths, String shaderName) {
		init(new Vec3(0), new Vec3(0), new Vec3(1), modelPath, texturePaths, shaderName);
	}

	private void init(Vec3 position, Vec3 rotation, Vec3 scale, String modelPath, String[] texturePaths,
                 String shaderName) {
		var meshes = loadModel("models/" + modelPath.toLowerCase(), texturePaths);
		this.shader = new Shader(shaderName);

		gos.add(new GameObject(new Vec3(position), new Vec3(rotation), new Vec3(scale), meshes[0], shader));
		for (int i = 1; i < meshes.length; i++)
			gos.add(new GameObject(new Vec3(position), new Vec3(rotation), new Vec3(scale), meshes[i], shader).setOwner(gos.get(0)));
	}

	public void create() {
		shader.create();
		for(var m : gos) {
			m.getMesh().create();
		}
	}

	public void destroy() {
		shader.destroy();
		for(var m : gos) {
			m.getMesh().destroy();
		}
	}


    private Mesh[] loadModel(String filePath, String[] texturePaths) {
        AIScene scene = Assimp.aiImportFile(filePath,
                Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_FlipUVs
                        | Assimp.aiProcess_Triangulate);

        if (scene == null)
            System.out.println("Couldn't load model at " + filePath);

        Mesh[] meshes = new Mesh[scene.mNumMeshes()];
        AIMesh[] aiMeshes = new AIMesh[meshes.length];
        int texIndex = 0;

        for (int i = 0; i < meshes.length; i++) {
            aiMeshes[i] = AIMesh.create(scene.mMeshes().get(i));

            int vertexCount = aiMeshes[i].mNumVertices();

            AIVector3D.Buffer vertices = aiMeshes[i].mVertices();
            AIVector3D.Buffer normals = aiMeshes[i].mNormals();

            Vertex[] vertexList = new Vertex[vertexCount];

            for (int n = 0; n < vertexCount; n++) {
                AIVector3D vertex = vertices.get(n);
                Vec3 meshVertex = new Vec3(vertex.x(), vertex.y(),
                        vertex.z());

                AIVector3D normal = normals.get(n);
                Vec3 meshNormal = new Vec3(normal.x(), normal.y(),
                        normal.z());

                Vec2 meshTextureCoord = new Vec2(0, 0);
                if (aiMeshes[i].mNumUVComponents().get(0) != 0) {
                    AIVector3D texture = aiMeshes[i].mTextureCoords(0).get(n);
                    meshTextureCoord.x = texture.x();
                    meshTextureCoord.y = texture.y();
                }

                vertexList[n] = new Vertex(meshVertex, meshNormal,
                        meshTextureCoord);
            }

            int faceCount = aiMeshes[i].mNumFaces();
            AIFace.Buffer indices = aiMeshes[i].mFaces();
            int[] indicesList = new int[faceCount * 3];

            for (int n = 0; n < faceCount; n++) {
                AIFace face = indices.get(n);
                indicesList[n * 3 + 0] = face.mIndices().get(0);
                indicesList[n * 3 + 1] = face.mIndices().get(1);
                indicesList[n * 3 + 2] = face.mIndices().get(2);
            }

            if (i < texturePaths.length)
                texIndex = i;

            meshes[i] = new Mesh(vertexList, indicesList, new Texture(texturePaths[texIndex]));
        }
        return meshes;
    }

	@Override
	public Vec3 position() {
		return gos.get(0).position;
	}

	@Override
	public Vec3 rotation() {
		return gos.get(0).rotation;
	}

	@Override
	public Matrix4f updateTransformation() {
		return gos.get(0).updateTransformation();
	}

	@Override
	public void resetTransformation() {
		for (var go : gos) {
			go.resetTransformation();
		}
	}
	
	public void render(Renderer renderer, Camera camera) {
		for (var go : gos) {
			renderer.renderMesh(go, camera);
		}
	}

	public ArrayList<GameObject> getGos() {
		return gos;
	}

	@Override
	public void setRotation(Vec3 rotation) {
		for (var go : gos) {
			go.rotation.x = rotation.x;
			go.rotation.y = rotation.y;
			go.rotation.z = rotation.z;
		}
	}

}
