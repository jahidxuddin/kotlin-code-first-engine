package de.ju.coozy.ecs.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.joml.Quaternionf
import org.joml.Vector3f

data class Transform(var position: Vector3f, var size: Vector3f, var rotation: Quaternionf) : Component<Transform> {
    override fun type() = Transform

    companion object : ComponentType<Transform>()
}