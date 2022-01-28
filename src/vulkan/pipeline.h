#pragma once

#include "engine_device.h"

#include <string>
#include <vector>

namespace Graphics {

	struct PipelineConfig {};

	PipelineConfig defaultPipelineConfig(uint32_t width, uint32_t height);

	void createPipeline(
		EngineDevice& device,
		const std::string& vertFilePath, 
		const std::string& fragFilePath,
		const PipelineConfig& configInfo
	);

	void createShaderModule(const std::vector<char>& code, VkShaderModule* shaderModule);

	// legg til en delete metode elns?
}