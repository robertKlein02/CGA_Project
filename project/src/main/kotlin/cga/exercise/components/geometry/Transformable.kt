package cga.exercise.components.geometry

import org.joml.Matrix4f
import org.joml.Vector3f

open class Transformable : ITransformable {

    private var modelMatrix = Matrix4f()
    var parent : Transformable? = null

    override fun rotateLocal(pitch: Float, yaw: Float, roll: Float) {
        modelMatrix.rotateXYZ(pitch, yaw, roll)
    }

    override fun rotateAroundPoint(pitch: Float, yaw: Float, roll: Float, altMidpoint: Vector3f) {
        val rotate = Matrix4f()
        rotate.translate(altMidpoint)
        rotate.rotateXYZ(pitch, yaw, roll)
        rotate.translate(Vector3f(altMidpoint).negate())
        modelMatrix = rotate.mul(modelMatrix)
    }

    override fun translateLocal(deltaPos: Vector3f) {
        modelMatrix.translate(deltaPos)
    }

    override fun translateGlobal(deltaPos: Vector3f) {
        val world = Matrix4f()
        world.translate(deltaPos)
        modelMatrix = world.mul(modelMatrix)
    }

    override fun scaleLocal(scale: Vector3f) {
        modelMatrix.scale(scale)
    }

    override fun getPosition(): Vector3f = Vector3f(modelMatrix.m30(), modelMatrix.m31(), modelMatrix.m32())

    override fun getWorldPosition(): Vector3f {
        val world = getWorldModelMatrix()
        return Vector3f(world.m30(), world.m31(), world.m32())
    }

    override fun getXAxis(): Vector3f = Vector3f(modelMatrix.m00(), modelMatrix.m01(), modelMatrix.m02()).normalize()

    override fun getYAxis(): Vector3f = Vector3f(modelMatrix.m10(), modelMatrix.m11(), modelMatrix.m12()).normalize()

    override fun getZAxis(): Vector3f = Vector3f(modelMatrix.m20(), modelMatrix.m21(), modelMatrix.m22()).normalize()

    override fun getWorldXAxis(): Vector3f {
        val world = getWorldModelMatrix()
        return Vector3f(world.m00(), world.m01(), world.m02()).normalize()
    }

    override fun getWorldYAxis(): Vector3f {
        val world = getWorldModelMatrix()
        return Vector3f(world.m10(), world.m11(), world.m12()).normalize()
    }

    override fun getWorldZAxis(): Vector3f {
        val world = getWorldModelMatrix()
        return Vector3f(world.m20(), world.m21(), world.m22()).normalize()
    }

    override fun getWorldModelMatrix(): Matrix4f {
        val mat = Matrix4f(modelMatrix)
        parent?.getWorldModelMatrix()?.mul(modelMatrix, mat)
        return mat
    }

    override fun getLocalModelMatrix(): Matrix4f = Matrix4f(modelMatrix)

    fun setPosition(x: Float, y: Float, z: Float) {
        //model_matrix = Matrix4f().translate(Vector3f(x,y,z))
        modelMatrix.setTranslation(x,y,z)
    }
}
