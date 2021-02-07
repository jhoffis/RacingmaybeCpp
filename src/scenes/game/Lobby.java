package scenes.game;

import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import com.codedisaster.steamworks.SteamID;

import adt.IAction;
import audio.AudioRemote;
import audio.SfxTypes;
import audio.Source;
import communication.Communicator;
import communication.GameInfo;
import communication.SteamCommunicator;
import communication.Translator;
import elem.ColorBytes;
import elem.interactions.LobbyTopbar;
import elem.interactions.TileUpgrade;
import elem.ui.IUIObject;
import elem.ui.UIButton;
import elem.ui.UILabel;
import elem.ui.UILabelRow;
import elem.ui.UISceneInfo;
import elem.ui.UIScrollable;
import elem.ui.UITextField;
import elem.ui.UIWindowInfo;
import elem.ui.modal.UIBonusModal;
import elem.upgrades.Store;
import elem.upgrades.Upgrade;
import engine.graphics.Renderer;
import engine.io.Window;
import engine.math.Vec2;
import engine.math.Vec3;
import game_modes.GameModes;
import game_modes.SinglePlayerMode;
import main.Features;
import main.Game;
import main.Texts;
import player_local.BankType;
import player_local.Player;
import player_local.Car.Car;
import player_local.Car.Rep;
import scenes.Scenes;
import scenes.adt.Scene;
import scenes.adt.SceneChangeAction;
import scenes.adt.Subscene;
import scenes.game.lobby_subscenes.CarChoiceSubscene;
import scenes.game.lobby_subscenes.SetupSubscene;
import scenes.game.lobby_subscenes.UpgradesSubscene;

/*
 * TODO need space for change subscene buttons somewhere by the subscenes.
 */

public class Lobby extends Scene {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = -5861049279182231248L;

	// GROUPS
	private int btnHeight;

	// MYPLAYER
	private final LobbyTopbar topbar;

	private final Store storeHandler;

	// BONUS
	private UIBonusModal bonusModal;

	// LOBBY
	private long countdownTemp  = -1; // <0 to not instantly trigger next-turn event
	private final UILabel countdownLabel;

	private final UIScrollable playerslabel;
	private final UIButton readyBtn;
	private final UILabel nextRace;
	private final UILabel endGoal;

	private final UITextField chatInput;
	private final UIScrollable chatOutput;

	private final Race race;
	private int currentLength;

	private boolean everyoneReady;
	private boolean checkRaceLights;

	private Player player;
	private Player comparedStats;

	private GameRemoteMaster game;

	private long tickTime = 60;

	private Communicator com;
	private final IAction endAllAction;

	// SUBSCENE

	private final Subscene[] subscenes;
	private final UpgradesSubscene upgradesSubscene;
	private final SetupSubscene setupSubscene;
	private final CarChoiceSubscene carChoiceSubscene;
	private int currentSubscene;

	private final UIWindowInfo controlWindow;

	private String extraPlayersListText;

