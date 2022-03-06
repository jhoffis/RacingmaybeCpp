#pragma once

#include "engine_device.h"

#include <string>
#include <vector>

namespace Graphics {

    struct PipelineConfig {
        VkViewport viewport;
        VkRect2D scissor;
        VkPipelineViewportStateCreateInfo viewportInfo;
        VkPipelineInputAssemblyStateCreateInfo inputAssemblyInfo;
        VkPipelineRasterizationStateCreateInfo rasterizationInfo;
        VkPipelineMultisampleStateCreateInfo multisampleInfo;
        VkPipelineColorBlendAttachmentState colorBlendAttachment;
        VkPipelineColorBlendStateCreateInfo colorBlendInfo;
        VkPipelineDepthStencilStateCreateInfo depthStencilInfo;
        VkPipelineLayout pipelineLayout = VK_NULL_HANDLE;
        VkRenderPass renderPass = VK_NULL_HANDLE;
        uint32_t subpass = 0;
    };

	PipelineConfig defaultPipelineConfig(uint32_t width, uint32_t height);

	void createPipeline(
		EngineDevice& device,
		const std::string& vertFilePath, 
		const std::string& fragFilePath,
		const PipelineConfig& configInfo
	);

    void destroyPipeline();

	void createShaderModule(const std::vector<char>& code, VkShaderModule* shaderModule);

	// legg til en delete metode elns?
}