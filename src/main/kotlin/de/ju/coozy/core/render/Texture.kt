package de.ju.coozy.core.render

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

class Texture(val id: Int, val width: Int, val height: Int) {

    fun bind(unit: Int = 0) {
        glActiveTexture(GL_TEXTURE0 + unit)
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun destroy() {
        glDeleteTextures(id)
    }

    companion object {
        fun fromMemory(imageBytes: ByteBuffer): Texture {
            MemoryStack.stackPush().use { stack ->
                val w = stack.mallocInt(1)
                val h = stack.mallocInt(1)
                val channels = stack.mallocInt(1)

                stbi_set_flip_vertically_on_load(false)

                val buffer = stbi_load_from_memory(imageBytes, w, h, channels, 4)
                    ?: throw RuntimeException("Failed to decode embedded GLB texture: ${stbi_failure_reason()}")

                val textureId = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, textureId)

                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

                glTexImage2D(
                    GL_TEXTURE_2D, 0, GL_RGBA8, w.get(0), h.get(0), 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, buffer
                )
                glGenerateMipmap(GL_TEXTURE_2D)
                glBindTexture(GL_TEXTURE_2D, 0)

                stbi_image_free(buffer)
                return Texture(textureId, w.get(0), h.get(0))
            }
        }
    }

}