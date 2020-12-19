precision mediump float;
in vec2 passTextureCoord;

out vec4 FragColor;

uniform sampler2D tex;

void main() {
	FragColor = texture(tex, passTextureCoord).rgba;
	
}