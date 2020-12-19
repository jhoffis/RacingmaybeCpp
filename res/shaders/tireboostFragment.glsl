precision mediump float;
flat in lowp float passHitTB;
in vec2 passTextureCoord;

out vec4 FragColor;

uniform sampler2D tex;

void main() {

	vec4 filteredColor = texture(tex, passTextureCoord).rgba;

	if(passHitTB < 0.5) {
		float gray = dot(filteredColor.rgb, vec3(0.299, 0.587, 0.114));
		filteredColor = vec4(gray, gray, gray, 0.5);
	}

	FragColor = filteredColor;
}