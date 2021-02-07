package audio;

import engine.math.Vec3;

public interface AudioRemote {

	void setVolume(AudioTypes type, float volume);
	float getVolume(AudioTypes type);

	void updateVolumeSfx();
	void updateVolumeMusic();
	
	CarAudio getNewCarAudio(String carname);
	
	Source get(SfxTypes sfx);
	Source getUpgrade(int i);
	Source getTaunt(int i);
	void checkMusic();
	void setListenerData(float x, float y, float z);
	void setListenerData(Vec3 vector);
}