	public Lobby(Features features, Race race, LobbyTopbar topbar) {
		super(features, topbar, Scenes.LOBBY);

		features.pushBackgroundColor(new ColorBytes(0, 0, 0, 0));
		
		// Init shit
		storeHandler = new Store();

		readyBtn = new UIButton(Texts.readyText);
		nextRace = new UILabel();
		endGoal = new UILabel();
		countdownLabel = new UILabel();

		this.topbar = topbar;
		this.race = race;

		
		updateResolution();

		float ssX = 0;
		float sceneChangeH = btnHeight * 1.1f;
		float ssY = topbar.getHeight();
		float ssW = Window.WIDTH * 3f / 5f;
		float ssH = Window.HEIGHT - ssY - sceneChangeH;

		float y = topbar.getHeight();
		float rectWidth = Window.WIDTH * 2f / 5f;
		float rectHeight = (Window.HEIGHT - topbar.getHeight()) / 2f;

		controlWindow = createWindow(ssW + rectWidth / 2, y + rectHeight, rectWidth / 2, rectHeight);
				
		playerslabel = new UIScrollable(sceneIndex, ssW, y, rectWidth, rectHeight);
		
		float chatInputHeight = rectHeight / 10f;
		float chatOutputHeight = rectHeight - chatInputHeight;
		chatOutput = new UIScrollable(sceneIndex, ssW, y + rectHeight, rectWidth / 2f,
				chatOutputHeight);
		chatOutput.setBottomHeavy(true);
		chatOutput.getWindow().options = Nuklear.NK_WINDOW_NO_INPUT | Nuklear.NK_WINDOW_NO_SCROLLBAR | Nuklear.NK_WINDOW_TITLE;
		
		chatInput = new UITextField(features, "Chat here...", true, false, 20, sceneIndex,
				ssW, y + rectHeight + chatOutputHeight, rectWidth / 2,
				chatInputHeight);
		chatInput.setSpecialInputAction((keycode) -> {
			// Pressed enter
			if (chatInput.isFocused() && !chatInput.getText().isEmpty())
				if (keycode == GLFW.GLFW_KEY_ENTER) {
					String text = chatInput.getText() + "#" + String.format("%.1f", 0.4f + (float) Features.ran.nextInt(160) / 100f);
					com.sendChat(player, text);
					chatInput.setText("");
					// SceneHandler.instance.getWindows().requestFocus();
				}
		});

		/*
		 * Subscene creation
		 */
		upgradesSubscene = new UpgradesSubscene(features, sceneIndex, topbar);
		setupSubscene = new SetupSubscene(features, sceneIndex);
		carChoiceSubscene = new CarChoiceSubscene(features, sceneIndex);

		subscenes = new Subscene[3];

		subscenes[0] = setupSubscene;
		subscenes[1] = carChoiceSubscene;
		subscenes[2] = upgradesSubscene;

		for (int i = 0; i < subscenes.length; i++) {
			subscenes[i].init(this, readyBtn, readyBtn);
			subscenes[i].setIndex(i);
			subscenes[i].createWindowsWithinBounds(ssX, ssY, ssW, ssH);
			subscenes[i].createBackground();
			subscenes[i].setVisible(false);
		}

		bonusModal = new UIBonusModal(features);
		upgradesSubscene.initLobby(storeHandler, bonusModal, readyBtn);
		storeHandler.setBonusModal(bonusModal);
		currentSubscene = 1;
		
		/*
		 * Bonus modal
		 */
		bonusModal.init((goldType) -> {
				Upgrade upgrade = bonusModal.popUpgrade();
				int goldCost = upgrade.getGoldCost(upgrade.getBonusLVL());
				int normalGain = upgrade.getNormalGain(upgrade.getBonusLVL());
				boolean gold = goldType == 1;
				var playerCombiner = bonusModal.getCombination();
				
				if (gold) {

					if (playerCombiner.getBank().canAfford(goldCost, BankType.GOLD)) {
						audio.get(SfxTypes.GOLD_BONUS).play();
						playerCombiner.getBank().buy(goldCost, BankType.GOLD);
					} else {
						audio.get(SfxTypes.BUY_FAILED).play();
						bonusModal.pushUpgrade(upgrade);
						return;
					}

				} else {
					audio.get(SfxTypes.NORMAL_BONUS).play();
					playerCombiner.getBank().add(normalGain, BankType.GOLD);
				}

				upgrade.getBonuses()[upgrade.getBonusLVL()].upgrade(upgrade.getRegVals(), playerCombiner, playerCombiner.getCarRep(),gold, false);
				upgrade.setBonusChoice(upgrade.getBonusLVL(), gold ? 2 : 1);
				
				bonusModal.pushBonusChoice(upgrade.getNameID(), upgrade.getBonusLVL(), gold);
				upgrade.upgradeBonus(true);

				// 0 = failed, 1 = bought, 2 = newbonus
				int res = 0;
				if (bonusModal.isFreeBonus()) {
					boolean more = storeHandler.isBonusToChooseFirst(playerCombiner, upgrade, true);
					res = (more || !bonusModal.upgrades.isEmpty() ? 2 : 1);
				}
				if (res != 2) {
					// om du kommer her så har du ikke betalt for oppgraderingen ennå. 
					res = storeHandler.upgrade(playerCombiner, bonusModal.getInitialUpgrade(), null, true);
				}
				
				if (res != 2) {
					bonusModal.setVisible(false);
					bonusModal.setFreeBonus(false);
					if (res == 1) {
						bonusModal.combine(this.player);
						bonusModal.updateServerBonuses(this.player, com);
						com.updateCloneToServer(this.player, Translator.getCloneString(this.player, false, true), 0);
						upgradesSubscene.reactBonus(true);
					} else {
						audio.get(SfxTypes.BUY_FAILED).play();
					}
					bonusModal.upgrades.clear();
				} else {
					audio.get(SfxTypes.NEW_BONUS).play();
					bonusModal.setFreeBonus(true);
				}

			}, () -> {
				audio.get(SfxTypes.NEW_BONUS).stop();
				audio.get(SfxTypes.CANCEL_BONUS).play();
				bonusModal.revert(player);
				upgradesSubscene.removeLastUpgrade();
				upgradesSubscene.reactBonus(false);
				int originalGold = bonusModal.getOriginalGoldAmount().firstElement();
				player.getBank().set(originalGold, BankType.GOLD);
				bonusModal.close();
				press();
			});

		/*
		 * changing subscenes
		 */
		UIButton<Integer>[] subsceneTabs = new UIButton[subscenes.length];
		for (int i = 0; i < subscenes.length; i++) {
			subsceneTabs[i] = new UIButton<Integer>(subscenes[i].getName());
			subsceneTabs[i].setPressedAction((newIndex) -> {
				audio.get(SfxTypes.REGULAR_PRESS).play();
				subscenes[currentSubscene].setVisible(false);
				currentSubscene = newIndex;
				subscenes[currentSubscene].setVisible(true);
			});
			subsceneTabs[i].setConsumerValue(i);
			add(subsceneTabs[i]);
		}
		
		topbar.setSubscenes(subsceneTabs);
		race.initWinVisual(upgradesSubscene);
		
		/*
		 * Right manipulation
		 */
		readyBtn.setPressedAction(() -> {
			byte ready = (byte) ((player.getReady() + 1) % 2);

			if (ready == 1) {
				audio.get(SfxTypes.READY).play();
				readyBtn.setTitle("Unr" + Texts.readyText.substring(1));
				player.setHistoryNow();
			} else {
				audio.get(SfxTypes.UNREADY).play();
				readyBtn.setTitle(Texts.readyText);
			}

			// and show car spinning
			player.setReady(ready);
			com.ready(player, player.getReady());
		});
		UIButton goBack = new UIButton(Texts.leaveText); 
		goBack.setPressedAction(() -> {
			if (player.getReady() != 1) {
				audio.get(SfxTypes.REGULAR_PRESS).play();
				game.gameOver(true);
			}
		});
		
		UIButton options = new UIButton(Texts.optionsText);
		options.setPressedAction(() -> {
			if (player.getReady() != 1) {
				audio.get(SfxTypes.REGULAR_PRESS).play();
				sceneChange.change(Scenes.OPTIONS, true);
			}
		});

		/*
		 * Quit
		 */
		endAllAction = () -> {
			if (game != null) {
				game.gameOver(false);
			}
			System.out.println("goback");

			if (player != null) {
				player.setIn(false);
				if (com != null)
					com.leave(player, false);
				else
					features.leave();
				player = null;
				comparedStats = null;
			} else {
				features.leave();
			}
			
			if (com != null) {
				com.close();
			}
			
			upgradesSubscene.CurrentUpgrade = null;
			bonusModal.close();
			
			// TODO Rydd opp i subscenes og reset biler

			race.turnOff();
			
			com = null; // FIXME this is sometimes run before endall can leave
						// the
			// lobby

			currentLength = 0;
			
			game = null;
			everyoneReady = false;

			sceneChange.change(Scenes.PREVIOUS_REGULAR, false);
		};

		add(readyBtn);
		add(goBack);
		add(options);

		topbar.setLobbyButtons(goBack, options);

		goBack.setNavigations(null, null, null, () -> options);
		readyBtn.setNavigations(() -> {
			UIButton btn = subscenes[currentSubscene].intoNavigationSide();
			if (btn == null)
				btn = readyBtn;
			return btn;
		}, null, () -> options, null);
		options.setNavigations(null, null, () -> goBack, () -> readyBtn);

		// createOnlineBtn.setNavigations(joinOnlineBtn, null, null,
		// refreshBtn);
		// gobackBtn.setNavigations(null, refreshBtn, joinOnlineBtn, null);
		// refreshBtn.setNavigations(gobackBtn, null, createOnlineBtn, null);
	}

