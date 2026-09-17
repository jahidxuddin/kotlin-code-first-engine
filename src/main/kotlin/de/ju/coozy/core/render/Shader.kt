package de.ju.coozy.core.render

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL41
import org.lwjgl.system.MemoryStack

class Shader(vertexShaderSourceCode: String, fragmentShaderSourceCode: String) {
    val id: Int

    init {
        val vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexShaderSourceCode)
        val fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderSourceCode)
        this.id = GL20.glCreateProgram()
        GL20.glAttachShader(this.id, vertexShader)
        GL20.glAttachShader(this.id, fragmentShader)
        GL20.glLinkProgram(this.id)
        GL20.glValidateProgram(this.id)
        GL20.glDeleteShader(vertexShader)
        GL20.glDeleteShader(fragmentShader)
    }

    private fun compileShader(shaderType: Int, shaderSourceCode: String): Int {
        val shaderId = GL20.glCreateShader(shaderType)
        GL20.glShaderSource(shaderId, shaderSourceCode)
        GL20.glCompileShader(shaderId)
        return shaderId
    }

    fun bind() {
        GL20.glUseProgram(this.id)
    }

    fun unbind() {
        GL20.glUseProgram(0)
    }

    private fun getUniformLocation(uniformName: String): Int {
        return GL20.glGetUniformLocation(this.id, uniformName)
    }

    fun setUniform(name: String, value: Float) {
        val location = getUniformLocation(name)
        if (location != -1) {
            GL41.glProgramUniform1f(this.id, location, value)
        }
    }

    fun setUniform(name: String, value: Double) {
        val location = getUniformLocation(name)
        if (location != -1) {
            GL41.glProgramUniform1d(this.id, location, value)
        }
    }

    fun setUniform(name: String, value: Matrix4f) {
        val location = getUniformLocation(name)
        if (location != -1) {
            MemoryStack.stackPush().use { stack ->
                val buffer = stack.mallocFloat(16)
                value.get(buffer)
                GL41.glProgramUniformMatrix4fv(this.id, location, false, buffer)
            }
        }
    }

    fun setUniform(name: String, value: Vector3f) {
        val location = getUniformLocation(name)
        if (location != -1) {
            GL41.glProgramUniform3f(this.id, location, value.x, value.y, value.z)
        }
    }

    fun setUniform(name: String, value: Int) {
        val location = getUniformLocation(name)
        if (location != -1) {
            GL41.glProgramUniform1i(this.id, location, value)
        }
    }

    fun destroy() {
        GL20.glDeleteProgram(this.id)
    }

}