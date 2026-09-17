package de.ju.coozy

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import de.ju.coozy.core.GameEngine
import de.ju.coozy.core.render.Mesh
import de.ju.coozy.core.render.Shader
import de.ju.coozy.core.render.primitives.Cube
import de.ju.coozy.core.utils.AssetManager
import de.ju.coozy.core.utils.ResourceLoader
import de.ju.coozy.ecs.components.CameraComponent
import de.ju.coozy.ecs.components.MeshComponent
import de.ju.coozy.ecs.components.TransformComponent
import de.ju.coozy.ecs.systems.CameraSystem
import de.ju.coozy.ecs.systems.MovementSystem
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

class Game : GameEngine() {

    private var world: World = configureWorld {
        systems {
            add(MovementSystem())
            add(CameraSystem())
        }
    }

    private var cameraEntity: Entity? = null

    private var lastMouseX = 540.0
    private var lastMouseY = 360.0
    private var firstMouse = true

    override fun handleInit() {
        GL11.glClearColor(0.08f, 0.08f, 0.1f, 1.0f)

        val cubeVert = ResourceLoader.loadShaderSource("/shaders/cube.vert")!!
        val cubeFrag = ResourceLoader.loadShaderSource("/shaders/cube.frag")!!
        AssetManager.registerShader("cube", Shader(cubeVert, cubeFrag))
        AssetManager.registerMesh("cube", Mesh(Cube.VERTICES, Cube.INDICES))

        world.entity {
            it += TransformComponent(position = Vector3f(0f, 0f, -5f))
            it += MeshComponent(AssetManager.getMesh("cube"))
        }

        cameraEntity = world.entity {
            it += TransformComponent(position = Vector3f(0.0f, 0.0f, 3.0f))
            it += CameraComponent().apply {
                projectionMatrix.perspective(
                    Math.toRadians(fov.toDouble()).toFloat(), 1080.0f / 720.0f, near, far
                )
            }
        }
    }

    override fun handleKeyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
            GLFW.glfwSetWindowShouldClose(window, true)
        }
    }

    override fun handleMousePosCallback(win: Long, xpos: Double, ypos: Double) {
        if (firstMouse) {
            lastMouseX = xpos
            lastMouseY = ypos
            firstMouse = false
        }

        val deltaX = (xpos - lastMouseX).toFloat()
        val deltaY = (lastMouseY - ypos).toFloat()
        lastMouseX = xpos
        lastMouseY = ypos

        cameraEntity?.let { entity ->
            with(world) {
                val cam = entity[CameraComponent]
                cam.yaw += deltaX * 0.1f
                cam.pitch = (cam.pitch + deltaY * 0.1f).coerceIn(-89.0f, 89.0f)
            }
        }
    }

    override fun handleLoop(delta: Float) {
        world.update(delta)

        val camEntity = cameraEntity ?: return

        with(world) {
            val camComp = camEntity[CameraComponent]

            val shader = AssetManager.getShader("cube")
            shader.bind()
            shader.setUniform("uView", camComp.viewMatrix)
            shader.setUniform("uProjection", camComp.projectionMatrix)

            world.family { all(TransformComponent, MeshComponent) }.forEach { entity ->
                val transform = entity[TransformComponent]
                val meshComp = entity[MeshComponent]

                transform.modelMatrix.identity().translate(transform.position).rotate(transform.rotation)
                    .scale(transform.size)

                shader.setUniform("uModel", transform.modelMatrix)

                meshComp.renderMesh.draw()
            }

            shader.unbind()
        }
    }

    override fun handleCleanup() {
        world.dispose()
        AssetManager.dispose()
    }
}

fun main() {
    Game().run()
}