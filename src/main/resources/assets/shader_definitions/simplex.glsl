float simplex(vec2 uv, int layers) {
    float value = 0.0;
    float amplitude = 1.0;
    float frequency = 1.0;
    float amplitudeMax = 0.0;

    for (int i = 0; i < layers; ++i) {
        value += amplitude * snoise(uv * frequency);

        amplitudeMax += amplitude;
        amplitude *= 2.0;
        frequency *= 0.5;
    }

    return value / amplitudeMax;
}