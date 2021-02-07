package engine.graphics;

import static org.lwjgl.nuklear.Nuklear.NK_ANTI_ALIASING_ON;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL14C.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14C.glBlendEquation;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryStack;

import elem.objects.Camera;
import elem.objects.GameObject;
import elem.objects.IGameObject;
import engine.math.Matrix4f;

public class Renderer {

	private final UIRender nkUI;
	private final Camera orthoCamera;

	public Renderer(UIRender nkUI) {
		this.nkUI = nkUI;
		orthoCamera = new Camera(false);
	}
	
	public void renderOrthoMesh(GameObject go) {
		renderMesh(go, orthoCamera);
	}

	public void renderMesh(GameObject go, Camera camera) {

		// glEnable(GL_CULL_FACE);
//		glDisable(GL_DEPTH_TEST);
		glEnable(GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
//		GL11.glDepthFunc(GL11.GL_ALWAYS);
		glEnable(GL_BLEND);
		glBlendEquation(GL_FUNC_ADD);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		// Bind
		var mesh = go.getMesh();
		mesh.bind(); // TODO enable here dynamically many
		mesh.getTexture().bind(GL13.GL_TEXTURE0);
		go.getShader().bind();

		// Set uniforms // TODO run these dynamically many times
		go.getShader().setUniform("model", go.getTransformation());
		go.resetTransformation();
		go.getShader().setUniform("view", Matrix4f.view(camera.getPosition(), camera.getRotation()));
		go.getShader().setUniform("projection", camera.getProjection());
		while (go.getShader().runUniform());

		// Draw
		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().length,
				GL11.GL_UNSIGNED_INT, 0);

		// Unbind
		go.getShader().unbind();
		mesh.getTexture().unbind();
		mesh.unbind();
	}

	public void renderNuklear(NkContext ctx, MemoryStack stack) {
		nkUI.setupRender(stack);
		nkUI.bind(ctx, stack, NK_ANTI_ALIASING_ON);
		nkUI.draw(ctx);
		nkUI.unbind();
	}

	public void destroy() {
		nkUI.destroy();
	}
}
