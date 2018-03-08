#version 300 es
precision mediump float;

uniform samplerCube uCubeTexture;
in vec3 texCoords;

out vec4 o_fragColor;


void main()
{

    o_fragColor = vec4(texture(uCubeTexture, texCoords).rgb, 1.0);
}