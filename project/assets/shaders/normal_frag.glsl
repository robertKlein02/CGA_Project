#version 330 core

//input from vertex shader

in vec3 Normal;



//fragment shader output
out vec4 color;



void main(){
   vec3 normals= normalize(Normal); // damit vektoren gleiche länge haben
    color= vec4 (normals.rgb,1.0f);
}
