package de.ju.coozy.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

data class TransformComponent(
    val position: Vector3f = Vector3f(),
    val size: Vector3f = Vector3f(1f, 1f, 1f),
    val rotation: Quaternionf = Quaternionf(),
    val modelMatrix: Matrix4f = Matrix4f()
) : Component<TransformComponent> {
    override fun type() = TransformComponent

    companion object : ComponentType<TransformComponent>()
}