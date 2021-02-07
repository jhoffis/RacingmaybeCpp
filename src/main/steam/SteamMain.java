package main.steam;

import java.util.function.Consumer;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;

public class SteamMain {

	public void init(Consumer<SteamHandler> afterInit) {
		new Thread(() -> {
			boolean succInit = true;
			try {
	
				SteamAPI.loadLibraries("./lib");
	
				if (SteamAPI.restartAppIfNecessary(1261300)) {
					System.out.println("Restarting through steam...");
					succInit = false;
				}
	
				if (!SteamAPI.init()) {
					System.out.println(
							"Steamworks initialization error, e.g. Steam client not running");
					succInit = false;
				}
			} catch (SteamException e) {
				// Error extracting or loading native libraries
				e.printStackTrace();
				succInit = false;
			}
	
			if (!succInit) {
				System.exit(-1);
			} else {
				afterInit.accept(new SteamHandler());
			}
		}).start();
	}

	public void update() {
		if (SteamAPI.isSteamRunning()) {
			SteamAPI.runCallbacks();
		}
	}

	public void destroy() {
		SteamAPI.releaseCurrentThreadMemory();
		SteamAPI.shutdown();
	}

}
