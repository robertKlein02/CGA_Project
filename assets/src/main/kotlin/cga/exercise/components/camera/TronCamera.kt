package cga.exercise.components.camera

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Math
import org.joml.Matrix4f

class TronCamera (fov: Float = 90f, width: Float = 16f, height: Float = 9f, var nearPlane: Float = 0.1f,
                  var farPlane: Float = 100f ): Transformable(), ICamera {

    var fieldOfView: Float
    var aspectRatio: Float

    init {
        fieldOfView = Math.toRadians(fov)
        aspectRatio = width / height
    }

    override fun getCalculateViewMatrix(): Matrix4f {
        val view = Matrix4f()
        return view.lookAt(getWorldPosition(), getWorldPosition().sub(getWorldZAxis()), getWorldYAxis())

    }

    override fun getCalculateProjectionMatrix(): Matrix4f {
        val projection = Matrix4f()
        return projection.perspective(fieldOfView, aspectRatio, nearPlane, farPlane)

    }

    override fun bind(shader: ShaderProgram) {
        shader.setUniform("view", getCalculateViewMatrix(), false)
        shader.setUniform("projection", getCalculateProjectionMatrix(), false)
    }
}
