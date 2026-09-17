package de.ju.coozy.ecs.systems

import com.github.quillraven.fleks.IntervalSystem
import de.ju.coozy.core.utils.AssetManager
import de.ju.coozy.ecs.components.CameraComponent
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11

class SkyboxSystem : IntervalSystem() {

    private val cameraFamily = world.family { all(CameraComponent) }
    private val skyViewMatrix = Matrix4f()

    override fun onTick() {
        val camEntity = cameraFamily.firstOrNull() ?: return
        val camComp = camEntity[CameraComponent]

        GL11.glDepthFunc(GL11.GL_LEQUAL)

        val skyShader = AssetManager.getShader("skybox")
        skyShader.bind()

        skyViewMatrix.set(Matrix3f(camComp.viewMatrix))

        skyShader.setUniform("uView", skyViewMatrix)
        skyShader.setUniform("uProjection", camComp.projectionMatrix)

        AssetManager.getMesh("cube").draw()

        skyShader.unbind()

        GL11.glDepthFunc(GL11.GL_LESS)
    }

}