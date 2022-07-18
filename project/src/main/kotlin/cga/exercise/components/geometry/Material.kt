package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import org.joml.Vector2f
import org.lwjgl.opengl.GL13.*

class Material(var diff: Texture2D,
               var emit: Texture2D,
               var specular: Texture2D,
               var shininess: Float = 50.0f,
               var tcMultiplier : Vector2f = Vector2f(1.0f)){

    fun bind(shaderProgram: ShaderProgram) {
        var i = 0
        diff.bind(i)
        shaderProgram.setUniform("diff", i++)
        emit.bind(i)
        shaderProgram.setUniform("emit", i++)
        specular.bind(i)
        shaderProgram.setUniform("specular", i)
        shaderProgram.setUniform("tcMultiplier", tcMultiplier)
        shaderProgram.setUniform("shininess", shininess)
    }
}