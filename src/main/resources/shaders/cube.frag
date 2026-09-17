#version 330 core
in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoords;

out vec4 FragColor;

uniform sampler2D uTexture;
uniform int uHasTexture;
uniform vec3 uColor;

// View / Camera position for specular reflection
uniform vec3 uViewPos;

// Ambient light
uniform vec3 uAmbientColor = vec3(0.35, 0.38, 0.45);

// Directional Light (Sunlight)
uniform int uHasDirLight = 1;
uniform vec3 uDirLightDir = vec3(0.3, 0.8, 0.5); // Vector pointing towards light source
uniform vec3 uDirLightColor = vec3(1.2, 1.15, 1.05);

// Point Light
uniform int uHasPointLight = 1;
uniform vec3 uLightPos = vec3(1.5, 2.0, -3.0);
uniform vec3 uLightColor = vec3(1.2, 1.0, 0.8);

void main() {
    vec4 baseColor = (uHasTexture == 1)
        ? texture(uTexture, TexCoords)
        : vec4(uColor, 1.0);

    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(uViewPos - FragPos);

    // Ambient
    vec3 ambient = uAmbientColor;

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    // 1. Directional Light (e.g. Sunlight)
    if (uHasDirLight == 1) {
        vec3 dirLightDir = normalize(uDirLightDir);
        float diff = max(dot(norm, dirLightDir), 0.0);
        totalDiffuse += diff * uDirLightColor;

        // Blinn-Phong specular highlight (car paint shine)
        vec3 halfDir = normalize(dirLightDir + viewDir);
        float spec = pow(max(dot(norm, halfDir), 0.0), 32.0);
        totalSpecular += 0.45 * spec * uDirLightColor;
    }

    // 2. Point Light
    if (uHasPointLight == 1) {
        vec3 pointLightDir = normalize(uLightPos - FragPos);
        float diff = max(dot(norm, pointLightDir), 0.0);

        float dist = length(uLightPos - FragPos);
        float attenuation = 1.0 / (1.0 + 0.045 * dist + 0.0075 * dist * dist);

        totalDiffuse += diff * uLightColor * attenuation;

        vec3 halfDir = normalize(pointLightDir + viewDir);
        float spec = pow(max(dot(norm, halfDir), 0.0), 32.0);
        totalSpecular += 0.5 * spec * uLightColor * attenuation;
    }

    // Combine diffuse, ambient and specular
    vec3 linearColor = (ambient + totalDiffuse) * baseColor.rgb + totalSpecular;

    // Gamma correction for realistic sRGB display (especially for glTF linear colors)
    vec3 srgbColor = pow(max(linearColor, vec3(0.0)), vec3(1.0 / 2.2));

    FragColor = vec4(srgbColor, baseColor.a);
}