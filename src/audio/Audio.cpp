#include "Audio.h"
#include "AudioMaster.h"
#include "AL/al.h"
#include "AL/alc.h"
#include <cstddef>
#include <iostream>

void createAudio() {
	// Setup
	auto device = alcOpenDevice(NULL);
	ALCcontext* ctx;
	if (device) {
		ctx = alcCreateContext(device, NULL);
		alcMakeContextCurrent(ctx);
	}

	// Check for EAX 2.0 support
	auto g_bEAX = alIsExtensionPresent("EAX2.0");
	// Generate Buffers
	alGetError(); // clear error code
	ALuint buffers[1];
	alGenBuffers(1, buffers);
	ALenum error;
	if ((error = alGetError()) != AL_NO_ERROR)
	{
		std::cout << "ERROR alGenBuffers :" << error << std::endl;
		return;
	}

	/*
	ALvoid* data{};
	ALsizei size{};
	ALsizei freq{};

	alBufferData(*buffers,
		AL_FORMAT_MONO16,
		data,
		size,
		freq
	);

	free(data);
	*/


}