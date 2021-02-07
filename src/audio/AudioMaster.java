package audio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;

import elem.upgrades.Upgrades;
import engine.math.Vec3;
import main.Features;

/**
 * 
 * Make some beautiful single sound effects for whenever you press something.
 * Like in aoe1
 * 
 * @author Jens Benz
 *
 */
public class AudioMaster implements AudioRemote {

	private static final String[] musicTitles = {"9000 days later.ogg", "beans incoming.ogg", "Ting tang whatever.ogg",
			"funkky.ogg", "Tenmillionface Housey Funk Battle.ogg",
			"thunderbirds fighting.ogg", "you may pass stylishly.ogg",
			"Real nice.ogg", "Save the princess or something.ogg", 
			"2am drive.ogg",
			"racing2.ogg", "blues_01.ogg"
			};
	private static final boolean[] musicPlayed = new boolean[musicTitles.length];

	private List<Integer> buffers;
	private long device, context;
	private float masterVolume, musicVolume, sfxVolume;

	private Source music;
	private HashMap<SfxTypes, Source> sfxs;
	private Source[] upgrades;
	private HashMap<Integer, Source> taunts;
	private Stack<CarAudio> cars; // FIXME load only once?
	
	private int turboBlowoffLow, turboBlowoffHigh,turbospool,straightcut,redline,tireboost,grind,nos, nosDown, soundbarrier,clutchIn,clutchOut, backfire;

