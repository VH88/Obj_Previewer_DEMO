#version 300 es

layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec2 vTexCoords;

out vec2 texCoords;
void main()
{
    texCoords = vTexCoords ;
    gl_Position = vPosition;
}