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
    private val normalShader: ShaderProgram

    private var shaderInUse: ShaderProgram

    private val meshListBlock = mutableListOf<Mesh>()
    private val meshListCurb = mutableListOf<Mesh>()
    private val meshListGround = mutableListOf<Mesh>()

    val bodenmatrix: Matrix4f = Matrix4f()
    val kugelMatrix: Matrix4f = Matrix4f()

    val ground: Renderable
    val blockLeft: Renderable
    val blockRight:Renderable
    var cycle : Renderable
    var wheel:Renderable
    val curbLeft:Renderable
    val curbRight:Renderable


    var speed:Float=3f
    var zahl:Int=200




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
        normalShader = ShaderProgram("assets/shaders/normal_vert.glsl", "assets/shaders/normal_frag.glsl")
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

        val objResBlock : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/block.obj")
        val objMeshListBlock : MutableList<OBJLoader.OBJMesh> = objResBlock.objects[0].meshes

        val objResCurb : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/curb.obj")
        val objMeshListCurb : MutableList<OBJLoader.OBJMesh> = objResCurb.objects[0].meshes

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

        val blockEmitTexture = Texture2D("assets/models/block/Standardmaterial_baseColor.jpg", true)
        val blockDiffTexture = Texture2D("assets/models/block/Standardmaterial_baseColor.jpg", true)
        val blockSpecTexture = Texture2D("assets/models/block/Standardmaterial_baseColor.jpg", true)

        val blockShininess = 1f
        val blockTCMultiplier = Vector2f(15f,2f)

        val blockMaterial = Material(blockDiffTexture, blockEmitTexture, blockSpecTexture, blockShininess,
            blockTCMultiplier)

        val curbEmitTexture = Texture2D("assets/textures/ground_diff.png", true)
        val curbDiffTexture = Texture2D("assets/textures/ground_diff.png", true)
        val curbSpecTexture = Texture2D("assets/textures/ground_diff.png", true)

        val curbShininess = 1f
        val curbTCMultiplier = Vector2f(15f,100f)

        val curbMaterial = Material(curbDiffTexture, curbEmitTexture, curbSpecTexture, curbShininess,
            curbTCMultiplier)



        for (mesh in objMeshListCurb) {
            meshListCurb.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes,curbMaterial))
        }

        for (mesh in objMeshListBlock) {
            meshListBlock.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes,blockMaterial))
        }


        for (mesh in objMeshListGround) {
            meshListGround.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes, groundMaterial))
        }

        bodenmatrix.scale(30f)
        bodenmatrix.rotateX(90f)



        kugelMatrix.scale(0.5f)

        ground = Renderable(meshListGround)
        blockLeft = Renderable(meshListBlock)
        blockRight = Renderable(meshListBlock)

        curbRight = Renderable(meshListCurb)
        curbLeft = Renderable(meshListCurb)



        camera.rotateLocal(Math.toRadians(-20f),0f, 0f)
        camera.translateLocal(Vector3f(0f, 0f, 20f))

        //camera.rotateLocal(Math.toRadians(-15f),0f, 0f)
        //camera.translateLocal(Vector3f(0f, 0f, 8f))

        cycle = ModelLoader.loadModel("assets/light Cycle/Car/SCI_FRS_13_HD.obj",
                toRadians(0f), toRadians(180f), 0f)?: throw Exception("Renderable can't be NULL!")

        wheel = ModelLoader.loadModel("assets/wheel.obj",
            toRadians(0f), toRadians(180f), 0f)?: throw Exception("Renderable can't be NULL!")

        cycle.scaleLocal(Vector3f(0.09f))


        cycle.setPosition(0f,-50f,40f)
        wheel.scaleLocal(Vector3f(0.2f))
        wheel.rotateLocal(0f, toRadians(0f),0f)
        camera.parent = cycle
        wheel.parent=cycle


        wheel.setPosition(-3f,0f,-5f)










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






        blockLeft.setPosition(-11f, -50f,0f)
        blockRight.setPosition(11f, -50f,0f)
        curbLeft.setPosition(-8.5f, -50f,0f)
        curbRight.setPosition(8.5f, -50f,0f)





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


        blockLeft.render(shaderInUse)
        blockRight.render(shaderInUse)
        curbRight.render(shaderInUse)
        curbLeft.render(shaderInUse)

        staticShader.setUniform("farbe", Vector3f(1f,1f,1f))

        camera.bind(shaderInUse)

        wheel.render(shaderInUse)
        cycle.render(shaderInUse)



        for(ground in grounds){
            this.ground.render(shaderInUse)
        }












        shaderInUse.setUniform("farbe", Vector3f(0.3f,0.3f,0.3f))


        ground.render(shaderInUse)
        var differenz= ground.getPosition().z() -cycle.getPosition().z()



        if (differenz>= (ground.getWorldZAxis().z()+45f)) {
            ground.setPosition(ground.getPosition().x(), ground.getPosition().y(), cycle.getPosition().z()-45f)
            blockLeft.setPosition(blockLeft.getPosition().x(), ground.getPosition().y(), ground.getPosition().z())
            blockRight.setPosition(blockRight.getPosition().x(), ground.getPosition().y(), ground.getPosition().z())
            curbLeft.setPosition(curbLeft.getPosition().x(), curbLeft.getPosition().y(), ground.getPosition().z())
            curbRight.setPosition(curbRight.getPosition().x(), curbRight.getPosition().y(), ground.getPosition().z())
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
             if (cycle.getPosition().x()>-7.1) {
                 cycle.translateLocal(Vector3f(speed*20 * -dt, 0f, 0f))
             }
         }
         window.getKeyState(GLFW_KEY_D) -> {
             if (cycle.getPosition().x()<7.1) {
                 cycle.translateLocal(Vector3f(speed*20 * dt, 0f, 0f))
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
        if (window.getKeyState(GLFW_KEY_3)) {
            shaderInUse = normalShader
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