	public AudioMaster(float master, float sfx, float music) {
		this.masterVolume = master;
		this.sfxVolume = sfx;
		this.musicVolume = music;
		
		try {
			device = ALC10.alcOpenDevice((ByteBuffer) null);
			ALCCapabilities deviceCaps = ALC.createCapabilities(device);

			if (device != 0) {
				context = ALC10.alcCreateContext(device, (IntBuffer) null);
				ALC10.alcMakeContextCurrent(context);
				AL.createCapabilities(deviceCaps);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		setListenerData(0, 0, 0);
		AL10.alDistanceModel(AL11.AL_INVERSE_DISTANCE_CLAMPED);
		
		/*
		 * LOAD SHIT
		 */
		new Thread(() -> {
			cars = new Stack<CarAudio>();
			buffers = new ArrayList<Integer>();
			sfxs = new HashMap<SfxTypes, Source>();
			taunts = new HashMap<Integer, Source>();
			upgrades = new Source[Upgrades.UPGRADE_NAMES.length];

			int regularPress = loadSound("audio/sfx/button/press_reg.ogg");
			int regularHover = loadSound("audio/sfx/button/hover_reg.ogg");
			int ready = loadSound("audio/sfx/button/ready.ogg");
			int unready = loadSound("audio/sfx/button/unready.ogg");
			int buysucc = loadSound("audio/sfx/button/money.ogg");
			int buyfail = loadSound("audio/sfx/button/failed.ogg");
			int redLight = loadSound("audio/sfx/race/redLight.ogg");
			int greenLight = loadSound("audio/sfx/race/greenLight.ogg");
			int countdown = loadSound("audio/sfx/race/tick.ogg");
			int start = loadSound("audio/sfx/race/tockstart.ogg");
			int openStore = loadSound("audio/sfx/upgrade/openStore.ogg");
			int closeStore = loadSound("audio/sfx/upgrade/closeStore.ogg");
			int joined = loadSound("audio/sfx/button/joined.ogg");
			int left = loadSound("audio/sfx/button/left.ogg");
			int chat = loadSound("audio/sfx/button/chat.ogg");
			int newBonus = loadSound("audio/sfx/bonus/newBonus.ogg");
			int cancelBonus = loadSound("audio/sfx/bonus/cancelBonus.ogg");
			int won = loadSound("audio/sfx/race/won.ogg");
			int lost = loadSound("audio/sfx/race/lost.ogg");
	
			createSfx(SfxTypes.REGULAR_PRESS, regularPress);
			createSfx(SfxTypes.REGULAR_HOVER, regularHover);
			createSfx(SfxTypes.READY, ready);
			createSfx(SfxTypes.UNREADY, unready);
			createSfx(SfxTypes.BUY, buysucc);
			createSfx(SfxTypes.BUY_FAILED, buyfail);
			createSfx(SfxTypes.REDLIGHT, redLight);
			createSfx(SfxTypes.GREENLIGHT, greenLight);
			createSfx(SfxTypes.COUNTDOWN, countdown);
			createSfx(SfxTypes.START, start);
			createSfx(SfxTypes.OPEN_STORE, openStore);
			createSfx(SfxTypes.CLOSE_STORE, closeStore);
			createSfx(SfxTypes.JOINED, joined);
			createSfx(SfxTypes.LEFT, left);
			createSfx(SfxTypes.CHAT, chat);
			createSfx(SfxTypes.NEW_BONUS, newBonus);
			createSfx(SfxTypes.CANCEL_BONUS, cancelBonus);
			createSfx(SfxTypes.WON, won);
			createSfx(SfxTypes.LOST, lost);
			createSfx(SfxTypes.WHOOSH, loadSound("audio/sfx/car/whoosh.ogg"));
			createSfx(SfxTypes.LOSTLIFE, loadSound("audio/sfx/race/lostlife.ogg"));
			createSfx(SfxTypes.START_ENGINE, loadSound("audio/sfx/car/startMotor.ogg"));
			createSfx(SfxTypes.GOLD_BONUS, loadSound("audio/sfx/bonus/goldBonus.ogg"));
			createSfx(SfxTypes.NORMAL_BONUS, loadSound("audio/sfx/bonus/normalBonus.ogg"));
			createSfx(SfxTypes.UNLOCKED, loadSound("audio/sfx/upgrade/unlocked.ogg"));
			
	
			for (int i = 0; i < Upgrades.UPGRADE_NAMES.length; i++) {
				int buffer = loadSound(
						"audio/sfx/upgrade/upgrade" + i + ".ogg");
				createUpgrade(i, buffer);
			}
			
			turboBlowoffLow = loadSound("audio/sfx/car/turboblowofflow.ogg");
			turboBlowoffHigh = loadSound("audio/sfx/car/turboblowoffhigh.ogg");
			turbospool = loadSound("audio/sfx/car/turbospool.ogg");
			straightcut = loadSound("audio/sfx/car/straightcutgears.ogg");
			redline = loadSound("audio/sfx/car/redline.ogg");
			grind = loadSound("audio/sfx/car/grind.ogg");
			nos = loadSound("audio/sfx/car/nos.ogg");
			nosDown = loadSound("audio/sfx/car/nosDown.ogg");
			soundbarrier = loadSound("audio/sfx/car/soundbarrier.ogg");
			clutchIn = loadSound("audio/sfx/car/clutch.ogg");
			clutchOut = loadSound("audio/sfx/car/unclutch.ogg");
			backfire = loadSound("audio/sfx/car/backfireSequential.ogg");
			tireboost = upgrades[Upgrades.tbID].getBuffer();
	
			createTaunt(0, loadSound("audio/sfx/taunt/greetings.ogg"));
			createTaunt(1, loadSound("audio/sfx/taunt/yes.ogg"));
			createTaunt(2, loadSound("audio/sfx/taunt/no.ogg"));
			createTaunt(3, loadSound("audio/sfx/taunt/hows_your_car_running.ogg"));
			createTaunt(4, loadSound("audio/sfx/taunt/giveer_the_beans.ogg"));
			createTaunt(5, loadSound("audio/sfx/taunt/need_more_tireboost.ogg"));
			createTaunt(6, loadSound("audio/sfx/taunt/woah.ogg"));
			createTaunt(7,
					loadSound("audio/sfx/taunt/i_have_more_money_than_you.ogg"));
			createTaunt(8,
					loadSound("audio/sfx/taunt/it_be_like_that_sometimes.ogg"));
			createTaunt(9, loadSound("audio/sfx/taunt/impressive_very_nice.ogg"));
			createTaunt(10, loadSound("audio/sfx/taunt/i_see.ogg"));
			createTaunt(11, loadSound("audio/sfx/taunt/ahaha.ogg"));
			createTaunt(12, loadSound("audio/sfx/taunt/perhaps.ogg"));
			createTaunt(13, loadSound("audio/sfx/taunt/i_dont_think_so.ogg"));
			createTaunt(14,
					loadSound("audio/sfx/taunt/start_the_game_already.ogg"));
			createTaunt(15, loadSound("audio/sfx/taunt/main_menu.ogg"));
			createTaunt(16, loadSound("audio/sfx/taunt/heh_nice_car.ogg"));
			createTaunt(17, loadSound("audio/sfx/taunt/nowhere.ogg"));
	
			this.music = new Source();
	
			updateVolumeSfx();
			updateVolumeMusic();
		}).start();
	}

	public AudioMaster(double master, double music, double sfx) {
		this((float) master, (float) music, (float) sfx);
	}

	@Override
	public void setListenerData(Vec3 v) {
		setListenerData(v.x, v.y, v.z);
	}
	
	@Override
	public void setListenerData(float x, float y, float z) {
		AL10.alListener3f(AL10.AL_POSITION, x, y, z);
		AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
	}

	public int loadSound(String file) {
		int buffer = AL10.alGenBuffers();
		buffers.add(buffer);
        ShortBuffer pcm = null;
        ByteBuffer vorbis = null;
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
        	
            try (
                InputStream source = new BufferedInputStream(new FileInputStream(file));
                ReadableByteChannel rbc = Channels.newChannel(source)) {
            	int bufferSize = source.available();
            	vorbis = BufferUtils.createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(vorbis);
                    if (bytes == -1) {
                        break;
                    }
                    if (vorbis.remaining() == 0) {
                    	vorbis = resizeBuffer(vorbis, vorbis.capacity() * 2);
                    }
                }
            } catch (IOException e) {
				e.printStackTrace();
			}

            vorbis.flip();
        	
            IntBuffer channels = stack.mallocInt(1), sampleRate = stack.mallocInt(1);
            pcm = STBVorbis.stb_vorbis_decode_memory(vorbis, channels, sampleRate);

            // Copy to buffer
            AL10.alBufferData(buffer, channels.get() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, pcm, sampleRate.get());
        }
		return buffer;
	}
	
