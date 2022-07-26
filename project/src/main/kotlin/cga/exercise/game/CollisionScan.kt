package cga.exercise.game

import cga.exercise.components.geometry.Renderable
import kotlin.math.abs

class CollisionScan {
    companion object{
        fun checkCollision(obj1 : Renderable?, obj2 : Renderable?) : Boolean {
            var distance = obj1?.getPosition()?.sub(obj2?.getPosition())?.length()
            return distance!! <= 40f
        }

        fun randtreffer(obj1: Renderable?, obj2: Renderable?) = !checkCollision(obj1, obj2) && abs(obj1!!.getPosition().z.toInt() - obj2!!.getPosition().z.toInt()) < 2f
    }
}