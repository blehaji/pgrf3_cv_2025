#version 330

in vec2 inPosition;

out vec2 texturePos;
out vec3 lightVector;
out vec3 normalVector;

uniform mat4 uModelMat;
uniform mat4 uViewMat;
uniform mat4 uProjMat;
uniform int uFuncType;

const float PI = radians(180);
const vec3 LIGHT = vec3(1.0, 0.0, 1);

const int WAVE = 1;
const int SPHERE = 2;
const int CYLINDER = 3;
const int HOURGLASS = 4;

vec3 calcPosition(vec2 position) {
    position = position*2 - 1;
    vec3 pos = vec3(position, 0);

    float azimuth, zenith;
    switch(uFuncType) {
        case WAVE:
        pos.z = 0.5 * cos(sqrt(5 * pow(pos.x, 2)) - sqrt(5 * pow(pos.y, 2)));
        break;

        case SPHERE:
        float azimuth = position.x * PI;
        float zenith = position.y * 2*PI;
        float R = 1;

        //vec3 pos;
        pos.x = R*sin(zenith)*sin(azimuth);
        pos.y = R*sin(zenith)*cos(azimuth);
        pos.z = R*cos(zenith);
        break;

        case CYLINDER:
        zenith = position.y * 2*PI;

        pos.x = sin(zenith);
        pos.y = cos(zenith);
        pos.z = position.x;
        break;

        case HOURGLASS:
        zenith = pos.y * 2*PI;

        pos.z = pos.x;
        pos.x = sin(zenith)*pos.z;
        pos.y = cos(zenith)*pos.z;
        break;
    }

    return pos;
}

vec3 calcNormal(vec3 pos, vec2 inPos) {
    vec3 dx = calcPosition(inPos + vec2(0.1, 0)) - pos;
    vec3 dy = calcPosition(inPos + vec2(0, 0.1)) - pos;

    if (uFuncType == SPHERE) {
        return pos;
    }

    return cross(dx, dy);
}

void main() {
    vec3 pos = calcPosition(inPosition);
    mat4 mvMat = uViewMat * uModelMat;
    vec4 mvPos = mvMat * vec4(pos, 1);
    vec3 mvPos3 = mvPos.xyz;

    texturePos = inPosition;
    lightVector = vec3(uViewMat * vec4(LIGHT, 1)) - mvPos3;
    normalVector = transpose(inverse(mat3(mvMat))) * calcNormal(pos, inPosition);

    gl_Position = uProjMat * mvPos;
}