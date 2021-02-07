package scenes.game;

import java.util.Arrays;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import audio.SfxTypes;
import communication.Communicator;
import elem.CursorType;
import elem.Font;
import elem.interactions.TransparentTopbar;
import elem.ui.UIButton;
import elem.ui.UIFont;
import elem.ui.UIScrollable;
import engine.graphics.Renderer;
import engine.io.Window;
import game_modes.SinglePlayerMode;
import main.Features;
import main.Texts;
import player_local.Player;
import player_local.Car.Car;
import scenes.Scenes;
import scenes.adt.Scene;
import scenes.adt.Visual;
import scenes.game.lobby_subscenes.UpgradesSubscene;
import scenes.game.racing_subscenes.FinishVisual;
import scenes.game.racing_subscenes.RaceVisual;
import scenes.game.racing_subscenes.WinVisual;
import settings_and_logging.hotkeys.RaceKeys;

/**
 * Kjï¿½r thread pï¿½ lobby nï¿½r du skal tilbake
 *
 * @author jonah
 */
public class Race extends Scene {

    /**
     * Generated value
     */

    private GameRemote game;
    private Communicator com;
    private Player player;
    private Lobby lobby;

    private RaceVisual raceVisual;
    private FinishVisual finishVisual;
    private final WinVisual winVisual;
    protected Visual currentVisual;

    private final RaceKeys keys;
    private int currentLength;
    private long startTime;
    private long waitTime;
    private boolean running;
    private int raceLights;
    private long[] raceLightsTimes;
    private boolean finished;
    private boolean[] finishedPlayers;
    private UIButton goBackBtn;

    private int waitingDNF;
    private long waitingDNFTime;

    private boolean initiated;
    private boolean close;

    private boolean everyoneFinishedChecked;

    // FIXME temp fps counter for racing
    // private long timer;
    // private int frames;

    private final UIScrollable raceLobbyLabel;
    private boolean finalizedFinishRaceLobby;
    private boolean finishedFirstRace;
    private long raceInformationTick;
    private boolean restart;
    private Consumer<Integer> createNewSingleplayerGameAction;

    public Race(Features features, TransparentTopbar topbar) {
        super(features, topbar, Scenes.RACE);
//        SpriteNumeric.CreateNumbers();
        keys = new RaceKeys();

        raceLobbyLabel = new UIScrollable(
                new UIFont(Font.BOLD_REGULAR, -1), Scenes.RACE, 
                Window.WIDTH - Window.WIDTH / 3.6f,
                Window.HEIGHT / 24, Window.WIDTH / 3.8f, Window.HEIGHT / 1.5f);

        raceVisual = new RaceVisual(features, this);

        goBackBtn = new UIButton("Continue..."); // hvorfor
        goBackBtn.setVisible(false);
        goBackBtn.setPressedAction(() -> {
            System.out.println("goback button was pressed");
            if (currentVisual.getClass().equals(WinVisual.class)) {
                audio.get(SfxTypes.LEFT).play();
                close = true;
                player.setIn(false);

                game.gameOver(true);
            } else {
                audio.get(SfxTypes.OPEN_STORE).play();
                readyNewRaceReset();
            }
        });

        UIButton leaderboardBtn = new UIButton(Texts.leaderboardText);
        leaderboardBtn.setPressedAction(() -> {
            sceneChange.change(Scenes.LEADERBOARD, true);
            audio.get(SfxTypes.REGULAR_PRESS).play();
        });

        add(goBackBtn);
        add(leaderboardBtn);

        finishVisual = new FinishVisual(features, raceLobbyLabel);
        finishVisual.setGoback(goBackBtn);

        winVisual = new WinVisual(features, raceLobbyLabel);
        winVisual.setGoback(goBackBtn);
        winVisual.setLeaderboardBtn(leaderboardBtn);
    }
    
    public void initWinVisual(UpgradesSubscene upgradesSubscene) {
    	winVisual.setUpgrades(upgradesSubscene);
    }

    private void readyNewRaceReset() {
        com.getGamemodeAllInfo();
        com.setInTheRace(player, false);
        sceneChange.change(Scenes.LOBBY, false);
        close = false;
        
        for (Player player : com.getPlayers()) {
        	Car car = player.getCar();
        	car.getModel().reset();
        	car.getAudio().reset();
        }

        finalizedFinishRaceLobby = false;
        everyoneFinishedChecked = false;
        running = false;
        finished = false;
        startTime = -1;

        finishVisual.init();
    }

