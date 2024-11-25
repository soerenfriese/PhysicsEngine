#define SKY_COLOR vec3(0.6, 0.6, 0.8)

#define SUN_SIZE 0.075
#define SUN_COLOR vec3(1.0)

#define CLOUD_COLOR vec3(0.975)
#define CLOUD_SPREAD 2.0
#define WIND_STRENGTH sqrt(5.0) / 8.0
#define WIND_DIRECTION vec2(2.0 / sqrt(5.0), 1.0 / sqrt(5.0))
#define CLOUD_DENSITY_FALLOFF_COEFFICIENT 0.1

vec3 skybox(vec3 position) {
    vec3 fragmentDirection = normalize(position);

    // Sky color
    float y = fragmentDirection.y;
    vec3 color;

    if (y > 0.0) {
        color = SKY_COLOR;
    } else {
        float f0 = 0.32 + 0.2 * y;
        color.r = color.g = color.b = f0;
    }

    // Horizon Fog
    color.rg += vec2(0.2 / (1.0 + abs(10.0 * y)));
    color.b += 0.4 / (1.0 + abs(25.0 * y));

    // Sun
    float angleToSunCenter = acos(dot(fragmentDirection, -lightDirection));
    if (angleToSunCenter < SUN_SIZE) {
        float sunEdgeDistance = 1.0 - angleToSunCenter / SUN_SIZE;
        float interpolationFactor = smoothstep(0.0, 1.0, 1.0 - exp(-14.0 * sunEdgeDistance));
        color = mix(color, SUN_COLOR, interpolationFactor);
    }

    // Clouds
    if (fragmentDirection.y > 0) {
        vec2 wind = time * WIND_STRENGTH * WIND_DIRECTION;
        vec2 cloudPosition = CLOUD_SPREAD * (fragmentDirection.xz / fragmentDirection.y);
        vec2 uv = cloudPosition + wind;

        float cloudDensity = simplex(uv, 3) * 0.5 + 0.5;
        float cloudDensityFalloff = exp(-CLOUD_DENSITY_FALLOFF_COEFFICIENT * length(cloudPosition));

        color = mix(color, CLOUD_COLOR, cloudDensity * cloudDensityFalloff);
    }

    /*float cloudHorizonHeight = 0.1;
    if (fragmentDirection.y > cloudHorizonHeight) {
        vec2 wind = time * WIND_STRENGTH * WIND_DIRECTION;
        vec2 cloudPosition = CLOUD_SPREAD * (fragmentDirection.xz / fragmentDirection.y);
        vec2 uv = cloudPosition + wind;

        float cloudDensity = simplex(uv, 3) * 0.5 + 0.5;
        float j = (fragmentDirection.y - cloudHorizonHeight) / (1.0 - cloudHorizonHeight);
        float cloudDensityFalloff = 1.0 - pow(1.0 - j, 4.0);

        color = mix(color, CLOUD_COLOR, cloudDensity * cloudDensityFalloff);
    }*/

    return color;
}

