package elem.ui.modal;

import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_INPUT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_NO_SCROLLBAR;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_group_begin;
import static org.lwjgl.nuklear.Nuklear.nk_group_end;
import static org.lwjgl.nuklear.Nuklear.nk_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_style_pop_vec2;
import static org.lwjgl.nuklear.Nuklear.nk_style_push_vec2;

import java.util.Stack;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import communication.Communicator;
import communication.Translator;
import elem.ColorBytes;
import elem.Font;
import elem.interactions.LobbyTopbar;
import elem.ui.IUIObject;
import elem.ui.IUIPressable;
import elem.ui.UIButton;
import elem.ui.UIFont;
import elem.ui.UISceneInfo;
import elem.ui.UIWindowInfo;
import elem.upgrades.ChosenBonus;
import elem.upgrades.Upgrade;
import elem.upgrades.Upgrades;
import engine.io.Window;
import main.Features;
import main.Texts;
import player_local.Bank;
import player_local.Player;
import scenes.Scenes;

public class UIBonusModal implements IUIObject, IUIPressable {

	private UIFont titleFont;
	private Features features;
	private final UIButton<Integer> goldBtn, normalBtn, cancelBtn;
	private UIButton<Integer> lastBtn;
	private UIWindowInfo window;

	private final Stack<Integer> originalGoldAmount = new Stack<>();
	private final Stack<ChosenBonus> chosenBonuses = new Stack<>();
	public final Stack<Upgrade> upgrades = new Stack<>();
	private Player combination;
	private boolean freeBonus;
	private Upgrade initialUpgrade;
	private String revertState;

	public UIBonusModal(Features features) {
		this.features = features;

		// Buttons
		goldBtn = new UIButton<>("");
		normalBtn = new UIButton<>("");
		cancelBtn = new UIButton<>(Texts.exitCancelText);
		lastBtn = normalBtn;

		window = UISceneInfo.createWindowInfo(Scenes.LOBBY,
				0, 
				Window.HEIGHT / LobbyTopbar.HEIGHT_RATIO, 
				Window.WIDTH, 
				Window.HEIGHT - Window.HEIGHT / LobbyTopbar.HEIGHT_RATIO);
		window.visible = false;
		window.z = 1;
		
		titleFont = new UIFont(Font.BOLD_ITALIC, 72);
	}
	
	public void init(Consumer<Integer> okAction, IAction cancelAction) {
		goldBtn.setPressedAction(okAction);
		goldBtn.setConsumerValue(1);
		normalBtn.setPressedAction(okAction);
		normalBtn.setConsumerValue(0);
		cancelBtn.setPressedAction(cancelAction);
	}

	@Override
	public void layout(NkContext ctx, MemoryStack stack) {

		if (!upgrades.isEmpty()) {
			var upgrade = upgrades.peek();
			
			// Create a rectangle for the window
			features.pushBackgroundColor(ctx, new ColorBytes(0x00, 0x00, 0x00, 0x66));
			if(window.begin(ctx)) {
				// Set own custom styling
				NkVec2 spacing = NkVec2.mallocStack(stack);
				NkVec2 padding = NkVec2.mallocStack(stack);

				float sp = Window.WIDTH / 30f;
				spacing.set(sp, 0);
				padding.set(sp * 2f, sp);

				nk_style_push_vec2(ctx, ctx.style().window().spacing(), spacing);
				nk_style_push_vec2(ctx, ctx.style().window().group_padding(), padding);

				float height = window.height;
				float heightElements = height / 10;

				// Height of group
				nk_layout_row_dynamic(ctx, height, 1);

				features.pushBackgroundColor(ctx, new ColorBytes(0x00, 0x00, 0x00, 0xdd));

				if (nk_group_begin(ctx, "ExitGroup", UIWindowInfo.OPTIONS_STANDARD)) {

					Nuklear.nk_style_push_font(ctx, titleFont.getFont());
					nk_layout_row_dynamic(ctx, titleFont.getHeight() * 1.1f, 1);
					nk_label(ctx, "\"" + Upgrades.UPGRADE_NAMES[upgrade.getNameID()] + "\" Bonus LVL "
							+ upgrade.getBonusLVLs()[upgrade.getBonusLVL()] + ":", Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_MIDDLE);
					Nuklear.nk_style_pop_font(ctx);
					nk_layout_row_dynamic(ctx, heightElements, 1);

					nk_layout_row_dynamic(ctx, heightElements / 2, 1);
					nk_label(ctx, "    The Left \"" + Texts.goldBonus + "\" option is stronger but costs " + upgrade.getGoldCost(upgrade.getBonusLVL())
							+ " gold!", Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_LEFT);
					nk_layout_row_dynamic(ctx, heightElements / 2, 1);
					nk_label(ctx, "    The Right \"" + Texts.normalBonus + "\" option is weaker but gives 1 gold!", Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_LEFT);

					nk_layout_row_dynamic(ctx, heightElements / 4f, 1);
					nk_layout_row_dynamic(ctx, heightElements / 2, 1);
					nk_label(ctx, "(You get gold mainly through choosing " + Texts.normalBonus + " whenever a bonus appears)", Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_LEFT);
					nk_layout_row_dynamic(ctx, heightElements / 2, 1);
					nk_label(ctx, "(Choosing \"" + Texts.goldBonus + "\" increases gold-cost of this bonus the next turn, whereas \"normal\" decreases it)", Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_LEFT);

					// SPACE
					nk_layout_row_dynamic(ctx, heightElements * 2 / 3, 1);

					String[] goldLabels = Upgrades.bonusesTextsGold[upgrade.getNameID()][upgrade.getBonusLVL()].split("\n");
					String[] normalLabels = Upgrades.bonusesTexts[upgrade.getNameID()][upgrade.getBonusLVL()].split("\n");

					int i = 0;
					while (i < goldLabels.length || i < normalLabels.length) {

						nk_layout_row_dynamic(ctx, heightElements / 2, 2);
						if (i < goldLabels.length)
							nk_label(ctx, goldLabels[i], NK_TEXT_ALIGN_LEFT);
						else
							nk_label(ctx, "", NK_TEXT_ALIGN_LEFT);

						if (i < normalLabels.length)
							nk_label(ctx, normalLabels[i], NK_TEXT_ALIGN_LEFT);
						i++;

					}

					// SPACE
					nk_layout_row_dynamic(ctx, heightElements * 2 / 3, 1);
					nk_label(ctx, "", Nuklear.NK_TEXT_ALIGN_CENTERED);

					nk_layout_row_dynamic(ctx, heightElements, 2);
					goldBtn.layout(ctx, stack);
					normalBtn.layout(ctx, stack);
					nk_layout_row_dynamic(ctx, heightElements / 12f, 1);
					nk_layout_row_dynamic(ctx, heightElements, 1);
					cancelBtn.layout(ctx, stack);

					// Unlike the window, the _end() function must be inside
					// the if() block
					nk_group_end(ctx);
				}

				features.popBackgroundColor(ctx);

				// Reset styling
				nk_style_pop_vec2(ctx);
				nk_style_pop_vec2(ctx);

			}
			nk_end(ctx);

			features.popBackgroundColor(ctx);
		}
	}

