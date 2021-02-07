package elem.ui;

import java.io.UnsupportedEncodingException;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamLeaderboardEntry;

import main.Features;
import player_local.Car.Car;

public class UILeaderboardPlayerButton implements IUIObject {
	
	private final SteamID steamID;
	private int carID = -1;
	private final UILabel label;

    public UILeaderboardPlayerButton(Features features, SteamLeaderboardEntry entry) {
        this.steamID = entry.getSteamIDUser();
        label = new UILabel();
        setBaseTitle(features, entry);
    }

    public void setBaseTitle(Features features, SteamLeaderboardEntry entry) {
    	label.text = entry.getGlobalRank() + ". " + hexToAscii(features.getSteamHandler().getUsername(steamID)) + ":    " + entry.getScore() + " SCORE";
        setTitle();
    }
    
    private String hexToAscii(String hexStr) {
        try {
			return new String(hexStr.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return null;
    }
    
    private void setTitle() {
    	if (carID != -1) {
    		label.text += " using a " + Car.CAR_TYPES[carID];
    	}
    }

	public SteamID getSteamID() {
		return steamID;
	}

	public void setCarID(int id) {
		this.carID = id;
		if (label.text != null)
			setTitle();
	}

	@Override
	public void layout(NkContext ctx, MemoryStack stack) {
		label.layout(ctx, stack);
	}
}
