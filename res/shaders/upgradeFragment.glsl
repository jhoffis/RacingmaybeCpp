precision mediump float;
in vec2 passTextureCoord;

out vec4 FragColor;

uniform sampler2D tex;
uniform float mouseTypeScheme;
uniform vec3 improvedLVL;

void main() {
	vec4 filteredColor = texture(tex, passTextureCoord).rgba;
	
	if (filteredColor.r == 0.0 && filteredColor.g == 0.0 && filteredColor.b == 0.0) {
		filteredColor.r = improvedLVL.r;
		filteredColor.g = improvedLVL.g;
		filteredColor.b = improvedLVL.b;
	}
	
	if(mouseTypeScheme == 1.0) { // down
		filteredColor.r = filteredColor.r * 0.8;
		filteredColor.g = filteredColor.g * 0.8;
		filteredColor.b = filteredColor.b * 0.8;
	} else if (mouseTypeScheme == 2.0) { // above
		filteredColor.r = filteredColor.r * 1.2;
		filteredColor.g = filteredColor.g * 1.2;
		filteredColor.b = filteredColor.b * 1.2;	
	} // else normal
	
	FragColor = filteredColor;
}