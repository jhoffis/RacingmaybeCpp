precision mediump float;
flat in lowp float passGreenlight;
in vec2 passTextureCoord;

out vec4 FragColor;

uniform sampler2D tex;

void main() {

	vec4 filteredColor = texture(tex, passTextureCoord).rgba;

	vec2 nothin = vec2(0.0, 0.0);

	if(passGreenlight > 0.5) {
		filteredColor.rb = nothin;
	} else {
		filteredColor.gb = nothin;
	}
	
	FragColor = filteredColor;
}