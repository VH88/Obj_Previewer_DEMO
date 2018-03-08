// Formula for maping equirent environment image from an NVIDIA Developer Zone
// GPU Gems. Chapter 17 Ambient Occlusion
// 17.4.1 Environment Lighting
// http://developer.download.nvidia.com/books/HTML/gpugems/gpugems_ch17.html

#version 300 es
precision highp float;

uniform sampler2D uCubeTexture;
in vec3 texCoords;

out vec4 o_fragColor;

const vec2 invTwoPiandPi = vec2(0.1592, 0.3183);

vec3 toLinear(in vec3 v){    return pow(v, vec3(2.2));}
vec4 toLinear(in vec4 v){    return vec4(pow(v.rgb, vec3(2.2)), v.a); }
vec3 toGamma(vec3 v){    return pow(v, vec3(1.0/ 2.2)); }
vec4 toGamma(vec4 v){   return vec4(toGamma(v.rgb), v.a);}

vec3 tonemapReinhard( vec3 color){    return color / (color + vec3(1.0)); }

vec3 tonemapFilmic(vec3 color)
{
    color = color * vec3(1.0);
    color = tonemapReinhard(color);
    vec3 x = vec3(max(0.0, color.r - 0.004), max(0.0, color.g - 0.004), max(0.0, color.b - 0.004));
    color = vec3(x*(6.2 * x + 0.5))/(x * (6.2 * x + 1.7) + 0.06);
    //color = pow(color, vec3(1.0 / 2.2));

    return color;
}

vec2 envMapEquirect(vec3 wcNormal )
{

    float Pi = 3.1415926;
    float TwoPi = 6.2831853;

    float theta = acos(wcNormal.y);
    float phi = atan( wcNormal.x, wcNormal.z) + Pi;

     return vec2(phi, theta) * invTwoPiandPi;
}

void main()
{
    vec4 color = vec4(texture(uCubeTexture, envMapEquirect(normalize( texCoords))).rgb, 1.0);
    color.rgb = tonemapFilmic(color.rgb);

    o_fragColor = ( color);
}