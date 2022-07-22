package cga.exercise.game

import cga.exercise.components.camera.TronCamera
import cga.exercise.components.geometry.Material
import cga.exercise.components.geometry.Mesh
import cga.exercise.components.geometry.Renderable
import cga.exercise.components.geometry.VertexAttribute
import cga.exercise.components.light.PointLight
import cga.exercise.components.light.SpotLight
import cga.exercise.components.shader.ShaderProgram
import cga.exercise.components.texture.CubemapTexture
import cga.exercise.components.texture.Texture2D
import cga.framework.GLError
import cga.framework.GameWindow
import cga.framework.ModelLoader
import cga.framework.OBJLoader
import org.joml.Math
import org.joml.Math.*
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.ARBFramebufferSRGB.GL_FRAMEBUFFER_SRGB
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30


/**
 * Created by Fabian on 16.09.2017.
 */
class Scene(private val window: GameWindow) {
    private val staticShader: ShaderProgram
    private val tronShader: ShaderProgram
    private val skyboxShader: ShaderProgram
    private val negativeShader: ShaderProgram

    private var shaderInUse: ShaderProgram

    private val meshListSphere = mutableListOf<Mesh>()
    private val meshListSphere2 = mutableListOf<Mesh>()
    private val meshListGround = mutableListOf<Mesh>()
    val bodenmatrix: Matrix4f = Matrix4f()
    val kugelMatrix: Matrix4f = Matrix4f()

    val ground: Renderable
    val sphere: Renderable
    var cycle : Renderable



    var speed:Float=1f
    var zahl:Int=200

    val sphere2:Renderable
    val sphere3:Renderable
    val sphere4:Renderable
    val sphere5:Renderable
    val sphere6:Renderable
    val sphere7:Renderable
    val sphere8:Renderable

    private val grounds = ArrayList<Renderable?>()
    private var groundZPos = 0f


    val camera = TronCamera()

    val pointLight : PointLight
    val pointLight2 : PointLight
    val pointLight3 : PointLight
    val pointLight4 : PointLight
    val pointLight5 : PointLight

    val spotLight: SpotLight
    val spotLight1: SpotLight
    val spotLight2: SpotLight
    val spotLight3: SpotLight
    val spotLight4: SpotLight
    val spotLight5: SpotLight

    //MouseParam
    var notFirstFrame = false
    var oldMousePosX = 0.0
    var oldMousePosY = 0.0


    // Define Vertices and Indices of Cubemap
    private var size: Float = 50000.0f
    private var skyboxVertices: FloatArray = floatArrayOf(
        -size, -size, size,
        size, -size, size,
        size, -size, -size,
        -size, -size, -size,
        -size, size, size,
        size, size, size,
        size, size, -size,
        -size, size, -size
    )

    private var skyboxIndices: IntArray = intArrayOf(
        //right
        1, 2, 6,
        6, 5, 1,
        //left
        0, 4, 7,
        7, 3, 0,
        //top
        4, 5, 6,
        6, 7, 4,
        //bottom
        0, 3, 2,
        2, 1, 0,
        //back
        0, 1, 5,
        5, 4, 0,
        //front
        3, 7, 6,
        6, 2, 3
    )

    private var cubeMap = CubemapTexture(skyboxVertices, skyboxIndices)
    private var cubeMapTexture = glGenTextures()



    private var direction = 0.0f
    private var difference: Vector3f = Vector3f(0.0f, 0.0f, 0.0f)

