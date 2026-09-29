#version 330 core
in vec3 TexCoords;
out vec4 FragColor;

void main() {
    vec3 dir = normalize(TexCoords);

    // 1. Himmelblau-Verlauf (Atmosphäre)
    vec3 zenithColor  = vec3(0.20, 0.52, 0.92); // Kräftiges Himmelblau (oben)
    vec3 horizonColor = vec3(0.72, 0.86, 0.98); // Heller Dunst / Horizont-Weißblau
    vec3 groundColor  = vec3(0.32, 0.35, 0.32); // Neutraler, matter Boden unter Horizont

    vec3 skyColor;
    if (dir.y > 0.0) {
        skyColor = mix(horizonColor, zenithColor, pow(dir.y, 0.55));
    } else {
        skyColor = mix(horizonColor, groundColor, pow(-dir.y, 0.4));
    }

    // 2. Sonne & Sonnenglow
    vec3 sunDir = normalize(vec3(0.4, 0.55, -0.6));
    float sunDot = max(dot(dir, sunDir), 0.0);

    // Sonnenscheibe
    float sunDisk = smoothstep(0.998, 0.9995, sunDot);
    vec3 sunColor = vec3(1.0, 0.98, 0.92) * 1.5;

    // Weiche Sonnenkorona
    float sunGlow = pow(sunDot, 64.0) * 0.4 + pow(sunDot, 8.0) * 0.15;
    skyColor += (sunDisk * sunColor) + (sunGlow * vec3(1.0, 0.92, 0.75));

    FragColor = vec4(skyColor, 1.0);
}