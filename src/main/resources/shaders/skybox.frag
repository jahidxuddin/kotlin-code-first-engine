#version 330 core
in vec3 TexCoords;
out vec4 FragColor;

// --- 3D Simplex-Noise ---

vec4 permute(vec4 x) { return mod(((x * 34.0) + 1.0) * x, 289.0); }
vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }

float snoise(vec3 v) {
    const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

    vec3 i  = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);

    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
    vec3 x3 = x0 - 1.0 + 3.0 * C.xxx;

    i = mod(i, 289.0);
    vec4 p = permute(permute(permute(
                                 i.z + vec4(0.0, i1.z, i2.z, 1.0))
                             + i.y + vec4(0.0, i1.y, i2.y, 1.0))
                     + i.x + vec4(0.0, i1.x, i2.x, 1.0));

    float n_ = 0.142857142857;
    vec3  ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z * ns.z);

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);

    vec4 x = x_ * ns.x + ns.yyyy;
    vec4 y = y_ * ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0) * 2.0 + 1.0;
    vec4 s1 = floor(b1) * 2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m * m, vec4(dot(p0, x0), dot(p1, x1), dot(p2, x2), dot(p3, x3)));
}

// Fraktale Überlagerung für Wolken
float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    vec3 shift = vec3(45.12);
    for (int i = 0; i < 4; ++i) {
        v += a * snoise(p);
        p = p * 2.1 + shift;
        a *= 0.5;
    }
    return v * 0.5 + 0.5;
}

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

    // 3. Prozedurale weiße Wolken (nur über dem Horizont)
    if (dir.y > 0.03) {
        // Flache Projektion für realistischere Wolkendecke
        vec3 cloudPlane = vec3(dir.xz / (dir.y + 0.15) * 1.8, 1.0);
        float cloudNoise = fbm(cloudPlane);

        // Wolkendichte & sanfte Konturen
        float cloudMask = smoothstep(0.52, 0.78, cloudNoise);
        cloudMask *= smoothstep(0.03, 0.25, dir.y); // Zum Horizont hin ausblenden

        vec3 cloudColor = vec3(0.98, 0.99, 1.0);
        // Leichte Schatten an der Wolkenunterseite
        vec3 cloudShadow = mix(vec3(0.82, 0.88, 0.94), cloudColor, smoothstep(0.55, 0.8, cloudNoise));

        skyColor = mix(skyColor, cloudShadow, cloudMask * 0.85);
    }

    FragColor = vec4(skyColor, 1.0);
}