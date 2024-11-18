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

vec3 calcPosition(vec2 position) {
    position = position*2 - 1;
    vec3 pos = vec3(position, 0);
    switch(uFuncType) {
        case 1:
        pos.z = 0.5 * cos(sqrt(5 * pow(pos.x, 2)) - sqrt(5 * pow(pos.y, 2)));
        break;

        case 2:
        float azimuth = position.x * PI;
        float zenith = position.y * 2*PI;
        float R = 1;

        //vec3 pos;
        pos.x = R*sin(zenith)*cos(azimuth);
        pos.y = R*sin(zenith)*sin(azimuth);
        pos.z = R*cos(zenith);
    }

    return pos;
}

vec3 calcNormal(vec3 pos) {
    vec3 dx = vec3(pos.x + 0.01, pos.y, pos.z) - pos;
    vec3 dy = vec3(pos.x, pos.y + 0.01, pos.z) - pos;

    return cross(dx, dy);
}

void main() {
    vec3 pos = calcPosition(inPosition);
    mat4 mvMat = uViewMat * uModelMat;

    texturePos = inPosition;
    lightVector = LIGHT - pos;
    normalVector = calcNormal(pos);

    gl_Position = uProjMat * mvMat * vec4(pos, 1);
}