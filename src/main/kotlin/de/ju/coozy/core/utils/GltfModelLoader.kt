package de.ju.coozy.core.utils

import de.ju.coozy.core.render.Material
import de.ju.coozy.core.render.Mesh
import de.ju.coozy.core.render.Model
import de.ju.coozy.core.render.SubMesh
import de.ju.coozy.core.render.Texture
import org.lwjgl.assimp.*
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

object GltfModelLoader {

    fun loadModel(modelId: String, resourcePath: String): Model {
        val stream = javaClass.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("GLB resource not found: $resourcePath")
        val bytes = stream.use { it.readAllBytes() }

        val byteBuffer = MemoryUtil.memAlloc(bytes.size).apply {
            put(bytes)
            flip()
        }

        val scene: AIScene
        try {
            scene = Assimp.aiImportFileFromMemory(
                byteBuffer,
                Assimp.aiProcess_Triangulate or
                        Assimp.aiProcess_GenNormals or
                        Assimp.aiProcess_JoinIdenticalVertices,
                ""
            ) ?: throw RuntimeException("Assimp failed to parse GLB: ${Assimp.aiGetErrorString()}")
        } finally {
            MemoryUtil.memFree(byteBuffer)
        }

        val loadedTextures = mutableMapOf<Int, Texture>()
        val numTextures = scene.mNumTextures()
        val texturesBuffer = scene.mTextures()

        if (texturesBuffer != null && numTextures > 0) {
            for (i in 0 until numTextures) {
                val aiTex = AITexture.create(texturesBuffer.get(i))
                if (aiTex.mHeight() == 0) {
                    val rawBuffer: ByteBuffer = aiTex.pcDataCompressed()
                    val texture = Texture.fromMemory(rawBuffer)
                    val texKey = "${modelId}_tex_$i"
                    AssetManager.registerTexture(texKey, texture)
                    loadedTextures[i] = texture
                }
            }
        }

        val materials = mutableListOf<Material>()
        val numMaterials = scene.mNumMaterials()
        val materialsBuffer = scene.mMaterials()

        if (materialsBuffer != null) {
            for (i in 0 until numMaterials) {
                val aiMat = AIMaterial.create(materialsBuffer.get(i))
                val path = AIString.calloc()

                val success = Assimp.aiGetMaterialTexture(
                    aiMat,
                    Assimp.aiTextureType_DIFFUSE,
                    0,
                    path,
                    null as IntArray?,
                    null,
                    null,
                    null,
                    null,
                    null
                )

                var diffuseTexture: Texture? = null
                if (success == Assimp.aiReturn_SUCCESS) {
                    val texPath = path.dataString()
                    if (texPath.startsWith("*")) {
                        val embeddedIndex = texPath.substring(1).toIntOrNull()
                        if (embeddedIndex != null) {
                            diffuseTexture = loadedTextures[embeddedIndex]
                        }
                    }
                }
                path.free()
                materials.add(Material(diffuseTexture = diffuseTexture))
            }
        }

        val subMeshes = mutableListOf<SubMesh>()
        val numMeshes = scene.mNumMeshes()
        val meshesBuffer = scene.mMeshes()

        if (meshesBuffer != null) {
            for (i in 0 until numMeshes) {
                val aiMesh = AIMesh.create(meshesBuffer.get(i))
                val meshKey = "${modelId}_mesh_$i"

                val vertices = extractVertices(aiMesh)
                val indices = extractIndices(aiMesh)

                val mesh = Mesh(vertices, indices)
                AssetManager.registerMesh(meshKey, mesh)

                val materialIndex = aiMesh.mMaterialIndex()
                val mat = if (materialIndex in materials.indices) materials[materialIndex] else Material()

                subMeshes.add(SubMesh(mesh = mesh, material = mat))
            }
        }

        Assimp.aiReleaseImport(scene)

        val model = Model(subMeshes)
        AssetManager.registerModel(modelId, model)
        return model
    }

    private fun extractVertices(aiMesh: AIMesh): FloatArray {
        val vertexCount = aiMesh.mNumVertices()
        val vertices = FloatArray(vertexCount * 8)

        val positions = aiMesh.mVertices()
        val normals = aiMesh.mNormals()
        val texCoords = aiMesh.mTextureCoords(0)

        for (i in 0 until vertexCount) {
            val offset = i * 8

            // Position (x, y, z)
            val pos = positions.get(i)
            vertices[offset + 0] = pos.x()
            vertices[offset + 1] = pos.y()
            vertices[offset + 2] = pos.z()

            // Normal (nx, ny, nz)
            if (normals != null) {
                val norm = normals.get(i)
                vertices[offset + 3] = norm.x()
                vertices[offset + 4] = norm.y()
                vertices[offset + 5] = norm.z()
            }

            // UV (u, v)
            if (texCoords != null) {
                val uv = texCoords.get(i)
                vertices[offset + 6] = uv.x()
                vertices[offset + 7] = uv.y()
            }
        }
        return vertices
    }

    private fun extractIndices(aiMesh: AIMesh): IntArray {
        val faceCount = aiMesh.mNumFaces()
        val indices = IntArray(faceCount * 3)
        val faces = aiMesh.mFaces()

        var idx = 0
        for (i in 0 until faceCount) {
            val face = faces.get(i)
            val numIndices = face.mNumIndices()
            val buf = face.mIndices()
            for (j in 0 until numIndices) {
                indices[idx++] = buf.get(j)
            }
        }
        return indices
    }

}