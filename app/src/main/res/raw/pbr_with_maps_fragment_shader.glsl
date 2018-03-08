#version 300 es

uniform samplerCube uTextureIrradianceCubemap;
uniform samplerCube uPrefilterMap;
uniform sampler2D uBRDFLUT;

uniform int uLightCount;
const int MAX_LIGHT_COUNT = 2 * 3;
// vec3 positions + vec3 color
uniform vec4 uLightSources[ MAX_LIGHT_COUNT ];

uniform sampler2D uAlbedo;
uniform sampler2D uTextureNormal;
uniform sampler2D uTextureAO;
uniform sampler2D uFresnel;
uniform sampler2D uMetallic;
uniform sampler2D uRoughness;

uniform vec3 uModelPosInEyeSpace;

in mat3 envMatrix;
in mat3 TBN;
//in mat3 invTBN;
in mat3 invMVSkyMatrix;
in vec3 eye_viewspace;
in vec3 eye_skyspace;
in vec3 normal_viewspace;
in vec2 texCoords;

out vec4 o_fragColor;
layout(location = 0) out vec3 color;


vec3 toLinear(in vec3 v){    return pow(v, vec3(2.2));}
vec4 toLinear(in vec4 v){    return vec4(pow(v.rgb, vec3(2.2)), v.a); }
vec3 toGamma(vec3 v){    return pow(v, vec3(1.0/ 2.2)); }
vec4 toGamma(vec4 v){   return vec4(toGamma(v.rgb), v.a);}

vec3 tonemapReinhard( vec3 color){    return color / (color + vec3(1.0)); }


// Tonemaping Operator by John Hable
// http://filmicworlds.com/blog/filmic-tonemapping-operators/
vec3 tonemapFilmic(vec3 color)
{
    color = color * vec3(1.0);
    color = tonemapReinhard(color);
    vec3 x = vec3(max(0.0, color.r - 0.004), max(0.0, color.g - 0.004), max(0.0, color.b - 0.004));
    color = vec3(x*(6.2 * x + 0.5))/(x * (6.2 * x + 1.7) + 0.06);
    //color = pow(color, vec3(1.0 / 2.2));

    return color;
}

// Specular distribution. a - uRoughness, low value is mirror, high is rubber
float distributionGGX(vec3 N, vec3 H, float a)
{

    float a2 = a * a * a * a;
    float NdotH = max(dot(N,H), 0.0);
    float NdotH2= NdotH * NdotH;
    float nom = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = 3.1415926 * denom * denom;
    return nom / denom;
}

// k adopts uRoughness a.
//k direct = ((a + 1)^2) / 8
//k IBL = a^2 / 2
float geometrySchlickGGX(float NdotV, float k)
{
    //k = (k + 1.0) * (k + 1.0) / 8.0;
    k = k + 1.0;
    k = (k * k) / 8.0;
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

vec3 frenelSchlick(float cosTheta, vec3 F0, float roughness)
{
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(1.0 - cosTheta, 5.0);
}

vec2 envMapEquirect(vec3 wcNormal )
{
    float flipEnvMap = -1.0;
    float Pi = 3.1415926;
    float TwoPi = 6.2831853;
    float theta = acos(-wcNormal.y);
    float phi = atan( wcNormal.x, wcNormal.z) + Pi;

    return vec2(phi / TwoPi, theta / Pi);
}

vec3 specularLighting(in vec3 N, in vec3 L, in vec3 V)
{
    float specularTerm = 0.0;
    float uSpecularReflectance = 64.0;
    float uSpecularIntensity = 6.0f;

    if( dot( N,L) > 0.0)
    {
        vec3 H = normalize( V + L);
        specularTerm = pow(dot(N,H), uSpecularReflectance);
    }
    return vec3(1.0, 1.0, 1.0)  * specularTerm * uSpecularIntensity;
}

vec3 reflectedRay(in vec3 N, in vec3 E)
{
    vec3 reflected = reflect(E, N);
    //reflected = vec3( invMVMatrix * invTBN * reflected);
    reflected = vec3(invMVSkyMatrix  * reflected);
    return reflected;
}

const float PI = 3.14159265359;
const float MAX_REFLECTION_LOD = 4.0;
void main()
 {
    vec3 albedo = toLinear( texture(uAlbedo, texCoords).rgb);
    vec3 frenel = toLinear(texture(uFresnel, texCoords).rgb);
    float metallic = pow(texture(uMetallic, texCoords).r, 2.2);
    float roughness = pow(texture(uRoughness, texCoords).r, 2.2);
    //roughness = 0.1;
    float ao = pow(texture(uTextureAO, texCoords).r, 2.2);

    //vec3 Nref = normalize(normal_viewspace);
    //vec3 Vref = normalize(eye_viewspace);

    vec3 N = normalize(texture(uTextureNormal, texCoords).rgb * 2.0 - 1.0);
    vec3 V = normalize(TBN * eye_viewspace);

    vec3 R =  reflect(-V, N);
    R =  invMVSkyMatrix *  R;

    vec3 F0 = vec3(0.04);
    F0 = mix(F0, frenel , metallic);

    vec3 F =  frenelSchlick(max(dot(N,V), 0.0), F0, roughness);

    vec3 kS = F;
    vec3 kD = vec3(1.0) - kS;
    kD *= 1.0 - metallic;

    vec3 irradiance =  (texture(uTextureIrradianceCubemap,  N).rgb);
    vec3 diffuse = irradiance * (texture(uAlbedo, texCoords )).rgb ;

    vec3 prefilteredColor =  (textureLod(uPrefilterMap,  R, roughness * MAX_REFLECTION_LOD).rgb);
    vec2 envBRDF = texture(uBRDFLUT, vec2(max(dot(N,V), 0.0), roughness )).rg;
    vec3 specular = prefilteredColor * (F * envBRDF.x + envBRDF.y);

    vec3 ambient = (kD * diffuse + specular) * ao;

    vec3 Lo = vec3(0.0);
    // For each light in the scene
    // DOSN"T WORK WITH NORMAL MAP, DISABLE FOR NOW!!! uLightCount = 0
    for(int i = 0; i < uLightCount; ++i)
    {
        // Per light radiance
        vec3 L = normalize( TBN * vec3(uLightSources[i*2]) +  eye_viewspace);
        vec3 H = normalize(V + L);

        float distance = length(vec3(uLightSources[i*2]) - eye_skyspace);
        float attenuation = 1.0 / (distance * distance);
        vec3 radiance = vec3(uLightSources[i*2 + 1]) * attenuation;

        // Cook - Torrence brdf
        float NDF = distributionGGX(N, H, roughness);
        float G = geometrySmith( N, V, L, roughness);

        vec3 nominator = NDF * G * F;
        float denominator = 4.0 * max ( dot (N, V), 0.0) * max(dot(N, L), 0.0);
        vec3 specular = nominator / max(denominator, 0.001);

        // Final radiance of Lo
        float NdotL = max (dot(N, L), 0.0);
        Lo += (kD * albedo / PI  + specular) * radiance * NdotL;


    }

    vec3 color = ambient + Lo;



//---------------------------------------------------------





    o_fragColor = vec4(toGamma(color), 1.0);

    //o_fragColor = _color;


}
