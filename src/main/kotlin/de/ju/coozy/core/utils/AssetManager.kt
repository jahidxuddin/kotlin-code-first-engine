package de.ju.coozy.core.utils

import de.ju.coozy.core.render.Mesh
import de.ju.coozy.core.render.Shader

object AssetManager {
    private val meshes = mutableMapOf<String, Mesh>()
    private val shaders = mutableMapOf<String, Shader>()

    fun registerMesh(name: String, mesh: Mesh): Mesh {
        meshes[name] = mesh
        return mesh
    }

    fun getMesh(name: String): Mesh =
        meshes[name] ?: error("Mesh '$name' nicht geladen!")

    fun registerShader(name: String, shader: Shader): Shader {
        shaders[name] = shader
        return shader
    }

    fun getShader(name: String): Shader =
        shaders[name] ?: error("Shader '$name' nicht geladen!")

    fun dispose() {
        meshes.values.forEach { it.destroy() }
        meshes.clear()

        shaders.values.forEach { it.destroy() }
        shaders.clear()
    }
}