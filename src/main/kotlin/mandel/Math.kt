package mandel

import kotlin.math.sqrt


typealias C = Complex

data class Complex(val re: Double, val im: Double) {

    operator fun plus(c: Complex) = Complex(re + c.re, im + c.im)

    operator fun minus(c: Complex) = Complex(re - c.re, im - c.im)

    operator fun unaryMinus() = Complex(-re, -im)

    operator fun times(c: Complex) = Complex(re * c.re - im * c.im, re * c.im + im * c.re)

    operator fun div(c: Complex): Complex {
        val divisor = re * re + c.im * c.im
        return Complex((re * c.re + im * c.im) / divisor, (im * c.re - re * c.im) / divisor)
    }

    val conj: Complex
        get() = Complex(re, -im)

    val square: Complex
        get() = Complex(re * re - im * im, 2 * re * im)

    val normSq: Double
        get() = re * re + im * im

    val norm: Double
        get() = sqrt(normSq)
}

val Double.j: C
    get() = Complex(0.0, this)

val Int.j: C
    get() = toDouble().j

val Float.j: C
    get() = toDouble().j

operator fun Double.plus(c: C) = Complex(this + c.re, c.im)