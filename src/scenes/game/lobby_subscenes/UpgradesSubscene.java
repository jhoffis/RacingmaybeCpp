 package scenes.game.lobby_subscenes;

import java.util.HashMap;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import audio.SfxTypes;
import communication.Translator;
import elem.Animation;
import elem.ColorBytes;
import elem.Font;
import elem.interactions.LobbyTopbar;
import elem.interactions.Tile;
import elem.interactions.TileUpgrade;
import elem.objects.Sprite;
import elem.ui.IUIObject;
import elem.ui.IUIPressable;
import elem.ui.UIButton;
import elem.ui.UIFont;
import elem.ui.UISceneInfo;
import elem.ui.UIScrollable;
import elem.ui.UIWindowInfo;
import elem.ui.modal.UIBonusModal;
import elem.upgrades.ICheckPositionAction;
import elem.upgrades.Layer;
import elem.upgrades.Store;
import elem.upgrades.Upgrades;
import engine.graphics.Renderer;
import engine.io.Window;
import engine.math.Vec2;
import main.Features;
import main.Game;
import main.ResourceHandler;
import main.Texts;
import player_local.Player;
import scenes.Scenes;
import scenes.adt.Subscene;
import scenes.game.Lobby;

/**
 * 
 * visualizer for store - main communicator between the two.
 * 
 * @author Jens Benz
 *
 */

public class UpgradesSubscene extends Subscene {

	public static Sprite[] TileSprites;
	private static UIFont saleFont, immediateUpgradesFont;
	public static float marginX, marginY;
	private static TileUpgrade nullTile;
	
	public TileUpgrade CurrentUpgrade;
	private final HashMap<Integer, IAction> extraStateChangeActions;
	private TileUpgrade pressedTile;
	private UIScrollable upgradeDetails;
	private int detailsScrollIndex;
	private Store store;
	private Player player;
	private UIWindowInfo historyWindow;
	private final UIButton<Void> historyHomeBtn, historyEndBtn, historyFwdBtn, historyBckBtn, undoBtn;
	private final UIButton<TileUpgrade> improveTileBtn;
	
	private Sprite upgradeHelpAnimation;
	private boolean notPressedAnyUpgrade, upgradeHelpDown = true;
	private float upgradeHelpY;

	public UpgradesSubscene(Features features, int sceneIndex, LobbyTopbar topbar) {
		super(features, sceneIndex, "Shop");
		
		extraStateChangeActions = new HashMap<>();

		if (saleFont == null) {
//			costFont = new UIFont(Font.BOLD_ITALIC, Window.HEIGHT / 28);
			saleFont = new UIFont(Font.REGULAR, Window.HEIGHT / 58);
			immediateUpgradesFont = new UIFont(Font.BOLD_ITALIC, Window.HEIGHT / 44);
		}

		historyFwdBtn = new UIButton<>(Texts.historyFwd);
		historyBckBtn = new UIButton<>(Texts.historyBck);
		historyHomeBtn = new UIButton<>(Texts.historyHome);
		historyEndBtn = new UIButton<>(Texts.historyEnd);
		undoBtn = new UIButton<>(Texts.undo);
		improveTileBtn = new UIButton<>(Texts.improveUpgrade);
		
		ResourceHandler.LoadSprite(new Vec2(), Window.HEIGHT / 10f, "./images/arrow.png", "main", (s) -> {
			upgradeHelpAnimation = s;
		});

		var size = TileUpgrade.size();
		var pos = new Vec2(0);
		int len = Upgrades.UPGRADE_NAMES.length;

		// Ikke lag mer enn en gang
		if (TileSprites != null) return;
		TileSprites = new Sprite[Upgrades.UPGRADE_NAMES.length + 2];
		
		for (int i = 0; i < len; i++) {
			int upgradeID = i;
			ResourceHandler.LoadSprite(pos, size,
					"./images/upgrade/upgrade" + i + ".png", "upgrade", (sprite) -> TileSprites[upgradeID] = sprite); 
		}
		
		ResourceHandler.LoadSprite(pos, size, "./images/upgrade/upgradeNull.png", "upgrade", (s1) -> {
				TileSprites[len] = s1;					
				nullTile = new TileUpgrade(null, new Vec2(0), null, s1);
				ResourceHandler.LoadSprite(pos, size, "./images/upgrade/upgradeSelected.png", "upgrade", (s2) -> {
					TileSprites[len + 1] = s2;
				});
			});

	}

