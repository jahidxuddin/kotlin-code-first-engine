package de.ju.coozy.core.camera

import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class Camera {

    val position: Vector3f = Vector3f(0.0f, 0.0f, 3.0f)
    private val forward = Vector3f(0.0f, 0.0f, -1.0f)
    private val up = Vector3f(0.0f, 1.0f, 0.0f)
    private val right = Vector3f(1.0f, 0.0f, 0.0f)

    private var yaw = -90.0f
    private var pitch = 0.0f

    var isAttached: Boolean = false
        private set
    private val offset = Vector3f(0.0f, 1.5f, 4.0f)

    init {
        updateVectors()
    }

    fun updateVectors() {
        val dir = Vector3f()
        dir.x = (cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble()))).toFloat()
        dir.y = sin(Math.toRadians(pitch.toDouble())).toFloat()
        dir.z = (sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble()))).toFloat()
        forward.set(dir).normalize()

        forward.cross(0.0f, 1.0f, 0.0f, right).normalize()
        right.cross(forward, up).normalize()
    }

    fun getViewMatrix(target: Vector3f): Matrix4f {
        if (this.isAttached) {
            position.set(target).add(offset)
            return Matrix4f().lookAt(position, target, up)
        } else {
            val center = Vector3f(position).add(forward)
            return Matrix4f().lookAt(position, center, up)
        }
    }

    fun rotate(deltaYaw: Float, deltaPitch: Float) {
        yaw += deltaYaw
        pitch += deltaPitch

        if (pitch > 89.0f) pitch = 89.0f
        if (pitch < -89.0f) pitch = -89.0f

        updateVectors()
    }

    fun move(fwd: Float, str: Float, vtc: Float) {
        if (this.isAttached) return

        position.add(Vector3f(forward).mul(fwd))
        position.add(Vector3f(right).mul(str))
        position.y += vtc
    }

    fun toggleAttached() {
        this.isAttached = !this.isAttached
    }

}