	@Override
	public void finalizeInit(AudioRemote audio, SceneChangeAction sceneChange, IAction sceneUpdate) {
		this.audio = audio;
		this.sceneChange = sceneChange;
		this.sceneUpdate = sceneUpdate;
		
		for (Subscene ss : subscenes) {
			ss.setAudio(audio);
			ss.setSceneChangeAction(sceneChange);
		}
	}
	
	@Override
	public void updateGenerally() {
		showNoUpgrades();
		
		for(Subscene ss : subscenes) {
			ss.updateGenerally();
		}
		subscenes[currentSubscene].setVisible(true);
		
		audio.setListenerData(0, 0, 0);
		
		for (var car : CarChoiceSubscene.CARS) {
			car.setRotation(new Vec3(0));
			car.resetTransformation();
		}
	}
	
	@Override
	public void updateResolution() {
		btnHeight = Window.HEIGHT / 12;
		
		if(subscenes != null) {
			for(Subscene ss : subscenes) {
				ss.updateResolution();
			}
		}
	}

	public void showNoUpgrades() {
		topbar.uncompare();
		upgradesSubscene.setUpgradeDetails(new IUIObject[0]);
		comparedStats = null;
	}
	
	public void showUpgrades(TileUpgrade currentUpgrade, Vec2 pos, boolean selected, boolean placed) {
		if (currentUpgrade != null) {
			float cost = storeHandler.getSelectedUpgradeCost(player.getBank());
			comparedStats = storeHandler.getSelectedUpgradeCarRep(player, currentUpgrade.getUpgrade(), 
					currentUpgrade.getLayerPos() != null ? currentUpgrade.getLayerPos() : pos);
			topbar.compareStats(cost, comparedStats);
		} else {
			showNoUpgrades();
		}
	}

