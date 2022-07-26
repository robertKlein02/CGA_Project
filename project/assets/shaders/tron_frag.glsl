#version 330 core

//input from vertex shader
in struct VertexData
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

//fragment shader output
out vec4 color;

//Texture Uniforms
uniform sampler2D diff;
uniform sampler2D emit;
uniform sampler2D specular;
uniform float shininess;

//Light Uniforms
uniform vec3 pointLightColor;
uniform vec3 pointLightAttParam;

uniform vec3 point2LightColor;
uniform vec3 point2LightAttParam;

uniform vec3 point3LightColor;
uniform vec3 point3LightAttParam;

uniform vec3 point4LightColor;
uniform vec3 point4LightAttParam;

uniform vec3 point5LightColor;
uniform vec3 point5LightAttParam;

uniform vec3 spotLightColor;
uniform vec2 spotLightAngle;
uniform vec3 spotLightAttParam;
uniform vec3 spotLightDir;

uniform vec3 spot1LightColor;
uniform vec2 spot1LightAngle;
uniform vec3 spot1LightAttParam;
uniform vec3 spot1LightDir;

uniform vec3 spot2LightColor;
uniform vec2 spot2LightAngle;
uniform vec3 spot2LightAttParam;
uniform vec3 spot2LightDir;

uniform vec3 spot3LightColor;
uniform vec2 spot3LightAngle;
uniform vec3 spot3LightAttParam;
uniform vec3 spot3LightDir;

uniform vec3 spot4LightColor;
uniform vec2 spot4LightAngle;
uniform vec3 spot4LightAttParam;
uniform vec3 spot4LightDir;

uniform vec3 spot5LightColor;
uniform vec2 spot5LightAngle;
uniform vec3 spot5LightAttParam;
uniform vec3 spot5LightDir;


uniform vec3 farbe;

float gamma = 2.2;
//float gamma = 1;

vec3 shade(vec3 n, vec3 l, vec3 v, vec3 dif, vec3 spec, float shine) {

    vec3 h = normalize(l+v);

    vec3 diffuse = dif * max(0.0, dot(n, l));
    vec3 reflectDir = reflect(-l, n);

    float cosb = max(0.0, dot(v, reflectDir));

    float specs = pow(max(dot(n, h), 0.0), shine*4);

    vec3 speculr =  spec * specs;

    return diffuse + speculr;
}

float attenuate(float len, vec3 attParam) {
   // return 1.0 / (attParam.x + attParam.y * len + attParam.z * len * len);
    return 1.0/ pow(len,2);
}

vec3 pointLightIntensity(vec3 lightColor, float len, vec3 attParam) {
    return lightColor * attenuate(len, attParam);}




vec3 spotLightIntensity(vec3 spotLightColour, float len, vec3 sp, vec3 spDir, vec3 attParam) {
    float cosTheta = dot(sp, normalize(spDir));
    float cosPhi = cos(spotLightAngle.x);
    float cosGamma = cos(spotLightAngle.y);

    float intensity = clamp((cosTheta - cosGamma)/(cosPhi - cosGamma), 0, 1.0);

    return spotLightColour * intensity * attenuate(len, attParam);
}

vec3 spot2LightIntensity(vec3 spot2LightColour, float len, vec3 sp, vec3 spDir, vec3 attParam) {
    float cosTheta = dot(sp, normalize(spDir));
    float cosPhi = cos(spot2LightAngle.x);
    float cosGamma = cos(spot2LightAngle.y);

    float intensity = clamp((cosTheta - cosGamma)/(cosPhi - cosGamma), 0, 1.0);

    return spot2LightColour * intensity * attenuate(len, attParam);
}

// Gamma performs gamma mapping
vec3 Gamma(vec3 colork) {
    return pow(colork, vec3(1.0 / gamma));
}


// InvGamma performs a conversion from sRGB into linear space
vec3 InvGamma(vec3 color) {
    return pow(color, vec3(gamma));
}

