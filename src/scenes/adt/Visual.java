package scenes.adt;

import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_style_push_vec2;

import java.util.ArrayList;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import elem.objects.GameObject;
import elem.ui.IUIObject;
import elem.ui.UIButton;
import main.Features;
import player_local.Player;

public abstract class Visual implements ISceneManipulation {

	protected Features features;
	protected ArrayList<GameObject> gos = new ArrayList<GameObject>();
	protected ArrayList<IUIObject> uios = new ArrayList<IUIObject>();
	protected Player player;
	private UIButton goback;

	public Visual(Features features) {
		this.features = features;
	}
	
	public abstract boolean hasAnimationsRunning();

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void addUIObject(IUIObject btn) {
		uios.add(btn);
	}

	public void removeAllUIObjects() {
		uios.clear();
	}
	
	public void addGameObject(GameObject go) {
		gos.add(go);
	}
	
	public void addGameObjectBulk(GameObject[] bulk) {
		for(GameObject go : bulk) {
			if (go != null)
				addGameObject(go);
		}
	}
	
	public void removeAllGameObjects() {
		for(GameObject go : gos) {
			if(go == null)
				continue;
			go.destroy();
		}
		gos.clear();
	}
	
	protected void goBackLayout(NkContext ctx, MemoryStack stack, float x, float y, float w, float h) {

		NkVec2 spacing = NkVec2.mallocStack(stack);
		NkVec2 padding = NkVec2.mallocStack(stack);

		spacing.set(0, 0);
		padding.set(0, 0);

		nk_style_push_vec2(ctx, ctx.style().window().spacing(), spacing);
		nk_style_push_vec2(ctx, ctx.style().window().padding(), padding);

		NkRect rect = NkRect.mallocStack(stack);
		rect.x(x).y(y).w(w).h(h);

		Nuklear.nk_window_set_focus(ctx, "finishGoBack");
		if (nk_begin(ctx, "finishGoBack", rect, Nuklear.NK_WINDOW_NO_SCROLLBAR | Nuklear.NK_WINDOW_NO_INPUT)) {

			nk_layout_row_dynamic(ctx, h, 1);
			goback.layout(ctx, stack);

		}
		nk_end(ctx);

		Nuklear.nk_style_pop_vec2(ctx);
		Nuklear.nk_style_pop_vec2(ctx);
	}
	
	public void setGoback(UIButton goBackVisual) {
		this.goback = goBackVisual;
	}
	
}
