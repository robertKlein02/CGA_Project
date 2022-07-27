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
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30
import java.util.Random
import kotlin.math.roundToInt


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
    private val meshListHindernis= mutableListOf<Mesh>()
    private val meshListStar= mutableListOf<Mesh>()


    var speed:Float=5f
    var level:Int=1
    var thisLevel:Int=1
    var points=0

    val ground: Renderable
    val blockLeft: Renderable
    val blockRight:Renderable
    var car : Renderable
    val curbLeft:Renderable
    val curbRight:Renderable



    lateinit var hindernis1:Renderable
    lateinit var hindernis2:Renderable
    lateinit var hindernis3:Renderable
    lateinit var hindernis4:Renderable
    lateinit var hindernis5:Renderable


    lateinit var star1:Renderable
    lateinit var star2:Renderable
    lateinit var star3:Renderable
    lateinit var star4:Renderable
    lateinit var star5:Renderable

    var star1Eingesammelt=false
    var star2Eingesammelt=false
    var star3Eingesammelt=false
    var star4Eingesammelt=false
    var star5Eingesammelt=false


    val camera = TronCamera()
    val firstPersonCamera = TronCamera()
    private var activeCamera = camera

    lateinit var pointLight : PointLight
    lateinit var pointLight2 : PointLight
    lateinit var pointLight3 : PointLight
    lateinit var pointLight4 : PointLight
    lateinit var pointLight5 : PointLight

    lateinit var spotLight: SpotLight
    lateinit var spotLight2: SpotLight
    lateinit var spotLight3: SpotLight
    lateinit var spotLight4: SpotLight
    lateinit var spotLight5: SpotLight

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

        //---------------------------------------OBJ-Loader------------------------------------------

        val objResBlock : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/block.obj")
        val objMeshListBlock : MutableList<OBJLoader.OBJMesh> = objResBlock.objects[0].meshes

        val objResCurb : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/curb.obj")
        val objMeshListCurb : MutableList<OBJLoader.OBJMesh> = objResCurb.objects[0].meshes

        val objResGround : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/groundFinal.obj")
        val objMeshListGround : MutableList<OBJLoader.OBJMesh> = objResGround.objects[0].meshes

        val objResHindernis : OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/hindernis.obj")
        val objMeshListHindernis : MutableList<OBJLoader.OBJMesh> = objResHindernis.objects[0].meshes

        val resStar: OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/stars.obj")
        val objStar: MutableList<OBJLoader.OBJMesh> = resStar.objects[0].meshes

        val groundEmitTexture = Texture2D("assets/textures/str.png", true)
        val groundDiffTexture = Texture2D("assets/textures/ground_diff.png", true)
        val groundSpecTexture = Texture2D("assets/textures/ground_spec.jpg", true)

        val blockEmitTexture = Texture2D("assets/models/block/Standardmaterial_baseColor.jpg", true)
        val blockDiffTexture = Texture2D("assets/models/block/Standardmaterial_baseColor.jpg", true)
        val blockSpecTexture = Texture2D("assets/models/block/Standardmaterial_baseColor.jpg", true)

        val curbEmitTexture = Texture2D("assets/textures/ground_diff.png", true)
        val curbDiffTexture = Texture2D("assets/textures/ground_diff.png", true)
        val curbSpecTexture = Texture2D("assets/textures/ground_diff.png", true)

        val starEmit = Texture2D("assets/textures/StarColor3.png", true)
        val starDiff = Texture2D("assets/textures/StarColor3.png", true)
        val starSpec = Texture2D("assets/textures/StarColor3.png", true)


        val stride = 8 * 4
        val attrPos = VertexAttribute(3, GL_FLOAT, stride, 0)
        val attrTC = VertexAttribute(2, GL_FLOAT, stride, 3 * 4)
        val attrNorm = VertexAttribute(3, GL_FLOAT, stride, 5 * 4)
        val vertexAttributes = arrayOf(attrPos,attrTC, attrNorm)

        groundEmitTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundDiffTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundSpecTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

        starEmit.setTexParams(GL_REPEAT, GL_REPEAT, GL11.GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        starDiff.setTexParams(GL_REPEAT, GL_REPEAT, GL11.GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        starSpec.setTexParams(GL_REPEAT, GL_REPEAT, GL11.GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

        val groundShininess = 60f
        val groundTCMultiplier = Vector2f(2f,20f)
        val groundTCMHindernuis= Vector2f(1f,1f)

        val blockShininess = 1f
        val blockTCMultiplier = Vector2f(15f,2f)

        val curbShininess = 1f
        val curbTCMultiplier = Vector2f(15f,100f)


        val groundMaterial = Material(groundDiffTexture, groundEmitTexture, groundSpecTexture, groundShininess,
                groundTCMultiplier)


        val blockMaterial = Material(blockDiffTexture, blockEmitTexture, blockSpecTexture, blockShininess,
            blockTCMultiplier)


        val curbMaterial = Material(curbDiffTexture, curbEmitTexture, curbSpecTexture, curbShininess,
            curbTCMultiplier)

        val hindernisMaterial = Material(groundDiffTexture, groundDiffTexture, groundSpecTexture, curbShininess,
            groundTCMHindernuis)

        val starMaterial = Material(starDiff, starEmit, starSpec, 40.0f, Vector2f(1.0f))



        for (mesh in objMeshListCurb) {
            meshListCurb.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes,curbMaterial))
        }

        for (mesh in objMeshListBlock) {
            meshListBlock.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes,blockMaterial))
        }


        for (mesh in objMeshListGround) {
            meshListGround.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes, groundMaterial))
        }

        for (mesh in objMeshListHindernis) {
            meshListHindernis.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes, hindernisMaterial))
        }

        for (mesh in objStar) {
            meshListStar.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes, starMaterial))
        }


        ground = Renderable(meshListGround)
        blockLeft = Renderable(meshListBlock)
        blockRight = Renderable(meshListBlock)
        curbRight = Renderable(meshListCurb)
        curbLeft = Renderable(meshListCurb)


        //---------------------------------------Camera------------------------------------------

        //thirdPerson Camera
        camera.rotateLocal(Math.toRadians(-20f),0f, 0f)
        camera.translateLocal(Vector3f(0f, 0f, 20f))

        //firstPerson Camera
        firstPersonCamera.rotateLocal(Math.toRadians(-10f),0f, 0f)
        firstPersonCamera.translateLocal(Vector3f(0f, 4.5f, 2.5f))



        //---------------------------------------ModelLoader------------------------------------------
        car = ModelLoader.loadModel("assets/light Cycle/Car/SCI_FRS_13_HD.obj",
                toRadians(0f), toRadians(180f), 0f)?: throw Exception("Renderable can't be NULL!")




        //---------------------------------------Scalieren------------------------------------------

        car.scaleLocal(Vector3f(0.09f))


        //---------------------------------------Parent------------------------------------------

        camera.parent = car
        firstPersonCamera.parent = car


        //---------------------------------------SetPosition------------------------------------------
        car.setPosition(0f,-50f,40f)
        blockLeft.setPosition(-11f, -50f,0f)
        blockRight.setPosition(11f, -50f,0f)
        curbLeft.setPosition(-8.5f, -50f,0f)
        curbRight.setPosition(8.5f, -50f,0f)

        ground.setPosition(0f,-50f,-0f)
    }

    fun render(dt: Float, t: Float) {


        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        // -----------------------rendering Skybox----------------------------
        glDepthFunc(GL_LEQUAL)
        skyboxShader.use()


        skyboxShader.setUniform("view", activeCamera.getCalculateViewMatrix(), false)
        skyboxShader.setUniform("projection", activeCamera.getCalculateProjectionMatrix(), false)


        GL30.glBindVertexArray(cubeMap.skyboxVAO)
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        glBindTexture(GL30.GL_TEXTURE_CUBE_MAP, cubeMapTexture)
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0)

        GL30.glBindVertexArray(0);
        glDepthFunc(GL_LESS);

        spawnHindernis()
        checkCollisionStar()
        checkCollisionHindernis()
        shaderInUse.use()
        activeCamera.bind(shaderInUse)
        shaderInUse.setUniform("farbe",Vector3f(0.5f,0.5f,0.5f))

        pointLight.bind(shaderInUse,"point")
        pointLight2.bind(shaderInUse,"point2")
        pointLight3.bind(shaderInUse,"point3")
        pointLight4.bind(shaderInUse,"point4")
        pointLight5.bind(shaderInUse,"point5")


        if (star1Eingesammelt==false) star1.render(shaderInUse)
        if (star2Eingesammelt==false) star2.render(shaderInUse)
        if (star3Eingesammelt==false) star3.render(shaderInUse)
        if (star4Eingesammelt==false) star4.render(shaderInUse)
        if (star5Eingesammelt==false) star5.render(shaderInUse)


        star1.rotateLocal(0f,0f,star1.getPosition().y() *0.05f*dt)
        star2.rotateLocal(0f,0f,star2.getPosition().y() *0.05f*dt)
        star3.rotateLocal(0f,0f,star3.getPosition().y() *0.05f*dt)
        star4.rotateLocal(0f,0f,star4.getPosition().y() *0.05f*dt)
        star5.rotateLocal(0f,0f,star5.getPosition().y() *0.05f*dt)


        hindernis1.render(shaderInUse)
        spotLight.bind(shaderInUse,"spot", activeCamera.getCalculateViewMatrix())
        hindernis2.render(shaderInUse)
        spotLight2.bind(shaderInUse,"spot2", activeCamera.getCalculateViewMatrix())
        hindernis3.render(shaderInUse)
        spotLight3.bind(shaderInUse,"spot3", activeCamera.getCalculateViewMatrix())
        hindernis4.render(shaderInUse)
        spotLight4.bind(shaderInUse,"spot4", activeCamera.getCalculateViewMatrix())
        hindernis5.render(shaderInUse)
        spotLight5.bind(shaderInUse,"spot5", activeCamera.getCalculateViewMatrix())

        shaderInUse.setUniform("farbe", Vector3f(0.4f,0.4f,0.4f))



        blockLeft.render(shaderInUse)
        blockRight.render(shaderInUse)
        curbRight.render(shaderInUse)
        curbLeft.render(shaderInUse)
        car.render(shaderInUse)

        shaderInUse.setUniform("farbe", Vector3f(0.5f))





        ground.render(shaderInUse)
        var differenz= ground.getPosition().z() -car.getPosition().z()



        if (differenz>= (ground.getWorldZAxis().z()+45f)) {


            ground.setPosition(ground.getPosition().x(), ground.getPosition().y(), car.getPosition().z()-45f)
            blockLeft.setPosition(blockLeft.getPosition().x(), ground.getPosition().y(), ground.getPosition().z())
            blockRight.setPosition(blockRight.getPosition().x(), ground.getPosition().y(), ground.getPosition().z())
            curbLeft.setPosition(curbLeft.getPosition().x(), curbLeft.getPosition().y(), ground.getPosition().z())
            curbRight.setPosition(curbRight.getPosition().x(), curbRight.getPosition().y(), ground.getPosition().z())
            star1Eingesammelt=false
            star2Eingesammelt=false
            star3Eingesammelt=false
            star4Eingesammelt=false
            star5Eingesammelt=false

            speed+=3
            thisLevel+=1

        }
        //println("1 ${cycle.getPosition().z()}")
       // println("2 ${ground.getPosition().z()}")
      //  println(differenz)

    }


    fun update(dt: Float, t: Float) {

        car.translateGlobal(Vector3f(0f, 0f, speed * -dt))

     when {
         window.getKeyState(GLFW_KEY_A) -> {
             if (car.getPosition().x()>-7.1) {
                 car.translateLocal(Vector3f(speed*10 * -dt, 0f, 0f))

                 if(car.getPosition().x()<-7.1){                                                  // Bei schnelleren geschw. auto ansonsten in curb oder haus
                     car.setPosition(-7.1f,car.getPosition().y(),car.getPosition().z())
                 }
             }
         }
         window.getKeyState(GLFW_KEY_D) -> {
             if (car.getPosition().x()<7.1) {
                 car.translateLocal(Vector3f(speed*10 * dt, 0f, 0f))

                 if(car.getPosition().x()>7.1){                                                  // Bei schnelleren geschw. auto ansonsten in curb oder haus
                     car.setPosition(7.1f,car.getPosition().y(),car.getPosition().z())
                 }
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
        if (window.getKeyState(GLFW_KEY_SPACE)) {
            if (speed==0f) gameReset()
        }
        if (window.getKeyState(GLFW_KEY_4)) {
            activeCamera = firstPersonCamera
        }
        if (window.getKeyState(GLFW_KEY_5)) {
            activeCamera = camera
        }

 }


    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    fun gameReset(){
        println("NEW: Game")

        speed=5f
        points=0

        ground.setPosition(0f,-50f,0f)
        car.setPosition(0f,-50f,40f)
        blockLeft.setPosition(-11f, -50f,0f)
        blockRight.setPosition(11f, -50f,0f)
        curbLeft.setPosition(-8.5f, -50f,0f)
        curbRight.setPosition(8.5f, -50f,0f)

        star1Eingesammelt=false
        star2Eingesammelt=false
        star3Eingesammelt=false
        star4Eingesammelt=false
        star5Eingesammelt=false

        hindernis1.setPosition(spurZufall(),-50f,car.getPosition().z()-20)
        hindernis2.setPosition(spurZufall(),-50f,car.getPosition().z()-35)
        hindernis3.setPosition(spurZufall(),-50f,car.getPosition().z()-50)
        hindernis4.setPosition(spurZufall(),-50f,car.getPosition().z()-65)
        hindernis5.setPosition(spurZufall(),-50f,car.getPosition().z()-80)
        
        star1.setPosition(spurZufall(),-49.5f,car.getPosition().z()-27)
        star2.setPosition(spurZufall(),-49.5f,car.getPosition().z()-42)
        star3.setPosition(spurZufall(),-49.5f,car.getPosition().z()-57)
        star4.setPosition(spurZufall(),-49.5f,car.getPosition().z()-72)
        star5.setPosition(spurZufall(),-49.5f,car.getPosition().z()-12)

        pointLight = PointLight(Vector3f(star1.getPosition().x(), star1.getPosition().y(), star1.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight2 = PointLight(Vector3f(star2.getPosition().x(), star2.getPosition().y(), star2.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight3 = PointLight(Vector3f(star3.getPosition().x(), star3.getPosition().y(), star3.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight4 = PointLight(Vector3f(star4.getPosition().x(), star4.getPosition().y(), star4.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight5 = PointLight(Vector3f(star5.getPosition().x(), star5.getPosition().y(), star5.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
    }



    fun spawnHindernis(){
        if (level==thisLevel){
            level+=1
            spwanStar()

            hindernis1=Renderable(meshListHindernis)
            hindernis2=Renderable(meshListHindernis)
            hindernis3=Renderable(meshListHindernis)
            hindernis4=Renderable(meshListHindernis)
            hindernis5=Renderable(meshListHindernis)


            hindernis1.setPosition(spurZufall(),-50f,car.getPosition().z()-20)
            hindernis2.setPosition(spurZufall(),-50f,car.getPosition().z()-35)
            hindernis3.setPosition(spurZufall(),-50f,car.getPosition().z()-50)
            hindernis4.setPosition(spurZufall(),-50f,car.getPosition().z()-65)
            hindernis5.setPosition(spurZufall(),-50f,car.getPosition().z()-80)



            spotLight = SpotLight(Vector3f(0f, 0f, -2f), Vector3f(1f,1f,1f),
                Vector3f(0.5f, 0.05f, 0.01f),Vector2f(toRadians(15f), toRadians(30f)))

            spotLight2 = SpotLight(Vector3f(0f, 0f, -2f), Vector3f(1f,1f,1f),
                Vector3f(0.5f, 0.05f, 0.01f),Vector2f(toRadians(15f), toRadians(30f)))

            spotLight3 = SpotLight(Vector3f(hindernis3.getPosition().x(), hindernis3.getPosition().y()+3, hindernis3.getPosition().z()+2), Vector3f(1f,1f,1f),
                Vector3f(1f, 0.05f, 0.01f), Vector2f(toRadians(0f), toRadians(180f)))

            spotLight4 = SpotLight(Vector3f(hindernis4.getPosition().x(), hindernis4.getPosition().y()+3, hindernis4.getPosition().z()+2), Vector3f(1f,1f,1f),
                Vector3f(1f, 0.05f, 0.01f), Vector2f(toRadians(0f), toRadians(-180f)))

            spotLight5 =  SpotLight(Vector3f(hindernis5.getPosition().x(), hindernis5.getPosition().y()+3, hindernis5.getPosition().z()+2), Vector3f(1f,1f,1f),
                Vector3f(1f, 0.05f, 0.01f), Vector2f(toRadians(0f), toRadians(90f)))

            spotLight.rotateLocal(toRadians(-10f), PI.toFloat(),0f)
            spotLight2.rotateLocal(toRadians(-10f), PI.toFloat(),0f)
            spotLight2.setPosition(-1f,0f,0f)
            spotLight.setPosition(1f,0f,0f)
            spotLight.parent=car
            spotLight2.parent=car


        }

    }

    fun spwanStar(){

        star1= Renderable(meshListStar)
        star2=Renderable(meshListStar)
        star3=Renderable(meshListStar)
        star4=Renderable(meshListStar)
        star5=Renderable(meshListStar)

        star1.rotateLocal(1.9f, 0f, 0.0f)
        star2.rotateLocal(1.9f, 0f, 0.0f)
        star3.rotateLocal(1.9f, 0f, 0.0f)
        star4.rotateLocal(1.9f, 0f, 0.0f)
        star5.rotateLocal(1.9f, 0f, 0.0f)

        star1.scaleLocal(Vector3f(0.05f))
        star2.scaleLocal(Vector3f(0.05f))
        star3.scaleLocal(Vector3f(0.05f))
        star4.scaleLocal(Vector3f(0.05f))
        star5.scaleLocal(Vector3f(0.05f))



        star1.setPosition(spurZufall(),-49.5f,car.getPosition().z()-27)
        star2.setPosition(spurZufall(),-49.5f,car.getPosition().z()-42)
        star3.setPosition(spurZufall(),-49.5f,car.getPosition().z()-57)
        star4.setPosition(spurZufall(),-49.5f,car.getPosition().z()-72)
        star5.setPosition(spurZufall(),-49.5f,car.getPosition().z()-12)

        // Pointlights
        pointLight = PointLight(Vector3f(star1.getPosition().x(), star1.getPosition().y(), star1.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight2 = PointLight(Vector3f(star2.getPosition().x(), star2.getPosition().y(), star2.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight3 = PointLight(Vector3f(star3.getPosition().x(), star3.getPosition().y(), star3.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight4 = PointLight(Vector3f(star4.getPosition().x(), star4.getPosition().y(), star4.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        pointLight5 = PointLight(Vector3f(star5.getPosition().x(), star5.getPosition().y(), star5.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))

    }


    fun checkCollisionStar() {

        if (abs(star1.getPosition().x() - car.getPosition().x()) < 0.5f  &&  abs(star1.getPosition().z() - car.getPosition().z())  < 0.5f){
            if (star1Eingesammelt==false){
                star1Eingesammelt=true
                points= points+1
                println(points)
            }
        }
        if (abs(star2.getPosition().x() - car.getPosition().x()) < 0.5f  &&  abs(star2.getPosition().z() - car.getPosition().z()) < 0.5f){
            if (star2Eingesammelt==false){
                star2Eingesammelt=true
                points= points+1
                println(points)
            }
        }
        if (abs(star3.getPosition().x() - car.getPosition().x()) < 0.5f  &&  abs(star3.getPosition().z() - car.getPosition().z()) < 0.5f){
            if (star3Eingesammelt==false){
                star3Eingesammelt=true
                points= points+1
                println(points)
            }
        }
        if (abs(star4.getPosition().x() - car.getPosition().x()) < 0.5f  &&  abs(star4.getPosition().z() - car.getPosition().z()) < 0.5f){
            if (star4Eingesammelt==false){
                star4Eingesammelt=true
                points= points+1
                println(points)
            }
        }
        if (abs(star5.getPosition().x() - car.getPosition().x()) < 0.5f  &&  abs(star5.getPosition().z()- car.getPosition().z())<0.5f){
            if (star5Eingesammelt==false){
                star5Eingesammelt=true
                points= points+1
                println(points)
            }
        }
    }


    fun checkCollisionHindernis(){

        if (abs(hindernis1.getPosition().x() - car.getPosition().x()) < 1.2f  &&  abs(hindernis1.getPosition().z() - car.getPosition().z())  < 1.2f) speed=0f
        if (abs(hindernis2.getPosition().x() - car.getPosition().x()) < 1.2f  &&  abs(hindernis2.getPosition().z() - car.getPosition().z())  < 1.2f) speed=0f
        if (abs(hindernis3.getPosition().x() - car.getPosition().x()) < 1.2f  &&  abs(hindernis3.getPosition().z() - car.getPosition().z())  < 1.2f) speed=0f
        if (abs(hindernis4.getPosition().x() - car.getPosition().x()) < 1.2f  &&  abs(hindernis4.getPosition().z() - car.getPosition().z())  < 1.2f) speed=0f
        if (abs(hindernis5.getPosition().x() - car.getPosition().x()) < 1.2f  &&  abs(hindernis5.getPosition().z() - car.getPosition().z())  < 1.2f) speed=0f
    }


    fun spurZufall():Float{

       var random = kotlin.random.Random.nextInt(0,8)
        if(random==0)return -1.17f
        if(random==1)return -2.925f
        if(random==2)return -4.59f
        if(random==3)return -6.5f
        if(random==4)return 1.17f
        if(random==5)return 2.925f
        if(random==6)return 4.59f
        else return 6.5f
    }


    fun onMouseMove(xpos: Double, ypos: Double) {

        val deltaX = xpos - oldMousePosX
        //var deltaY = ypos - oldMousePosY

        oldMousePosX = xpos
       // oldMousePosY = ypos

        if(notFirstFrame) {
            activeCamera.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))
            //camera.rotateAroundPoint(toRadians(deltaY.toFloat()*0.05f), 0f,0f,camera.getXAxis())
        }

        notFirstFrame = true


    }

    fun cleanup() {}
}