	public void pushUpgrade(Upgrade upgrade) {
		if(upgrade != null) {
			upgrades.push(upgrade);
			goldBtn.setTitle(Texts.goldBonus + " " + -upgrade.getGoldCost(upgrade.getBonusLVL()));
			normalBtn.setTitle(Texts.normalBonus + " +1");
		}
	}

	public Upgrade popUpgrade() {
		if (upgrades.isEmpty())
			return null;
		return upgrades.pop();
	}

	public boolean isVisible() {
		return window.visible;
	}

	public void setVisible(boolean visible) {
		window.visible = visible;

		if (visible)
			press();
	}

	public void input(int keycode, int action) {

		if (action != GLFW.GLFW_RELEASE) {
			if (keycode == GLFW.GLFW_KEY_LEFT) {
				goldBtn.hover();
				lastBtn = goldBtn;
			} else if (keycode == GLFW.GLFW_KEY_RIGHT) {
				normalBtn.hover();
				lastBtn = normalBtn;
			} else if (keycode == GLFW.GLFW_KEY_UP && lastBtn != null) {
				lastBtn.hover();
			} else if (keycode == GLFW.GLFW_KEY_DOWN) {
				cancelBtn.hover();
			} else if (keycode == GLFW.GLFW_KEY_ENTER) {
				UISceneInfo.getHoveredButton(Scenes.GENERAL_NONSCENE).runPressedAction();
				UISceneInfo.clearHoveredButton(Scenes.GENERAL_NONSCENE);
			}
		}
	}

	public void combine(Player player) {
		if (player != null && combination != null)
			player.setClone(combination.upgrades, combination.getBank(), combination.getLayer(), combination.getCarRep());
		else
			System.out.println("ERROR rep or combination is null");
		setCombination(null);
	}

	public void setCombination(Player player) {
		if (player == null) {
			this.combination = null;
			chosenBonuses.clear();
			originalGoldAmount.clear();
		} else if (this.combination == null) {
			this.combination = player.getClone();
			this.revertState = player.peekHistory();
		}
	}

	public void revert(Player player) {
		if (revertState != null)
			Translator.setCloneString(player, revertState);
	}

	public Player getCombination() {
		return combination;
	}

	public void setBankAndOriginalGoldAmount(Bank bank, int gold) {
		combination.setBank(bank);
		originalGoldAmount.push(gold);
	}

	public Stack<Integer> getOriginalGoldAmount() {
		return originalGoldAmount;
	}

	public void pushBonusChoice(int nameID, int bonusLVL, boolean gold) {
		chosenBonuses.push(new ChosenBonus(nameID, bonusLVL, gold));
	}

	public void updateServerBonuses(Player player, Communicator com) {
		while (chosenBonuses.isEmpty() == false) {
			ChosenBonus bonus = chosenBonuses.pop();
			com.upgradeGold(player, bonus.getNameID(), bonus.getBonusLVL(), bonus.isGold());
		}
	}

	@Override
	public void press() {
		goldBtn.press();
		normalBtn.press();
		cancelBtn.press();
	}

	@Override
	public void release() {
		goldBtn.release();
		normalBtn.release();
		cancelBtn.release();
	}

	public void setFreeBonus(boolean b) {
		freeBonus = b;
	}

	public boolean isFreeBonus() {
		return freeBonus;
	}

	public void close() {
		combination = null;
		window.visible = false;
		upgrades.clear();		
	}
	
	public void setInitialUpgrade(Upgrade upgrade) {
		initialUpgrade = upgrade;
	}

	public Upgrade getInitialUpgrade() {
		return initialUpgrade;
	}

}
