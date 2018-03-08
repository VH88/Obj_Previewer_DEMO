#version 300 es
precision highp float;

in vec2 texCoords;

out vec4 o_fragColor;
const float PI = 3.14159265359;
const uint SAMPLE_COUNT = 1024u;

// NOTE: to avoid visible edge on mipmaps glEnable(GL_TEXTURE_CUBE_MAP_SEAMPLESS)
// hopefullty this function would be added in future versions
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
// As described by Epic Games take the GGX NDF in the spherical sample vector
vec3 ImportanceSampleGGX(vec2 Xi, vec3 N, float roughness)
{   //Using squared roughness for better visual results as described on Disney's research
    float a = roughness*roughness;

    //a = a *a;

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
float geometrySchlickGGX(float NdotV, float k)
{
    //k = (k + 1.0) * (k + 1.0) / 8.0;
    //k = k + 1.0;
    k = (k * k) / 2.0;
    float nom = NdotV;
    float denom = NdotV * (1.0 - k) + k;
    return nom / denom;
}
float geometrySmith(vec3 N, vec3 V, vec3 L, float k)
{
    float NdotV = max(dot(N,V), 0.0);
    float NdotL = max(dot(N,L), 0.0);
    float ggx1 = geometrySchlickGGX(NdotV, k);
    float ggx2 = geometrySchlickGGX(NdotL, k);
    return ggx1 * ggx2;
}

vec2 IntegrateBRDF(float NdotV, float roughness)
{
    vec3 V;
    V.x = sqrt(1.0 - NdotV * NdotV);
    V.y = 0.0;
    V.z = NdotV;

    float A = 0.0;
    float B = 0.0;

    vec3 N = vec3(0.0, 0.0, 1.0);

    for(uint i = 0u; i < SAMPLE_COUNT; ++i)
    {
        vec2 Xi = HammersleyNoBitOps(i, SAMPLE_COUNT);
        vec3 H =  ImportanceSampleGGX(Xi, N, roughness);
        vec3 L = normalize(2.0 * dot(V,H) * H - V);

        float NdotL = max(L.z, 0.0);
        float NdotH = max(H.z, 0.0);
        float VdotH = max(dot(V,H), 0.0);

        if( NdotL > 0.0)
        {
            float G = geometrySmith(N, V, L, roughness);
            float G_Vis = (G * VdotH) / (NdotH * NdotV);
            float Fc = pow(1.0 - VdotH, 5.0);

            A+= (1.0 - Fc) * G_Vis;
            B+= Fc * G_Vis;
        }

    }
    A /= float(SAMPLE_COUNT);
    B /= float(SAMPLE_COUNT);
    return vec2(A, B);
}

void main()
{
    vec2 integratedBRDF = IntegrateBRDF(texCoords.x, texCoords.y);
    o_fragColor = vec4( integratedBRDF, 0.0, 1.0);
}