	/**
	 * 
	 *            - outtext from server
	 */
	public void updatePlayerList() {
		if (com == null) return;
		
		String string = com.updateLobby(player);
		if (string == null) return;

		everyoneReady = true;
		String[] outputs = string.split("#");

		// FIXME This slows down the game as it is unnecessary to ask for
		// this here.

		int index = 0;

		String endgoal = outputs[index];// com.getEndGoal();
		index++;
		index++;

		nextRace.setText("Next race: " + currentLength + " m"); // FIXME do
																// these
																// need to
																// be set
																// every
																// frame?
		this.endGoal.setText(endgoal);

		int n = 0;
		boolean ready = false;
		byte id = -1;
		ArrayList<IUIObject> texts = new ArrayList<>();
		texts.add(new UILabelRow(Rep.getInfoTitles(), ";", playerslabel.getRowHeight(), Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_TOP));
//		if (upgradesSubscene.CurrentUpgrade != null) {
//			texts.add(new UILabel(upgradesSubscene.CurrentUpgrade.getUpgrade().getRegularValuesString() + "#WON"));
//		}
		StringBuilder playerline = new StringBuilder();
		for (int fromIndex = index; fromIndex < outputs.length; fromIndex++) {
			n++;

			// Player name then Car name
			// finish this line.
			switch (n) {
				case 1, 2 -> playerline.append(outputs[fromIndex]).append(", ");
				case 3 -> {
					ready = Integer.parseInt(outputs[fromIndex]) == 1;
					if (!ready) {
						everyoneReady = false;
					}
				}
				case 4 -> {
					int points = Integer.parseInt(outputs[fromIndex]);
					playerline.append(points).append(" point").append(points != 1 ? "s, " : ", ");
				}
				case 5 -> {
					int vmoney = Integer.parseInt(outputs[fromIndex]);
					playerline.append(vmoney).append(" v$");
				}
				case 6 -> id = Byte.parseByte(outputs[fromIndex]);
				case 7 -> {
					boolean me = player.getID() == id;
					String color = ready ? "#G" : (me ? "#LBEIGE" : "");
					String playerText = playerline.append(color).toString();
					String carText = outputs[fromIndex];
					texts.add(new UILabel(playerText, Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_BOTTOM));
					texts.add(new UILabelRow(carText, ";", color, playerslabel.getRowHeight(), Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_MIDDLE));
					if (me && comparedStats != null) {
						texts.add(new UILabelRow(comparedStats.getCarInfoDiff(player), ";", color, playerslabel.getRowHeight(), Nuklear.NK_TEXT_ALIGN_CENTERED | Nuklear.NK_TEXT_ALIGN_TOP));
					}
					texts.add(new UILabel());
					playerline = new StringBuilder();
					n = 0;
				}
			}

		}

		if (com.isSingleplayer() && extraPlayersListText != null) {
			texts.addAll(Arrays.asList(UILabel.split(extraPlayersListText, "\n")));
		}
		
		playerslabel.setText(texts.toArray(new IUIObject[0]));
		
		countdown();

		// Update chat
		if (!com.isSingleplayer())
			addChatToChatWindow(com.getChat(player));

		// Check with the server what the location and length is
		if (!game.isPlaceChecked()) {
			int currentLength = com.getTrackLength();
			game.setPlaceChecked(true);
			if (currentLength != -1) {
				setCurrentLength(currentLength);
			}
		}

	}

