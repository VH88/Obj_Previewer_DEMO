#version 300 es
precision mediump float;

uniform sampler2D map;
in vec2 uv;
layout(location = 0)out vec3 color;
void main() {
    color = texture(map, uv).rgb;
}
