package elem.interactions;

import static org.lwjgl.nuklear.Nuklear.nk_end;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IStringAction;
import elem.objects.Sprite;
import elem.ui.IUIObject;
import elem.ui.UIFont;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import elem.upgrades.Layer;
import elem.upgrades.RegVals;
import elem.upgrades.Upgrade;
import elem.upgrades.Upgrades;
import engine.graphics.Renderer;
import engine.io.Window;
import engine.math.Vec2;
import engine.math.Vec3;
import player_local.Bank;
import player_local.BankType;
import player_local.Player;
import player_local.Car.Rep;
import scenes.Scenes;
import scenes.game.lobby_subscenes.UpgradesSubscene;

public class TileUpgrade extends Tile {
	
	private ArrayList<IStringAction> layoutTextAction = new ArrayList<>();
	private ArrayList<Boolean> layoutIsPlain = new ArrayList<>();
	private Upgrade upgrade;
	private UIWindowInfo window, windowShadow, saleWindow, saleWindowShadow;
	private UILabel priceLabel, saleLabel;
	private float dragX, dragY, diffOGX, diffOGY;
	private boolean dragOn, movable = true;
	private Vec2 layerPos;
	private Player player;
	
	private static final Vec3[] TileColors = {
		new Vec3(0),
		new Vec3(4f / 255f, 47f / 255f, 102f / 255f),
		new Vec3(184f / 255f, 46f / 255f, 8f / 255f),
		new Vec3(28f / 255f, 69f / 255f, 39f / 255f),
		new Vec3(0f / 255f, 141f / 255f, 145f / 255f),
		new Vec3(99f / 255f, 10f / 255f, 68f / 255f),
	};
	
	public TileUpgrade(Vec2 topleftPoint, Sprite normal, int modifier) {
		super.normalSprite = normal;
		init(modifier);
		
		movable = false;
		if (normal != null)
			initUILayout(topleftPoint);
	}
	
	public TileUpgrade(Player player, Vec2 topleftPoint, Upgrade upgrade, Sprite normal) {
		setUpgrade(player, upgrade);
		super.normalSprite = normal;
		init(0);
		
		if (normal != null)
			initUILayout(topleftPoint);
	}
	
	public void init(int modifier) {
		if (upgrade != null && player != null) {
			layoutTextAction.add(() -> "$" + upgrade.getCostMoney(player.getBank()));
			layoutIsPlain.add(false);
			int maxlvl = upgrade.getMaxLVL();
			if (maxlvl != 1) {
				layoutTextAction.add(() -> upgrade.getLVL() + (maxlvl != -1 ? "/" + maxlvl : ""));
				layoutIsPlain.add(true);
			}
		}

		if (modifier > 1) {
			layoutTextAction.add(() -> "x" + modifier);
			layoutIsPlain.add(true);
		}
	}
	
	public TileUpgrade() {
//		TODO setAsString(str);
	}
	
	private void initUILayout(Vec2 topleftPoint) {
		priceLabel = new UILabel();
		saleLabel = new UILabel();
		double x = topleftPoint.x, y = topleftPoint.y, w = normalSprite.getWidth(), h = normalSprite.getHeight();
		windowShadow = UISceneInfo.createWindowInfo(Scenes.LOBBY, x + w / 2f, y + h / 2f, w, h);
		window = UISceneInfo.createWindowInfo(Scenes.LOBBY, x, y, w, h);

		w = w / 2f;
		h = h / 2f;
		saleWindow = UISceneInfo.createWindowInfo(Scenes.LOBBY, x, y, w, h);
		saleWindowShadow = UISceneInfo.createWindowInfo(Scenes.LOBBY, x, y, w, h);
		saleLabel.setOptions(Nuklear.NK_TEXT_ALIGN_RIGHT | Nuklear.NK_TEXT_ALIGN_MIDDLE);
		
		setPos((float) x, (float) y);
	}
	
	@Override
	public TileUpgrade clone() {
		return this;
	}
	
