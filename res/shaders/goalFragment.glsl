precision mediump float;
in vec2 passTextureCoord;

out vec4 FragColor;

uniform sampler2D tex;
uniform vec4 tint;

void main() {

	vec4 filteredColor = texture(tex, passTextureCoord).rgba;

	if (!(tint.r == 0.0 && tint.g == 0.0 && tint.b == 0.0 && tint.a == 0.0)) {
		filteredColor.rgb = mix(filteredColor.rgb, tint.rgb, tint.a);
	}

	if (filteredColor.a < 1.0)
		filteredColor.a = 0.0; 

	FragColor = filteredColor;
	
	//FragColor = vec4(vec3(gl_FragCoord.z), 1.0);
}