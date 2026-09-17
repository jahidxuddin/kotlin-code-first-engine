#version 330 core
in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoords;

out vec4 FragColor;

uniform sampler2D uTexture;
uniform int uHasTexture;
uniform vec3 uColor;
uniform vec3 uLightPos;
uniform vec3 uLightColor;

void main() {
    vec4 baseColor = (uHasTexture == 1)
        ? texture(uTexture, TexCoords)
        : vec4(uColor, 1.0);

    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(uLightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);

    vec3 ambient = 0.25 * uLightColor;
    vec3 diffuse = diff * uLightColor;

    FragColor = vec4((ambient + diffuse) * baseColor.rgb, baseColor.a);
}