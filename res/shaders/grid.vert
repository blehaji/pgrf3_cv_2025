#version 330

in vec2 inPosition;

out vec2 texturePos;
out vec3 lightVector;
out vec3 normalVector;
out vec3 fragPos;
out vec4 shadowPos;

uniform mat4 uModelMat;
uniform mat4 uViewMat;
uniform mat4 uProjMat;
uniform int uFuncType;
uniform float uTime;
uniform vec3 uLightPosition;
uniform mat4 uLightVPMat;

const float PI = radians(180);
const vec3 LIGHT = vec3(1.5, 0.0, 1);

const int GRID = 0;
const int WAVE = 1;
const int SPHERE = 2;
const int CYLINDER = 3;
const int HOURGLASS = 4;
const int SPHERICAL_HOURGLASS = 5;
const int TENT = 6;

vec3 calcPosition(vec2 position) {
    vec3 pos = vec3(position, 0);

    float azimuth, zenith;
    switch(uFuncType) {
        case GRID:
        pos.x = pos.x*2 - 1;
        pos.y = pos.y*2 - 1;
        break;

        case WAVE:
        pos.x = pos.x*2 - 1;
        pos.y = pos.y*2 - 1;
        pos.z = 0.2 * cos(3*(pos.x + (uTime/10000))) * sin(5*(pos.y + (uTime/5000)));
        break;

        case SPHERE:
        azimuth = pos.x * 2*PI;
        zenith = pos.y * PI;

        pos.x = sin(zenith)*sin(azimuth);
        pos.y = sin(zenith)*cos(azimuth);
        pos.z = cos(zenith);
        break;

        case CYLINDER:
        azimuth = pos.x * 2*PI;

        pos.z = (1 - pos.y)*2 - 1;
        pos.x = sin(azimuth);
        pos.y = cos(azimuth);
        break;

        case HOURGLASS:
        azimuth = pos.x * 2*PI;

        pos.z = (1 - pos.y)*2 - 1;
        pos.x = sin(azimuth)*pos.z;
        pos.y = cos(azimuth)*pos.z;
        break;

        case SPHERICAL_HOURGLASS:
        azimuth = pos.x * 2*PI;
        zenith = pos.y * PI;

        pos.x = sin(zenith)*sin(azimuth)*cos(zenith);
        pos.y = sin(zenith)*cos(azimuth)*cos(zenith);
        pos.z = cos(zenith);
        break;

        case TENT:
        pos.x = pos.x*2 - 1;
        pos.y = pos.y*2 - 1;
        pos.z = (1 - abs(pos.x)) * (1 - abs(pos.y));
        break;
    }

    return pos;
}

vec3 calcNormal(vec3 pos, vec2 inPos) {
    vec3 dx = calcPosition(inPos + vec2(0.1, 0)) - pos;
    vec3 dy = calcPosition(inPos + vec2(0, 0.1)) - pos;

    return cross(dx, dy);
}

void main() {
    texturePos = inPosition;

    vec3 pos = calcPosition(inPosition);
    mat4 mvMat = uViewMat * uModelMat;
    vec4 mvPos = mvMat * vec4(pos, 1);
    vec3 mvPos3 = mvPos.xyz/mvPos.w;

    fragPos = mvPos3;
    lightVector = vec3(uViewMat * vec4(uLightPosition, 1)) - mvPos3;
    normalVector = transpose(inverse(mat3(mvMat))) * calcNormal(pos, inPosition);
    shadowPos = uLightVPMat * uModelMat * vec4(pos, 1);

    gl_Position = uProjMat * mvPos;
}