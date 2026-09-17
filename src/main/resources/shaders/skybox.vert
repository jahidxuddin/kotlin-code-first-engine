#version 330 core
layout (location = 0) in vec3 aPos;

out vec3 TexCoords;

uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    TexCoords = aPos;
    // Multiplikation ohne Translation
    vec4 pos = uProjection * uView * vec4(aPos, 1.0);
    // z = w bewirkt nach der Division pos.z / pos.w = 1.0 (maximaler Tiefenwert)
    gl_Position = pos.xyww;
}