void main() {




    //Ambient
   // float ambientStrength = 0.01;
    //vec3 ambient = ambientStrength * spotLightColor;

    vec3 n = normalize(vertexData.normale);
    vec3 v = normalize(vertexData.toCamera);



    float lpLength = length(vertexData.toPointLight);
    vec3 lp = vertexData.toPointLight/lpLength;

    float lp3Length = length(vertexData.toPoint2Light);
    vec3 lp3 = vertexData.toPoint2Light/lp3Length;

    float lp4Length = length(vertexData.toPoint3Light);
    vec3 lp4 = vertexData.toPoint3Light/lp4Length;

    float lp5Length = length(vertexData.toPoint4Light);
    vec3 lp5 = vertexData.toPoint4Light/lp5Length;

    float lp6Length = length(vertexData.toPoint5Light);
    vec3 lp6 = vertexData.toPoint5Light/lp6Length;

    float spLength = length(vertexData.toSpotLight);
    vec3 sp = vertexData.toSpotLight/spLength;

    float sp1Length = length(vertexData.toSpot1Light);
    vec3 sp1 = vertexData.toSpot1Light/sp1Length;

    float sp2Length = length(vertexData.toSpot2Light);
    vec3 sp2 = vertexData.toSpot2Light/sp2Length;

    float sp3Length = length(vertexData.toSpot3Light);
    vec3 sp3 = vertexData.toSpot3Light/sp3Length;

    float sp4Length = length(vertexData.toSpot4Light);
    vec3 sp4 = vertexData.toSpot4Light/sp4Length;

    float sp5Length = length(vertexData.toSpot5Light);
    vec3 sp5 = vertexData.toSpot5Light/sp5Length;



    vec3 diffCol = texture(diff, vertexData.tc).xyz;
    vec3 emitCol = texture(emit, vertexData.tc).xyz;
    vec3 specularCol = texture(specular, vertexData.tc).xyz;

    //diffCol=InvGamma(diffCol);
    //emitCol=InvGamma(emitCol);
    //specularCol=InvGamma(specularCol);





    //vec3 diffCol = pow(texture(diff, vertexData.tc).rgb, vec3(gamma));
    //vec3 emitCol = pow(texture(emit, vertexData.tc).rgb, vec3(gamma));
    //vec3 specularCol = pow(texture(specular, vertexData.tc).rgb, vec3(gamma));



    //emissive
     vec3 result = emitCol * farbe*1.5;






    //Pointlight
    result += shade(n, lp, v, diffCol, specularCol, shininess) *
        pointLightIntensity(pointLightColor, lpLength, pointLightAttParam);

    result += shade(n, lp3, v, diffCol, specularCol, shininess) *
    pointLightIntensity(point2LightColor, lp3Length, point2LightAttParam);

    result += shade(n, lp4, v, diffCol, specularCol, shininess) *
    pointLightIntensity(point3LightColor, lp4Length, point3LightAttParam);

    result += shade(n, lp5, v, diffCol, specularCol, shininess) *
    pointLightIntensity(point4LightColor, lp5Length, point4LightAttParam);

    result += shade(n, lp6, v, diffCol, specularCol, shininess) *
    pointLightIntensity(point5LightColor, lp6Length, point5LightAttParam);






    //Spotlight
    result += shade(n, sp, v, diffCol, specularCol, shininess) *
        spotLightIntensity(spotLightColor, spLength, sp, spotLightDir, spotLightAttParam) * 20;

    result += shade(n, sp1, v, diffCol, specularCol, shininess) *
    spotLightIntensity(spot1LightColor, sp1Length, sp1, spot1LightDir, spot1LightAttParam) * 20 ;

    result += shade(n, sp2, v, diffCol, specularCol, shininess) *
    spotLightIntensity(spot2LightColor, sp2Length, sp2, spot2LightDir, spot2LightAttParam);

    result += shade(n, sp3, v, diffCol, specularCol, shininess) *
    spotLightIntensity(spot3LightColor, sp3Length, sp3, spot3LightDir, spot3LightAttParam);

    result += shade(n, sp4, v, diffCol, specularCol, shininess) *
    spotLightIntensity(spot4LightColor, sp4Length, sp4, spot4LightDir, spot4LightAttParam);

    result += shade(n, sp5, v, diffCol, specularCol, shininess) *
    spotLightIntensity(spot5LightColor, sp5Length, sp5, spot5LightDir, spot5LightAttParam);




    //result= Gamma(result);

    //Ambient
    //result += ambient * specularCol;

   color = vec4(result, 1.0);

   // color = vec4(pow(result,vec3(1.0/gamma)), 1.0);

}