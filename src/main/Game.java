package main;

import static org.lwjgl.glfw.GLFW.glfwIconifyWindow;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import adt.IAction;
import audio.AudioMaster;
import audio.SfxTypes;
import elem.Font;
import elem.interactions.LobbyTopbar;
import elem.interactions.RegularTopbar;
import elem.interactions.TopbarInteraction;
import elem.interactions.TransparentTopbar;
import elem.ui.Console;
import elem.ui.UIButton;
import elem.ui.UIFont;
import elem.ui.UISceneInfo;
import elem.ui.modal.UIExitModal;
import elem.ui.modal.UIUsernameModal;
import engine.graphics.Renderer;
import engine.graphics.UIRender;
import engine.io.InputHandler;
import engine.io.Window;
import engine.utils.Timer;
import main.steam.SteamHandler;
import main.steam.SteamMain;
import scenes.SceneHandler;
import scenes.Scenes;
import scenes.adt.Scene;
import scenes.game.Lobby;
import scenes.game.Race;
import scenes.regular.JoiningScene;
import scenes.regular.LeaderboardScene;
import scenes.regular.MainMenuScene;
import scenes.regular.MultiplayerScene;
import scenes.regular.OptionsScene;
import scenes.regular.SingleplayerScene;
import settings_and_logging.RegularSettings;

public class Game {

	public static final boolean 
	RELEASE
	= true
//	= false
	, DEBUG
//	= true
	= false
	, STEAM
	= true;
//	= false;
	
	public static final String NAME = "Racingmaybe | ", VERSION = "Update 9";
	public static final int TICK_STD = 25;

	public static IAction hoverAction;
	
	private final AudioMaster audio;
	private final SceneHandler sceneHandler;
	private final Timer timer;
	private final Renderer renderer;
	private final UIRender ui;
	private final Features features;

	private final Window window;
	private final SteamMain steam;
	private boolean running, confirmedExit;
	private final UIExitModal exitModal;
	private SteamHandler steamHandler;

	public static void main(String[] args) {
		long startuptime = System.currentTimeMillis();
		System.setProperty("org.lwjgl.librarypath", new File("natives").getAbsolutePath());
		new Game(startuptime);
	}
	
	public Game(long startuptime) {
		steam = new SteamMain();
		RegularSettings settings = new RegularSettings();
		if (STEAM)
			steam.init((sh) -> {
				steamHandler = sh;
				steamHandler.initUsername(settings);
			});

		if (RELEASE) {
			try {
				String logname = "errorLog_" + VERSION;
				File file = new File(logname + ".txt");
				if (!file.exists())
					file.createNewFile();
				else {
					int i = 2;
					do {
						file = new File(logname + "_" + i + ".txt");
						i++;
					} while (file.exists() && file.length() > 0);
				}
				PrintStream err = new PrintStream(file);
				System.setErr(err);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		timer = new Timer();
		long afterStartup = System.currentTimeMillis();
		System.out.println("Startup finished at: " + (afterStartup - startuptime) + "ms");

		// 250-300 ms
		audio = new AudioMaster(settings.getMasterVolume(),
				settings.getSfxVolume(), settings.getMusicVolume());
		
		long afterAudio = System.currentTimeMillis();
		System.out.println("Audio time: " + (afterAudio - afterStartup) + "ms");

		// 1000-1500 ms
		window = new Window(settings.getFullscreen(), settings.getVsync());
		
		long afterWindow = System.currentTimeMillis();
		System.out.println("Window time: " + (afterWindow - afterAudio) + "ms");
		
		// 0 ms
		features = new Features(audio, settings, steamHandler, window);
		
		long afterFeatures = System.currentTimeMillis();
		System.out.println("Features time: " + (afterWindow - afterWindow) + "ms");

		// Buttons
		hoverAction = () -> audio.get(SfxTypes.REGULAR_HOVER).play();
		UIButton minimizeButton = new UIButton("-");
		minimizeButton.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			glfwIconifyWindow(window.getWindow());
		});

		UIButton closeButton = new UIButton("X");
		closeButton.setPressedAction(() -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			GLFW.glfwSetWindowShouldClose(window.getWindow(), true);
		});
		
		UISceneInfo.addPressableToScene(Scenes.GENERAL_NONSCENE, minimizeButton);
		UISceneInfo.addPressableToScene(Scenes.GENERAL_NONSCENE, closeButton);

		/*
		 * Join modal
		 */
		UIUsernameModal usernameModal = new UIUsernameModal(features, audio);
		exitModal = new UIExitModal(features);

		sceneHandler = new SceneHandler(exitModal, usernameModal, DEBUG ? new Console(features) : null);

		ui = new UIRender();

		InputHandler input = new InputHandler(window, ui.getNkContext());
		input.setCurrent(sceneHandler);
		renderer = new Renderer(ui);

		// finally set the font as gl has been init
		UIFont standardFont = new UIFont(Font.REGULAR, Window.HEIGHT / 42);
		standardFont.use(ui.getNkContext());
		
		/*
		 * Topbars
		 */
		TopbarInteraction topbar = new TopbarInteraction(window);
		
		RegularTopbar regularTopbar = new RegularTopbar(features, minimizeButton, closeButton, topbar);
		LobbyTopbar lobbyTopbar = new LobbyTopbar(features, minimizeButton, closeButton, new TopbarInteraction(window));
		TransparentTopbar transparentTopbar = new TransparentTopbar(topbar, 18);