	public void setCurrentLength(int currentLength) {
		if (com.isSingleplayer() && this.currentLength > 0) {
			chatOutput.addText(player.getPoints() + "p " + this.currentLength + "m: " + (player.getTime() > 0 ? 
					(player.getTime() / 1000f) + " sec" :
					"DNF#DNF"));
		}
		
		this.currentLength = currentLength;
		race.setCurrentLength(currentLength);
	}

	private void addChatToChatWindow(String newText) {
		if (newText != null) {

			// Adding text to the chatwindow
			String[] pitchSplit = newText.split("#");
			if (pitchSplit.length > 1)
				newText = newText.substring(0, newText.length() - 1 - pitchSplit[pitchSplit.length - 1].length());
			
			chatOutput.addText(newText + "\n");

			/*
			 * TAUNT(s)
			 */
			String[] tauntCheck = newText.split(": ");

			Source taunt = null;
			String pattern = "\\d+";
			String twice = null;
			if (tauntCheck.length > 1 && tauntCheck[1].length() >= 1) {
				String single = tauntCheck[1].substring(0, 1);

				if (tauntCheck[1].length() >= 2) {
					twice = tauntCheck[1].substring(0, 2);
				}

				if (twice != null && twice.matches(pattern)) {
					int twiceTaunt = Integer.parseInt(twice);
					taunt = audio.getTaunt(twiceTaunt);
				} else if (single.matches(pattern)) {
					int singleTaunt = Integer.parseInt(single);
					taunt = audio.getTaunt(singleTaunt);
				}

			}
			if (taunt != null)
				taunt.play(Float.parseFloat(pitchSplit[pitchSplit.length - 1].replace(',', '.')));
			else
				audio.get(SfxTypes.CHAT).play();
		}
	}

	/**
	 * @param name
	 *            - username
	 * @param role
	 *            - int value (0,1) represents boolean
	 */
	public void createNewLobby(String name, int role, boolean multiplayer, int type) {

		initBeforeJoined();

		GameInfo info = new GameInfo(features, storeHandler, game);
		info.createGameID();

		info.setAudio(audio);
		if (multiplayer)
			com = new SteamCommunicator(features, storeHandler, game, info, this);
		else
			com = info;
		
		joinNewLobby(name, role, null);

		setupSubscene.initGameModeManipulation(info, player);

		if (player == null || player.getID() == -200) { // if -200 its failed
			game.endAll();
			return;
		}

		sceneChange.change(Scenes.LOBBY, true);

		if (!multiplayer) {
			// Singleplayer
			info.init(GameModes.SINGLEPLAYER, this.player, type);
		}
		initAfterJoined();
	}

	/**
	 * @param name
	 *            - username
	 * @param host
	 *            - int value (0,1) represents boolean
	 * @param role
	 */
	public void joinNewLobby(String name, int role, SteamID host) {

		player = new Player(name, (byte) -200, (byte) role, null);

		// Client
		Consumer<Player> afterJoined = null;
		if (!player.isHost()) {
			initBeforeJoined();

			afterJoined = (player) -> {
				if (player == null) {
					System.out.println("Could not join! Ending in afterjoined");
					game.endAll();
					return;
				}

				this.player = player;

				sceneChange.change(Scenes.LOBBY, true);
				initAfterJoined();
				com.getGoldCosts(player);
			};

			if (com == null) {
				GameInfo info = new GameInfo(features, storeHandler, game);
				info.setAudio(audio);
				com = new SteamCommunicator(features, storeHandler, game, info, this); 
				setupSubscene.initGameModeManipulation(info, player);
			} else {
				return;
			}
		}
		player.setCar(new Car(0, true));
		carChoiceSubscene.setCom(com);

		try {
			player = com.join(player, Game.VERSION,
					player.isHost()
							? GameInfo.JOIN_TYPE_VIA_CREATOR
							: GameInfo.JOIN_TYPE_VIA_CLIENT,
					afterJoined, host);

			if (player != null && player.isHost()) {
				com.createNewGame(player);
			}
		} catch (NullPointerException e) {
			System.out.println("ERROR: closed down! " + e.getMessage());
			game.gameOver(true);
		}

	}

