package de.ju.coozy.core

import org.lwjgl.glfw.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.*

abstract class GameEngine {

    protected var window: Long = 0

    protected abstract fun handleInit()

    protected abstract fun handleKeyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int)

    protected abstract fun handleMousePosCallback(win: Long, xpos: Double, ypos: Double)

    protected abstract fun handleLoop(delta: Float)

    protected abstract fun handleCleanup()

    fun run() {
        GLFWErrorCallback.createPrint(System.err).set()

        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        window = GLFW.glfwCreateWindow(1080, 720, "Game Engine", MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) throw RuntimeException("Failed to create the GLFW window")

        GLFW.glfwSetKeyCallback(
            window
        ) { window: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            handleKeyCallback(window, key, scancode, action, mods)
        }

        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED)

        GLFW.glfwSetCursorPosCallback(window) { l, d, d1 ->
            this.handleMousePosCallback(l, d, d1)
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            val vidmode: GLFWVidMode = checkNotNull(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()))
            GLFW.glfwSetWindowPos(
                window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2
            )
        }
        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwSwapInterval(1)

        GLFW.glfwShowWindow(window)

        GL.createCapabilities()

        GL11.glEnable(GL11.GL_DEPTH_TEST)

        handleInit()

        var lastFrame = 0.0f

        while (!GLFW.glfwWindowShouldClose(window)) {
            val currentFrame = GLFW.glfwGetTime().toFloat()
            val deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

            handleLoop(deltaTime)

            GLFW.glfwSwapBuffers(window)
            GLFW.glfwPollEvents()
        }

        handleCleanup()

        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        GLFW.glfwTerminate()
        Objects.requireNonNull(GLFW.glfwSetErrorCallback { _: Int, _: Long -> })!!.free()
    }

}