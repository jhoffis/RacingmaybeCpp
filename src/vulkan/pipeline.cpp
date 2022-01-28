#include "pipeline.h"
#include <fstream>
#include <assert.h>
#include <iostream>
using namespace Graphics;

// kan godt hende at dette ma inn i en struct senere fordi man skal vel kunne ha mer enn en shader om gangen
// pluss jeg prover a unnga fields...
static EngineDevice* _device;
VkPipeline graphicsPipeline;
VkShaderModule vertShader;
VkShaderModule fragShader;


std::vector<char> readFile(std::string const& filename)
{
	std::ifstream file(filename, std::ios::ate | std::ios::binary);
	assert(file.is_open());

	size_t fileSize = static_cast<size_t>(file.tellg());
	std::vector<char> buffer(fileSize);

	file.seekg(0);
	file.read(buffer.data(), fileSize);

	file.close();

	return buffer;
}

void Graphics::createPipeline(
	EngineDevice& device,
	const std::string& vertFilePath,
	const std::string& fragFilePath,
	const PipelineConfig& configInfo
) {
	_device = &device;
	auto vert = readFile(vertFilePath);
	auto frag = readFile(fragFilePath);
}

void Graphics::createShaderModule(const std::vector<char>& code, VkShaderModule* shaderModule) {
	VkShaderModuleCreateInfo createInfo{};
	createInfo.sType = VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
	createInfo.codeSize = code.size();
	createInfo.pCode = reinterpret_cast<const uint32_t*>(code.data());

	if (vkCreateShaderModule(_device->device(), &createInfo, nullptr, shaderModule) != VK_SUCCESS) {
		throw std::runtime_error("Failed to create shader module");
	}
}

PipelineConfig Graphics::defaultPipelineConfig(uint32_t width, uint32_t height) {
	PipelineConfig configInfo{};

	return configInfo;
}