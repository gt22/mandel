package mandel

import javafx.animation.AnimationTimer
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseButton.PRIMARY
import javafx.scene.input.MouseButton.SECONDARY
import javafx.scene.paint.Color
import mandel.Styles.Companion.rootClass
import tornadofx.*
import java.util.stream.IntStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sqrt

fun main() = launch<Appl>()


class Appl : App(MainView::class, Styles::class)

class MainView : View() {

    val zoomPerSec = 0.5
    private val mLeft = -2.5
    private val mRight = 1.5
    private val mBot = -1.5
    private val mTop = 1.5
    val dynamicIter = 200
    val staticIter = 1000
    var curIter = staticIter
    val alwaysStaticIterWidth = 0.0005
    private val colorFactor = 1000 / staticIter
    private val colors = Array(staticIter) { i ->
        val c = i * colorFactor
        Color.rgb((c * c) % 255, c % 255, c % 255)
    }

    var cLeft = mLeft
    var cRight = mRight
    private var cBot = mBot
    private var cTop = mTop

    private lateinit var c: Canvas

    var zoomX = -1.0
    var zoomY = -1.0
    var zoomDir = 0

    override val root = vbox {
        primaryStage.width = 800.0
        primaryStage.height = 600.0
        addClass(rootClass)
        c = canvas {
            widthProperty().bind(primaryStage.widthProperty())
            heightProperty().bind(primaryStage.heightProperty())
        }
        setOnMousePressed {
            when(it.button) {
                PRIMARY -> zoomDir = 1
                SECONDARY -> zoomDir = -1
                else -> {}
            }
            if (cRight - cLeft > alwaysStaticIterWidth) {
                curIter = dynamicIter
            }
        }
        setOnMouseReleased {
            zoomDir = 0
            curIter = staticIter
            draw()
        }
        setOnMouseMoved {
            zoomX = it.sceneX
            zoomY = it.sceneY
        }
        setOnMouseDragged {
            zoomX = it.sceneX
            zoomY = it.sceneY
        }
    }

    private fun renorm(i: Double, sl: Double, sr: Double, tl: Double, tr: Double) =
        (((i - sl) / sr) * (tr - tl)) + tl


    private fun toMandelCoords(x: Double, y: Double): Pair<Double, Double> {
        return renorm(x, 0.0, c.width, cLeft, cRight) to
                renorm(y, 0.0, c.height, cBot, cTop)
    }

    fun zoomOut(x: Double, y: Double, z: Double) {
        val (mx, my) = toMandelCoords(x, y)
        val z1 = 1 - z
        val ox = mx * z
        val oy = my * z
        cLeft = (cLeft - ox) / z1
        cRight = (cRight - ox) / z1
        cBot = (cBot - oy) / z1
        cTop = (cTop - oy) / z1

        if (cLeft < mLeft) {
            cLeft = mLeft
        }
        if(cRight > mRight) {
            cRight = mRight
        }
        if (cBot < mBot) {
            cBot = mBot
        }
        if (cTop > mTop) {
            cTop = mTop
        }
    }

    fun zoomIn(x: Double, y: Double, z: Double) {
        val (mx, my) = toMandelCoords(x, y)
        cLeft -= (cLeft - mx) * z
        cRight -= (cRight - mx) * z
        cBot -= (cBot - my) * z
        cTop -= (cTop - my) * z
    }

    private fun cardioidCheck(a: Double, b: Double): Boolean {
        val xh = a - 0.25
        val rho = sqrt(xh * xh + b * b)
        val theta = atan2(b, a - 0.25)
        val rhoC = (1 - cos(theta)) / 2
        return rho <= rhoC
    }

    private fun checkPoint(a: Double, b: Double): Int {
        if (a * a + b * b > 4) {
            return 0
        }
        if (cardioidCheck(a, b)) {
            return curIter
        }

        var i = 1

        var re = a
        var im = b
        while (i < curIter) {
            val rsq = re * re
            val isq = im * im
            if (rsq + isq > 4) {
                return i
            }
            im *= re //im = im * re
            im += im + b // im = 2 * im * re + b
            re = rsq - isq + a

            i++
        }
        return i
    }

    fun draw() {
        with(c.graphicsContext2D.pixelWriter) {
            val w = c.width.toInt()
            val h = c.height.toInt()
            val iter = Array(w) { Array(h) { 0 } }
            IntStream.range(0, w).parallel()
                .mapToObj(Integer::valueOf)
                .flatMap { i -> IntStream.range(0, h).mapToObj {j -> i to j} }
                .forEach {
                    val (i, j) = it
                    val (a, b) = toMandelCoords(i.toDouble(), j.toDouble())
                    val mandel = checkPoint(a, b)
                    iter[i][j] = mandel
                }
            for (i in 0 until w) {
                for (j in 0 until h) {
                    val mandel = iter[i][j]
                    val c = if (mandel == curIter) Color.BLACK else colors[mandel]
                    setColor(i, j, c)
                }
            }
        }
    }

    init {
        draw()
        ZoomTimer(this).start()
    }

}

class ZoomTimer(private val v: MainView) : AnimationTimer() {
    
    private var prev = System.nanoTime()
    
    override fun handle(now: Long) {
        val dt = (now.toDouble() - prev) / (1000 * 1000 * 1000)
        prev = now
        with(v) {
            if (zoomDir != 0) {
                val w = cRight - cLeft
                if (curIter == dynamicIter && w < alwaysStaticIterWidth) {
                    curIter = staticIter
                } else if (curIter == staticIter && w > alwaysStaticIterWidth) {
                    curIter = dynamicIter
                }
                when (zoomDir) {
                    1 -> zoomIn(zoomX, zoomY, zoomPerSec * dt)
                    -1 -> zoomOut(zoomX, zoomY, zoomPerSec * dt)
                    else -> {
                    }
                }
                draw()
            }
        }
    }

}

class Styles : Stylesheet() {

    companion object {
        val rootClass by cssclass()
    }

    init {

    }

}