	public void carSelectUpdate(int selectedCarIndex) {
		player.getCar().switchTo(selectedCarIndex);
		player.getLayer().reset();
		
		player.resetHistory();
		storeHandler.resetAllTowardsPlayer(player, com.getGamemode());
		com.updateCloneToServer(player, Translator.getCloneString(player, false, true), 1);

		upgradesSubscene.reset();
		showNoUpgrades();
	}

	public void updateStore() {
		com.setPrices(player, storeHandler);
	}
	
	/**
	 * Sets up game remote
	 */
	private void initBeforeJoined() {
		// FIXME gjør meg bedre for her lager man muligens flere gameRemotes enn nødvendig
		if (game == null) {
			game = new GameRemoteMaster(endAllAction);
		}
	}

	private void initAfterJoined() {
		UISceneInfo.clearHoveredButton(sceneIndex);

		// if for instance races > 0 in gamemode
		if (com.isGameStarted() == 0) {
			updateStore();
			
			currentSubscene = player.isHost() ? setupSubscene.getIndex() : carChoiceSubscene.getIndex();
			topbar.setTabsVisible(true);
			carSelectUpdate(0);
			player.createModifierTiles();
		} else {
			currentSubscene = upgradesSubscene.getIndex();
			topbar.setTabsVisible(false);
			storeHandler.createNeededTiles(player);
		}

		chatOutput.clear();
		if (com.isSingleplayer())
			chatOutput.getWindow().name = "Log:";
		else
			chatOutput.getWindow().name = "Chat:";

		topbar.setStats(player);
		upgradesSubscene.afterJoined(player);
		carChoiceSubscene.afterJoined(player);

		player.upgrades.setAudio(audio);
		
		updateGenerally();
	}

	public void countdown() {
	// Start the countdown
		if (everyoneReady || com.doubleCheckStartedRace() || tickTime <= 0) {
			raceStarted();
		} else if (com.getRaceCountdown() != -1 && !com.getGamemode().getClass().equals(SinglePlayerMode.class)) {
			countdownTemp = com.getRaceCountdown() - System.currentTimeMillis();

			long time = (countdownTemp / 1000) + 1;
			
			if (tickTime > 0 && time < tickTime) {
				tickTime--;
				if(tickTime <= 3)
					audio.get(SfxTypes.COUNTDOWN).play();
			}
				
			if (countdownTemp >= 0) {
				countdownLabel.setText("Starting: " + time);
			}
		}
		
		if (game.isStarted()) {
			if (checkRaceLights && com.updateRaceLights(player)) {
				checkRaceLights = !race.createRaceLights();
				race.initWindow();
				sceneChange.change(Scenes.RACE, false);
				everyoneReady = false;
				System.out.println("create");
			}
		}
	}

	private void raceStarted() {
		if (!game.isStarted()) {
			com.startRace();
			if (!race.isInitiated()) {
				race.setPlayer(player);
				race.setCom(com);
				System.out.println("My player: " + player.toString());
				race.initRestBeforeFirstRace(game, com.isSingleplayer());
				topbar.setTabsVisible(false);
				if (!com.isSingleplayer() && player.isHost())
					features.startLobby();
			}
			
			com.clearRaceCountdown();
			tickTime = GameInfo.countdown_std;
			countdownTemp = -1;
			audio.get(SfxTypes.START).play();
			game.setStarted(true);
			checkRaceLights = true;
			currentSubscene = upgradesSubscene.getIndex();
			subscenes[currentSubscene].setVisible(true);
			readyBtn.setTitle(Texts.readyText);
			player.getCar().updateVolume();
			player.getCar().reset();

			if (com != null) {
				storeHandler.setGoldCostsToBuffers(player);
			}
		}
	}

