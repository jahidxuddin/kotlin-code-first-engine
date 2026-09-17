package de.ju.coozy.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class MeshComponent(
    var meshId: String = "cube"
) : Component<MeshComponent> {
    override fun type() = MeshComponent

    companion object : ComponentType<MeshComponent>()
}