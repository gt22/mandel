package mandel

import javafx.animation.AnimationTimer
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseButton.PRIMARY
import javafx.scene.input.MouseButton.SECONDARY
import javafx.scene.paint.Color
import mandel.Styles.Companion.rootClass
import tornadofx.*

fun main() = launch<Appl>()


class Appl : App(MainView::class, Styles::class)

class MainView : View() {

    val zoomPerSec = 0.5
    val mLeft = -2.5
    val mRight = 1.5
    val mBot = -1.5
    val mTop = 1.5
    val dynamicIter = 100
    val staticIter = 250
    var curIter = staticIter
    val alwaysStaticIterWidth = 0.0005
    val colorFactor
        get() = 1000 / curIter


    var cLeft = mLeft
    var cRight = mRight
    var cBot = mBot
    var cTop = mTop

    lateinit var c: Canvas

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
//        setOnMouseClicked {
//            zoomIn(it.sceneX, it.sceneY, 0.1)
//            zoomOut(it.sceneX, it.sceneY, 0.1)
//        }
    }

    fun renorm(i: Double, sl: Double, sr: Double, tl: Double, tr: Double) =
        (((i - sl) / sr) * (tr - tl)) + tl


    fun toMandelCoords(x: Double, y: Double): Pair<Double, Double> {
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
        val z1 = 1 - z
        val ox = mx * z
        val oy = my * z
        cLeft = cLeft * z1 + ox
        cRight = cRight * z1 + ox
        cBot = cBot * z1 + oy
        cTop = cTop * z1 + oy
    }

    fun checkPoint(a: Double, b: Double): Int {

        var i = 0
        val c = a + b.j
        var z = 0.j
        while (i < curIter) {
            z = z.tSquare + c
            if (z.normSquare > 4) {
                return i
            }
            i++
        }
        return i
    }

    fun draw() {
        with(c.graphicsContext2D.pixelWriter) {
            val w = c.width.toInt() - 1
            val h = c.height.toInt() - 1
            val hist = Array(curIter + 1) { 0 }
            val iters = Array(w + 1) { Array(h + 1) { curIter } }
            var total = 0
            val wd = w.toDouble()
            val hd = h.toDouble()
            for (i in 0..w) {

                val a = renorm(i.toDouble(), 0.0, wd, cLeft, cRight)
                for (j in 0..h) {
                    val b = renorm(j.toDouble(), 0.0, hd, cBot, cTop)
                    val mandel = checkPoint(a, b)
                    iters[i][j] = mandel
                    hist[mandel]++
                    total++
                    if (mandel == curIter) {
                        setColor(i, j, Color.BLACK)
                    } else {
                        val c = mandel * colorFactor
                        setColor(i, j, Color.rgb((c * c) % 255, c % 255, c % 255))
                    }

                }
            }
        }
    }

    init {
        draw()
        ZoomTimer(this).start()
    }

}

class ZoomTimer(val v: MainView) : AnimationTimer() {
    
    var prev = System.nanoTime()
    
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