	@Override
	public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		//(layerPos != null ? layer.getTimesMod(layerPos) : "x") + splitter +
		outString.append(movable ? 1 : 0);
		if (upgrade != null) {
			upgrade.getCloneString(outString, lvlDeep, splitter, test, all);
			outString.append(splitter + layerPos.x);
			outString.append(splitter + layerPos.y);
		} else
			outString.append(splitter + "x");
	}
	
	@Override
	public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
		movable = Integer.parseInt(cloneString[fromIndex.getAndIncrement()]) != 0;
		if (!cloneString[fromIndex.get()].equals("x")) {
			if (upgrade == null)
				upgrade = new Upgrade();
			upgrade.setCloneString(cloneString, fromIndex);
			layerPos = new Vec2(Float.parseFloat(cloneString[fromIndex.getAndIncrement()]),
					Float.parseFloat(cloneString[fromIndex.getAndIncrement()]));
			normalSprite = UpgradesSubscene.TileSprites[upgrade.getNameID()];
			if (normalSprite != null) {
				initUILayout(UpgradesSubscene.genRealPos(layerPos));
			}
		} else {
			// nulltile?
		}
	}

	public void renderUILayout(NkContext ctx, MemoryStack stack, Bank bank, UIFont immediateFont, UIFont saleFont) {

		windowShadow.focus = false;
		Nuklear.nk_style_push_font(ctx, immediateFont.getFont());
		if (windowShadow.begin(ctx))
			renderPrice(ctx, stack, bank, true);
		nk_end(ctx);
		
		if (window.begin(ctx))
			renderPrice(ctx, stack, bank, false);
		nk_end(ctx);
		Nuklear.nk_style_pop_font(ctx);

		if (upgrade != null && player != null && upgrade.hasSale(player.getBank())) {
			Nuklear.nk_style_push_font(ctx, saleFont.getFont());
			float height = saleFont.getHeight() * 1.1f;
			saleWindowShadow.focus = false;
			if (saleWindowShadow.begin(ctx))
				renderSale(ctx, stack, height, true);
			nk_end(ctx);
				
			if (saleWindow.begin(ctx))
				renderSale(ctx, stack, height, false);
			nk_end(ctx);
			Nuklear.nk_style_pop_font(ctx);
		}
	}
	
	private void renderPrice(NkContext ctx, MemoryStack stack, Bank bank, boolean shadow) {
		for (int i = 0; i < layoutTextAction.size(); i++) {
			String labelText = getText(i, bank, shadow);
			if (labelText != null) {
				Nuklear.nk_layout_row_dynamic(ctx, size() / 4f, 1);
				priceLabel.setText(labelText);
				priceLabel.layout(ctx, stack);
			}
		}
	}
	
	private void renderSale(NkContext ctx, MemoryStack stack, float height, boolean shadow) {
		Nuklear.nk_layout_row_dynamic(ctx, height, 1);
		int saleNr = (int) (-(1f - upgrade.getPrice().getSale(player.getBank(), upgrade.getNameID())) * 100f);
		StringBuilder saleText = new StringBuilder();
		if (saleNr > 0)
			saleText.append('+');
		saleText.append(saleNr).append('%');
		if (shadow)
			saleText.append("#BLACK");
		saleLabel.setText(saleText.toString());
		saleLabel.layout(ctx, stack);
	}
	
	@Override
	public void render(Renderer renderer) {
		if (upgrade != null) {
			if (placed) {
				Vec3 color = TileColors[upgrade.getLVL()];
				normalSprite.getShader().setUniform("improvedLVL", color);
			} else {
				normalSprite.getShader().setUniform("improvedLVL", TileColors[0]);
			}
			normalSprite.getShader().setUniform("playable", upgrade.isFullyUpgraded());
		}
		
		super.render(renderer);
	}
	
	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		boolean res = super.mouseButtonInput(button, action, x, y);
		
		if (action != GLFW.GLFW_RELEASE && movable && mouseAbove) {
			dragOn = true;
			dragX = x;
			dragY = y;
			diffOGX = diffX;
			diffOGY = diffY;
		} else if (dragOn) {
			dragOn = false;
			if (moveTileBuyAction != null && moveTileBuyAction.check(this, new Vec2(diffX, diffY))) {
				place();
			}
			else
				setPos(diffOGX, diffOGY);
		}
		
		return res;
	}
	
	public void place() {
		movable = false;
		placed = true;
		layoutTextAction.set(0, () -> "$" + upgrade.getCostMoney(player.getBank()));
		if (layoutTextAction.size() > 1) {
			// fjern LVL text
			layoutTextAction.remove(1);
			layoutIsPlain.remove(1);
		}
		upgrade.setUpgrade((regularValues, player, rep, gold, test) -> {
			var cloned = regularValues.clone();
			cloned.values[Rep.nosSize] = 0;
			cloned.upgrade(rep);
		});
	}

	@Override
	public boolean mousePosInput(float x, float y) {
		boolean res = super.mousePosInput(x, y);
		
		if (dragOn) {
			setPos(diffOGX - (dragX - x), diffOGY - (dragY - y));
			uiUpdateAction.check(this, new Vec2(diffX, diffY));
		}

		return res;
	}
	
	@Override
	public void setPos(float x, float y) {
		super.setPos(x, y);
		float shadow = size() / 48f;
		windowShadow.setPosition(diffX + shadow, diffY + shadow);
		window.setPosition(diffX, diffY);
		
		x = diffX + window.width / 2f;
		y = diffY - window.height / 10f;
		saleWindow.setPosition(x, y);
		saleWindowShadow.setPosition(x + shadow, y + shadow);
		placed = false;
	}
	
	@Override
	public void setPosY(float y) {
		super.setPosY(y);
		float shadow = size() / 48f;
		windowShadow.setPosition(diffX + shadow, diffY + shadow);
		window.setPosition(diffX, diffY);
		
		y = diffY - window.height / 10f;
		float x = saleWindow.x;
		saleWindow.setPosition(x, y);
		saleWindowShadow.setPosition(x + shadow, y + shadow);
		placed = false;
	}

	public int getUpgradeId() {
		return upgrade.getNameID();
	}

	public Upgrade getUpgrade() {
		return upgrade;
	}
	
	public String getText(int i, Bank bank, boolean shadow) {
		boolean notPlain = !layoutIsPlain.get(i);
		if (notPlain && upgrade.isFullyUpgraded())
			return null;

		StringBuilder res = new StringBuilder(layoutTextAction.get(i).getText());
		if (shadow) {
			res.append("#BLACK");
		} else if (notPlain) {
			res.append(bank.canAfford(upgrade.getCostMoney(player.getBank()), BankType.MONEY)
						? "#G"
						: "#R");
		}
		
		return res.toString(); 
	}

	public String getSaleText(int i, Player player) {
		int sale = ((int) (upgrade.getPrice().getSale(player.getBank(), i) * 100f) - 100);
		String saleText = "";
		
		if(sale != 0) {
			saleText = " " + (sale >= 0 ? "+" : "-") + Math.abs(sale) + "%";
		}
		return saleText;
	}

	public static float size() {
		return Window.HEIGHT / (float) (Upgrades.UPGRADE_NAMES.length - 1);
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setUpgrade(Player player, Upgrade upgrade) {
		this.player = player;
		this.upgrade = upgrade;
	}

	public void revertBonusLVL() {
		if (upgrade != null)
			upgrade.revertBonusLVL();
	}

	public IUIObject[] getInfo(Layer layer, Vec2 pos, boolean selected, boolean placed) {
		return (IUIObject[]) upgrade.getInfo(layer, this.layerPos != null ? this.layerPos : pos, mouseAbove && !selected, placed).toArray(new IUIObject[0]);
	}

	public boolean isMoving() {
		return dragOn;
	}

	public void modifyNeighbour(RegVals regularValues) {
		regularValues.combine(upgrade.getNeighbourModifier());
	}

	public void setLayerPos(Vec2 pos) {
		this.layerPos = pos;
	}

	public void setTimesMod(int timesMod) {
		if (timesMod > 1) {
			layoutTextAction.add(() -> "x" + timesMod);
			layoutIsPlain.add(true);
		}
	}

	public Vec2 getLayerPos() {
		return layerPos;
	}

	@Override
	public String getTileTypeId() {
		return Tile.tileUpgradeId;
	}

}
