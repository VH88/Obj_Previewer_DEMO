#version 300 es
uniform vec3 uLightPos;

uniform sampler2D uTextureDiffuse;
uniform sampler2D uTextureNormal;
uniform sampler2D uTextureCubemap;

in mat3 TBN;
in mat3 invTBN;
in mat3 invVMatrix;
in vec3 eye_viewspace;
in vec3 normal_viewspace;
in vec2 texCoords;

out vec4 o_fragColor;


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

// Specular distribution. a - roughness, low value is mirror, high is rubber
float distributionGGX(vec3 N, vec3 H, float a)
{
    float a2 = a * a;
    float NdotH = max(dot(N,H), 0.0);
    float NdotH2= NdotH * NdotH;
    float nom = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = 3.1415926 * denom * denom;
    return nom / denom;
}

// k adopts roughness a.
//k direct = ((a + 1)^2) / 8
//k IBL = a^2 / 2
float geometrySchlickGGX(float NdotV, float k)
{
    k = (k + 1.0) * (k + 1.0) / 8.0;
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

vec3 frenelSchlick(float cosTheta, vec3 F0)
{
    return F0 + (vec3(1.0) - F0) * pow(1.0 - cosTheta, 5.0);
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
    reflected = vec3(invVMatrix  * reflected);
    return reflected;
}
void main()
 {
    vec4 materialDiffuseColor = toLinear( texture(uTextureDiffuse, texCoords));
    vec4 materialAmbientColor = vec4(0.1,0.1,0.1, 1.0) * materialDiffuseColor;

    float distance = length(uLightPos + eye_viewspace);

    vec3 textureNormal_tangentspace = normalize(texture(uTextureNormal, texCoords).rgb * 2.0 - 1.0);
    vec3 light_tangentspace = vec3(TBN * uLightPos);
    vec3 eye_tangentspace = vec3(TBN * eye_viewspace);
    vec3 lightDir = normalize(light_tangentspace  + eye_tangentspace);

    float lightPower = 2.0;
    float cosTheta = max(dot(textureNormal_tangentspace, lightDir), 0.1);
    vec4 lightColor = toLinear(vec4(1.0, 1.0, 1.0, 1.0));

    vec4 Iamb = materialAmbientColor;
    vec4 Idif = lightColor * lightPower * cosTheta / (1.0 + (0.25 * distance * distance));
    vec4 Ispe = vec4(specularLighting(textureNormal_tangentspace, lightDir, normalize(eye_tangentspace)), 1.0) / (1.0 + (0.25 * distance * distance));

    vec4 color = materialDiffuseColor * (Iamb + Idif + Ispe);
//---------------------------------------------------------
    //vec3 E =  normalize(eye_tangentspace);
    vec3 E =  normalize(eye_viewspace);
    //vec3 reflected = reflectedRay( textureNormal_tangentspace, E);
    vec3 reflected = reflectedRay( normal_viewspace, E);

    //reflected = invTBN * reflected;
    reflected = reflected;
    vec2 ref = envMapEquirect(reflected);
    vec4 _color = (texture(uTextureCubemap,  ref));

    _color.rgb = tonemapFilmic(_color.rgb);

    //_color = vec4(1.0, 1.0, 1.0, 1.0) * distributionGGX(normal_viewspace, normalize( eye_viewspace + uLightPos), 0.6);
    //_color = vec4(1.0, 1.0, 1.0, 1.0) * geometrySmith(normal_viewspace, normalize(eye_viewspace), normalize( eye_viewspace + uLightPos), 1.0);

    vec3 F0 = vec3(0.04);
    F0 = mix(F0, vec3(0.21, 0.21, 0.21), 0.2);

    _color.rgb = frenelSchlick(max(dot(normal_viewspace, normalize( eye_viewspace )), 0.0), F0);


    o_fragColor = (_color);

    //o_fragColor = _color;


}
