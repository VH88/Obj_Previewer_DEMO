#version 300 es

uniform mat4 uMVPMatrix;
layout(location = 0) in vec4 vPosition;

out vec3 texCoords;
void main()
{
    texCoords = vec3(vPosition);
    gl_Position = uMVPMatrix * vPosition;
}