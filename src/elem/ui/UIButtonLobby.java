package elem.ui;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;

import elem.ColorBytes;
import main.Features;

/**
 * Allows for selection and double clicking
 * 
 * @author Jens Benz
 *
 */
public class UIButtonLobby extends UIButton {

	private Features features;
	private SteamID lobby;
	private boolean selected;
	private long lastTimePressed;

	public UIButtonLobby(Features features, String title ) {
		super(title);
		this.features = features;
	}

	public void setSelected(boolean selected) {
		ColorBytes normal = null;
		ColorBytes active = null;
		ColorBytes hover = null;

		this.selected = selected;
		
		if (selected) {
			normal = new ColorBytes(0x22, 0x22, 0x22, 0xff);
			active = new ColorBytes(0x11, 0x11, 0x11, 0xff);
			hover = new ColorBytes(0x33, 0x33, 0x33, 0xff);
		} else {
			normal = new ColorBytes(0x22, 0x22, 0x22, 0x55);
			active = new ColorBytes(0x11, 0x11, 0x11, 0xff);
			hover = new ColorBytes(0x55, 0x55, 0x55, 0xdd);
		}

		super.normal = normal.create();
		super.active = active.create();
		super.hover = hover.create();
	}

	/**
	 * 0 = unselect, 1 = select, 2 = doubleclick and run
	 */
	public int click() {
		long now = System.currentTimeMillis();
		System.out.println("Time: " + (now - lastTimePressed));
		if (now - lastTimePressed < 250) {
			setSelected(true);
			return 2;
		}
		lastTimePressed = now;
		
		setSelected(!selected);
		return selected ? 1 : 0;
	}
	
	public void joinThisLobby() {
		new SteamMatchmaking(features).joinLobby(lobby);
	}
	
	public SteamID getLobby() {
		return lobby;
	}

	public void setLobby(SteamID lobby) {
		this.lobby = lobby;
	}

	public boolean isSelected() {
		return selected;
	}
	
}