	@Override
	public void updateGenerally() {
		undoBtn.setEnabled(false);
	}

	@Override
	public void updateResolution() {
		if (player != null) {
			for (Tile btn : store.getAllTiles(player)) {
				btn.updateResolution();
			}
		}

		marginX = Store.FromX() + TileUpgrade.size() * 1.2f;
		marginY = Store.FromY() + TileUpgrade.size();
	}
	
	@Override
	protected void initDown(Lobby lobby, UIButton outNavigationBottom, UIButton outNavigationSide) {
	}

	public void initLobby(Store store, UIBonusModal bonusModal, UIButton readyBtn) {
		this.store = store;
		
		Consumer<IUIPressable> pressed = (pressable) -> {
			var t = (TileUpgrade) pressable;
			pressedTile = t;
			detailsScrollIndex = 0;
			showUpgrades(t, null);
			audio.getUpgrade(t.getUpgradeId()).play();
			notPressedAnyUpgrade = false;
 		};
		
		ICheckPositionAction moveTileBuy = (t, pos) -> {
			// om innenfor map; flytt XY til der du vil egt ha den, som ikke er 0.
			if (player.getRole() < Player.COMMENTATOR && t.getClass().equals(TileUpgrade.class) && (pos = checkTilePos(pos)) != null && player.isHistoryNow()) {
				if (attemptBuyUpgrade((TileUpgrade) t, pos, bonusModal, readyBtn)) {
					pos = genRealPos(pos);
					t.setPos(pos.x, pos.y);
					return true;
				} else {
					audio.get(SfxTypes.BUY_FAILED).play();				
				}
			}
			return false;
		};
		
		ICheckPositionAction uiUpdate = (t, pos) -> {
			if (t.getClass().equals(TileUpgrade.class)) {
				pos = checkTilePos(pos);
				showUpgrades((TileUpgrade) t, pos);
			}
			return false;
		};
		
		ICheckPositionAction mouseAbove = (tile, pos) -> {
			if (!tile.getClass().equals(TileUpgrade.class) || (CurrentUpgrade != null &&
					(CurrentUpgrade.isMoving())))
				return false;
			TileUpgrade t = (TileUpgrade) tile;
			pos = checkTilePos(pos);
			showUpgrades(t, pos);
				
			if (t.equals(pressedTile)) {
				upgradeDetails.setScrollIndex(detailsScrollIndex);
			}
			
			if (tile.placed) {
				improveTileBtn.hoverFake();
			}
			return false;
		};
		
		IAction hoverExit = () -> {
			if (!((CurrentUpgrade != null && CurrentUpgrade.isHovered()) && (pressedTile == null || !pressedTile.equals(CurrentUpgrade))))
				showUpgrades(pressedTile, null);
			upgradeDetails.setScrollIndex(detailsScrollIndex);
			improveTileBtn.unhover();
		};
		
		store.connectSubscene(this);
		Store.tileInit = (tile, player) -> {
			tile.setPressedAction(pressed);
			if (player.getRole() != Player.COMMENTATOR)
				tile.setMovedAction(moveTileBuy);
			tile.setHoverAction(Game.hoverAction);
			tile.setMouseAboveAction(mouseAbove);
			tile.setHoverExitAction(hoverExit);
			tile.setUpdateUI(uiUpdate);
		};
		
		historyFwdBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			if (!player.historyForward()) return;
			CurrentUpgrade = null;
			pressedTile = null;
			if (extraStateChangeActions.containsKey(Scenes.CURRENT))
				extraStateChangeActions.get(Scenes.CURRENT).run();
			store.resetTowardsPlayer(player);
		});
		
		historyBckBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			if (!player.historyBack()) return;
			CurrentUpgrade = null;
			pressedTile = null;
			if (extraStateChangeActions.containsKey(Scenes.CURRENT))
				extraStateChangeActions.get(Scenes.CURRENT).run();
			store.resetTowardsPlayer(player);
		});
		
		historyHomeBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			if (!player.historyBackHome()) return;
			CurrentUpgrade = null;
			pressedTile = null;
			if (extraStateChangeActions.containsKey(Scenes.CURRENT))
				extraStateChangeActions.get(Scenes.CURRENT).run();
			store.resetTowardsPlayer(player);
		});
		
		historyEndBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			if (!player.setHistoryNow()) return;
			CurrentUpgrade = null;
			pressedTile = null;
			if (extraStateChangeActions.containsKey(Scenes.CURRENT))
				extraStateChangeActions.get(Scenes.CURRENT).run();
			store.resetTowardsPlayer(player);
		});
		
		
		undoBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			CurrentUpgrade = null;
			pressedTile = null;
			player.undoHistory();
			store.resetTowardsPlayer(player);
			undoBtn.setEnabled(player.canUndoHistory());
			lobby.getCom().undoHistory(player);
		});
		
		improveTileBtn.setPressedAction((tile) -> {
			var pos = tile.getLayerPos();
			int result = store.buyTileAction.buy(player, tile.getUpgrade(), pos);
			if (result > 0) {
				reactAfterBuy(tile, pos, bonusModal, readyBtn, result);
			}
		});
		
		add(historyFwdBtn);
		add(historyBckBtn);
		add(historyHomeBtn);
		add(historyEndBtn);
		add(improveTileBtn);
		add(undoBtn);
	}
	
	public void add(int sceneIndex, IAction extraStateChangeAction) {
		extraStateChangeActions.put(sceneIndex, extraStateChangeAction);
		UISceneInfo.addPressableToScene(sceneIndex, historyFwdBtn);
		UISceneInfo.addPressableToScene(sceneIndex, historyBckBtn);
		UISceneInfo.addPressableToScene(sceneIndex, historyHomeBtn);
		UISceneInfo.addPressableToScene(sceneIndex, historyEndBtn);
		UISceneInfo.addWindowToScene(sceneIndex, upgradeDetails.getWindow());
		UISceneInfo.addWindowToScene(sceneIndex, historyWindow);
	}
	
	private void showUpgrades(TileUpgrade tile, Vec2 pos) {
		CurrentUpgrade = tile;
		boolean selected = tile != null && pressedTile != null && tile.equals(pressedTile);
		boolean placed = tile != null && tile.placed;
		if (tile != null) {
			upgradeDetails.setText(tile.getInfo(player.getLayer(), pos, selected, placed));
			
			if (player.getRole() < Player.COMMENTATOR && player.isHistoryNow() && tile.placed && !tile.getUpgrade().isFullyUpgraded()) {
				upgradeDetails.addObject(improveTileBtn);
				improveTileBtn.setConsumerValue(tile);
			}
		}
		if (lobby != null)
			lobby.showUpgrades(tile, pos, selected, placed);
	}

	public static Vec2 genRealPos(Vec2 pos) {
		return new Vec2(marginX + (pos.x * TileUpgrade.size()),
				marginY + (pos.y * TileUpgrade.size()));
	}

	private Vec2 checkTilePos(Vec2 pos) {
		float size = TileUpgrade.size();
		Vec2 res = null;
		float xL = Store.FromX() + (size * Layer.w) + (size / 2);
		float xG = marginX - size / 2;
		float yL = Store.FromY() + (size * Layer.h) + (size / 2);
		float yG = marginY - size / 2;

		if ((pos.x < xL
		  && pos.x > xG) &&
		 	(pos.y < yL
		  && pos.y > yG)) {
			// sjekk X ved 0
			int newX = (int) ((pos.x + size / 2f - marginX) / size);
			if (newX < 0 || newX >= Layer.w) return null;
			int newY = (int) ((pos.y + size / 2f - marginY) / size);
			if (newY < 0 || newY >= Layer.h) return null;
			res = new Vec2(newX, newY);
		}
		return res;
	}
	
	private boolean attemptBuyUpgrade(TileUpgrade tile, Vec2 pos, UIBonusModal bonusModal, UIButton readyBtn) {
		if (!player.getLayer().isOpen(pos)) return false;
		
		bonusModal.setInitialUpgrade(player.upgrades.getUpgrade(tile.getUpgradeId()));
		int ret = store.attemptBuyTile(player, tile, pos);
		if (ret > 0) {
			player.addTile(tile, pos);
			tile.setLayerPos(pos);
			tile.setTimesMod(player.getLayer().getTimesMod(pos));
			tile.getUpgrade().place(ret == 1);			
		
			store.removeTile(tile);
			store.createNeededTiles(player);
			reactAfterBuy(tile, pos, bonusModal, readyBtn, ret);
			return true;
		}
		return false;
	}
	
	private void reactAfterBuy(TileUpgrade tile, Vec2 pos, UIBonusModal bonusModal, UIButton readyBtn, int result) {
		if (result == 1) {
			audio.get(SfxTypes.BUY).play();
			lobby.getCom().updateCloneToServer(player, Translator.getCloneString(player, false, true), 0);
			undoBtn.setEnabled(player.canUndoHistory());
			if (!tile.placed)
				tile.placed = true;
			showUpgrades(tile, pos);
		} else {
			// Actually before tile is bought because you must select gold or normal bonus
			audio.get(SfxTypes.NEW_BONUS).play();
			bonusModal.setVisible(true);
			bonusModal.setCombination(player);
			bonusModal.setBankAndOriginalGoldAmount(player.getBank(), player.getBank().getGold());
			bonusModal.press();
		}
		
		if (player.getReady() == 1) {
			// TODO sentraliser denne i lobby
			player.setReady((byte) 0);
			readyBtn.setTitle(Texts.readyText);
			audio.get(SfxTypes.UNREADY).play();
			lobby.getCom().ready(player, player.getReady());
		}
	}
	
	/**
	 * Runs after you've bought and potentially not canceled a chain of bonus choices.
	 */
	public void reactBonus(boolean successful) {
		if (successful) {
			undoBtn.setEnabled(player.canUndoHistory());
			store.createNeededTiles(player);
		}
	}

	public void setPlayer(Player player) {
		this.player = player;
		store.resetTowardsPlayer(player);
	}
	
	public void afterJoined(Player player) {
		this.player = player;

		if (Game.DEBUG)
			player.getBank().addSale(0.5f, 0, 0);
		
		notPressedAnyUpgrade = true;
	}

	@Override
	public void createWindowsWithinBounds(float x, float y, float width,
			float height) {
		x = TileUpgrade.size() / (Upgrades.UPGRADE_NAMES.length * 0.8f);
		y = Store.FromY();
		float w = Store.FromX() - x * 2f;
		upgradeDetails = new UIScrollable(sceneIndex, x,
				y, w,
				Window.HEIGHT - y - TileUpgrade.size() / 2f);
		upgradeDetails.addScrollingAction((rowsFrom) -> {
			if (CurrentUpgrade != null && pressedTile != null && CurrentUpgrade.equals(pressedTile))
				detailsScrollIndex = rowsFrom;
		});

		x += x + w;
		width -= x;
		
		height = TileUpgrade.size() / 2;
		y = Window.HEIGHT - height;
		historyWindow = createWindow(x, y, width, height);
	}
	
	// private void upgradeSelect(int upgradeIndex) {
	// press();
	// storeTiles.get(upgradeIndex).setSelected(true);
	// storeTiles.get(upgradeIndex).runPressedAction();
	// }

	@Override
	public void keyInput(int keycode, int action) {
		if (action != GLFW.GLFW_RELEASE && CurrentUpgrade != null && CurrentUpgrade.isHovered() && CurrentUpgrade.placed) {
			if (keycode == GLFW.GLFW_KEY_SPACE)
				improveTileBtn.runPressedAction();
		}
	}

	@Override
	public void mouseScrollInput(float x, float y) {
		if (CurrentUpgrade != null && CurrentUpgrade.isHovered())
			upgradeDetails.getWindow().focus = true;
		upgradeDetails.scroll(y);
	}

	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			boolean hitATile = false;
			for (var btn : store.getAllTiles(player)) {
				if (hitATile = btn.mouseButtonInput(button, action, x, y)) {
					break;
				}
			}
			if (action == GLFW.GLFW_RELEASE && !hitATile && !upgradeDetails.getWindow().focus)
				showUpgrades(null, null);
		}
		return false;
	}

	@Override
	public void mousePosInput(float x, float y) {
		for (var btn : store.getAllTiles(player)) {
			if (btn.mousePosInput(x, y))
				break;
		}
	}

	@Override
	public void tick(float delta) {
	}

	/**
	 * Use me to render the engine
	 */
	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		// renderer.renderMesh(car, camera);

		if (player == null) return;
		
		var tiles = player.getLayer().getDobArr();
		for (int x = 0; x < tiles.length; x++) {
			for (int y = 0; y < tiles[x].length; y++) {
				if (tiles[x][y] != null) {
					tiles[x][y].render(renderer);
				} else {
					nullTile.setPos(marginX + (x * TileUpgrade.size()), marginY + (y * TileUpgrade.size()));
					nullTile.render(renderer);
				}
			}
		}


		for (var btn : store.getAllTiles(player)) {
			btn.render(renderer);
		}
		
		if (notPressedAnyUpgrade) {
			if (upgradeHelpDown) {
				upgradeHelpY += 1.2 * delta;
				if (upgradeHelpY > 20)
					upgradeHelpDown = !upgradeHelpDown; 
			} else {
				upgradeHelpY -= 1.2 * delta;
				if (upgradeHelpY < 0)
					upgradeHelpDown = !upgradeHelpDown; 
			}
			upgradeHelpAnimation.setPosition(new Vec2(marginX - TileUpgrade.size(), marginY + upgradeHelpY));
			upgradeHelpAnimation.updateTransformation();
			renderer.renderOrthoMesh(upgradeHelpAnimation);
		}

		if (CurrentUpgrade != null) {
			CurrentUpgrade.renderSelected(renderer, getSelected(), false);
		}
		
		if (pressedTile != null && (CurrentUpgrade == null || !CurrentUpgrade.equals(pressedTile))) {
			pressedTile.renderSelected(renderer, getSelected(), true);
		}
	}

	/**
	 * Use me to render the buttons and stuff
	 */
	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		if (player == null) return;
		
		for (var btn : store.getStoreTiles()) {
			btn.renderUILayout(ctx, stack, player.getBank(), immediateUpgradesFont, saleFont);
		}
		var tiles = player.getLayer().getDobArr();
		int ranName = 0;
		for (int x = 0; x < Layer.w; x++) {
			for (int y = 0; y < Layer.h; y++) {
				if (tiles[x][y] != null) {
					tiles[x][y].renderUILayout(ctx, stack, player.getBank(), immediateUpgradesFont, saleFont);
				} else if (player.getLayer().hasTimesMod(x, y)) {
					NkRect rect = NkRect.mallocStack(stack);

					var pos = checkTilePos(new Vec2(marginX + (TileUpgrade.size() * x),
							marginY + (TileUpgrade.size() * y)));
					if (pos == null) continue;
					pos = genRealPos(pos);

					rect.x(pos.x).y(pos.y).w(TileUpgrade.size()).h(TileUpgrade.size());
					if(Nuklear.nk_begin(ctx, "timesModRanName" + ranName++, rect, Nuklear.NK_WINDOW_NO_INPUT)) {
						Nuklear.nk_layout_row_dynamic(ctx, immediateUpgradesFont.getHeight(), 1);
						Nuklear.nk_label(ctx, "x" + player.getLayer().getTimesMod(x, y), Nuklear.NK_TEXT_ALIGN_LEFT);
					}
					Nuklear.nk_end(ctx);
				}
			}
		}

		features.pushBackgroundColor(ctx, new ColorBytes(0, 0, 0, 0x86));
		upgradeDetails.layout(ctx, stack);
		features.popBackgroundColor(ctx);
	
		if (historyWindow.begin(ctx)) {
			Nuklear.nk_layout_row_dynamic(ctx, historyWindow.height * 0.85f, player.getRole() < Player.COMMENTATOR ? 5 : 4);
			historyHomeBtn.layout(ctx, stack);
			historyBckBtn.layout(ctx, stack);
			historyFwdBtn.layout(ctx, stack);
			historyEndBtn.layout(ctx, stack);
			if (player.getRole() < Player.COMMENTATOR)
				undoBtn.layout(ctx, stack);
		}
		Nuklear.nk_end(ctx);
	}

	@Override
	public void mousePositionInput(float x, float y) {
	}

	public void setUpgradeDetails(IUIObject[] details) {
		upgradeDetails.setText(details);
	}

	public void removeLastUpgrade() {
//		audio.get(SfxTypes.BUY_FAILED).play();
		player.getLayer().remove(CurrentUpgrade);
		// FIXME trykking av cancel og så endre CurrentUpgrade før du får slettet den du la ned.
		CurrentUpgrade = null;
		pressedTile = null;
	}

	@Override
	public void createBackground() {
		ResourceHandler.LoadSprite("./images/back/back.jpg", "background", (sprite) -> backgroundImage = sprite);
	}

	@Override
	public UIButton intoNavigationSide() {
		return null;
	}

	@Override
	public UIButton intoNavigationBottom() {
		return null;
	}

	@Override
	public void setVisible(boolean visible) {
		if (upgradeDetails != null)
			upgradeDetails.getWindow().visible = visible;
	}

	public Sprite getSelected() {
		return TileSprites[TileSprites.length - 1];
	}

	@Override
	public void destroy() {
		for (var tileSprite : TileSprites) {
			tileSprite.destroy();
		}
	}

	public void reset() {
		CurrentUpgrade = null;
		pressedTile = null;
		undoBtn.setEnabled(false);
	}

}
