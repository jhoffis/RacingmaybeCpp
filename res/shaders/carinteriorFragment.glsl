precision mediump float;
in vec2 passTextureCoord;

out vec4 FragColor;

uniform sampler2D tex;
uniform sampler2D lightingTexture;
uniform float lightDistanceNext;
uniform float lightDistancePrev;

void main() {
	vec4 lightingColor = texture(lightingTexture, passTextureCoord);
	vec4 filteredColor = texture(tex, passTextureCoord).rgba;
	
	// x er framme så 0.0 er bak så 1.0 - beregn en gang i cpu ikke 40000 ganger her.
	float diff = 0.3;
	float strength = 1.2;
	float yellowfy = 0.85;
	float inc = 1.0;
	bool lit = false; 

	// tegn bare fra -0.25 til 1.5 liksom.
	
	if (lightingColor.b != 0.0) { 
	
		float ldNext = lightDistanceNext;
		float fade = 1.0;

		// from 0.0 to 0.5 you fade in 0.5 to 1.0 fade out the other way.
		if (ldNext > 0.5)  {
			ldNext = 1.0 - (ldNext * (2.0 * ldNext));
		}
		
		if (ldNext >= lightingColor.b - diff) {
		
			float spaceBetween = ldNext - lightingColor.b;
			if (spaceBetween < 0.0) {
				fade = 1.0 - abs(spaceBetween) / diff;
			}
			
			inc = 1.0 + (strength * lightingColor.r * fade);
			lit = true;
		}
	} 
	
	if (lightingColor.g != 0.0 && lightDistanceNext > lightingColor.g - diff && lightDistanceNext < lightingColor.g + diff) {
		float fade = 1.0 - abs(lightDistanceNext - lightingColor.g) / diff;
		inc = 1.0 + (strength * lightingColor.r * fade);
		lit = true;
	}
	
	if (lit) {
		filteredColor.r = filteredColor.r * inc;
		filteredColor.g = filteredColor.g * inc;
		filteredColor.b = filteredColor.b * (inc * yellowfy);
	}

	FragColor = filteredColor;
	//FragColor = vec4(vec3(gl_FragCoord.z), 1.0);
}