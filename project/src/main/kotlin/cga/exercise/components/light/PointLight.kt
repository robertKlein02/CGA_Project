package cga.exercise.components.light

import cga.exercise.components.geometry.Transformable
import cga.exercise.components.shader.ShaderProgram
import org.joml.Vector3f

open class PointLight (var lightPosition : Vector3f,
                       var lightColor: Vector3f, var attParam : Vector3f): Transformable(), IPointLight {

    init {
        translateGlobal(lightPosition)   // 1
    }

    override fun bind(shaderProgram: ShaderProgram, name: String) {
        shaderProgram.setUniform(name + "LightPos",getWorldPosition())  // 2
        shaderProgram.setUniform(name + "LightColor", lightColor)
        shaderProgram.setUniform(name + "LightAttParam", attParam)
    }
}

//