    public void initRestBeforeFirstRace(GameRemote game, boolean singleplayer) {
        this.game = game;
        initiated = true;

        raceVisual.initRest(player, audio);
        finishVisual.setPlayer(player);
        winVisual.setPlayer(player);
        winVisual.initRest(singleplayer);

        currentVisual = raceVisual;

        finishedPlayers = new boolean[com.getPlayers().length];

        com.setChosenCarAudio();
        com.setChosenCarModels();

        Consumer<Car> finishPlayerAnimationAction = (car) -> {
            if (!finished) return;
            int speed = car.getSpeed();
            if (speed < 5)
            	speed = car.getModel().getSpeed();
            if (speed > 5) {
                finishVisual.addFinish(car, speed);
            }
        };

        com.addFinishPlayerAnimationAction(finishPlayerAnimationAction);
    }

    @Override
    public void updateGenerally() {
    }

    @Override
    public void updateResolution() {
        float padding = Window.HEIGHT / 140f;
        raceLobbyLabel.setPadding(padding / 2f, padding);

        finishVisual.updateResolution();
        winVisual.updateResolution();
        raceVisual.updateResolution();

    }

    public boolean isInitiated() {
        return initiated;
    }

    public void initWindow() {
        readyNewRaceReset();
        com.setInTheRace(player, true);
        if (player.getRole() != Player.COMMENTATOR) {
        	changeVisual(raceVisual);
        	raceVisual.initBeforeNewRace(com.getOpponent(player));
        } else {
        	changeVisual(finishVisual);
        	finished = true;
        }
//		player.setOpponent((byte) 0);
    }

    public void createTryAgainButton(Consumer<Integer> createNewSingleplayerGameAction) {
        this.createNewSingleplayerGameAction = createNewSingleplayerGameAction;

        UIButton tryAgainBtn = new UIButton(Texts.tryAgain);
        tryAgainBtn.setPressedAction(() -> {
            restart = true;
            goBackBtn.runPressedAction();
        });

        add(tryAgainBtn);

        winVisual.setTryAgainBtn(tryAgainBtn);
    }

    public boolean createRaceLights() {
        long[] timesDifferences = com.getRaceLights();
        if (timesDifferences != null) {

            raceLights = 0;
            raceLightsTimes = new long[timesDifferences.length];

            long now = System.currentTimeMillis();
            for (int i = 0; i < raceLightsTimes.length; i++) {
                raceLightsTimes[i] = timesDifferences[i] + now;
                System.out.println("RACE TIME " + i + ": " + raceLightsTimes[i]
                        + ", " + timesDifferences[i]);
            }
            return true;
        }
        return false;
    }

    private void controlRaceLightsCountdown() {
        // Controls countdown and cheating and such shait.
        if (raceVisual != null && !running) {

            if (raceLightsTimes != null && System
                    .currentTimeMillis() >= raceLightsTimes[raceLights]) {

                this.raceLights++;

                // CONTROL LIGHTS
                if (raceLights == 4) {
                    waitTime = System.currentTimeMillis() + 1000;
                    startTime = raceLightsTimes[3];
                    running = true;

                    audio.get(SfxTypes.GREENLIGHT).play();

                    if (!player.getCar().hasTireboost()) {
                        raceVisual.setWarning("");
                    }
                } else {
                    audio.get(SfxTypes.REDLIGHT).play();
                }

                raceVisual.setBallCount(raceLights);
            }

            // CHEATING
            if (raceLights < 4 && player.getCar().getStats().speed > 0) {
                finishRace(true);
            }
        } else if (raceLights == 4 && waitTime < System.currentTimeMillis()) {
            raceLights = 0;
            raceVisual.setBallCount(raceLights);
            if (player.getRole() == Player.COMMENTATOR)
            	finishRace(true);
        }
    }

