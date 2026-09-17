#version 330 core
in vec3 FragPos;
in vec2 TexCoords;

out vec4 FragColor;

uniform vec3 uLightPos;
uniform vec3 uLightColor;
uniform sampler2D uTexture;
uniform int uHasTexture;

void main() {
    // Falls keine Textur gebunden ist: Sichtbares Orange, sonst Texturfarbe
    vec4 baseColor = (uHasTexture == 1) ? texture(uTexture, TexCoords) : vec4(0.85, 0.35, 0.2, 1.0);

    // Automatische Flächennormale
    vec3 norm = normalize(cross(dFdx(FragPos), dFdy(FragPos)));

    vec3 lightDir = normalize(uLightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);

    // Grundlicht (Ambient) + diffuses Licht
    vec3 ambient = 0.25 * uLightColor;
    vec3 diffuse = diff * uLightColor;

    vec3 result = (ambient + diffuse) * baseColor.rgb;
    FragColor = vec4(result, baseColor.a);
}