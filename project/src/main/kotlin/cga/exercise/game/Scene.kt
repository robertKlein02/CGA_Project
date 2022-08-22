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
    private val toonShader: ShaderProgram
    private var shaderInUse: ShaderProgram

    private val meshListBlock = mutableListOf<Mesh>()
    private val meshListCurb = mutableListOf<Mesh>()
    private val meshListGround = mutableListOf<Mesh>()
    private val meshListHindernis= mutableListOf<Mesh>()
    private val meshListStar= mutableListOf<Mesh>()
    private val meshListSpeedup= mutableListOf<Mesh>()



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
    val speedup: Renderable


    var hindernis1=Hindernis()
    var hindernis2=Hindernis()
    var hindernis3=Hindernis()
    var hindernis4=Hindernis()
    var hindernis5=Hindernis()

    val star1=Star()
    val star2=Star()
    val star3=Star()
    val star4=Star()
    val star5=Star()


    val camera = TronCamera()
    val firstPersonCamera = TronCamera()
    val gameOverCamera =TronCamera()
    val topviewcamera=TronCamera()
    private var activeCamera = camera



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




    //scene setup
    init {
        // GAME INSTRUCTION
        println("\n")
        println("############################ Controls #######################\n")
        println("Press 'A' oder 'D' to move left or right")
        println("Press 'SPACE' for reset")
        println("Press '4' or '5' to change the camera perspective.")
        println("Press '1' or '2' to switch between shaders.\n")

        println("########################### How to play #####################\n")
        println("Try to collect all the stars and avoid the stones")
        println("Have fun!\n")
        println("########################### Start ###########################\n")

        skyboxShader = ShaderProgram("assets/shaders/skyBoxVert.glsl", "assets/shaders/skyBoxFrag.glsl")
        staticShader = ShaderProgram("assets/shaders/simple_vert.glsl", "assets/shaders/simple_frag.glsl")
        tronShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/tron_frag.glsl")
        negativeShader = ShaderProgram("assets/shaders/tron_vert.glsl", "assets/shaders/negative_frag.glsl")
        normalShader = ShaderProgram("assets/shaders/normal_vert.glsl", "assets/shaders/normal_frag.glsl")
        toonShader = ShaderProgram("assets/shaders/toon_vert.glsl", "assets/shaders/toon_frag.glsl")
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

        var objResspeedup: OBJLoader.OBJResult = OBJLoader.loadOBJ("assets/models/speedup.obj")
        val objMeshlistSpeedup: MutableList<OBJLoader.OBJMesh> = objResspeedup.objects[0].meshes

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


        val speedupEmit = Texture2D("assets/textures/speedup_emit3.jpg", true)
        val speedupDiff = Texture2D("assets/textures/speedup_emit3.jpg", true)
        val speedupSpec = Texture2D("assets/textures/speedup_emit3.jpg", true)


        val stride = 8 * 4
        val attrPos = VertexAttribute(3, GL_FLOAT, stride, 0)
        val attrTC = VertexAttribute(2, GL_FLOAT, stride, 3 * 4)
        val attrNorm = VertexAttribute(3, GL_FLOAT, stride, 5 * 4)
        val vertexAttributes = arrayOf(attrPos,attrTC, attrNorm)

        groundEmitTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundDiffTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        groundSpecTexture.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

        speedupEmit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        speedupDiff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        speedupSpec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

        starEmit.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        starDiff.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)
        starSpec.setTexParams(GL_REPEAT, GL_REPEAT, GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR)

        val groundShininess = 60f
        val groundTCMultiplier = Vector2f(2f,20f)
        val groundTCMHindernuis= Vector2f(1f,1f)

        val blockShininess = 1f
        val blockTCMultiplier = Vector2f(15f,2f)

        val curbShininess = 1f
        val curbTCMultiplier = Vector2f(15f,100f)

        val speedupShininess = 10f
        val speedupTCMultiplier = Vector2f(1f,1f)


        val groundMaterial = Material(groundDiffTexture, groundEmitTexture, groundSpecTexture, groundShininess,
                groundTCMultiplier)


        val blockMaterial = Material(blockDiffTexture, blockEmitTexture, blockSpecTexture, blockShininess,
            blockTCMultiplier)


        val curbMaterial = Material(curbDiffTexture, curbEmitTexture, curbSpecTexture, curbShininess,
            curbTCMultiplier)

        val hindernisMaterial = Material(groundDiffTexture, groundDiffTexture, groundSpecTexture, curbShininess,
            groundTCMHindernuis)

        val starMaterial = Material(starDiff, starEmit, starSpec, 40.0f, Vector2f(1.0f))

        val speedupMaterial = Material(speedupDiff,speedupEmit,speedupSpec,speedupShininess, speedupTCMultiplier)



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

        for (mesh in objMeshlistSpeedup) {
            meshListSpeedup.add(Mesh(mesh.vertexData, mesh.indexData, vertexAttributes, speedupMaterial))
        }


        ground = Renderable(meshListGround)
        blockLeft = Renderable(meshListBlock)
        blockRight = Renderable(meshListBlock)
        curbRight = Renderable(meshListCurb)
        curbLeft = Renderable(meshListCurb)
        speedup = Renderable(meshListSpeedup)


        //---------------------------------------Camera------------------------------------------

        //thirdPerson Camera
        camera.rotateLocal(toRadians(-20f),0f, 0f)
        camera.translateLocal(Vector3f(0f, 0f, 20f))

        //firstPerson Camera
        firstPersonCamera.rotateLocal(toRadians(-10f),0f, 0f)
        firstPersonCamera.translateLocal(Vector3f(0f, 4.5f, 2.5f))

        //Vogelperspektive Camera
        topviewcamera.rotateLocal(toRadians(-45f),0f, 0f)
        topviewcamera.translateLocal(Vector3f(0f, 0f, 40f))

        //gameover Camera
        gameOverCamera.rotateLocal(toRadians(30f), toRadians(180f), 0f)
        gameOverCamera.translateLocal(Vector3f(0f, 4.5f, 15f))


        //---------------------------------------ModelLoader------------------------------------------
        car = ModelLoader.loadModel("assets/light Cycle/Car/SCI_FRS_13_HD.obj",
                toRadians(0f), toRadians(180f), 0f)?: throw Exception("Renderable can't be NULL!")




        //---------------------------------------Scalieren------------------------------------------

        car.scaleLocal(Vector3f(0.09f))


        //---------------------------------------Parent------------------------------------------

        camera.parent = car
        firstPersonCamera.parent = car
        topviewcamera.parent =car
        gameOverCamera.parent=car


        //---------------------------------------SetPosition------------------------------------------
        car.setPosition(0f,-50f,40f)
        blockLeft.setPosition(-11f, -50f,0f)
        blockRight.setPosition(11f, -50f,0f)
        curbLeft.setPosition(-8.5f, -50f,0f)
        curbRight.setPosition(8.5f, -50f,0f)

        ground.setPosition(0f,-50f,-0f)
        speedup.setPosition(0f,-48f,-45f)
        speedup.rotateLocal(toRadians(90f),0f, 0f)
        speedup.scaleLocal(Vector3f(0.5f,0.2f,0.2f))
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

        checkCollisionStar(star1)
        checkCollisionStar(star2)
        checkCollisionStar(star3)
        checkCollisionStar(star4)
        checkCollisionStar(star5)

        checkCollisionHindernis(hindernis1.hindernis)
        checkCollisionHindernis(hindernis2.hindernis)
        checkCollisionHindernis(hindernis3.hindernis)
        checkCollisionHindernis(hindernis4.hindernis)
        checkCollisionHindernis(hindernis5.hindernis)

        shaderInUse.use()
        activeCamera.bind(shaderInUse)
        shaderInUse.setUniform("farbe",Vector3f(0.5f,0.5f,0.5f))


        star1.pointLight.bind(shaderInUse,"point")
        star2.pointLight.bind(shaderInUse,"point2")
        star3.pointLight.bind(shaderInUse,"point3")
        star4.pointLight.bind(shaderInUse,"point4")
        star5.pointLight.bind(shaderInUse,"point5")

        if (star1.eingesammelt==false){
            star1.star.render(shaderInUse)

        }
        if (star2.eingesammelt==false){
            star2.star.render(shaderInUse)

        }
        if (star3.eingesammelt==false){
            star3.star.render(shaderInUse)

        }
        if (star4.eingesammelt==false){
            star4.star.render(shaderInUse)

        }
        if (star5.eingesammelt==false){
            star5.star.render(shaderInUse)

        }


        


        star1.star.rotateLocal(0f,0f,star1.star.getPosition().y() *0.05f*dt)
        star2.star.rotateLocal(0f,0f,star2.star.getPosition().y() *0.05f*dt)
        star3.star.rotateLocal(0f,0f,star3.star.getPosition().y() *0.05f*dt)
        star4.star.rotateLocal(0f,0f,star4.star.getPosition().y() *0.05f*dt)
        star5.star.rotateLocal(0f,0f,star5.star.getPosition().y() *0.05f*dt)

        gameOverCamera.rotateAroundPoint(0f,car.getPosition().y() *0.01f*dt,0f, Vector3f(car.getPosition()))


        hindernis1.hindernis.render(shaderInUse)
        spotLight.bind(shaderInUse,"spot", activeCamera.getCalculateViewMatrix())
        hindernis2.hindernis.render(shaderInUse)
        spotLight2.bind(shaderInUse,"spot2", activeCamera.getCalculateViewMatrix())
        hindernis3.hindernis.render(shaderInUse)
        spotLight3.bind(shaderInUse,"spot3", activeCamera.getCalculateViewMatrix())
        hindernis4.hindernis.render(shaderInUse)
        spotLight4.bind(shaderInUse,"spot4", activeCamera.getCalculateViewMatrix())
        hindernis5.hindernis.render(shaderInUse)
        spotLight5.bind(shaderInUse,"spot5", activeCamera.getCalculateViewMatrix())

        shaderInUse.setUniform("farbe", Vector3f(0.4f,0.4f,0.4f))



        blockLeft.render(shaderInUse)
        blockRight.render(shaderInUse)
        curbRight.render(shaderInUse)
        curbLeft.render(shaderInUse)
        car.render(shaderInUse)
        speedup.render(shaderInUse)

        shaderInUse.setUniform("farbe", Vector3f(0.5f))


        ground.render(shaderInUse)


        var differenz= ground.getPosition().z() -car.getPosition().z()


        if (differenz>= (ground.getWorldZAxis().z()+45f)) {


            ground.setPosition(ground.getPosition().x(), ground.getPosition().y(), car.getPosition().z()-45f)
            blockLeft.setPosition(blockLeft.getPosition().x(), ground.getPosition().y(), ground.getPosition().z())
            blockRight.setPosition(blockRight.getPosition().x(), ground.getPosition().y(), ground.getPosition().z())
            curbLeft.setPosition(curbLeft.getPosition().x(), curbLeft.getPosition().y(), ground.getPosition().z())
            curbRight.setPosition(curbRight.getPosition().x(), curbRight.getPosition().y(), ground.getPosition().z())
            speedup.setPosition(speedup.getPosition().x(), speedup.getPosition().y(), ground.getPosition().z()-45f)

            star1.eingesammelt=false
            star2.eingesammelt=false
            star3.eingesammelt=false
            star4.eingesammelt=false
            star5.eingesammelt=false


            thisLevel+=1

        }
        //println("1 ${cycle.getPosition().z()}")
       // println("2 ${ground.getPosition().z()}")
      //  println(differenz)

    }


    fun hindernisBewegen(hindernis: Hindernis,dt: Float){

        if (hindernis.nachRechts){
            hindernis.hindernis.translateLocal(Vector3f(  0.5f*speed  * +dt,0f,0f))
            if (hindernis.hindernis.getPosition().x() >= 6.5f){
                hindernis.nachLinks=true
                hindernis.nachRechts=false
            }
        }

        if(hindernis.nachLinks){
            hindernis.hindernis.translateLocal(Vector3f(   0.5f*speed  * -dt,0f,0f))
            if (hindernis.hindernis.getPosition().x() <= -6.5f){
                hindernis.nachLinks=false
                hindernis.nachRechts=true
            }
        }
    }

    fun update(dt: Float, t: Float) {

        car.translateGlobal(Vector3f(0f, 0f, speed * -dt))

        hindernisBewegen(hindernis1,dt)
        hindernisBewegen(hindernis2,dt)
        hindernisBewegen(hindernis3,dt)
        hindernisBewegen(hindernis4,dt)
        hindernisBewegen(hindernis5,dt)



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
        if (window.getKeyState(GLFW_KEY_3)) {
            shaderInUse = toonShader
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
        if (window.getKeyState(GLFW_KEY_6)) {
            activeCamera = topviewcamera
        }
 }


    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {}

    fun gameReset(){
        println("NEW: Game")


        speed=5f
        points=0

        activeCamera=camera

        ground.setPosition(0f,-50f,0f)
        car.setPosition(0f,-50f,40f)
        blockLeft.setPosition(-11f, -50f,0f)
        blockRight.setPosition(11f, -50f,0f)
        curbLeft.setPosition(-8.5f, -50f,0f)
        curbRight.setPosition(8.5f, -50f,0f)
        speedup.setPosition(0f,-48f,-45f)

        star1.eingesammelt=false
        star2.eingesammelt=false
        star3.eingesammelt=false
        star4.eingesammelt=false
        star5.eingesammelt=false

        hindernis1.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-20)
        hindernis2.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-35)
        hindernis3.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-50)
        hindernis4.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-65)
        hindernis5.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-80)

        star1.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-12)
        star2.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-27)
        star3.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-42)
        star4.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-57)
        star5.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-72)

        star1.pointLight = PointLight(Vector3f(star1.star.getPosition().x(), star1.star.getPosition().y(), star1.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        star2.pointLight = PointLight(Vector3f(star2.star.getPosition().x(), star2.star.getPosition().y(), star2.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        star3.pointLight = PointLight(Vector3f(star3.star.getPosition().x(), star3.star.getPosition().y(), star3.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        star4.pointLight = PointLight(Vector3f(star4.star.getPosition().x(), star4.star.getPosition().y(), star4.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        star5.pointLight = PointLight(Vector3f(star5.star.getPosition().x(), star5.star.getPosition().y(), star5.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
    }




    fun spawnHindernis(){
        if (level==thisLevel){
            level+=1
            spwanStar()

            hindernis1.hindernis=Renderable(meshListHindernis)
            hindernis2.hindernis=Renderable(meshListHindernis)
            hindernis3.hindernis=Renderable(meshListHindernis)
            hindernis4.hindernis=Renderable(meshListHindernis)
            hindernis5.hindernis=Renderable(meshListHindernis)


            hindernis1.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-20)
            hindernis2.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-35)
            hindernis3.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-50)
            hindernis4.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-65)
            hindernis5.hindernis.setPosition(spurZufall(),-50f,car.getPosition().z()-80)



            spotLight = SpotLight(Vector3f(0f, 0f, -2f), Vector3f(1f,1f,1f),
                Vector3f(0.5f, 0.05f, 0.01f),Vector2f(toRadians(15f), toRadians(30f)))

            spotLight2 = SpotLight(Vector3f(0f, 0f, -2f), Vector3f(1f,1f,1f),
                Vector3f(0.5f, 0.05f, 0.01f),Vector2f(toRadians(15f), toRadians(30f)))

            spotLight3 = SpotLight(Vector3f(hindernis3.hindernis.getPosition().x(), hindernis3.hindernis.getPosition().y()+3, hindernis3.hindernis.getPosition().z()+2), Vector3f(1f,1f,1f),
                Vector3f(1f, 0.05f, 0.01f), Vector2f(toRadians(0f), toRadians(180f)))

            spotLight4 = SpotLight(Vector3f(hindernis4.hindernis.getPosition().x(), hindernis4.hindernis.getPosition().y()+3, hindernis4.hindernis.getPosition().z()+2), Vector3f(1f,1f,1f),
                Vector3f(1f, 0.05f, 0.01f), Vector2f(toRadians(0f), toRadians(-180f)))

            spotLight5 =  SpotLight(Vector3f(hindernis5.hindernis.getPosition().x(), hindernis5.hindernis.getPosition().y()+3, hindernis5.hindernis.getPosition().z()+2), Vector3f(1f,1f,1f),
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

        star1.star= Renderable(meshListStar)
        star2.star=Renderable(meshListStar)
        star3.star=Renderable(meshListStar)
        star4.star=Renderable(meshListStar)
        star5.star=Renderable(meshListStar)

        star1.star.rotateLocal(1.9f, 0f, 0.0f)
        star2.star.rotateLocal(1.9f, 0f, 0.0f)
        star3.star.rotateLocal(1.9f, 0f, 0.0f)
        star4.star.rotateLocal(1.9f, 0f, 0.0f)
        star5.star.rotateLocal(1.9f, 0f, 0.0f)

        star1.star.scaleLocal(Vector3f(0.05f))
        star2.star.scaleLocal(Vector3f(0.05f))
        star3.star.scaleLocal(Vector3f(0.05f))
        star4.star.scaleLocal(Vector3f(0.05f))
        star5.star.scaleLocal(Vector3f(0.05f))


        star1.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-12)
        star2.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-27)
        star3.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-42)
        star4.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-57)
        star5.star.setPosition(spurZufall(),-49.5f,car.getPosition().z()-72)

        // Pointlights
        star1.pointLight = PointLight(Vector3f(star1.star.getPosition().x(), star1.star.getPosition().y(), star1.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        star2.pointLight = PointLight(Vector3f(star2.star.getPosition().x(), star2.star.getPosition().y(), star2.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        star3.pointLight = PointLight(Vector3f(star3.star.getPosition().x(), star3.star.getPosition().y(), star3.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        star4.pointLight = PointLight(Vector3f(star4.star.getPosition().x(), star4.star.getPosition().y(), star4.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))
        star5.pointLight = PointLight(Vector3f(star5.star.getPosition().x(), star5.star.getPosition().y(), star5.star.getPosition().z()), Vector3f(1f, 1f, 1f),
            Vector3f(0.1f, 0.5f, 0.05f))

    }


    fun checkCollisionStar(star: Star) {

        var minimumDistanz=0.5f

        if (abs(star.star.getPosition().x() - car.getPosition().x()) < minimumDistanz  &&  abs(star.star.getPosition().z() - car.getPosition().z())  < minimumDistanz){
            if (star.eingesammelt==false){
                star.eingesammelt=true
                star.pointLight.setPosition(car.getPosition().x(),car.getPosition().y()+2,car.getPosition().z())
                points= points+1
                speed= speed+1
                println("$points Stars caught")
            }
        }

    }


    fun checkCollisionHindernis(renderable: Renderable){

        var minimumDistanz=1.2f

        if (abs(renderable.getPosition().x() - car.getPosition().x()) < minimumDistanz  &&  abs(renderable.getPosition().z() - car.getPosition().z())  < minimumDistanz){
            speed=0f
            activeCamera=gameOverCamera
        }
    }


    fun spurZufall():Float{

       var random = kotlin.random.Random.nextInt(0,8)
        if(random==0)return -1.17f
        if(random==1)return -2.925f
        if(random==2)return -4.59f
        if(random==3)return -6.5f
        if(random==4)return 1.17f
        if(random==5)return 2.925f
        if(random==1)return 4.59f
        else return 6.1f
    }


    fun onMouseMove(xpos: Double, ypos: Double) {
//
    //    val deltaX = xpos - oldMousePosX
    //    //var deltaY = ypos - oldMousePosY
//
    //    oldMousePosX = xpos
    //   // oldMousePosY = ypos
//
    //    if(notFirstFrame) {
    //        activeCamera.rotateAroundPoint(0f, toRadians(deltaX.toFloat() * 0.05f), 0f, Vector3f(0f))
    //        //camera.rotateAroundPoint(toRadians(deltaY.toFloat()*0.05f), 0f,0f,camera.getXAxis())
    //    }
//
    //    notFirstFrame = true
//

    }

    fun cleanup() {}
}
