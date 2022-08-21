#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec3 normals;

//uniforms
uniform mat4 model_matrix;
uniform mat4 view;
uniform mat4 projection;
uniform vec2 tcMultiplier;

uniform vec3 pointLightPos;
uniform vec3 point2LightPos;
uniform vec3 point3LightPos;
uniform vec3 point4LightPos;
uniform vec3 point5LightPos;

uniform vec3 spotLightPos;
uniform vec3 spot1LightPos;
uniform vec3 spot2LightPos;
uniform vec3 spot3LightPos;
uniform vec3 spot4LightPos;
uniform vec3 spot5LightPos;


//toon elemente
uniform vec3 lightDir;


out struct VertexData
{
    vec3 toCamera;
    vec3 toPointLight;
    vec3 toPoint2Light;
    vec3 toPoint3Light;
    vec3 toPoint4Light;
    vec3 toPoint5Light;

    vec3 toSpotLight;
    vec3 toSpot1Light;
    vec3 toSpot2Light;
    vec3 toSpot3Light;
    vec3 toSpot4Light;
    vec3 toSpot5Light;
    vec2 tc;
    vec3 normale;
} vertexData;

//
void main(){

    mat4 modelview =  view*model_matrix;
    vec4 modelViewPosition = modelview * vec4(position, 1.0f);
    vec4 pos = projection * modelViewPosition;
    vec4 norm = transpose(inverse(modelview)) * vec4(normals, 0.0f);

    //POINT
    vec4 lp = view * vec4(pointLightPos, 1.0);
    vertexData.toPointLight = (lp - modelViewPosition).xyz;

    vec4 lp3 = view * vec4(point2LightPos, 1.0);
    vertexData.toPoint2Light = (lp3 - modelViewPosition).xyz;

    vec4 lp4 = view * vec4(point3LightPos, 1.0);
    vertexData.toPoint3Light = (lp4 - modelViewPosition).xyz;

    vec4 lp5 = view * vec4(point4LightPos, 1.0);
    vertexData.toPoint4Light = (lp5 - modelViewPosition).xyz;

    vec4 lp6 = view * vec4(point5LightPos, 1.0);
    vertexData.toPoint5Light = (lp6 - modelViewPosition).xyz;


    //SPOT
    vec4 lp2 = view * vec4(spotLightPos, 1.0);
    vertexData.toSpotLight = (lp2 - modelViewPosition).xyz;

    vec4 lp21 = view * vec4(spot1LightPos, 1.0);
    vertexData.toSpot1Light = (lp21 - modelViewPosition).xyz;

    vec4 lp22 = view * vec4(spot2LightPos, 1.0);
    vertexData.toSpot2Light = (lp22 - modelViewPosition).xyz;

    vec4 lp23 = view * vec4(spot3LightPos, 1.0);
    vertexData.toSpot3Light = (lp23 - modelViewPosition).xyz;

    vec4 lp24 = view * vec4(spot4LightPos, 1.0);
    vertexData.toSpot4Light = (lp24 - modelViewPosition).xyz;

    vec4 lp25 = view * vec4(spot5LightPos, 1.0);
    vertexData.toSpot5Light = (lp25 - modelViewPosition).xyz;

    vertexData.toCamera = -modelViewPosition.xyz;
    gl_Position = pos;
    vertexData.normale = norm.xyz;
    vertexData.tc =  texCoord * tcMultiplier;
}