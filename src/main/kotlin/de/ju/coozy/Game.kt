package de.ju.coozy

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import de.ju.coozy.core.GameEngine
import de.ju.coozy.core.render.Mesh
import de.ju.coozy.core.render.Shader
import de.ju.coozy.core.render.primitives.Cube
import de.ju.coozy.core.render.primitives.Plane
import de.ju.coozy.core.utils.AssetManager
import de.ju.coozy.core.utils.GltfModelLoader
import de.ju.coozy.core.utils.ResourceLoader
import de.ju.coozy.ecs.components.*
import de.ju.coozy.ecs.systems.CameraSystem
import de.ju.coozy.ecs.systems.RenderSystem
import de.ju.coozy.ecs.systems.SkyboxSystem
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11

class Game : GameEngine() {

    private lateinit var world: World
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
        AssetManager.registerMesh("plane", Mesh(Plane.VERTICES, Plane.INDICES))

        val skyVert = ResourceLoader.loadShaderSource("/shaders/skybox.vert")!!
        val skyFrag = ResourceLoader.loadShaderSource("/shaders/skybox.frag")!!
        AssetManager.registerShader("skybox", Shader(skyVert, skyFrag))

        GltfModelLoader.loadModel("rihal", "/models/rihal.glb")
        GltfModelLoader.loadModel("quran", "/models/quran.glb")

        world = configureWorld {
            systems {
                add(CameraSystem())
                add(RenderSystem())
                add(SkyboxSystem())
            }
        }

        world.entity {
            it += TransformComponent(
                position = Vector3f(0.0f, -1.0f, 0.0f),
                size = Vector3f(200.0f, 1.0f, 200.0f)
            )
            it += MeshComponent("plane")
        }

        world.entity {
            it += TransformComponent(
                position = Vector3f(0.0f, -1.0f, 0.0f),
                size = Vector3f(0.01f, 0.01f, 0.01f)
            )
            it += ModelComponent("rihal")
        }

        world.entity {
            it += TransformComponent(
                position = Vector3f(0.0f, -0.42f, 0.05f),
                rotation = Quaternionf()
                    .rotateY(Math.toRadians(90.0).toFloat())
                    .rotateX(Math.toRadians(12.0).toFloat()),
                size = Vector3f(0.0016f, 0.0016f, 0.0016f)
            )
            it += ModelComponent("quran")
        }

        cameraEntity = world.entity {
            it += TransformComponent(
                position = Vector3f(0.0f, 2.0f, 5.0f)
            )
            it += CameraComponent().apply {
                isAttached = false
                yaw = -90.0f
                pitch = -20.0f
                projectionMatrix.perspective(
                    Math.toRadians(fov.toDouble()).toFloat(), 1080.0f / 720.0f, near, far
                )
            }
        }

        world.entity {
            it += DirectionalLightComponent(
                direction = Vector3f(-0.3f, -0.8f, -0.4f).normalize(),
                color = Vector3f(1.0f, 0.98f, 0.92f),
                intensity = 1.3f
            )
        }

        world.entity {
            it += TransformComponent(position = Vector3f(1.5f, 4.0f, 2.0f))
            it += PointLightComponent(color = Vector3f(1.0f, 0.9f, 0.8f), intensity = 1.5f)
        }
    }

    override fun handleKeyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
            GLFW.glfwSetWindowShouldClose(window, true)
        }
        if (key == GLFW.GLFW_KEY_F && action == GLFW.GLFW_RELEASE) {
            toggleFullscreen(window)
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
    }

    override fun handleCleanup() {
        world.dispose()
        AssetManager.dispose()
    }
}

fun main() {
    Game().run()
}