precision mediump float;

in vec2 passTextureCoord;

out vec4 FragColor;

uniform sampler2D tex;
uniform float nosAmountLevelPositionY;
uniform vec3 nosStrength;

void main() {
	vec4 filteredColor = texture(tex, passTextureCoord).rgba;
	float compVal = filteredColor.b;
	
	if(compVal > 0.70) {
		if(gl_FragCoord.y > nosAmountLevelPositionY) {
			
			float alpha = 1.0 - compVal;
			if(alpha < 0.0) {
				alpha = 0.0;
			}
		
			filteredColor.a = alpha;
		} else {
			vec3 nos = nosStrength;
			nos.r = nos.r * compVal;
			nos.g = nos.g * compVal;
			nos.b = nos.b * compVal;
			filteredColor.rgb = nos;
		}
	}

	FragColor = filteredColor;
}