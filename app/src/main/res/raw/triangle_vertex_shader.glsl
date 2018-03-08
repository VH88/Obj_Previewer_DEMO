#version 300 es

uniform mat4 uMVPMatrix;
layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec2 vTexCoords;

out vec2 uv;
void main()
{
    uv = vTexCoords;
    gl_Position = vPosition;
}