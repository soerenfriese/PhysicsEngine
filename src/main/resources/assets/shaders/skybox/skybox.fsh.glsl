#version 330 core

in vec3 fragmentPosition;

uniform vec3 lightDirection;
uniform vec3 lightColor;
uniform float time;

out vec4 fragColor;

#define snoise(v) -1.0
#define simplex(v) -1.0
#define skybox(v) v

void main() {
    fragColor = vec4(skybox(fragmentPosition), 1.0);
}