		/*
		 * Global exitmodal
		 */
		usernameModal.setButtonActions();		
		Consumer<Integer> initMovingIntoALobby = (type) -> {
			usernameModal.setVisible(true, type != 0);
			usernameModal.setStandardInputText(features.getUsername());
		};
		
		exitModal.setButtonActions(() -> {
			features.leave();
			glfwSetWindowShouldClose(window.getWindow(), true);
			confirmedExit = true;
		}, () -> {
			audio.get(SfxTypes.REGULAR_PRESS).play();
			exitModal.setVisible(false);
			sceneHandler.getCurrentScene().press();
		});


		ResourceHandler resourceHandler = new ResourceHandler();
		
		long scenesNow = System.currentTimeMillis();
		System.out.println("Before scenes: " + (scenesNow - startuptime) + "ms");
		/*
		 * Scenes
		 */
		Scene[] scenes = new Scene[Scenes.AMOUNT - 1];
		scenes[Scenes.RACE] = new Race(features, transparentTopbar);
		scenes[Scenes.LOBBY] = new Lobby(features, (Race) scenes[Scenes.RACE], lobbyTopbar);
		
		Consumer<Integer> createNewSingleplayerGameAction = (i) -> {
			((Lobby) scenes[Scenes.LOBBY]).createNewLobby("Player", 2, false, i);
			((LeaderboardScene) scenes[Scenes.LEADERBOARD]).setLeaderboard(i);
			audio.get(SfxTypes.START_ENGINE).play();
		};
		
		scenes[Scenes.MAIN_MENU] = new MainMenuScene(features, regularTopbar);
		scenes[Scenes.SINGLEPLAYER] = new SingleplayerScene(features,
				createNewSingleplayerGameAction, regularTopbar);
		((Race) scenes[Scenes.RACE]).createTryAgainButton(createNewSingleplayerGameAction);
		scenes[Scenes.OPTIONS] = new OptionsScene(features, regularTopbar);
		scenes[Scenes.JOINING] = new JoiningScene((Lobby) scenes[Scenes.LOBBY], transparentTopbar);
		scenes[Scenes.MULTIPLAYER] = new MultiplayerScene(features, regularTopbar, initMovingIntoALobby);
		scenes[Scenes.LEADERBOARD] = new LeaderboardScene(features, regularTopbar);

		sceneHandler.init(features, scenes, audio);
		
		((OptionsScene) scenes[Scenes.OPTIONS]).initOptions(settings,
				input.getKeys(), audio, sceneHandler);
		((Race) scenes[Scenes.RACE]).setLobby((Lobby) scenes[Scenes.LOBBY]);
		window.setSceneHandler(sceneHandler);

		features.setLobby((Lobby) scenes[Scenes.LOBBY]);
		features.setCloseUsernameModalAction(()-> {
			usernameModal.setVisible(false, false);
			if(Scenes.CURRENT < Scenes.LOBBY) {
				scenes[Scenes.JOINING].updateGenerally();
				sceneHandler.changeScene(Scenes.JOINING, false);
			}
		});
		
		System.out.println("Through scenes: " + (System.currentTimeMillis() - scenesNow) + "ms");
		
		if (steamHandler != null) {
			steamHandler.setFeatures(features);
			steamHandler.setJoinActions(initMovingIntoALobby);
		}
		
		timer.init();
		long shaderCreationNow = System.currentTimeMillis();
		System.out.println("Amount to create: " + resourceHandler.getAmount());
		while (resourceHandler.isNotDone()) {
			resourceHandler.createNext();
		}
		resourceHandler.destroy();
		sceneHandler.updateResolution();

		System.out.println("ShaderCreationNow: " + (System.currentTimeMillis() - shaderCreationNow) + "ms");

		/*
		 * Show the window until it is closed 
		 */
		glfwShowWindow(window.getWindow());
		running = true;
		System.out.println("Startup: " + (System.currentTimeMillis() - startuptime) + "ms");
		gameLoop();

		/*
		 * After gameloops ends
		 */
		steam.destroy();
		window.destroy();
		audio.destroy();
		sceneHandler.destroy();

		Runtime.getRuntime().halt(0);
		System.exit(0);
	}

	private void gameLoop() {
		float delta;
		while (running) {
			if (window.isClosing()) {
				if (!confirmedExit && window.isFocused()) {
					audio.get(SfxTypes.REGULAR_PRESS).play();
					exitModal.setVisible(!exitModal.isVisible());
					UISceneInfo.decideFocusedWindow(window.getWindow());
					GLFW.glfwSetWindowShouldClose(window.getWindow(), false);
				} else if(Scenes.CURRENT < Scenes.LOBBY || ((Lobby) sceneHandler.getScene(Scenes.LOBBY)).getGame().isGameOver()) {
					running = false;
					return;
				} else {
					((Lobby) sceneHandler.getScene(Scenes.LOBBY)).getGame().gameOver(true);
				}
			} else if(window.shouldUpdateViewport()) {
				window.updateViewport();
			}

			delta = timer.getDelta();

			// update game
			tick(delta);

			// draw the game
			render(delta);

			int err = GL11.glGetError();
			if (err != GL11.GL_NO_ERROR) {
				System.out.println("GLERROR: " + err);
			}
			
			window.swapBuffers();
		}
	}

	private void tick(float delta) {
		steam.update();
		window.update();
		sceneHandler.tick(delta);
		audio.checkMusic();
	}

	private void render(float delta) {
		sceneHandler.renderGame(renderer, window.getWindow(), delta);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			sceneHandler.renderUILayout(ui.getNkContext(), stack);
			renderer.renderNuklear(ui.getNkContext(), stack);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}


