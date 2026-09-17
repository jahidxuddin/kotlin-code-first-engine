package de.ju.coozy.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.joml.Matrix4f
import org.joml.Vector3f

data class CameraComponent(
    var yaw: Float = -90.0f,
    var pitch: Float = 0.0f,
    val forward: Vector3f = Vector3f(0.0f, 0.0f, -1.0f),
    val up: Vector3f = Vector3f(0.0f, 1.0f, 0.0f),
    val right: Vector3f = Vector3f(1.0f, 0.0f, 0.0f),

    var isAttached: Boolean = false,
    val offset: Vector3f = Vector3f(0.0f, 1.5f, 4.0f),
    val target: Vector3f = Vector3f(0.0f, 0.0f, 0.0f),

    val viewMatrix: Matrix4f = Matrix4f(),
    val projectionMatrix: Matrix4f = Matrix4f(),
    var fov: Float = 45.0f,
    var near: Float = 0.1f,
    var far: Float = 100.0f
) : Component<CameraComponent> {
    override fun type() = CameraComponent

    companion object : ComponentType<CameraComponent>()
}