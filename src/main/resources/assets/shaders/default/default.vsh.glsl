#version 330 core

layout(location=0) in vec3 position;
layout(location=1) in vec3 normal;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform mat4 lightProjection;
uniform mat4 lightView;

out vec3 fragmentPosition;
out vec3 fragmentNormal;
out vec4 lightFragmentPosition;

void main() {
    gl_Position = projection * view * model * vec4(position, 1.0);

    fragmentPosition = vec3(model * vec4(position, 1.0));
    fragmentNormal = transpose(inverse(mat3(model))) * normal;

    lightFragmentPosition = lightProjection * lightView * model * vec4(position, 1.0);
}