#version 300 es
precision highp float;

uniform samplerCube uCubeTexture;
in vec3 texCoords;

out vec4 o_fragColor;

const float PI = 3.14159265359;
void main()
{
    vec3 normal = normalize(texCoords);
    vec3 irradiance = vec3(0.0);

    // Calculate irradiance
    vec3 up = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(up, normal);
    up = cross(normal, right);

    float sampleDelta = 0.025;
    float nrSample = 0.0;

    for(float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta)
    {
        for(float theta = 0.0; theta < 0.5 * PI; theta += sampleDelta)
        {
            // spherical to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi), sin(theta) * sin(phi), cos(theta));
            // tangent space to world
            vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * normal;
            // Scale the sampled color value by cos(theta) due to light being weaker at larger angles
            // and by sin(theta) to account for smaller sample areas in the higher hemisphere areas
            irradiance += texture(uCubeTexture, sampleVec).rgb * cos(theta) * sin(theta);
            nrSample++;
        }
    }
    irradiance = PI * irradiance * (1.0 / float(nrSample));

    o_fragColor = vec4(irradiance, 1.0);
}