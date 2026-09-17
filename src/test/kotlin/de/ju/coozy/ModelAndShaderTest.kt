package de.ju.coozy

import de.ju.coozy.core.render.Shader
import de.ju.coozy.core.utils.GltfModelLoader
import de.ju.coozy.core.utils.ResourceLoader
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil
import kotlin.test.Test
import kotlin.test.assertTrue

class ModelAndShaderTest {

    @Test
    fun testShadersAndModelLoading() {
        check(GLFW.glfwInit()) { "GLFW init failed" }
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        val window = GLFW.glfwCreateWindow(100, 100, "Test", MemoryUtil.NULL, MemoryUtil.NULL)
        check(window != MemoryUtil.NULL) { "Window creation failed" }
        GLFW.glfwMakeContextCurrent(window)
        GL.createCapabilities()

        // 1. Verify cube shader compiles & links
        val cubeVert = ResourceLoader.loadShaderSource("/shaders/cube.vert")!!
        val cubeFrag = ResourceLoader.loadShaderSource("/shaders/cube.frag")!!
        val cubeShader = Shader(cubeVert, cubeFrag)
        assertTrue(cubeShader.id > 0)
        cubeShader.destroy()

        // 2. Verify skybox shader compiles & links
        val skyVert = ResourceLoader.loadShaderSource("/shaders/skybox.vert")!!
        val skyFrag = ResourceLoader.loadShaderSource("/shaders/skybox.frag")!!
        val skyShader = Shader(skyVert, skyFrag)
        assertTrue(skyShader.id > 0)
        skyShader.destroy()

        // 3. Verify car.glb model loading and material colors
        val car = GltfModelLoader.loadModel("test_car", "/models/car.glb")
        assertTrue(car.subMeshes.isNotEmpty())
        for ((idx, subMesh) in car.subMeshes.withIndex()) {
            val color = subMesh.material.diffuseColor
            println("Submesh $idx diffuse color: ($color.x, $color.y, $color.z)")
        }
        car.destroy()

        GLFW.glfwDestroyWindow(window)
        GLFW.glfwTerminate()
    }
}
