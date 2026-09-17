package de.ju.coozy.ecs.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import de.ju.coozy.ecs.components.CameraComponent
import de.ju.coozy.ecs.components.TransformComponent
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class CameraSystem : IteratingSystem(
    family { all(CameraComponent, TransformComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val cam = entity[CameraComponent]
        val transform = entity[TransformComponent]

        val dir = Vector3f(
            (cos(Math.toRadians(cam.yaw.toDouble())) * cos(Math.toRadians(cam.pitch.toDouble()))).toFloat(),
            sin(Math.toRadians(cam.pitch.toDouble())).toFloat(),
            (sin(Math.toRadians(cam.yaw.toDouble())) * cos(Math.toRadians(cam.pitch.toDouble()))).toFloat()
        )
        cam.forward.set(dir).normalize()
        cam.forward.cross(0.0f, 1.0f, 0.0f, cam.right).normalize()
        cam.right.cross(cam.forward, cam.up).normalize()

        if (cam.isAttached) {
            transform.position.set(cam.target).add(cam.offset)
            cam.viewMatrix.identity().lookAt(transform.position, cam.target, cam.up)
        } else {
            val center = Vector3f(transform.position).add(cam.forward)
            cam.viewMatrix.identity().lookAt(transform.position, center, cam.up)
        }
    }
}