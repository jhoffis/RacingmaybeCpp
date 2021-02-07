package scenes.game;

public interface GameRemote {

	public void endAll();

	public boolean isPlaceChecked();
	
	public void setPlaceChecked(boolean b);

	public boolean isStarted();
	
	public void setStarted(boolean b);

	public void gameOver(boolean b);
	
	public boolean isGameOver();
	
}
