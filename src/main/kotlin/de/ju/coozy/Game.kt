package de.ju.coozy

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import de.ju.coozy.core.GameEngine
import de.ju.coozy.core.camera.Camera
import de.ju.coozy.core.render.Mesh
import de.ju.coozy.core.render.Shader
import de.ju.coozy.core.render.primitives.Cube
import de.ju.coozy.core.utils.ResourceLoader
import de.ju.coozy.ecs.components.Transform
import de.ju.coozy.ecs.systems.MovementSystem
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import kotlin.system.exitProcess

class Game : GameEngine() {

    private var world: World = configureWorld {
        systems {
            add(MovementSystem())
        }
    }

    private val camera: Camera = Camera()

    private val staticTarget: Vector3f = Vector3f(0.0f, 0.0f, -5.0f)

    private var cubeShader: Shader? = null
    private var cubeMesh: Mesh? = null
    private var cubeModel: Matrix4f = Matrix4f()
    private var cubeProjection: Matrix4f = Matrix4f()
    private var cubeView: Matrix4f = Matrix4f()

    override fun handleInit() {
        val cubeVertexShaderCode: String? = ResourceLoader.loadShaderSource("/shaders/cube.vert")
        val cubeFragmentShaderCode: String? = ResourceLoader.loadShaderSource("/shaders/cube.frag")

        if (cubeVertexShaderCode == null || cubeFragmentShaderCode == null) {
            println("Failed to load vertex or fragment shader")
            exitProcess(0)
        }

        this.cubeShader = Shader(cubeVertexShaderCode, cubeFragmentShaderCode)
        this.cubeMesh = Mesh(Cube.VERTICES, Cube.INDICES)

        cubeProjection.perspective(Math.toRadians(45.0).toFloat(), 1080.0f / 720.0f, 0.1f, 100.0f)
        GL11.glClearColor(0.08f, 0.08f, 0.1f, 1.0f)
        GL11.glEnable(GL11.GL_DEPTH_TEST)

        world.entity {
            it += Transform(
                position = Vector3f(0.0f, 0.0f, -5.0f), size = Vector3f(1.0f, 1.0f, 1.0f), rotation = Quaternionf()
            )
        }
    }

    override fun handleKeyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {}

    override fun handleMousePosCallback(win: Long, xpos: Double, ypos: Double) {}

    override fun handleLoop(delta: Float) {
        world.update(delta)

        cubeView.set(camera.getViewMatrix(staticTarget))

        cubeShader?.bind()
        cubeShader?.setUniform("uView", cubeView)
        cubeShader?.setUniform("uProjection", cubeProjection)

        world.family { all(Transform) }.forEach { entity ->
            val transform = entity[Transform]

            cubeModel.identity().translate(transform.position).rotate(transform.rotation).scale(transform.size)

            cubeShader?.setUniform("uModel", cubeModel)
            cubeMesh?.draw()
        }

        cubeShader?.unbind()
    }

    override fun handleCleanup() {
        this.cubeShader?.destroy()
        this.cubeMesh?.destroy()
    }
}

fun main() {
    Game().run()
}