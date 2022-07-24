#version 330 core

layout(location = 0) in vec3 position;
layout(location = 2) in vec3 normal;

//uniforms
// translation object to world
uniform mat4 model_matrix;
uniform mat4 view;
uniform mat4 perspective;



out vec3 Normal;

//
void main(){
    mat4 modelview = view * model_matrix;
    vec4 pos = modelview * vec4(position, 1.0f);

    vec4 norm = transpose(inverse(modelview)) *  vec4(normal, 0.0f);
    Normal = norm.xyz;


    gl_Position = perspective * pos;
}