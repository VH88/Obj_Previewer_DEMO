#version 300 es

uniform mat4 uMVMatrix;
uniform mat4 uEnvMatrix;
uniform mat4 uMVPMatrix;
uniform mat4 uInvMVSkyMatrix;
uniform mat4 uMVSkyMatrix;

layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec2 vTexCoord;
layout(location = 2) in vec3 vNormal;
layout(location = 3) in vec3 vTangent;
layout(location = 4) in vec3 vBitangent;

out mat3 envMatrix;
out mat3 TBN;
//out mat3 invTBN;
out mat3 invMVSkyMatrix;
out vec3 eye_viewspace;
out vec3 eye_skyspace;
out vec3 normal_viewspace;
out vec2 texCoords;

void main()
{
    texCoords = vTexCoord;
    eye_skyspace = vec3(uMVSkyMatrix * vPosition);
    eye_viewspace = vec3(0.0) - vec3(uMVMatrix * vPosition);
    normal_viewspace = vec3(uMVMatrix* vec4(vNormal, 0.0));
    vec3 tangent_viewspace = vec3(uMVMatrix* vec4(vTangent, 0.0));
    vec3 bitangent_viewspace = vec3(uMVMatrix* vec4(vBitangent, 0.0));

    float fDotT = dot( tangent_viewspace, tangent_viewspace);
    float fDotB = dot(bitangent_viewspace, bitangent_viewspace);
    float fInvM = 1.0/ sqrt(max(fDotT, fDotB));

    envMatrix = mat3(uEnvMatrix);

    TBN = mat3(tangent_viewspace * fInvM, bitangent_viewspace * fInvM, normal_viewspace);

    //invTBN = inverse(TBN);
    invMVSkyMatrix = mat3(uInvMVSkyMatrix);

    gl_Position = uMVPMatrix * vPosition;
}
