precision mediump float;
in vec2 passTextureCoord;

out vec4 FragColor;

uniform sampler2D tex;

void main() {
	FragColor = texture(tex, passTextureCoord).rgba;
	
	//FragColor = vec4(vec3(gl_FragCoord.z), 1.0);
}