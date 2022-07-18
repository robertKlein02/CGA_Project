package cga.exercise.components.geometry

import cga.exercise.components.shader.ShaderProgram

class Renderable (val meshList: MutableList<Mesh>): Transformable() , IRenderable{

    override fun render(shaderProgram: ShaderProgram) {
        shaderProgram.setUniform("model_matrix", getWorldModelMatrix(), false)
        meshList.forEach { it.render(shaderProgram) }
    }

}