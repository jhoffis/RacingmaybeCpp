in vec3 position;
in vec2 textureCoord;

flat out lowp float passGreenlight;
out vec2 passTextureCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform bool green;

void main() {
	gl_Position = projection * view * model * vec4(position, 1.0);
	passTextureCoord = textureCoord;
	if(green == true) {
		passGreenlight = 1.0;
	} else {
		passGreenlight = 0.0;
	}
}