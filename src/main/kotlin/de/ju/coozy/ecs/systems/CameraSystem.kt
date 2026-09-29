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

        val yawRad = Math.toRadians(cam.yaw.toDouble())
        val pitchRad = Math.toRadians(cam.pitch.toDouble())

        if (cam.isAttached) {
            val distance = 7.0f
            val heightOffset = 1.5f

            val offsetX = (distance * cos(pitchRad) * cos(yawRad)).toFloat()
            val offsetY = (distance * sin(pitchRad)).toFloat()
            val offsetZ = (distance * cos(pitchRad) * sin(yawRad)).toFloat()

            val lookTarget = Vector3f(cam.target).add(0.0f, heightOffset, 0.0f)

            transform.position.set(lookTarget).add(offsetX, offsetY, offsetZ)
            cam.up.set(0.0f, 1.0f, 0.0f)
            cam.viewMatrix.identity().lookAt(transform.position, lookTarget, cam.up)
        } else {
            val dir = Vector3f(
                (cos(yawRad) * cos(pitchRad)).toFloat(),
                sin(pitchRad).toFloat(),
                (sin(yawRad) * cos(pitchRad)).toFloat()
            )
            cam.forward.set(dir).normalize()
            cam.forward.cross(0.0f, 1.0f, 0.0f, cam.right).normalize()
            cam.right.cross(cam.forward, cam.up).normalize()

            val center = Vector3f(transform.position).add(cam.forward)
            cam.viewMatrix.identity().lookAt(transform.position, center, cam.up)
        }
    }

}