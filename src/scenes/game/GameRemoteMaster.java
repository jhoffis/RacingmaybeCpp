package scenes.game;

import adt.IAction;

public class GameRemoteMaster implements GameRemote {

	private final IAction endAllAction;
	private boolean placeChecked;
	private boolean started;
	private boolean gameOver;
	private boolean isEnding;
	
	public GameRemoteMaster(IAction endAllAction) {
		this.endAllAction = endAllAction;
	}
	
	@Override
	public void endAll() {
		if (!isEnding) {
			isEnding = true;
			endAllAction.run();
		}
	}

	@Override
	public boolean isPlaceChecked() {
		return placeChecked;
	}

	@Override
	public void setPlaceChecked(boolean b) {
		this.placeChecked = b;
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public void setStarted(boolean b) {
		this.started = b;
	}

	@Override
	public void gameOver(boolean b) {
		gameOver = b;		
	}

	@Override
	public boolean isGameOver() {
		return gameOver;
	}

}