	private ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

	public void createSfx(SfxTypes type, int buffer) {
		sfxs.put(type, new Source(buffer));
	}

	public void createUpgrade(int i, int buffer) {
		upgrades[i] = new Source(buffer);
	}

	public void createTaunt(int i, int buffer) {
		taunts.put(i, new Source(buffer));
	}

	public void destroy() {

		for (int buffer : buffers) {
			AL10.alDeleteBuffers(buffer);
		}
		buffers.clear();

		for (Source s : sfxs.values()) {
			s.delete();
		}

		for (Source s : upgrades) {
			s.delete();
		}
		
		music.delete();
		
		while(!cars.isEmpty()) {
			cars.pop().destroy();
		}

		ALC10.alcDestroyContext(context);
		ALC10.alcCloseDevice(device);
	}

	@Override
	public void setVolume(AudioTypes type, float volume) {
		switch (type) {
			case MASTER :
				masterVolume = volume;
				break;
			case MUSIC :
				musicVolume = volume;
				break;
			case SFX :
				sfxVolume = volume;
				break;
		}
	}

	@Override
	public float getVolume(AudioTypes type) {
		switch (type) {
			case MASTER :
				return masterVolume;
			case MUSIC :
				return musicVolume;
			case SFX :
				return sfxVolume;
			default :
				return -1;
		}
	}

	@Override
	public void updateVolumeSfx() {
		float volume = this.masterVolume * this.sfxVolume;

		for (Entry<SfxTypes, Source> entry : sfxs.entrySet()) {
			Source sfx = entry.getValue();
			sfx.volume(volume);
		}
		for (Entry<Integer, Source> entry : taunts.entrySet()) {
			Source taunt = entry.getValue();
			taunt.volume(volume);
		}
		for (Source up : upgrades) {
			up.volume(volume);
		}
	}

	@Override
	public void updateVolumeMusic() {
		music.volume(masterVolume * musicVolume);
	}

	@Override
	public CarAudio getNewCarAudio(String carname) {

		int acc = loadSound("audio/sfx/car/motorAcc" + carname + ".ogg");
		int dcc = loadSound("audio/sfx/car/motorDcc" + carname + ".ogg");

		CarAudio car = new CarAudio(acc, dcc, this);

		car.setTurboBlowoff(turboBlowoffLow, turboBlowoffHigh);
		car.setTurbospool(new Source(turbospool));
		car.setStraightcut(new Source(straightcut));
		car.setRedline(new Source(redline));
		car.setTireboost(new Source(tireboost));
		car.setGrind(new Source(grind));
		car.setNos(new Source(nos));
		car.setNosDown(new Source(nosDown));
		car.setSoundbarrier(new Source(soundbarrier));
		car.setClutch(new Source(), clutchIn, clutchOut);
		car.setBackfire(new Source(backfire));

		int[] gears = new int[11];

		for (int i = 0; i < gears.length; i++) {
			gears[i] = loadSound("audio/sfx/car/gear" + i + ".ogg");
		}

		car.setGears(gears);
		
		cars.push(car);

		return car;
	}

	@Override
	public Source get(SfxTypes sfx) {
		return sfxs.get(sfx);
	}

	@Override
	public Source getUpgrade(int i) {
		return upgrades[i];
	}

	@Override
	public Source getTaunt(int i) {
		if (taunts.containsKey(i))
			return taunts.get(i);
		else
			return null;
	}

	/**
	 * Sjekker og spiller tilfeldig musikk om ledig og lyd er på 
	 */
	@Override
	public void checkMusic() {
		if (!music.isPlaying() && musicVolume > 0) {
			boolean reset = true;
			for (boolean songPlayed : musicPlayed) {
				if (!songPlayed) {
					reset = false;
					break;
				}
			}
			if (reset) {
				for (int i = 0; i < musicPlayed.length; i++) {
					musicPlayed[i] = false;
				}
			}
			
			int nextSong = 0;
			do {
				nextSong = Features.ran.nextInt(musicTitles.length);
			} while (musicPlayed[nextSong]);

			musicPlayed[nextSong] = true;
			this.music.play(loadSound("audio/music/" + musicTitles[nextSong]));
		}
	}
	
}
