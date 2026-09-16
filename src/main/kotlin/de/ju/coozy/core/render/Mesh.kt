package de.ju.coozy.core.render

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import kotlin.FloatArray
import kotlin.Int
import kotlin.IntArray

class Mesh(vertices: FloatArray, indices: IntArray) {

    private val vao: Int = GL30.glGenVertexArrays()
    private val vbo: Int = GL15.glGenBuffers()
    private val ebo: Int = GL15.glGenBuffers()
    private val elementCount: Int = indices.size

    init {
        GL30.glBindVertexArray(vao)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW)

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW)

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0L)
        GL20.glEnableVertexAttribArray(0)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)
    }

    fun draw() {
        GL30.glBindVertexArray(vao)
        GL11.glDrawElements(GL11.GL_TRIANGLES, elementCount, GL11.GL_UNSIGNED_INT, 0)
        GL30.glBindVertexArray(0)
    }

    fun destroy() {
        GL30.glDeleteVertexArrays(vao)
        GL15.glDeleteBuffers(vbo)
        GL15.glDeleteBuffers(ebo)
    }

}