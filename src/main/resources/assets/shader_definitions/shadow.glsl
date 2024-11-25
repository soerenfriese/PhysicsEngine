const vec2[] OFFSETS = vec2[](
    vec2(-1.0, -1.0),
    vec2( 0.0, -1.0),
    vec2( 1.0, -1.0),
    vec2(-1.0,  0.0),
    vec2( 0.0,  0.0),
    vec2( 1.0,  0.0),
    vec2(-1.0,  1.0),
    vec2( 0.0,  1.0),
    vec2( 1.0,  1.0)
);

float bilerp(float v0, float v1, float v2, float v3, float x, float y) {
    float x0 = mix(v0, v1, x);
    float x1 = mix(v3, v2, x);
    return mix(x0, x1, y);
}

float bilerp9(float d00, float d10, float d20, float d01, float d11, float d21, float d02, float d12, float d22, float x, float y) {
    float v0 = (d00 + d10 + d01 + d11) / 4.0;
    float v1 = (d10 + d20 + d11 + d21) / 4.0;
    float v2 = (d11 + d21 + d12 + d22) / 4.0;
    float v3 = (d01 + d11 + d02 + d12) / 4.0;

    return bilerp(v0, v1, v2, v3, x, y);
}

vec3 shadow(vec3 value) {
    vec3 pos = (lightFragmentPosition.xyz / lightFragmentPosition.w) * 0.5 + 0.5;

    if (pos.z > 1.0) return value;

    vec2 uv = pos.xy;
    vec2 size = textureSize(depthTexture, 0);
    vec2 texelSize = 1.0 / size;

    pos.z = pos.z;

    float bias = 0.0002;
    float samples[9];

    for (int i = 0; i < 9; ++i) {
        samples[i] = (pos.z - bias) < texture(depthTexture, uv + OFFSETS[i] * texelSize).r ? 1.0 : 0.0;
    }

    vec2 f = fract(uv * size);
    float intensity = bilerp9(samples[0], samples[1], samples[2], samples[3], samples[4], samples[5], samples[6], samples[7], samples[8], f.x, f.y);

    return intensity * value;
}

