package main.steam;

import java.util.function.Consumer;

import com.codedisaster.steamworks.SteamAuth.AuthSessionResponse;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamFriends.PersonaChange;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;

import main.Features;
import settings_and_logging.RegularSettings;

public class SteamHandler implements SteamUserCallback, SteamFriendsCallback{

	private Consumer<Integer> initMovingIntoALobby;
	private SteamFriends friends;
	private SteamUser user;
	private Features features;
	
	public SteamHandler() {
		this.friends = new SteamFriends(this);
		this.user = new SteamUser(this);
		
		System.out.println("My steam id is " + getMySteamID());
	}

	@Override
	public void onSetPersonaNameResponse(boolean success, boolean localSuccess, SteamResult result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPersonaStateChange(SteamID steamID, PersonaChange change) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameOverlayActivated(boolean active) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameLobbyJoinRequested(SteamID steamIDLobby, SteamID steamIDFriend) {
		features.setSelectedLobby(steamIDLobby);
		initMovingIntoALobby.accept(0);
	}

	@Override
	public void onAvatarImageLoaded(SteamID steamID, int image, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFriendRichPresenceUpdate(SteamID steamIDFriend, int appID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameRichPresenceJoinRequested(SteamID steamIDFriend, String connect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameServerChangeRequested(String server, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onValidateAuthTicket(SteamID steamID, AuthSessionResponse authSessionResponse, SteamID ownerSteamID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEncryptedAppTicket(SteamResult result) {
		// TODO Auto-generated method stub
		
	}
	
	public SteamID getMySteamID() {
		return user.getSteamID();
	}
	
	public String getUsername(SteamID steamID) {
		return friends.getFriendPersonaName(steamID);
	}

	public void setFeatures(Features features) {
		this.features = features;
	}

	public void setJoinActions(Consumer<Integer> initMovingIntoALobby) {
		this.initMovingIntoALobby = initMovingIntoALobby;
	}

	public void initUsername(RegularSettings settings) {
		if (settings.getUsername() == null) 
			settings.setUsername(getUsername(getMySteamID()));
	}
	
}
