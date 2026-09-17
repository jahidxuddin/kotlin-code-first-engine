package de.ju.coozy.ecs.systems

import com.github.quillraven.fleks.IntervalSystem
import de.ju.coozy.core.utils.AssetManager
import de.ju.coozy.ecs.components.*
import org.joml.Vector3f

class RenderSystem : IntervalSystem() {

    private val cameraFamily = world.family { all(CameraComponent) }
    private val lightFamily = world.family { all(PointLightComponent, TransformComponent) }
    private val meshFamily = world.family { all(TransformComponent, MeshComponent) }
    private val modelFamily = world.family { all(TransformComponent, ModelComponent) }

    override fun onTick() {
        val camEntity = cameraFamily.firstOrNull() ?: return
        val camComp = camEntity[CameraComponent]

        val shader = AssetManager.getShader("cube")
        shader.bind()
        shader.setUniform("uView", camComp.viewMatrix)
        shader.setUniform("uProjection", camComp.projectionMatrix)
        shader.setUniform("uTexture", 0)

        val lightEntity = lightFamily.firstOrNull()
        if (lightEntity != null) {
            val lightTransform = lightEntity[TransformComponent]
            val lightComp = lightEntity[PointLightComponent]
            shader.setUniform("uLightPos", lightTransform.position)
            shader.setUniform("uLightColor", lightComp.color)
        } else {
            shader.setUniform("uLightPos", Vector3f(0f, 10f, 0f))
            shader.setUniform("uLightColor", Vector3f(1f, 1f, 1f))
        }

        meshFamily.forEach { entity ->
            val transform = entity[TransformComponent]
            val meshComp = entity[MeshComponent]
            val mesh = AssetManager.getMesh(meshComp.meshId)

            updateModelMatrix(transform)
            shader.setUniform("uModel", transform.modelMatrix)
            shader.setUniform("uHasTexture", 0)
            mesh.draw()
        }

        modelFamily.forEach { entity ->
            val transform = entity[TransformComponent]
            val modelComp = entity[ModelComponent]
            val model = AssetManager.getModel(modelComp.modelId)

            updateModelMatrix(transform)
            shader.setUniform("uModel", transform.modelMatrix)

            for ((mesh, material) in model.subMeshes) {
                val texture = material.diffuseTexture
                if (texture != null) {
                    shader.setUniform("uHasTexture", 1)
                    texture.bind(0)
                } else {
                    shader.setUniform("uHasTexture", 0)
                }
                mesh.draw()
            }
        }

        shader.unbind()
    }

    private fun updateModelMatrix(transform: TransformComponent) {
        transform.modelMatrix.identity().translate(transform.position).rotate(transform.rotation).scale(transform.size)
    }

}