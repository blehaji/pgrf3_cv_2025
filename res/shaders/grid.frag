#version 330

in vec2 texturePos;
in vec3 lightVector;
in vec3 normalVector;
in vec3 fragPos;
in vec4 shadowPos;

out vec4 outColor;

uniform int uColorMode;
uniform vec3 uColor;
uniform sampler2D uTexture;
uniform bool uEnableLighting;
uniform sampler2D uShadowMap;
uniform bool uEnableShadows;

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
    vec2 uv = vec2(1 - texturePos.x, texturePos.y);
    vec3 shadowTexPos = (shadowPos.xyz/shadowPos.w + 1)/2;
    bool inLightFrustum =
        shadowTexPos.x >= 0 && shadowTexPos.x <= 1
        && shadowTexPos.y >= 0 && shadowTexPos.y <= 1
        && shadowTexPos.z >= 0 && shadowTexPos.z <= 1;

    switch (uColorMode) {
        case COLOR_MODE_COLOR:
        baseColor = uColor;
        break;

        case COLOR_MODE_TEXTURE:
        baseColor = texture2D(uTexture, uv).xyz;
        break;

        case COLOR_MODE_VIEW_NORMAL:
        baseColor = nd;
        break;

        case COLOR_MODE_UV:
        baseColor = vec3(uv, 0);
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

    float shadow = 0;
    if (inLightFrustum && uEnableShadows) {
        vec2 texelSize = 1/textureSize(uShadowMap, 0);
        texelSize *= abs(NDL) * 0.75 + 0.25;

        float bias = 1e-5;
        if (abs(shadowPos.w - 1) > bias) {
            bias *= pow(10, log(lightDist));
            float angle = max(pow(-abs(NDL) + 1, 2), 0.15);
            bias += min(5e-3 / pow(18, log(NDL)), 5e-4);
        }

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                float depth = texture(uShadowMap, shadowTexPos.xy + texelSize*vec2(x, y)).z;
                shadow += depth < shadowTexPos.z - bias ? 1 : 0;
            }
        }
        shadow /= 25;
    }

    if (uEnableLighting) {
        outColor = vec4((AMBIENT_COLOR + (1 - shadow)*attenuation*(totalDiffuse + totalSpecular)) * baseColor, 1);
    } else {
        outColor = vec4(baseColor, 1);
    }
}