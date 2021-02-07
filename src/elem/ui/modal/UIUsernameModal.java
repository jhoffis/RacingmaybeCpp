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

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import audio.AudioRemote;
import audio.SfxTypes;
import elem.ColorBytes;
import elem.ui.IUIObject;
import elem.ui.IUIPressable;
import elem.ui.UIButton;
import elem.ui.UILabel;
import elem.ui.UISceneInfo;
import elem.ui.UITextField;
import elem.ui.UIWindowInfo;
import engine.io.Window;
import main.Features;
import main.Texts;
import player_local.Player;
import scenes.Scenes;

public class UIUsernameModal implements IUIPressable, IUIObject {

	private Features features;
	private AudioRemote audio;
	private UIWindowInfo window;
	private String modalTitle;
	private UILabel usernameLabel, titleLabel, privatePublicLabel;
	private UITextField usernameField, titleField;
	private float fieldHeight;
	private final UIButton okBtn, cancelBtn, privatePublicBtn, amountPlayersBtn, spectatorBtn;
	private boolean create;
	
	private boolean publicLobby, spectator;
	private int amountPlayers;
	private final int amountPlayersMax = 8;

	public UIUsernameModal(Features features, AudioRemote audio) {
		this.audio = audio;
		usernameLabel = new UILabel("Username:");
		titleLabel = new UILabel("Lobbyname:");
		privatePublicLabel = new UILabel("");
		
		float x = (float) Window.WIDTH / 4.65f, y = (float) Window.HEIGHT / 3.16f, w = (float) Window.WIDTH / 1.546f; 
		fieldHeight = Window.HEIGHT / 22;
		
		final int usernameLength = 16;
		usernameField = new UITextField(features, "", false, false, usernameLength, Scenes.GENERAL_NONSCENE,
				x, 
				y,
				w, 
				fieldHeight);
		usernameField.getWindow().z = 2;
		
		titleField = new UITextField(features, "", false, false, 32, Scenes.GENERAL_NONSCENE,
				x, 
				y + (fieldHeight * 1.1f),
				w, 
				fieldHeight);
		titleField.getWindow().z = 2;
		
		this.features = features;

		// Buttons
		okBtn = new UIButton(Texts.exitOKText);
		cancelBtn = new UIButton(Texts.exitCancelText);
		privatePublicBtn = new UIButton(Texts.privateText);
		amountPlayersBtn = new UIButton("");
		spectatorBtn = new UIButton(Texts.spectator + "?");
		
		privatePublicBtn.setPressedAction(() -> setPrivatePublic(!publicLobby));
		amountPlayersBtn.setPressedAction(() -> setAmountPlayers((amountPlayers) % amountPlayersMax + 1));
		amountPlayersBtn.setPressedActionRight(() -> {
			if (amountPlayers == 1)
				amountPlayers = 5;
			setAmountPlayers(amountPlayers - 1);
		});
		spectatorBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			spectator = !spectator;
			spectatorBtn.setTitle((spectator ? Texts.player : Texts.spectator) + "?");
		});

		window = UISceneInfo.createWindowInfo(Scenes.GENERAL_NONSCENE,
				0, 
				0, 
				Window.WIDTH, 
				Window.HEIGHT);
		window.visible = false;
		window.z = 2;

		UISceneInfo.addPressableToScene(Scenes.GENERAL_NONSCENE, this);
		UISceneInfo.setChangeHoveredButtonAction(Scenes.GENERAL_NONSCENE, okBtn);
		UISceneInfo.setChangeHoveredButtonAction(Scenes.GENERAL_NONSCENE, cancelBtn);
		UISceneInfo.setChangeHoveredButtonAction(Scenes.GENERAL_NONSCENE, privatePublicBtn);
		UISceneInfo.setChangeHoveredButtonAction(Scenes.GENERAL_NONSCENE, spectatorBtn);
	}
	
	public void setButtonActions() {
		okBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			String name = getInputText();

			if (name == null || name.isBlank()) {
				setLabel("Don't leave your name empty! Try again below:");
				return;
			}

			if (name.length() > 16) {
				setLabel("Your name is too long! Max 16 chars! Try again below:");
				return;
			}

			features.getSettings().setUsername(getInputText());

			setLabel("Joining... consider canceling if 15 seconds goes by");

			if (create) {
				features.createNewLobby(name, spectator ? Player.COMMENTATOR : Player.HOST, titleField.getText(), publicLobby, amountPlayers);
			} else {
				features.joinNewLobby(name, spectator ? Player.COMMENTATOR : Player.PLAYER);
			}
		});
		cancelBtn.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			setVisible(false, false);
			features.leave();
		});
	}
	
	@Override
	public void layout(NkContext ctx, MemoryStack stack) {
		features.pushBackgroundColor(ctx,
				new ColorBytes(0x00, 0x00, 0x00, 0x66));
		if (window.begin(ctx)) {
			// Set own custom styling
			NkVec2 spacing = NkVec2.mallocStack(stack);
			NkVec2 padding = NkVec2.mallocStack(stack);

			float sp = Window.WIDTH / 30f;
			spacing.set(sp, 0);
			padding.set(sp * 2f, sp);
			

			nk_style_push_vec2(ctx, ctx.style().window().spacing(),
					spacing);
			nk_style_push_vec2(ctx, ctx.style().window().group_padding(),
					padding);

			int height = Window.HEIGHT * 2 / 5;
			int heightElements = height / 4;

			// Move group down a bit
			nk_layout_row_dynamic(ctx, height / 2, 1);

			// Height of group
			nk_layout_row_dynamic(ctx, height, 1);

			features.pushBackgroundColor(ctx,
					new ColorBytes(0x00, 0x00, 0x00, 0xFF));

			if (nk_group_begin(ctx, "ExitGroup", UIWindowInfo.OPTIONS_STANDARD)) {
				if (create) {
					nk_layout_row_dynamic(ctx, heightElements / 2, 1);
					nk_label(ctx, modalTitle, NK_TEXT_ALIGN_LEFT);
	
					nk_layout_row_dynamic(ctx, fieldHeight, 2);
					usernameLabel.layout(ctx, stack);
	
					nk_layout_row_dynamic(ctx, fieldHeight, 2);
					titleLabel.layout(ctx, stack);
	
					nk_layout_row_dynamic(ctx, heightElements / 10, 1);	
					nk_layout_row_dynamic(ctx, heightElements / 2, 4);
					privatePublicBtn.layout(ctx, stack);
					privatePublicLabel.layout(ctx, stack);
					amountPlayersBtn.layout(ctx, stack);
					spectatorBtn.layout(ctx, stack);
	
					nk_layout_row_dynamic(ctx, heightElements / 10, 1);	
				} else {
					nk_layout_row_dynamic(ctx, heightElements, 1);
					nk_label(ctx, modalTitle, NK_TEXT_ALIGN_LEFT);

					nk_layout_row_dynamic(ctx, heightElements, 1);
					usernameField.layoutTextfieldItself(ctx);
				}
				
				nk_layout_row_dynamic(ctx, heightElements, 2);
				okBtn.layout(ctx, stack);
				cancelBtn.layout(ctx, stack);

				// Unlike the window, the _end() function must be inside
				// the if() block
				nk_group_end(ctx);
			}

			features.popBackgroundColor(ctx);

			// Reset styling
			nk_style_pop_vec2(ctx);
			nk_style_pop_vec2(ctx);

		} else {
			nk_end(ctx);
			features.popBackgroundColor(ctx); // not visible
			return;
		}
		nk_end(ctx);
		features.popBackgroundColor(ctx);
		
		if (!create) return;
		usernameField.getWindow().focus = true;
		usernameField.layout(ctx, stack);

		titleField.getWindow().focus = true;
		titleField.layout(ctx, stack);
	}

	public void release() {
		okBtn.release();
		cancelBtn.release();
		privatePublicBtn.release();
		amountPlayersBtn.release();
		spectatorBtn.release();
	}

	public void press() {
		okBtn.press();
		cancelBtn.press();
		privatePublicBtn.press();
		amountPlayersBtn.press();
		spectatorBtn.press();
	}

	public boolean isVisible() {
		return window.visible;
	}

	public void setVisible(boolean visible, boolean create) {
		window.visible = visible;
		usernameField.getWindow().visible = visible;
		titleField.getWindow().visible = visible;

		if (visible) {
			window.focus = true;
			this.create = create;
			spectator = false;
			
			if (create) {
				modalTitle = Texts.createOnlineText + ":";
				titleField.setText(features.getUsername() + "'s game");
				setPrivatePublic(true);
				setAmountPlayers(2); // 2 players
			} else {
				modalTitle = Texts.usernameText;
			}

			usernameField.focus(true);
			press();
			okBtn.hover();
		} else {
			UISceneInfo.clearHoveredButton(Scenes.GENERAL_NONSCENE);
		}
	}

	public UIButton getCancelBtn() {
		return cancelBtn;
	}

	public void setLabel(String string) {
		this.modalTitle = string;
	}

	public String getInputText() {
		return usernameField.getText().trim();
	}

	public void input(int keycode, int action) {

		usernameField.input(keycode, action);
		titleField.input(keycode, action);
		
		switch (keycode) {
			case GLFW.GLFW_KEY_UP :
			case GLFW.GLFW_KEY_LEFT :
				okBtn.hover();
				break;
			case GLFW.GLFW_KEY_DOWN :
			case GLFW.GLFW_KEY_RIGHT :
				getCancelBtn().hover();
				break;
			case GLFW.GLFW_KEY_ENTER :
				var btn = UISceneInfo.getHoveredButton(Scenes.GENERAL_NONSCENE);
				if (btn == null)
					btn = okBtn;
				btn.runPressedAction();
				UISceneInfo.clearHoveredButton(Scenes.GENERAL_NONSCENE);
				break;
		}
	}

	public void setStandardInputText(String username) {
		usernameField.setPretext(username);
		usernameField.setText(username);
	}

	public void mouseButtonInput(int button, int action, float x, float y) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action != GLFW.GLFW_RELEASE) {
			usernameField.tryFocus(x, y, false);
			titleField.tryFocus(x, y, false);
		}
	}
	
	public void runCancelAction() {
		cancelBtn.runPressedAction();
	}
	
	private void setPrivatePublic(boolean publicLobby) {
		audio.get(SfxTypes.REGULAR_PRESS).play();
		this.publicLobby = publicLobby;
		if (publicLobby) {
			privatePublicBtn.setTitle(Texts.privateText);
			privatePublicLabel.setText("Will be public");
		} else {
			privatePublicBtn.setTitle(Texts.publicText);
			privatePublicLabel.setText("Will be private");
		}
	}
	
	private void setAmountPlayers(int amount) {
		audio.get(SfxTypes.REGULAR_PRESS).play();
		amountPlayers = amount;
		amountPlayersBtn.setTitle("Players: " + amount);
	}
	
	public void updateResolution() {
		fieldHeight = usernameField.getWindow().height * 1.1f;
	}

}
