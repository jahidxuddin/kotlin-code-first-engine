package de.ju.coozy.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import de.ju.coozy.core.render.Mesh

data class MeshComponent(val renderMesh: Mesh) : Component<MeshComponent> {
    override fun type() = MeshComponent

    companion object : ComponentType<MeshComponent>()
}