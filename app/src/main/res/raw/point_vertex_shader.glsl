#version 300 es

uniform mat4 uMVPMatrix;
layout(location = 0) in vec4 vPosition;
void main() {
    gl_Position = uMVPMatrix * vPosition;
    gl_PointSize = 10.0;
}
