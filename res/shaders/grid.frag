#version 330

in vec2 texturePos;
in vec3 lightVector;
in vec3 normalVector;

out vec4 outColor;
uniform sampler2D uTexture;
uniform sampler2D uTextureGlobe;

const vec3 AMBIENT_COLOR = vec3(0.3, 0.3, 1);
const vec3 DIFFUSE_COLOR = vec3(0.3, 0.3, 0.5);
const float INTENSITY = 2;

vec2 horizontallyFlip(vec2 pos) {
    return vec2(-1, 1) * (vec2(0, 1) - pos);
}

void main() {
    //outColor = vec4(1, 1, 1, 0);
    //outColor = texture2D(uTextureGlobe, horizontallyFlip(texturePos));
//    vec3 baseColor = texture2D(uTextureGlobe, horizontallyFlip(texturePos)).xyz;
    vec3 baseColor = vec3(1, 1, 0);

    vec3 ld = normalize(lightVector);
    vec3 nd = normalize(normalVector);
    float NDL = max(dot(nd, ld), 0);

    vec3 totalDiffuse = INTENSITY * NDL * DIFFUSE_COLOR;

    outColor = vec4((AMBIENT_COLOR + totalDiffuse) * baseColor, 0);
}