    @Override
    public void tick(float delta) {

        if (game.isGameOver()) {
        	int type = -1;
        	if (com.isSingleplayer())
        		type = com.getGamemode().getType();
        	
            game.endAll();
            
            if (restart && type != -1) {
                restart = false;
                createNewSingleplayerGameAction.accept(type);
            }
            return;
        }

        if (currentVisual != null)
            currentVisual.tick(delta);

        if (!finished) {
            Car car = player.getCar();
            car.updateSpeed(delta);
            if (raceLights == 4) {
                int reactionTime = (int) (System.currentTimeMillis() - waitTime + 1000);
                if (car.startBoost(reactionTime))
                    raceVisual.setStartboostTime(reactionTime, car.tireboostLoss(reactionTime));
                else if (car.getStats().throttle)
                    raceVisual.setStartboostTime(reactionTime);
            }

            raceVisual.setExtraGamemodeInfoText(com);
            if (!com.isSingleplayer() && player.getCar().getStats().distance > 0 && raceInformationTick < System.currentTimeMillis()) {
                com.raceInformation(player, (float) -player.getCar().getStats().distance, player.getCar().getSpeed(), System.currentTimeMillis() - startTime); // FIXME baklengs distance
                raceInformationTick = System.currentTimeMillis() + 100;
            }

            checkDistanceLeft();
            controlRaceLightsCountdown();

            if (raceVisual.hasOpponent()) {
                if (player.getOpponent() == -1) {
                	raceVisual.removeOpponent();
                } else {
//                	System.out.println("update opponent");
                	raceVisual.updateOpponentDistance(System.currentTimeMillis() - getStartTime(), currentLength, delta); // FIXME fungerer ikke etter et race selv om med delta så spinner hjul alt for kjapt
                }
            }
        } else {
            updateResults();
            
            if (everyoneFinishedChecked && !currentVisual.equals(winVisual)) {
                lobby.countdown();
            } else {
            	controlRaceLightsCountdown();
           		for (Player p : com.getPlayers()) {
           			p.getCar().getModel().updatePositionByInformation(System.currentTimeMillis() - getStartTime(), currentLength, delta);
           		}
            }
        }
    }

    @Override
    public void renderGame(Renderer renderer, long window, float delta) {
    	
        if (currentVisual != null && !close) {
            currentVisual.renderGame(renderer, window, delta);
        }

        // if (System.currentTimeMillis() - timer > 1000) {
        // System.out.println("FPS RACE: " + frames);
        // timer = System.currentTimeMillis() + 1000;
        // frames = 0;
        // }
        //
        // frames++;

    }

    @Override
    public void renderUILayout(NkContext ctx, MemoryStack stack) {
        if (currentVisual != null && !close) {
            currentVisual.renderUILayout(ctx, stack);
        }
    }

    private void updateResults() {

         understandRaceLobbyFromServer(com.updateRaceLobby(player, false));

        if (goBackBtn == null || finalizedFinishRaceLobby) return;
        
        if (everyoneFinishedChecked && (!com.isSingleplayer() || !currentVisual.hasAnimationsRunning())
                ) {
            // Stop race aka make ready the next race
            // This is still in racewindow, but is to initialize
            // the closing of the racing part.
            // FIXME, no update / tick after this is ran

            finalizedFinishRaceLobby = true;
            game.setPlaceChecked(false);

//            if (!finishedFirstRace)
//                finishedFirstRace = true;

        	press();
        	goBackBtn.setVisible(true);

            if (com.isGameOver() != 0) {

                understandRaceLobbyFromServer(
                        com.updateRaceLobby(player, true));

                winVisual.setEveryoneDone(true);

                String leaderboardScoreText = "";
                String leaderboardScoreExplaination = "";

                if (com.isSingleplayer()) {
                    //beregn score
                    String[] scoreTexts = ((SinglePlayerMode) com.getGamemode()).getCreateScore();
                    if (com.getGamemode().isWinner(player))
                    	features.setAllowedChallenges(com.getGamemode().getType());
                    leaderboardScoreText = scoreTexts[0];
                    leaderboardScoreExplaination = scoreTexts[1];
                }

                winVisual.claimWinner(com, leaderboardScoreText, leaderboardScoreExplaination);
                
                for (Player player : com.getPlayers()) {
                	Car car = player.getCar();
                	car.getAudio().reset();
                }
                
                changeVisual(winVisual);
            } else if (com.isSingleplayer()) {
                lobby.setExtraPlayersListText(singleplayerRaceLobbyAfter());
                goBackBtn.runPressedAction();
            }
        } else {
            // Disable start game button
            goBackBtn.setVisible(false);
        }
    }

    public void understandRaceLobbyFromServer(String codedString) {
        if (codedString == null)
            return;

        String[] outputs = codedString.split("#");

        String result = "Tracklength: " + currentLength
                + " meters.#BLACK\nPlayers:#BLACK";

        // Everyone finished
        if (outputs[0].equals("1") && !everyoneFinishedChecked) {
            afterRaceChangeVisual();
            everyoneFinishedChecked = true;
            com.updateRaceCountdown(player, false);
            game.setStarted(false);
            raceLightsTimes = null;
        }

        result += raceLobbyLabelDecode(outputs);

        raceLobbyLabel.setText(result);
    }

