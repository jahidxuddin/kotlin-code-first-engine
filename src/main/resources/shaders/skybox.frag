#version 330 core
in vec3 TexCoords;
out vec4 FragColor;

// --- 3D Simplex-Noise (nahtlos & frei von Dreiecksartefakten) ---

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

// Fraktale Überlagerung
float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    vec3 shift = vec3(100.0);
    for (int i = 0; i < 4; ++i) {
        v += a * snoise(p);
        p = p * 2.0 + shift;
        a *= 0.5;
    }
    return v * 0.5 + 0.5;
}

// Schneller Hash für Sterne
float hash31(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

vec3 renderStars(vec3 dir, float densityBonus) {
    if (dir.y <= 0.0) return vec3(0.0);

    vec3 p = dir * 80.0;
    vec3 cell = floor(p);
    vec3 frac = fract(p) - 0.5;

    vec3 stars = vec3(0.0);

    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                vec3 neighbor = vec3(float(x), float(y), float(z));
                vec3 curCell = cell + neighbor;

                float h = hash31(curCell);
                float threshold = mix(0.97, 0.94, densityBonus);

                if (h > threshold) {
                    vec3 offset = (vec3(hash31(curCell + 1.0), hash31(curCell + 2.0), hash31(curCell + 3.0)) - 0.5) * 0.7;
                    float dist = length(frac - neighbor - offset);

                    float core = smoothstep(0.1, 0.0, dist);
                    float glow = exp(-dist * 18.0) * 1.4;

                    vec3 tint = mix(vec3(0.6, 0.8, 1.0), vec3(1.0, 0.6, 0.9), fract(h * 31.0));
                    stars += tint * (core + glow);
                }
            }
        }
    }
    return stars * smoothstep(0.0, 0.15, dir.y);
}

void main() {
    vec3 dir = normalize(TexCoords);

    // Grundhimmel
    vec3 zenithColor  = vec3(0.02, 0.015, 0.04);
    vec3 horizonColor = vec3(0.05, 0.04,  0.07);
    vec3 groundColor  = vec3(0.01, 0.01,  0.015);

    vec3 skyColor = (dir.y > 0.0)
    ? mix(horizonColor, zenithColor, pow(dir.y, 0.7))
    : mix(horizonColor, groundColor, pow(-dir.y, 0.5));

    // Ausrichtung des Nebel-Bandes
    float band = abs(dir.x * 0.75 + dir.z * 0.45 - dir.y * 0.35);
    float bandMask = exp(-band * band * 5.0) * smoothstep(0.0, 0.3, dir.y);

    // Organischer Gas-Nebel ohne Kanten
    vec3 p = dir * 2.8;
    float n1 = fbm(p);
    float n2 = fbm(p + vec3(n1 * 1.2));
    float nebulaMask = pow(n2, 1.5) * bandMask;

    // Farbpalette
    vec3 colorCore = vec3(1.0, 0.75, 0.45); // Warmes Gold
    vec3 colorMid  = vec3(0.85, 0.22, 0.60); // Magenta
    vec3 colorDust = vec3(0.20, 0.12, 0.55); // Dunkles Indigo

    vec3 nebulaColor = mix(colorDust, colorMid, smoothstep(0.1, 0.45, nebulaMask));
    nebulaColor = mix(nebulaColor, colorCore, smoothstep(0.45, 0.85, nebulaMask));

    skyColor += nebulaColor * nebulaMask * 2.6;

    // Sterne
    skyColor += renderStars(dir, nebulaMask);

    FragColor = vec4(skyColor, 1.0);
}