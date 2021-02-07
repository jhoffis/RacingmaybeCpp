package settings_and_logging;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import main.steam.SteamHandler;

public class RegularSettings extends Settings {

	private int lineWidth, lineHeight, lineClientFullscreen, lineVsync,
			 lineMaster, lineMusic, lineSfx, lineUsername;

	public RegularSettings() {

		lineWidth = 0;
		lineHeight = 1;
		lineClientFullscreen = 2;
		lineVsync = 3;
		lineMaster = 4;
		lineMusic = 5;
		lineSfx = 6;
		lineUsername = 7;

		if (super.init("settings.properties")) {
			// Get resolution that is set to the computer right now.
			GraphicsDevice gd = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			int width = gd.getDisplayMode().getWidth();
			int height = gd.getDisplayMode().getHeight();

			setUsername("");
			setWidth(width);
			setHeight(height);
			setFullscreen(true);
			setVsync(false);
			setMasterVolume(0.5);
			setMusicVolume(0.3);
			setSfxVolume(1);

		}

	}

	public void setWidth(int v) {
		writeToLine("Width=" + v, lineWidth);
	}

	public int getWidth() {
		return getSettingInteger(lineWidth);
	}

	public void setHeight(int v) {
		writeToLine("Height=" + v, lineHeight);
	}

	public int getHeight() {
		return getSettingInteger(lineHeight);
	}

	public void setFullscreen(boolean v) {
		writeToLine("ClientFullscreen=" + (v ? 1 : 0), lineClientFullscreen);
	}

	public void setVsync(boolean b) {
		writeToLine("Vsync=" + (b ? 1 : 0), lineVsync);
	}

	public boolean getVsync() {
		return getSettingBoolean(lineVsync);
	}
	
	public void setMasterVolume(double val) {
		writeToLine("masterVolume=" + val, lineMaster);
	}

	public void setMusicVolume(double val) {
		writeToLine("musicVolume=" + val, lineMusic);
	}

	public void setSfxVolume(double val) {
		writeToLine("sfxVolume=" + val, lineSfx);
	}

	public double getMasterVolume() {
		return getSettingDouble(lineMaster);
	}

	public double getMusicVolume() {
		return getSettingDouble(lineMusic);
	}

	public double getSfxVolume() {
		return getSettingDouble(lineSfx);
	}

	public boolean getFullscreen() {
		return getSettingBoolean(lineClientFullscreen);
	}

	public void setUsername(String username) {
		writeToLine("username=" + username, lineUsername);
	}

	public String getUsername() {
		return getSetting(lineUsername);
	}

}
