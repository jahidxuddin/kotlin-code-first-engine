package de.ju.coozy.core.utils

import de.ju.coozy.core.render.Mesh
import de.ju.coozy.core.render.Model
import de.ju.coozy.core.render.Shader
import de.ju.coozy.core.render.Texture

object AssetManager {
    private val meshes = mutableMapOf<String, Mesh>()
    private val shaders = mutableMapOf<String, Shader>()
    private val textures = mutableMapOf<String, Texture>()
    private val models = mutableMapOf<String, Model>()

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

    fun registerTexture(name: String, texture: Texture): Texture {
        textures[name] = texture
        return texture
    }

    fun getTexture(name: String): Texture =
        textures[name] ?: error("Texture '$name' nicht geladen!")

    fun registerModel(name: String, model: Model): Model {
        models[name] = model
        return model
    }

    fun getModel(name: String): Model =
        models[name] ?: error("Model '$name' nicht geladen!")

    fun dispose() {
        meshes.values.forEach { it.destroy() }
        meshes.clear()

        shaders.values.forEach { it.destroy() }
        shaders.clear()

        textures.values.forEach { it.destroy() }
        textures.clear()

        models.values.forEach { it.destroy() }
        models.clear()
    }
}