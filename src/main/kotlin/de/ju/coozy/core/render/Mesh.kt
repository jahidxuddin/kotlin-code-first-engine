package de.ju.coozy.core.render

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil

class Mesh(vertices: FloatArray, indices: IntArray) {

    private val vaoId: Int = GL30.glGenVertexArrays()
    private val vboId: Int = GL15.glGenBuffers()
    private val eboId: Int = GL15.glGenBuffers()
    val vertexCount: Int = indices.size

    init {
        GL30.glBindVertexArray(vaoId)

        val vboBuffer = MemoryUtil.memAllocFloat(vertices.size).apply {
            put(vertices)
            flip()
        }
        try {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vboBuffer, GL15.GL_STATIC_DRAW)
        } finally {
            MemoryUtil.memFree(vboBuffer)
        }

        val eboBuffer = MemoryUtil.memAllocInt(indices.size).apply {
            put(indices)
            flip()
        }
        try {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId)
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, eboBuffer, GL15.GL_STATIC_DRAW)
        } finally {
            MemoryUtil.memFree(eboBuffer)
        }

        val stride = 8 * java.lang.Float.BYTES // 32 Bytes pro Vertex

        // Location 0: Position (x, y, z)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0L)
        GL20.glEnableVertexAttribArray(0)

        // Location 1: Normale (nx, ny, nz)
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, (3 * java.lang.Float.BYTES).toLong())
        GL20.glEnableVertexAttribArray(1)

        // Location 2: UV (u, v)
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, stride, (6 * java.lang.Float.BYTES).toLong())
        GL20.glEnableVertexAttribArray(2)

        GL30.glBindVertexArray(0)
    }

    fun draw() {
        GL30.glBindVertexArray(vaoId)
        GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0L)
        GL30.glBindVertexArray(0)
    }

    fun destroy() {
        GL30.glDeleteVertexArrays(vaoId)
        GL15.glDeleteBuffers(vboId)
        GL15.glDeleteBuffers(eboId)
    }
}