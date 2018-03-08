#version 300 es
precision highp float;

uniform samplerCube uCubeTexture;
in vec3 texCoords;
uniform float roughness;
uniform float resolution; // resolution of the source cubemap

out vec4 o_fragColor;
const float PI = 3.14159265359;
const uint SAMPLE_COUNT = 4096u;

// NOTE: to avoid visible edge on mipmaps glEnable(GL_TEXTURE_CUBE_MAP_SEAMPLESS)
// hopefullty this function would be added in future versions

float distributionGGX(float NdotH, float a)
{

    float a2 = a * a * a * a;
    float NdotH2= NdotH * NdotH;
    float nom = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = 3.1415926 * denom * denom;
    return nom / denom;
}
// As described by Epic Games take the GGX NDF in the spherical sample vector
vec3 ImportanceSampleGGX(vec2 Xi, vec3 N, float roughness)
{   //Using squared roughness for better visual results as described on Disney's research
    float a = roughness*roughness;

    float phi = 2.0 * PI * Xi.x;
    float cosTheta = sqrt((1.0 - Xi.y) / (1.0 + (a*a - 1.0) * Xi.y));
    float sinTheta = sqrt(1.0 - cosTheta*cosTheta);

    // from spherical coordinates to cartesian coordinates
    vec3 H;
    H.x = cos(phi) * sinTheta;
    H.y = sin(phi) * sinTheta;
    H.z = cosTheta;

    // from tangent-space vector to world-space sample vector
    vec3 up        = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
    vec3 tangent   = normalize(cross(up, N));
    vec3 bitangent = cross(N, tangent);

    vec3 sampleVec = tangent * H.x + bitangent * H.y + N * H.z;
    return normalize(sampleVec);
}

float RadicalInverse_VdC(uint bits)
{
    bits = (bits << 16u) | (bits >> 16u);
    bits = ((bits & 0x55555555u) << 1u) | ((bits & 0xAAAAAAAAu) >> 1u);
    bits = ((bits & 0x33333333u) << 2u) | ((bits & 0xCCCCCCCCu) >> 2u);
    bits = ((bits & 0x0F0F0F0Fu) << 4u) | ((bits & 0xF0F0F0F0u) >> 4u);
    bits = ((bits & 0x00FF00FFu) << 8u) | ((bits & 0xFF00FF00u) >> 8u);
    return float(bits) * 2.3283064365386963e-10; // / 0x100000000
}
// Hemmersley described by Holger Dammertz. Based on Van Der Corpus sequence
// This function gives us the low-discrepancy sample i of the total sample set of size N.
vec2 Hammersley(uint i, uint N)
{
    return vec2(float(i)/float(N), RadicalInverse_VdC(i));
}

// NOTE: Not all OpenGL support bit operations (WebGL and OpenGL 2 es)
// for those use alternative below
float VanDerCorpus(uint n, uint base)
{
        float invBase = 1.0 / float(base);
        float denom   = 1.0;
        float result  = 0.0;
        // Older hardware has loop restrictions
        for(uint i = 0u; i < 32u; ++i)
        {
            if(n > 0u)
            {
                denom   = mod(float(n), 2.0);
                result += denom * invBase;
                invBase = invBase / 2.0;
                n       = uint(float(n) / 2.0);
            }
        }

        return result;
}
vec2 HammersleyNoBitOps(uint i, uint N)
{
    return vec2(float(i)/float(N), VanDerCorpus(i, 2u));
}
void main()
{
    // Pre-compute the specular portion of the indirect reflectance equasion
    // using importance sampling given a random low-discrepancy sequence
    // based on Quasi-Monte-Carlo method
    vec3 N = normalize(texCoords);
    vec3 R = N;
    vec3 V = R;

    float totalWeight = 0.0;
    vec3 prefilteredColor = vec3(0.0);

 for (uint i = 0u; i < SAMPLE_COUNT; ++i)
    {
        vec2 Xi = HammersleyNoBitOps(i, SAMPLE_COUNT);
        vec3 H =  ImportanceSampleGGX(Xi, N, roughness);
        float HdotV = max(dot(V,H), 0.0);
        float NdotH = max(dot(N,H), 0.0);
        vec3 L = normalize(2.0 * HdotV * H - V);

        //------------------------------------------------------------------------//
        // To reduce artifacts due to undersampling sample texel from a source mipmap
        // Method described by Chetan Jags
        float D = distributionGGX(NdotH, roughness);
        float pdf = (D * NdotH / (4.0 * HdotV)) + 0.0001;
        float saTexel = 4.0 * PI / (6.0 * resolution * resolution);
        float saSample = 1.0 / (float(SAMPLE_COUNT) * pdf + 0.0001);
        float mipLevel = roughness == 0.0 ? 0.0 : 0.5 * log2(saSample / saTexel);
        //-----------------------------------------------------------------------//

        float NdotL = max(dot(N,L),0.0);
        if(NdotL > 0.0)
        {
            prefilteredColor += texture(uCubeTexture, L, mipLevel).rgb * NdotL;
            totalWeight += NdotL;
        }
    }
    prefilteredColor = prefilteredColor / totalWeight;

    o_fragColor = vec4(prefilteredColor, 1.0);
}