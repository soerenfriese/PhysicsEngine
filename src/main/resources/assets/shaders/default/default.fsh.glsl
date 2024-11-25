#version 330 core

in vec3 fragmentPosition;
in vec3 fragmentNormal;
in vec4 lightFragmentPosition;

uniform vec3 color;
uniform vec3 lightDirection;
uniform vec3 lightColor;
uniform vec3 viewPosition;
uniform sampler2D depthTexture;

out vec4 fragColor;

#define shadow(v) v

void main() {
    float ambientStrength = 0.25;
    vec3 ambient = ambientStrength * lightColor;

    vec3 n1 = normalize(fragmentNormal);
    vec3 n2 = normalize(-lightDirection);
    float d1 = max(0.0, dot(n1, n2));
    vec3 diffuse = d1 * lightColor;

    float specularStrength = sign(d1) * 0.25;
    vec3 viewDirection = normalize(viewPosition - fragmentPosition);
    vec3 reflectDirection = reflect(-n2, n1);
    float d2 = max(0.0, dot(viewDirection, reflectDirection));
    vec3 specular = specularStrength * pow(d2, 32.0) * lightColor;

    vec3 lighting = (ambient + shadow(diffuse + specular)) * color;

    fragColor = vec4(lighting, 1.0);
}