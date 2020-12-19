precision mediump float;

in vec2 passTextureCoord;

out vec4 FragColor;

uniform float speedPercentage;
uniform sampler2D tex;

void main() {
	vec4 filteredColor = texture(tex, passTextureCoord).rgba;
	filteredColor.a = filteredColor.a * speedPercentage;

	FragColor = filteredColor;
}