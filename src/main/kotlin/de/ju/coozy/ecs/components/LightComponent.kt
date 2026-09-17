package de.ju.coozy.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.joml.Vector3f

data class PointLightComponent(
    val color: Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
    var intensity: Float = 1.0f,
    var constant: Float = 1.0f,
    var linear: Float = 0.09f,
    var quadratic: Float = 0.032f
) : Component<PointLightComponent> {
    override fun type() = PointLightComponent
    companion object : ComponentType<PointLightComponent>()
}

data class DirectionalLightComponent(
    val direction: Vector3f = Vector3f(-0.2f, -1.0f, -0.3f).normalize(),
    val color: Vector3f = Vector3f(1.0f, 0.95f, 0.8f),
    var intensity: Float = 0.8f
) : Component<DirectionalLightComponent> {
    override fun type() = DirectionalLightComponent
    companion object : ComponentType<DirectionalLightComponent>()
}