#version 330 core

layout(location=0) in vec3 position;

uniform mat4 projection;
uniform mat4 view;

out vec3 fragmentPosition;

void main() {
    vec3 viewSpacePosition = vec3(view * vec4(position, 0.0));

    gl_Position = projection * vec4(viewSpacePosition, 1.0);

    fragmentPosition = position;
}