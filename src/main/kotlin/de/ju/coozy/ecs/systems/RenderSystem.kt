package de.ju.coozy.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import de.ju.coozy.core.utils.AssetManager
import de.ju.coozy.ecs.components.*
import org.joml.Vector3f
import org.lwjgl.opengl.GL13

class RenderSystem : IteratingSystem(
    family { all(TransformComponent).any(MeshComponent, ModelComponent) }
) {

    private val defaultMeshColor = Vector3f(0.25f, 0.25f, 0.28f)

    override fun onTick() {
        val shader = AssetManager.getShader("cube")
        shader.bind()

        world.family { all(CameraComponent) }.firstOrNull()?.let { camEntity ->
            val cam = camEntity[CameraComponent]
            shader.setUniform("uView", cam.viewMatrix)
            shader.setUniform("uProjection", cam.projectionMatrix)

            if (camEntity has TransformComponent) {
                shader.setUniform("uViewPos", camEntity[TransformComponent].position)
            }
        }

        world.family { all(DirectionalLightComponent) }.firstOrNull()?.let { dirEntity ->
            val dirLight = dirEntity[DirectionalLightComponent]
            val towardsLight = Vector3f(dirLight.direction).negate().normalize()
            val lightColor = Vector3f(dirLight.color).mul(dirLight.intensity)
            shader.setUniform("uHasDirLight", 1)
            shader.setUniform("uDirLightDir", towardsLight)
            shader.setUniform("uDirLightColor", lightColor)
        } ?: run {
            shader.setUniform("uHasDirLight", 0)
        }

        world.family { all(TransformComponent, PointLightComponent) }.firstOrNull()?.let { lightEntity ->
            val lightTransform = lightEntity[TransformComponent]
            val lightComp = lightEntity[PointLightComponent]
            val lightColor = Vector3f(lightComp.color).mul(lightComp.intensity)
            shader.setUniform("uHasPointLight", 1)
            shader.setUniform("uLightPos", lightTransform.position)
            shader.setUniform("uLightColor", lightColor)
        } ?: run {
            shader.setUniform("uHasPointLight", 0)
        }

        shader.setUniform("uAmbientColor", Vector3f(0.35f, 0.38f, 0.45f))

        super.onTick()

        shader.unbind()
    }

    override fun onTickEntity(entity: Entity) {
        val transform = entity[TransformComponent]
        val shader = AssetManager.getShader("cube")

        transform.modelMatrix.identity()
            .translate(transform.position)
            .rotate(transform.rotation)
            .scale(transform.size)
        shader.setUniform("uModel", transform.modelMatrix)

        if (entity has MeshComponent) {
            val meshComp = entity[MeshComponent]
            val mesh = AssetManager.getMesh(meshComp.meshId)

            shader.setUniform("uHasTexture", 0)
            shader.setUniform("uColor", defaultMeshColor)
            mesh.draw()
        }

        if (entity has ModelComponent) {
            val modelComp = entity[ModelComponent]
            val model = AssetManager.getModel(modelComp.modelId)

            for ((mesh, material) in model.subMeshes) {
                val tex = material.diffuseTexture
                if (tex != null) {
                    shader.setUniform("uHasTexture", 1)
                    GL13.glActiveTexture(GL13.GL_TEXTURE0)
                    tex.bind()
                    shader.setUniform("uTexture", 0)
                } else {
                    shader.setUniform("uHasTexture", 0)
                }
                shader.setUniform("uColor", material.diffuseColor)

                mesh.draw()
            }
        }
    }

}