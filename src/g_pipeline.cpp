#include "g_pipeline.h"
#include <fstream>
#include <assert.h>
#include <iostream>

std::string readFile(std::string const& filename)
{
	using BufIt = std::istreambuf_iterator<char>;
	std::ifstream in(filename);
	assert(in.is_open());
	return std::string(BufIt(in.rdbuf()), BufIt());
}

void Graphics::createPipeline(const std::string& vertFilePath, const std::string& fragFilePath) {
	auto vert = readFile(vertFilePath);
	auto frag = readFile(fragFilePath);
}
