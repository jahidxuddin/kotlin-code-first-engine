package de.ju.coozy.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import de.ju.coozy.ecs.components.Transform
import kotlin.math.sin

class MovementSystem : IteratingSystem(
    family { all(Transform) }
) {
    private var totalTime: Float = 0f

    override fun onTickEntity(entity: Entity) {
        val transform = entity[Transform]

        totalTime += deltaTime

        val rotationSpeed = Math.toRadians(45.0).toFloat() * deltaTime
        transform.rotation.rotateAxis(rotationSpeed, 0.5f, 1.0f, 0.0f).normalize()

        transform.position.x = sin(totalTime * 1.5f) * 2.0f
    }
}