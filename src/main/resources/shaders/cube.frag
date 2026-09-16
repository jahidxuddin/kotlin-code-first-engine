#version 330 core
out vec4 FragColor;

uniform vec4 uColor;

void main() {
    FragColor = vec4(gl_FragCoord.x / 1080.0, gl_FragCoord.y / 720.0, 0.0, 1.0);
}