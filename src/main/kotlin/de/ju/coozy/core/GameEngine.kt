package de.ju.coozy.core

import org.lwjgl.glfw.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.*

abstract class GameEngine {

    protected var window: Long = 0

    private var windowedX: Int = 100
    private var windowedY: Int = 100
    private var windowedWidth: Int = 1080
    private var windowedHeight: Int = 720

    protected abstract fun handleInit()

    protected abstract fun handleKeyCallback(window: Long, key: Int, scancode: Int, action: Int, mods: Int)

    protected abstract fun handleMousePosCallback(win: Long, xpos: Double, ypos: Double)

    protected abstract fun handleLoop(delta: Float)

    protected abstract fun handleCleanup()

    protected fun toggleFullscreen(targetWindow: Long = this.window) {
        val isFullscreen = GLFW.glfwGetWindowMonitor(targetWindow) != MemoryUtil.NULL

        if (!isFullscreen) {
            MemoryStack.stackPush().use { stack ->
                val pX = stack.mallocInt(1)
                val pY = stack.mallocInt(1)
                val pWidth = stack.mallocInt(1)
                val pHeight = stack.mallocInt(1)

                GLFW.glfwGetWindowPos(targetWindow, pX, pY)
                GLFW.glfwGetWindowSize(targetWindow, pWidth, pHeight)

                windowedX = pX.get(0)
                windowedY = pY.get(0)
                windowedWidth = pWidth.get(0)
                windowedHeight = pHeight.get(0)
            }

            val monitor = GLFW.glfwGetPrimaryMonitor()
            val mode = GLFW.glfwGetVideoMode(monitor)
            if (mode != null) {
                GLFW.glfwSetWindowMonitor(targetWindow, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate())
            }
        } else {
            GLFW.glfwSetWindowMonitor(
                targetWindow,
                MemoryUtil.NULL,
                windowedX,
                windowedY,
                windowedWidth,
                windowedHeight,
                GLFW.GLFW_DONT_CARE
            )
        }
    }

    fun run() {
        GLFWErrorCallback.createPrint(System.err).set()

        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        val baseTitle = "Coozy Scenes"
        window = GLFW.glfwCreateWindow(1080, 720, baseTitle, MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) throw RuntimeException("Failed to create the GLFW window")

        GLFW.glfwSetKeyCallback(window) { win: Long, key: Int, scancode: Int, action: Int, mods: Int ->
            handleKeyCallback(win, key, scancode, action, mods)
        }

        GLFW.glfwSetFramebufferSizeCallback(window) { _, width, height ->
            GL11.glViewport(0, 0, width, height)
        }

        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED)

        GLFW.glfwSetCursorPosCallback(window) { win, xpos, ypos ->
            this.handleMousePosCallback(win, xpos, ypos)
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            val vidmode: GLFWVidMode = checkNotNull(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()))
            windowedX = (vidmode.width() - pWidth.get(0)) / 2
            windowedY = (vidmode.height() - pHeight.get(0)) / 2
            windowedWidth = pWidth.get(0)
            windowedHeight = pHeight.get(0)

            GLFW.glfwSetWindowPos(window, windowedX, windowedY)
        }

        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwSwapInterval(1)

        GLFW.glfwShowWindow(window)

        GL.createCapabilities()

        GL11.glEnable(GL11.GL_DEPTH_TEST)

        handleInit()

        var lastFrame = GLFW.glfwGetTime().toFloat()
        var fpsTimer = 0.0f
        var frameCounter = 0

        while (!GLFW.glfwWindowShouldClose(window)) {
            val currentFrame = GLFW.glfwGetTime().toFloat()
            val deltaTime = currentFrame - lastFrame
            lastFrame = currentFrame

            frameCounter++
            fpsTimer += deltaTime
            if (fpsTimer >= 1.0f) {
                val fps = frameCounter / fpsTimer
                val msPerFrame = (fpsTimer / frameCounter) * 1000.0f
                GLFW.glfwSetWindowTitle(
                    window,
                    String.format(Locale.US, "%s | FPS: %.0f (%.2f ms)", baseTitle, fps, msPerFrame)
                )
                frameCounter = 0
                fpsTimer = 0.0f
            }

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

            handleLoop(deltaTime)

            GLFW.glfwSwapBuffers(window)
            GLFW.glfwPollEvents()
        }

        handleCleanup()

        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)?.free()
    }
}