    //scene setup
    init {
        skyboxShader = ShaderProgram("assets/shaders/skyBoxVert.glsl", "assets/shaders/skyBoxFrag.glsl")
        staticShader = ShaderProgram("assets/shaders/simple_vert.glsl", "assets/shaders/simple_frag.glsl")
        tronShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")
        negativeShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/negative_frag.glsl")

        shaderInUse = tronShader
        //initial opengl state

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); GLError.checkThrow()
        glDisable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW)
        glCullFace(GL_BACK)
        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()

        //-------------------------------------CubeMap--------------------------------------------

        // Loading Cubemap faces
        val facesCubeMap: ArrayList<String> = arrayListOf()
        facesCubeMap.addAll(
            listOf(
                "assets/textures/Yokohama3/negz.jpg",
                "assets/textures/Yokohama3/posz.jpg",
                "assets/textures/Yokohama3/posy.jpg",
                "assets/textures/Yokohama3/negy.jpg",
                "assets/textures/Yokohama3/posx.jpg",
                "assets/textures/Yokohama3/negx.jpg"
            )
        )

        cubeMapTexture = cubeMap.loadCubeMap(facesCubeMap)

        val objResSphere : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/sphere.obj")
        val objMeshListSphere : MutableList<OBJLoader.OBJMesh> = objResSphere.objects[0].meshes
        val objMeshListSphere2 : MutableList<OBJLoader.OBJMesh> = objResSphere.objects[0].meshes

        val objResGround : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/groundFinal.obj")
        val objMeshListGround : MutableList<OBJLoader.OBJMesh> = objResGround.objects[0].meshes

        val stride = 8 * 4
        val attrPos = VertexAttribute(3, GL_FLOAT, stride, 0)
        val attrTC = VertexAttribute(2, GL_FLOAT, stride, 3 * 4)
        val attrNorm = VertexAttribute(3, GL_FLOAT, stride, 5 * 4)

        val vertexAttributes = arrayOf(attrPos,attrTC, attrNorm)

        val groundEmitTexture = Texture2D("assets/textures/ground_emit.png", true)
        val groundDiffTexture = Texture2D("assets/textures/ground_diff.png", true)
        val groundSpecTexture = Texture2D("assets/textures/ground_spec.png", true)

        groundEmitTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundDiffTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundSpecTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

        val groundShininess = 60f
        val groundTCMultiplier = Vector2f(15f,100f)


        val groundMaterial = Material(groundDiffTexture, groundEmitTexture, groundSpecTexture, groundShininess,
                groundTCMultiplier)

        val sphereEmitTexture = Texture2D("assets/textures/sphere_emit.png", true)
        val sphere2EmitTexture = Texture2D("assets/textures/sphere2_emit.png", true)
        val sphereDiffTexture = Texture2D("assets/textures/sphere_diff.png", true)
        val sphereSpecTexture = Texture2D("assets/textures/sphere_spec.png", true)

        val sphereShininess = 1f
        val sphereTCMultiplier = Vector2f(1f)

        val sphereMaterial = Material(sphereDiffTexture, sphereEmitTexture, sphereSpecTexture, sphereShininess,
            sphereTCMultiplier)

        val sphereMaterial2 = Material(sphereDiffTexture, sphere2EmitTexture, sphereSpecTexture, sphereShininess,
            sphereTCMultiplier)

        for (mesh in objMeshListSphere) {
            meshListSphere.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes,sphereMaterial))
        }

        for (mesh in objMeshListSphere2) {
            meshListSphere2.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes,sphereMaterial2))
        }

        for (mesh in objMeshListGround) {
            meshListGround.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes, groundMaterial))
        }

        bodenmatrix.scale(30f)
        bodenmatrix.rotateX(90f)



        kugelMatrix.scale(0.5f)

        ground = Renderable(meshListGround)
        sphere = Renderable(meshListSphere)
        sphere2 = Renderable(meshListSphere)
        sphere3 = Renderable(meshListSphere)
        sphere4 = Renderable(meshListSphere)

        sphere5 = Renderable(meshListSphere2)
        sphere6 = Renderable(meshListSphere2)
        sphere7 = Renderable(meshListSphere2)
        sphere8 = Renderable(meshListSphere2)

        camera.rotateLocal(Math.toRadians(-35f),0f, 0f)
        camera.translateLocal(Vector3f(0f, 0f, 30f))

        //camera.rotateLocal(Math.toRadians(-15f),0f, 0f)
        //camera.translateLocal(Vector3f(0f, 0f, 8f))

        cycle = ModelLoader.loadModel("assets/light Cycle/Car/SCI_FRS_13_HD.obj",
                toRadians(0f), toRadians(180f), 0f)?: throw Exception("Renderable can't be NULL!")

        cycle.scaleLocal(Vector3f(0.09f))
        cycle.setPosition(0f,-50f,40f)
        camera.parent = cycle










        pointLight = PointLight(Vector3f(0f, 1f, 0f), Vector3f(1f, 0f, 0f),
                Vector3f(0.1f, 0.5f, 0.05f))
        pointLight2 = PointLight(Vector3f(0f, 1f, -10f), Vector3f(0f, 1f, 0f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight3 = PointLight(Vector3f(0f, 1f, -20f), Vector3f(0f, 0f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight4 = PointLight(Vector3f(0f, 1f, 10f), Vector3f(0f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight5 = PointLight(Vector3f(0f, 1f, 20f), Vector3f(1f, 1f, 0f),
            Vector3f(0.1f, 0.5f, 0.05f))

        spotLight = SpotLight(Vector3f(0f, 1f, -2f), Vector3f(1f,1f,0.6f),
                Vector3f(0.5f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        spotLight1 = SpotLight(Vector3f(-10f, 5f, -10f), Vector3f(1f,1f,1f),
            Vector3f(1f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        spotLight2 = SpotLight(Vector3f(10f, 5f, -10f), Vector3f(1f,1f,1f),
            Vector3f(0.5f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        spotLight3 = SpotLight(Vector3f(-10f, 5f, 10f), Vector3f(1f,1f,1f),
            Vector3f(0.5f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        spotLight4 = SpotLight(Vector3f(10f, 5f, 10f), Vector3f(1f,1f,1f),
            Vector3f(0.5f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        spotLight5 = SpotLight(Vector3f(-2f, 2f, 1f), Vector3f(0f,0f,1f),
            Vector3f(0.5f, 0.05f, 0.01f), Vector2f(toRadians(15f), toRadians(30f)))

        spotLight.rotateLocal(toRadians(-10f), PI.toFloat(),0f)
        spotLight1.rotateLocal(toRadians(90f),0f,0f)
        spotLight2.rotateLocal(toRadians(90f),0f,0f)
        spotLight3.rotateLocal(toRadians(90f),0f,0f)
        spotLight4.rotateLocal(toRadians(90f),0f,0f)
        spotLight5.rotateLocal(toRadians(85f), toRadians(0f),toRadians(0f))


        spotLight.parent = cycle
        spotLight5.parent= cycle


        sphere8.parent=sphere
        sphere7.parent=sphere2
        sphere6.parent=sphere3
        sphere5.parent=sphere4




        sphere5.scaleLocal(Vector3f(0.4f))
        sphere6.scaleLocal(Vector3f(0.4f))
        sphere7.scaleLocal(Vector3f(0.4f))
        sphere8.scaleLocal(Vector3f(0.4f))

        sphere.scaleLocal(Vector3f(2f))
        sphere2.scaleLocal(Vector3f(2f))
        sphere3.scaleLocal(Vector3f(2f))
        sphere4.scaleLocal(Vector3f(2f))
        sphere.setPosition(10f, 3f,10f)
        sphere2.setPosition(10f, 3f,-10f)
        sphere3.setPosition(-10f, 3f,10f)
        sphere4.setPosition(-10f, 3f,-10f)



        sphere5.setPosition(2f, 0f,2f)
        sphere6.setPosition(2f, 0f,2f)
        sphere7.setPosition(2f, 0f,2f)
        sphere8.setPosition(2f, 0f,2f)

        ground.setPosition(0f,-50f,-0f)
    }

    fun render(dt: Float, t: Float) {
       //println(cycle.getPosition())

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        // -----------------------rendering Skybox----------------------------
        glDepthFunc(GL_LEQUAL)
        skyboxShader.use()




        skyboxShader.setUniform("view", camera.getCalculateViewMatrix(), false)
        skyboxShader.setUniform("projection", camera.getCalculateProjectionMatrix(), false)


        GL30.glBindVertexArray(cubeMap.skyboxVAO)
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        glBindTexture(GL30.GL_TEXTURE_CUBE_MAP, cubeMapTexture)
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0)

        GL30.glBindVertexArray(0);
        glDepthFunc(GL_LESS);



        shaderInUse.use()



        staticShader.setUniform("farbe", Vector3f(0f,1f,0f))

        camera.bind(shaderInUse)


        cycle.render(shaderInUse)



        for(ground in grounds){
            this.ground.render(shaderInUse)
        }












        shaderInUse.setUniform("farbe", Vector3f(0.5f,0.5f,0.5f))


        ground.render(shaderInUse)
        var differenz= ground.getPosition().z() -cycle.getPosition().z()



        if (differenz>= (ground.getWorldZAxis().z()+45f)) {
            ground.setPosition(ground.getPosition().x(), ground.getPosition().y(), cycle.getPosition().z()-45f)
            speed+=1

        }
        //println("1 ${cycle.getPosition().z()}")
       // println("2 ${ground.getPosition().z()}")
      //  println(differenz)





    }




    fun update(dt: Float, t: Float) {

     cycle.translateGlobal(Vector3f(0f, 0f, speed * -dt))

     when {
         window.getKeyState(GLFW_KEY_A) -> {
             println(cycle.getPosition().x())
             if (cycle.getPosition().x()>-7) {
                 cycle.translateLocal(Vector3f(100 * -dt, 0f, 0f))
             }
         }
         window.getKeyState(GLFW_KEY_D) -> {
             if (cycle.getPosition().x()<7) {
                 cycle.translateLocal(Vector3f(100 * dt, 0f, 0f))
             }
         }
     }
        //---------------------Handle shader switching--------------------------------
        if (window.getKeyState(GLFW_KEY_1)) {
            shaderInUse = tronShader
        }
        if (window.getKeyState(GLFW_KEY_2)) {
            shaderInUse = negativeShader
        }
 }





 //       when {
 //           window.getKeyState(GLFW_KEY_W) -> {
 //               if (window.getKeyState(GLFW_KEY_A)) {
//
 //                   cycle.rotateLocal(0f,1.5f * dt,0f)
//
//
 //               }
 //               if (window.getKeyState(GLFW_KEY_D)) {
//
 //                   cycle.rotateLocal(0f, 1.5f * -dt,0f)
//
 //               }
 //               cycle.translateLocal(Vector3f(0f, 0f, 50 * -dt))
 //           }
 //           window.getKeyState(GLFW_KEY_S) -> {
 //               if (window.getKeyState(GLFW_KEY_A)) {
 //                   cycle.rotateLocal(0f,1.5f * dt,0f)
 //               }
 //               if (window.getKeyState(GLFW_KEY_D)) {
 //                   cycle.rotateLocal(0f, 1.5f * -dt,0f)
 //               }
 //               cycle.translateLocal(Vector3f(0f, 0f, 2f * dt))
 //           }
 //       }
//


        fun spawnGround() {
            var newRing = ground




                grounds.add(newRing)
                grounds[grounds.size - 1]?.translateLocal(
                    Vector3f(
                        0f,
                        0f,
                        cycle.getPosition().z()
                    )
                )




        }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    fun onMouseMove(xpos: Double, ypos: Double) {

        val deltaX = xpos - oldMousePosX
        //var deltaY = ypos - oldMousePosY

        oldMousePosX = xpos
       // oldMousePosY = ypos

        if(notFirstFrame) {
            camera.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))
            //camera.rotateAroundPoint(toRadians(deltaY.toFloat()*0.05f), 0f,0f,camera.getXAxis())
        }

        notFirstFrame = true


    }

    fun cleanup() {}
}
