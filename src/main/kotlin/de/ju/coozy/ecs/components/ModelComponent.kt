package de.ju.coozy.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ModelComponent(val modelId: String) : Component<ModelComponent> {
    override fun type() = ModelComponent

    companion object : ComponentType<ModelComponent>()
}