	@Override
	public void keyInput(int keycode, int action) {
		if (bonusModal.isVisible()) {
			bonusModal.input(keycode, action);
			return;
		}

		if (chatInput.isFocused()) {
			chatInput.input(keycode, action);
			return;
		}
		currentSubscene().keyInput(keycode, action);

		if (action != GLFW.GLFW_RELEASE) {
			// Downstroke for quicker input
			generalHoveredButtonNavigation(readyBtn, keycode);

			if (keycode == GLFW.GLFW_KEY_R) {
				readyBtn.runPressedAction();
			}
		}
		
	}
	
	@Override
	public void mouseScrollInput(float x, float y) {
		upgradesSubscene.mouseScrollInput(x, y);
		playerslabel.scroll(y);
		chatOutput.scroll(y);
	}

	@Override
	public boolean mouseButtonInput(int button, int action, float x, float y) {
		boolean down = super.mouseButtonInput(button, action, x, y);
		
		if (!bonusModal.isVisible())
			currentSubscene().mouseButtonInput(button, down ? 1 : 0, x, y);
		if (down) {
			if (!com.isSingleplayer() && chatInput.tryFocus(x, y, true))
				updateGenerally();
			if (bonusModal.isVisible()) {
				bonusModal.release();
			}
		}
		
		return false;
	}

	@Override
	public void mousePositionInput(float x, float y) {
		if (!bonusModal.isVisible())
			currentSubscene().mousePosInput(x, y);
	}

	@Override
	public void tick(float delta) {
		if (game.isGameOver()) {
			game.endAll();
			return;
		}

		currentSubscene().tick(delta);
		updatePlayerList();
	}

	@Override
	public void renderGame(Renderer renderer, long window, float delta) {
		currentSubscene().renderBackground(renderer);
		currentSubscene().renderGame(renderer, window, delta);
	}

	@Override
	public void renderUILayout(NkContext ctx, MemoryStack stack) {
		// Set the padding of the group

		if (bonusModal.isVisible())
			bonusModal.layout(ctx, stack);
		else {

			features.setBackgroundColor(ctx);
			/*
			 * MAIN SHIT
			 */

			// subscene ( left side )
			currentSubscene().renderUILayout(ctx, stack);

			// now we begin on the right side
			if (!currentSubscene().equals(carChoiceSubscene)) {
					
				features.pushBackgroundColor(ctx, new ColorBytes(0x00, 0x00, 0x00, 0x86));
				
				/*
				 * Player list at the right top side
				 */
				playerslabel.layout(ctx, stack);
				
				/*
				 * Chat / log
				 */
				chatOutput.layout(ctx, stack);
				if (com != null && !com.isSingleplayer()) {
					chatInput.layout(ctx, stack);
				}

				features.popBackgroundColor(ctx);
			}

			
			float rowSpacingY = Window.HEIGHT / 192.75f;

			/*
			 * Buttons
			 */
			if (controlWindow.begin(ctx, stack, 0, 0, 0, rowSpacingY)) {
				String[] endGoalLines = endGoal.getText().split(";");
				for (String line : endGoalLines) {
					nk_layout_row_dynamic(ctx,
							btnHeight / endGoalLines.length - rowSpacingY, 1); // nested
					// row
					Nuklear.nk_label(ctx, line, Nuklear.NK_TEXT_ALIGN_LEFT
							| Nuklear.NK_TEXT_ALIGN_MIDDLE);

				}

				rowSpacingY = btnHeight - rowSpacingY;
				nk_layout_row_dynamic(ctx, rowSpacingY, 1);
				nextRace.layout(ctx, stack);
				nk_layout_row_dynamic(ctx, rowSpacingY, 1);
				countdownLabel.layout(ctx, stack);
				nk_layout_row_dynamic(ctx, rowSpacingY * 2, 1);
				readyBtn.layout(ctx, stack);
			}
			nk_end(ctx);
		}
	}

	/*
	 * Getters and setters
	 */
	public void setExtraPlayersListText(String extraPlayersListText) {
		this.extraPlayersListText = extraPlayersListText;
	}

	public Subscene currentSubscene() {
		return subscenes[currentSubscene];
	}

	public GameRemote getGame() {
		return game;
	}

	@Override
	public void destroy() {
		for (Subscene ss : subscenes) {
			ss.destroy();
		}
	}

	public Communicator getCom() {
		return com;
	}

}