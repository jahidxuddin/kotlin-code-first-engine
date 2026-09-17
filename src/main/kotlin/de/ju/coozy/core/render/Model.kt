package de.ju.coozy.core.render

import org.joml.Vector3f

data class Material(
    val diffuseTexture: Texture? = null,
    val diffuseColor: Vector3f = Vector3f(1f, 1f, 1f)
)

data class SubMesh(
    val mesh: Mesh,
    val material: Material
)

class Model(val subMeshes: List<SubMesh>) {
    fun destroy() {
        subMeshes.forEach {
            it.mesh.destroy()
            it.material.diffuseTexture?.destroy()
        }
    }
}