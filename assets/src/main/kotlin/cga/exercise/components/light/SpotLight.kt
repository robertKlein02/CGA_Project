package cga.exercise.components.light

import cga.exercise.components.shader.ShaderProgram
import cga.framework.GLError
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f

class SpotLight(pos : Vector3f, color : Vector3f, attParam: Vector3f, var angle: Vector2f) :
        PointLight(pos, color, attParam), ISpotLight {


    override fun bind(shaderProgram: ShaderProgram, name: String, viewMatrix: Matrix4f) {
        super.bind(shaderProgram,name); GLError.checkThrow()
        shaderProgram.setUniform(name + "LightAngle", angle); GLError.checkThrow()
        shaderProgram.setUniform(name + "LightDir", getWorldZAxis().negate().mul(Matrix3f(viewMatrix)))//1
    }
}