package elem.interactions;

import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import elem.ui.IUIObject;
import elem.ui.UIButton;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import engine.io.Window;
import main.Features;
import player_local.Player;
import scenes.Scenes;

/**
 * 
 * @author Jhoffis
 *
 */
public class LobbyTopbar extends TransparentTopbar implements IUIObject {

	public static final float HEIGHT_RATIO = 7.11f;
	
	private UIWindowInfo playerInfoWindow, rightButtonsWindow;
	private Player myPlayer;
	private Player comparedStats;
	private float comparedCost;
	private final int rows = 3;
	private UIButton minimizeButton, closeButton, goBackButton, optionsButton;
	
	private UIWindowInfo subsceneTabsWindow;
	private UIButton[] subsceneTabs;

	private UIWindowInfo immediateWindow;
	private UILabel immediateUpgrade;

	public LobbyTopbar(Features features, UIButton minimizeButton, UIButton closeButton, TopbarInteraction topbar) {
		super(topbar, HEIGHT_RATIO);
		topbar.setHeightRatio(HEIGHT_RATIO);
		
		this.minimizeButton = minimizeButton;
		this.closeButton = closeButton;
		
		// Layout settings
		final float rightbuttonsWidth = 10;
		final float height = topbar.getHeight();
		
		float playerInfoRightButtonsSplitt = Window.WIDTH - Window.WIDTH / rightbuttonsWidth;
		System.out.println("WIDTH " + playerInfoRightButtonsSplitt / 2.2f);
		subsceneTabsWindow = UISceneInfo.createWindowInfo(Scenes.GENERAL_NONSCENE,
				0, 
				0, 
				playerInfoRightButtonsSplitt / 2.2f, 
				height / 3f);
		
		immediateWindow = UISceneInfo.createWindowInfo(Scenes.GENERAL_NONSCENE,
				TileUpgrade.size() * 2f, 
				subsceneTabsWindow.height, 
				subsceneTabsWindow.getXWidth() - TileUpgrade.size() * 2f, 
				height - subsceneTabsWindow.height);
		immediateUpgrade = new UILabel();
		
		playerInfoWindow = UISceneInfo.createWindowInfo(Scenes.GENERAL_NONSCENE,
				subsceneTabsWindow.getXWidth(), 
				0, 
				playerInfoRightButtonsSplitt - subsceneTabsWindow.getXWidth(), 
				height);
	
		rightButtonsWindow = UISceneInfo.createWindowInfo(Scenes.GENERAL_NONSCENE,
				playerInfoRightButtonsSplitt, 
				0, 
				Window.WIDTH / rightbuttonsWidth, 
				height);
		
		playerInfoWindow.visible = false;
		rightButtonsWindow.visible = false;

		topbar.setHeightRatio(22f);
	}

	public void layout(NkContext ctx, MemoryStack stack) {
		// Set own custom styling
		
		if (subsceneTabsWindow.begin(ctx)) {
			Nuklear.nk_layout_row_dynamic(ctx, subsceneTabsWindow.height * 0.9f, subsceneTabs.length);
			for (var btn : subsceneTabs) {
				btn.layout(ctx, stack);
			}
		}
		nk_end(ctx);
		
		NkColor green = NkColor.mallocStack(stack).set((byte) 0, (byte) 255,
				(byte) 0, (byte) 255);
		NkColor white = NkColor.mallocStack(stack).set((byte) 255, (byte) 255,
				(byte) 255, (byte) 255);
		NkColor red = NkColor.mallocStack(stack).set((byte) 255, (byte) 0,
				(byte) 0, (byte) 255);
		NkColor color = null;

		int height = getHeight() * 3 / 4;
		float refinedPositionX = getHeight() / 20f;

		if(playerInfoWindow.begin(ctx, stack, refinedPositionX, height / 3f * 0.65f, refinedPositionX, 0)) {

			// Layout
			if (myPlayer != null) {
				for (int y = 0; y < rows; y++) {
					nk_layout_row_dynamic(ctx, height / rows, 2);

					for (int x = 0; x < 2; x++) {
						String info = myPlayer.getInfo(comparedCost, comparedStats, x, y);

						int lastNumber = 0;
						String number = info.substring(info.length() - 1,
								info.length());
						if (number.matches("\\d+"))
							lastNumber = Integer.valueOf(number);

						switch (lastNumber) {
							case 1 :
								color = red;
								break;
							case 2 :
								color = green;
								break;
							default :
								color = white;
								break;

						}
						Nuklear.nk_style_push_color(ctx,
								ctx.style().text().color(), color);

						info = info.substring(0, info.length() - 1);

						Nuklear.nk_label(ctx, info, Nuklear.NK_TEXT_ALIGN_LEFT);

						Nuklear.nk_style_pop_color(ctx);
					}

				}
			}
		}
		nk_end(ctx);
		
		/*
		 * right buttons
		 */
		float refinedPositionY = height / 14;
		
		if(rightButtonsWindow.begin(ctx, stack, refinedPositionX, refinedPositionY, refinedPositionX, refinedPositionY)) {

			height = height / rows;
			// Layout
			nk_layout_row_dynamic(ctx, height, 2);
			minimizeButton.layout(ctx, stack);
			closeButton.layout(ctx, stack);
			nk_layout_row_dynamic(ctx, height, 1);
			goBackButton.layout(ctx, stack);
			nk_layout_row_dynamic(ctx, height, 1);
			optionsButton.layout(ctx, stack);
		}
		nk_end(ctx);
		
		if(immediateWindow.begin(ctx)) {
			nk_layout_row_dynamic(ctx, immediateWindow.height * 0.9f, 1);
			immediateUpgrade.layout(ctx, stack);
		}
		nk_end(ctx);
	}
	
	@Override
	public void select() {
		topbar.select(this);
	}

	public void setStats(Player myPlayer) {
		this.myPlayer = myPlayer;
	}

	public void compareStats(float cost, Player stats) {
		this.comparedStats = stats;
		this.comparedCost = cost;
	}

	public void uncompare() {
		this.comparedStats = null;
		this.comparedCost = 0;
	}

	public void setLobbyButtons(UIButton goBackButton,
			UIButton optionsButton) {
		this.goBackButton = goBackButton;
		this.optionsButton = optionsButton;
	}

	@Override
	public void setVisible(boolean visible) {
		if(playerInfoWindow == null || rightButtonsWindow == null)
			return;
		
		playerInfoWindow.visible = visible;
		rightButtonsWindow.visible = visible;
	}

	public void setSubscenes(UIButton[] subscenes) {
		this.subsceneTabs = subscenes;
	}

	public void setTabsVisible(boolean b) {
		subsceneTabsWindow.visible = b;
	}
	
	public float getSplitWidth() {
		return subsceneTabsWindow.width;
	}

}