    private String singleplayerRaceLobbyAfter() {
        String codedString = com.updateRaceLobby(player, false);

        if (codedString == null)
            return null;

        String[] outputs = codedString.split("#");
        return raceLobbyLabelDecode(outputs);
    }

    private String raceLobbyLabelDecode(String[] outputs) {
        int n;
        int stageLength = 6;
        int playerIndex = 0;
        boolean finished = false;
        String color = "";
        String nameLine = "";
        StringBuilder infoLine = new StringBuilder();
        StringBuilder timeLine = new StringBuilder();
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < outputs.length; i++) {
            n = i % stageLength;

            switch (n) {

                case 1:
                    color = "";
                    nameLine = "\n     " + outputs[i];
                    infoLine = new StringBuilder();
                    timeLine = new StringBuilder();
                    break;
                case 2:

                    // Controlling whether player has finished or not

                    if (i < stageLength)
                        color = "won";
                    else
                        color = "black";

                    if (Integer.parseInt(outputs[i]) == 1) {
                        finished = true;

                        // Controlling animation of players finishing after a
                        // race
                        boolean prevFinished;
                        try {
                            prevFinished = finishedPlayers[playerIndex];
                        } catch (IndexOutOfBoundsException e) {
                            prevFinished = false;
                            finishedPlayers[playerIndex] = true;
                        }

                        if (!prevFinished) {
                            finishedPlayers[playerIndex] = true;
                        }

                    } else {
                        finished = false;
                        color = "nf";
                    }

                    break;
                case 3:
                    long thisPlayerTime = Long.parseLong(outputs[i]);
                    if (thisPlayerTime == -1) {

                        timeLine.append("DNF");
                        color = "dnf";

                    } else if (finished) {

                        timeLine.append("Time: ").append(Float.parseFloat(outputs[i]) / 1000).append(" seconds");

                    } else if (raceLightsTimes != null && System
                            .currentTimeMillis() >= raceLightsTimes[3]) {

                        timeLine.append("Time: ").append((float) (System.currentTimeMillis()
                                - raceLightsTimes[3]) / 1000).append(" seconds");

                    } else if (startTime == -1) {
                        // This will only happen if you are dnf-ed
                        if (raceLightsTimes == null
                                && com.updateRaceLights(player))
                            createRaceLights();

                        timeLine.append("Waiting");

                        // Dots ...
                        if (waitingDNFTime < System.currentTimeMillis()) {
                            waitingDNFTime = System.currentTimeMillis() + 300;
                            waitingDNF = (waitingDNF + 1) % 4;
                        }
                        timeLine.append(".".repeat(Math.max(0, waitingDNF)));
                    }
                    break;

                case 4:
                    infoLine.append(outputs[i]); //whos ahead?
                    break;
                case 5:

                    String gold = outputs[i];

                    if (!gold.equals("x") && gold.length() != 0)
                        infoLine.append(", ").append(gold);


                    result.append(nameLine).append("#").append(color).append("\n").append(timeLine).append("#").append(color).append("\n")
                            .append(infoLine).append("#").append(color);
                    playerIndex++;

                    break;
            }

        }
        return result.toString();
    }

    public void checkDistanceLeft() {
        if (player.getCar().getDistance() >= currentLength) {
            // Push results and wait for everyone to finish. Then get a winner.'
            finishRace(false);
        }
    }

    private void finishRace(boolean cheated) {
        System.out.println("Finished");

        player.setReady(0);
        com.ready(player, player.getReady());
        raceVisual.removeOpponent();

        Arrays.fill(finishedPlayers, false);
        
        finished = true;
        features.getWindow().mouseStateHide(false);
        long time = cheated ? -1 : System.currentTimeMillis() - startTime;
        com.finishRace(player, time);

        player.getCar().regenTurboBlow();

        raceLights = 0;
        features.getWindow().setCursor(CursorType.cursorNormal);

        changeVisual(finishVisual);
        audio.setListenerData(finishVisual.getCameraPosition());

        // Legg til knapper og sÃ¥nt
        everyoneFinishedChecked = false;
    }

    public void afterRaceChangeVisual() {

        // The problem is that it tells if the race is over here and now
        // instead of when it is confirmed.

        // TODO if(com.isGameOverPossible(player) == 1) {
//		if (com.isGameOver() == 0) {
//		}

    }

    private void changeVisual(Visual newVisual) {

        if (newVisual.equals(currentVisual)) {
            System.out.println("Same visual");
            return;
        }

        currentVisual = newVisual;
    }

    @Override
    public void keyInput(int key, int action) {

        if (!finished) {
            Car car = player.getCar();
            
            if (keys.isNos(key))
                car.nos(action != GLFW.GLFW_RELEASE);
            
            if (action != GLFW.GLFW_RELEASE) {
                /*
                 * PRESS
                 */
                if (keys.isThrottle(key)) {
                    boolean safe = (raceLights > 0 || running);
                    if (!car.throttle(true, safe) && !safe)
                        raceVisual.setWarning("Wanna DNF?");
                }
//				else if (keys.isStompThrottle(key)) {
//					car.stompThrottle(false);
//				} else if (keys.isBrake(key)) {
//					car.brake(true);
//				} 
//				else if (keys.isClutch(key)) {
//					car.clutch(true);
//				} 
                else if (keys.isBlowTurbo(key)) {
                    car.blowTurbo(true);
                }
                // Gearbox
//				else if (!car.getStats().sequentialShift) {
//					for (int i = 0; i <= car.getRep().getGearTop(); i++) {
//						if (keys.isGear(key, i)) {
//							car.shift(i);
//						}
//					}
//				} else {
//					if (keys.isShiftUp(key)) {
//						// up arrow
//						car.shiftUp(true);
//					}
//					if (keys.isShiftDown(key)) {
//						// down arrow
//						car.shiftDown(true);
//					}
//				}
            } else {
                /*
                 * RELEASE
                 */
                if (keys.isThrottle(key)) {
                    car.throttle(false, true);
                    raceVisual.getGearbox().updatePowerloss(System.currentTimeMillis());
                    if (!running)
                        raceVisual.setWarning("");
                }
//				else if (keys.isStompThrottle(key)) {
//					car.stompThrottle(true);
//				} else if (keys.isBrake(key)) {
//					car.brake(false);
//				} 
//				else if (keys.isClutch(key)) {
//					car.clutch(false);
//				}
                else if (keys.isBlowTurbo(key)) {
                    car.blowTurbo(false);
                }
//				else if (keys.isEngineON(key)) {
//					car.setEngineON(true);
//				}
//				else if (car.getStats().sequentialShift) {
//					if (keys.isShiftUp(key)) {
//						// up arrow
//						car.shiftUp(false);
//					}
//					if (keys.isShiftDown(key)) {
//						// down arrow
//						car.shiftDown(false);
//					}
//				}
            }
        } else {
            if (key == GLFW.GLFW_KEY_ENTER && action == GLFW.GLFW_RELEASE && goBackBtn != null)
                goBackBtn.runPressedAction();
            if (currentVisual != null)
                currentVisual.keyInput(key, action);
        }
    }

    @Override
    public void mouseScrollInput(float x, float y) {
    	currentVisual.mouseScrollInput(x, y);
    }

    @Override
    public boolean mouseButtonInput(int button, int action, float x, float y) {
    	super.mouseButtonInput(button, action, x, y);
        currentVisual.mouseButtonInput(button, action, x, y);
        return false;
    }

    @Override
    public void mousePositionInput(float x, float y) {
        currentVisual.mousePosInput(x, y);
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(int currentLength) {
        this.currentLength = currentLength;
    }

    public Communicator getCom() {
        return com;
    }

    public void setCom(Communicator com2) {
        this.com = com2;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public long getStartTime() {
        if (raceLightsTimes != null)
            return raceLightsTimes[3];
        else
            return -1;
    }

    public long[] getRaceLights() {
        return raceLightsTimes;
    }

    public boolean isFinishedFirstRace() {
        return finishedFirstRace;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public void turnOff() {
        finishedFirstRace = false;
        raceLightsTimes = null;
        initiated = false;
        player = null;
        close = false;
        finished = false;
        finishedPlayers = null;
        everyoneFinishedChecked = false;
    }

    @Override
    public void destroy() {
        if (finishVisual != null) {
            finishVisual.removeAllGameObjects();
            finishVisual.removeAllUIObjects();
        }
        if (raceVisual != null) {
            raceVisual.removeAllGameObjects();
            raceVisual.removeAllUIObjects();
        }
        raceVisual = null;
        finishVisual = null;
        currentVisual = null;
        goBackBtn = null;

        removeGameObjects();
    }

}
