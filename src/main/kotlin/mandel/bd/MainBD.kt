package mandel.bd

import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseButton
import javafx.scene.paint.Color
import mandel.Appl
import mandel.MainView
import mandel.Styles
import mandel.Styles.Companion.rootClass
import tornadofx.*
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO
import java.math.MathContext

fun main() = launch<Appl>()

typealias Array2D<T> = Array<Array<T>>

class Appl : App(MainView::class, Styles::class)

class MainView : View() {
    val ctx = MathContext.DECIMAL64
    override val root = vbox {
        primaryStage.width = 800.0
        primaryStage.height = 600.0
        addClass(rootClass)
        c = canvas {
            widthProperty().bind(primaryStage.widthProperty())
            heightProperty().bind(primaryStage.heightProperty())
        }
        setOnMouseClicked {
            when(it.button) {
                MouseButton.PRIMARY -> zoomIn(it.sceneX, it.sceneY)
                MouseButton.SECONDARY -> zoomOut(it.sceneX, it.sceneY)
                else -> {}
            }
            draw()
        }
    }

    fun renorm(i: BigDecimal, sl: BigDecimal, sr: BigDecimal, tl: BigDecimal, tr: BigDecimal) =
        i.subtract(sl, ctx).divide(sr.subtract(sl, ctx), ctx).multiply(tr.subtract(tl, ctx), ctx).add(tl, ctx)

    fun toMandelCoords(x: Double, y: Double): Pair<BigDecimal, BigDecimal> {
        return renorm(x.toBigDecimal(ctx), ZERO, c.width.toBigDecimal(ctx), cLeft, cRight) to
                renorm(y.toBigDecimal(ctx), ZERO, c.height.toBigDecimal(ctx), cBot, cTop)
    }

    fun zoomOut(x: Double, y: Double) {
        val (mx, my) = toMandelCoords(x, y)
        val z1 = ONE.subtract(z, ctx)
        cLeft = cLeft.divide(z1, ctx).subtract(mx, ctx)
        cRight = cRight.divide(z1, ctx).subtract(mx, ctx)
        cBot = cBot.divide(z1, ctx).subtract(my, ctx)
        cTop = cTop.divide(z1, ctx).subtract(my, ctx)

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

    fun zoomIn(x: Double, y: Double) {
        val (mx, my) = toMandelCoords(x, y)
        val z1 = ONE.subtract(z, ctx)
        val ox = mx.multiply(z1)
        val oy = my.multiply(z1)
        cLeft = cLeft.multiply(z1, ctx).subtract(ox, ctx)
        cRight = cRight.multiply(z1, ctx).subtract(ox, ctx)
        cBot = cBot.multiply(z1, ctx).subtract(oy, ctx)
        cTop = cTop.multiply(z1, ctx).subtract(oy, ctx)
    }

    val z = 0.5.toBigDecimal(ctx)
    val mLeft = (-2.5).toBigDecimal(ctx)
    val mRight = 1.5.toBigDecimal(ctx)
    val mBot = (-1.5).toBigDecimal(ctx)
    val mTop = 1.5.toBigDecimal(ctx)
    val maxIter = 150

    var cLeft = mLeft
    var cRight = mRight
    var cBot = mBot
    var cTop = mTop

    lateinit var c: Canvas


    val TWO = 2.toBigDecimal(ctx)
    val FOUR = 4.toBigDecimal(ctx)

    fun checkPoint(a: BigDecimal, b: BigDecimal): Int {

        var i = 0
        var za = ZERO
        var zb = ZERO
        while (i < maxIter) {
            za = za.pow(2, ctx).subtract(zb.pow(2, ctx), ctx).add(a, ctx)
            zb = TWO.multiply(za, ctx).multiply(zb, ctx).add(b, ctx)
            i += 1
            if (za.pow(2, ctx).add(zb.pow(2, ctx), ctx) > FOUR) {
                return i
            }
        }
        return i
    }

    fun draw() {
        with(c.graphicsContext2D.pixelWriter) {
            val w = c.width.toInt() - 1
            val h = c.height.toInt() - 1
            val hist = Array(maxIter + 1) { 0 }
            val iters = Array(w + 1) { Array(h + 1) { maxIter } }
            var total = 0
            for (i in 0..w) {

                val a = renorm(i.toBigDecimal(ctx), ZERO, w.toBigDecimal(ctx), cLeft, cRight)
                for (j in 0..h) {
                    val b = renorm(j.toBigDecimal(ctx), ZERO, h.toBigDecimal(ctx), cBot, cTop)
                    val mandel = checkPoint(a, b)
                    iters[i][j] = mandel
                    hist[mandel]++
                    total++
                }
            }
            val hues = Array(maxIter + 1) { Color.WHITE }
            var hu = 0.0
            for (i in 0..maxIter) {
                hu += hist[i].toDouble() / total
                if (hu > 1.0) {
                    hu = 1.0
                }
                hues[i] = Color.hsb((1 - hu) * 360, 1.0, 1.0)
            }
            hues[maxIter] = Color.BLACK
            for (i in 0..w) {
                for (j in 0..h) {
                    val it = iters[i][j]
                    setColor(i, j, hues[it])
                }
            }
        }
    }

    init {
        draw()
    }

}


class Styles : Stylesheet() {

    companion object {
        val rootClass by cssclass()
    }

    init {

    }

}