#version 330

in vec2 texturePos;
in vec3 lightVector;
in vec3 normalVector;
in vec3 fragPos;

out vec4 outColor;

uniform int uColorMode;
uniform vec3 uColor;
uniform sampler2D uTexture;
uniform bool uEnableLighting;

const vec3 AMBIENT_COLOR = vec3(0.4);
const vec3 DIFFUSE_COLOR = vec3(0.6);
const vec3 SPECULAR_COLOR = vec3(0.8);
const float INTENSITY = 1;
const float SHININESS = 30;
const float SPECULAR_POWER = 1;

const int COLOR_MODE_COLOR = 0;
const int COLOR_MODE_TEXTURE = 1;
const int COLOR_MODE_VIEW_NORMAL = 2;
const int COLOR_MODE_UV = 3;
const int COLOR_MODE_DEPTH = 4;
const int COLOR_MODE_FRAG_POS = 5;
const int COLOR_MODE_LIGHT_DIST = 6;

void main() {
    vec3 ld = normalize(lightVector);
    vec3 nd = normalize(normalVector);
    float NDL = max(dot(nd, ld), 0);
    float lightDist = length(lightVector);
    float attenuation = min(1, 1/pow(lightDist, 2));
    vec3 vd = normalize(-fragPos);
    vec3 hd = normalize(ld + vd);
    float HDN = max(dot(hd, nd), 0);
    vec3 baseColor;

    switch (uColorMode) {
        case COLOR_MODE_COLOR:
        baseColor = uColor;
        break;

        case COLOR_MODE_TEXTURE:
        baseColor = texture2D(uTexture, texturePos).xyz;
        break;

        case COLOR_MODE_VIEW_NORMAL:
        baseColor = nd;
        break;

        case COLOR_MODE_UV:
        baseColor = vec3(texturePos, 0);
        break;

        case COLOR_MODE_DEPTH:
        baseColor = vec3(1/(gl_FragCoord.z / gl_FragCoord.w));
        break;

        case COLOR_MODE_FRAG_POS:
        baseColor = fragPos;
        break;

        case COLOR_MODE_LIGHT_DIST:
        baseColor = vec3(1 / lightDist);
        break;
    }

    vec3 totalDiffuse = INTENSITY * NDL * DIFFUSE_COLOR;
    vec3 totalSpecular = SPECULAR_POWER * pow(HDN, 4 * SHININESS) * SPECULAR_COLOR;

    if (uEnableLighting) {
        outColor = vec4((AMBIENT_COLOR + attenuation*(totalDiffuse + totalSpecular)) * baseColor, 1);
    } else {
        outColor = vec4(baseColor, 1);
    }
}