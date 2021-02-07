package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;
import java.util.function.Consumer;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;

import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamMatchmaking;
import com.codedisaster.steamworks.SteamMatchmaking.ChatEntryType;
import com.codedisaster.steamworks.SteamMatchmaking.ChatMemberStateChange;
import com.codedisaster.steamworks.SteamMatchmaking.ChatRoomEnterResponse;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamResult;

import adt.IAction;
import audio.AudioRemote;
import audio.SfxTypes;
import elem.ColorBytes;
import elem.ui.UIButton;
import elem.ui.UIButtonLobby;
import elem.ui.UISceneInfo;
import engine.io.Window;
import main.steam.SteamHandler;
import scenes.Scenes;
import scenes.game.Lobby;
import settings_and_logging.RegularSettings;

public class Features implements SteamMatchmakingCallback {

	// Std random for å unngå mye allokering
	public final static Random ran = new Random();

	private Stack<ColorBytes> backgroundColorCache = new Stack<ColorBytes>();
	private AudioRemote audio;

	private RegularSettings settings;
	private Window window;

	private SteamHandler steamHandler;
	private ArrayList<SteamID> lobbiesChecked = new ArrayList<SteamID>();
	private HashMap<SteamID, UIButtonLobby> lobbies = new HashMap<SteamID, UIButtonLobby>();
	private SteamID currentLobby = null;
	private String name;
	private int role;
	private UIButtonLobby selectedLobby = null;
	private String lobbiesInnerText = "";
	private Lobby lobby;
	private SteamMatchmaking matchMaking;

	private IAction closeUsernameModalAction;

	private String lobbyName;

	private int allowedChallenges;

	private Consumer<UIButtonLobby> lobbyBtnAction;

	public Features(AudioRemote audio,
			RegularSettings settings, SteamHandler steamHandler,
			Window window) {
		this.settings = settings;
		this.audio = audio;
		this.steamHandler = steamHandler;
		this.window = window;
		if (steamHandler != null)
			matchMaking = new SteamMatchmaking(this);
	}

	public void setBackgroundColor(NkContext ctx) {
		ColorBytes bg = backgroundColorCache.peek();
		ctx.style().window().fixed_background().data().color().set(bg.r(),
				bg.g(), bg.b(), bg.a());
	}

	public void pushBackgroundColor(NkContext ctx, ColorBytes color) {
		pushBackgroundColor(color);
		Nuklear.nk_style_push_color(ctx,
				ctx.style().window().fixed_background().data().color(),
				getBackgroundColorCache().peek().create());
	}

	public void pushBackgroundColor(ColorBytes color) {
		backgroundColorCache.push(color);
	}

	public void popBackgroundColor(NkContext ctx) {
		backgroundColorCache.pop();
		Nuklear.nk_style_pop_color(ctx);
	}


	public Stack<ColorBytes> getBackgroundColorCache() {
		return backgroundColorCache;
	}
	
	public void createLobbyBtnAction(UIButton joinBtnReference) {
		lobbyBtnAction = (btn) -> {
			System.out.println("press lobby btn");
			audio.get(SfxTypes.REGULAR_PRESS).play();
			if (this.selectedLobby != null && !this.selectedLobby.equals(btn)) {
				this.selectedLobby.setSelected(false);
			}
			
			this.selectedLobby = btn;
			int click = this.selectedLobby.click();
			if (click == 2) {
				joinBtnReference.setEnabled(true);
				joinBtnReference.runPressedAction();
			} else {
				joinBtnReference.setEnabled(click != 0);
			}
		};
	}

	@Override
	public void onLobbyMatchList(int lobbiesMatching) {
		lobbies.clear();
		lobbiesChecked.clear();
		
//		if (Game.DEBUG) {
//			UIButtonLobby testBtn = new UIButtonLobby(this, "");
//			testBtn.setConsumerValue(testBtn);
//			testBtn.setPressedAction(lobbyBtnAction);
//			lobbies.put(new SteamID(), testBtn);
//			UISceneInfo.addPressableToScene(Scenes.MULTIPLAYER, testBtn);
//		}
		
		for (int i = 0; i < lobbiesMatching; i++) {

			UIButtonLobby btn = new UIButtonLobby(this, "");
			SteamID lobbyID = new SteamMatchmaking(this).getLobbyByIndex(i);
			lobbies.put(lobbyID, btn);

			btn.setTitleAlignment(Nuklear.NK_TEXT_ALIGN_LEFT);
			btn.setLobby(lobbyID);
			btn.setConsumerValue(btn);
			btn.setPressedAction(lobbyBtnAction);
			UISceneInfo.addPressableToScene(Scenes.MULTIPLAYER, btn);

			boolean ack = new SteamMatchmaking(this).requestLobbyData(lobbyID);
			System.out.println(ack);
		}

		if (lobbies.isEmpty())
			lobbiesInnerText = "no lobbies found";
		else
			lobbiesInnerText = "";
	}

