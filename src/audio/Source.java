package audio;

import org.lwjgl.openal.AL10;

import engine.math.Vec3;

/**
 * NB! Posisjon av lyd og velocity fungerer bare med MONO og ikke STEREO!!!
 */
public class Source {

	private int sourceID;

	public Source() {
		sourceID = AL10.alGenSources();
		AL10.alSourcef(sourceID, AL10.AL_ROLLOFF_FACTOR, 2f);
		AL10.alSourcef(sourceID, AL10.AL_REFERENCE_DISTANCE, 8);
		AL10.alSourcef(sourceID, AL10.AL_MAX_DISTANCE, 200);
		
		volume(1);
		pitch(1);
		position(0, 0, 0);
	}
	
	public Source(int buffer) {
		this();
		AL10.alSourcei(sourceID, AL10.AL_BUFFER, buffer);
	}

	public void play(int buffer) {
		stop();
		AL10.alSourcei(sourceID, AL10.AL_BUFFER, buffer);
		resume();
	}
	
	public void play() {
		stop();
		resume();
	}
	
	public void play(float pitch) {
		pitch(pitch);
		play();
	}

	public void stop() {
		AL10.alSourceStop(sourceID);
	}

	public void pause() {
		AL10.alSourcePause(sourceID);
	}

	public void resume() {
		AL10.alSourcePlay(sourceID);
	}

	public void velocity(float x, float y, float z) {
		AL10.alSource3f(sourceID, AL10.AL_VELOCITY, x, y, z); 
	}

	public void loop(boolean loop) {
		AL10.alSourcei(sourceID, AL10.AL_LOOPING,
				loop ? AL10.AL_TRUE : AL10.AL_FALSE);
	}

	public void volume(float volume) {
		AL10.alSourcef(sourceID, AL10.AL_GAIN, volume);
	}

	public void pitch(float pitch) {
		AL10.alSourcef(sourceID, AL10.AL_PITCH, pitch);
	}

	public void position(Vec3 position) {
		AL10.alSource3f(sourceID, AL10.AL_POSITION, position.x, position.y, position.z);
	}
	
	public void position(float x, float y, float z) {
		AL10.alSource3f(sourceID, AL10.AL_POSITION, x, y, z);
	}

	public boolean isPlaying() {
		return AL10.alGetSourcei(sourceID,
				AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}
	
	public void delete() {
		stop();
		AL10.alDeleteSources(sourceID);
	}

	public int getBuffer() {
		return AL10.alGetBufferi(sourceID, AL10.AL_BUFFER);
	}
}