	@Override
	public void onLobbyDataUpdate(SteamID steamIDLobby, SteamID steamIDMember, boolean success) {
		if (!lobbiesChecked.contains(steamIDLobby)) {

			UIButtonLobby lobbyBtn = null;
			lobbiesChecked.add(steamIDLobby);
			
			if (lobbies.containsKey(steamIDLobby)) {
				lobbyBtn = lobbies.get(steamIDLobby);
			} else {
				return;
			}
			String title = matchMaking.getLobbyData(steamIDLobby, "name");
			boolean started = false;
			try {
				started = Integer.parseInt(matchMaking.getLobbyData(steamIDLobby, "started")) != 0;
			} catch (NumberFormatException e) {
				System.out.println("Fant ikke Started data fra lobby " + e.getMessage());
			}
			
			if (title.equals("") || started) {
				lobbiesChecked.remove(steamIDLobby);
				lobbies.remove(steamIDLobby);
				
				if (lobbies.isEmpty())
					lobbiesInnerText = "no lobbies found";
				return;
			}
			String[] versionCheck = title.split(", ");

			if(versionCheck.length > 0 && versionCheck[versionCheck.length - 1].equals(Game.VERSION)) {
				lobbyBtn.setEnabled(true);
				lobbyBtn.setTitle(title + "  -  " + matchMaking.getNumLobbyMembers(steamIDLobby) + "/"
						+ matchMaking.getLobbyMemberLimit(steamIDLobby));
			} else {
				lobbiesChecked.remove(steamIDLobby);
				lobbies.remove(steamIDLobby);
			}
		}
	}

	@Override
	public void onLobbyKicked(SteamID steamIDLobby, SteamID steamIDAdmin,
			boolean kickedDueToDisconnect) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLobbyInvite(SteamID steamIDUser, SteamID steamIDLobby,
			long gameID) {
		// TODO Auto-generated method stub
		System.out.println("onLobbyInvite");

	}

	@Override
	public void onLobbyGameCreated(SteamID steamIDLobby,
			SteamID steamIDGameServer, int ip, short port) {
		// TODO Auto-generated method stub
		System.out.println("onLobbyGameCreated");
	}

	@Override
	public void onLobbyEnter(SteamID steamIDLobby, int chatPermissions,
			boolean blocked, ChatRoomEnterResponse response) {
		currentLobby = steamIDLobby;
		lobby.joinNewLobby(name, role, getLobbyOwner());
		closeUsernameModalAction.run();
	}
	
	public SteamID getLobbyOwner() {
		if(currentLobby != null) {
			return matchMaking.getLobbyOwner(currentLobby);
		}
		return null;
	}

	@Override
	public void onLobbyCreated(SteamResult result, SteamID steamIDLobby) {
		if (result == SteamResult.OK) {
			System.out.println("WADDAAPPPPp " + steamIDLobby.getAccountID());
			matchMaking.setLobbyData(steamIDLobby, "name", lobbyName);
			matchMaking.setLobbyData(steamIDLobby, "started", "0");
			System.out.println(lobbyName);

			// System.out.println("name: " + new SteamFriends(new
			// SteamFriendsCallback).getFriendPersonaName(steamIDLobby));
			currentLobby = steamIDLobby;
			lobby.createNewLobby(name, role, true, -1);
			closeUsernameModalAction.run();
		}
	}

	@Override
	public void onLobbyChatUpdate(SteamID steamIDLobby,
			SteamID steamIDUserChanged, SteamID steamIDMakingChange,
			ChatMemberStateChange stateChange) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLobbyChatMessage(SteamID steamIDLobby, SteamID steamIDUser,
			ChatEntryType entryType, int chatID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFavoritesListChanged(int ip, int queryPort, int connPort,
			int appID, int flags, boolean add, int accountID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFavoritesListAccountsUpdated(SteamResult result) {
		// TODO Auto-generated method stub

	}

	public UIButtonLobby[] getLobbies() {
		UIButtonLobby[] list = new UIButtonLobby[lobbies.size()];
		return lobbies.values().toArray(list);
	}

	public void leave() {
		System.out.println("leave lobby");
		if (currentLobby != null)
			new SteamMatchmaking(this).leaveLobby(currentLobby);
		currentLobby = null;
	}

	public void clearLobbies() {
		lobbies.clear();
		selectedLobby = null;
	}

	public void setSelectedLobby(SteamID steamIDLobby) {
		selectedLobby = new UIButtonLobby(this, "fake");
		selectedLobby.setLobby(steamIDLobby);
	}

	public UIButtonLobby getSelectedLobby() {
		return selectedLobby;
	}

	public void joinNewLobby(String name, int role) {
		if (selectedLobby != null && selectedLobby.getLobby() != null) {
			this.name = name;
			this.role = role;
			selectedLobby.joinThisLobby();
		}
	}

	public void createNewLobby(String name, int role, String lobbyName, boolean publicLobby, int amount) {
		this.name = name;
		this.role = role;
		this.lobbyName = lobbyName + ", " + Game.VERSION;
		matchMaking.createLobby(publicLobby ? SteamMatchmaking.LobbyType.Public : SteamMatchmaking.LobbyType.FriendsOnly, amount);
	}

	public void startLobby() {
		matchMaking.setLobbyData(currentLobby, "started", "1");
	}


	public RegularSettings getSettings() {
		return settings;
	}

	public String getUsername() {
		String username = settings.getUsername();
		if (username == null) {
			username = steamHandler.getUsername(steamHandler.getMySteamID());
		}
		return username;
	}

	public String getLobbiesInnerText() {
		return lobbiesInnerText;
	}

	public void setLobbiesInnerText(String lobbiesInnerText) {
		this.lobbiesInnerText = lobbiesInnerText;
	}

	public Window getWindow() {
		return window;
	}

	public SteamHandler getSteamHandler() {
		return steamHandler;
	}

	public void setLobby(Lobby lobby) {
		this.lobby = lobby;
	}

	public void setCloseUsernameModalAction(IAction action) {
		this.closeUsernameModalAction = action;
	}

	public void setAllowedChallenges(int i) {
		if (i > allowedChallenges)
			allowedChallenges = i;
	}
	
	public int getAllowedChallenges() {
		return allowedChallenges;
	}

}
