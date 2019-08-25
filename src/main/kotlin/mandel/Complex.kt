package mandel

import scientifik.kmath.operations.Complex

typealias C = Complex

val Double.j: C
    get() = Complex(0.0, this)

val Int.j: C
    get() = toDouble().j

val Float.j: C
    get() = toDouble().j

val Complex.tSquare: Complex
    get() = Complex(re * re - im * im, 2 * re * im)

val Complex.normSquare: Double
    get() = re * re + im * im

operator fun Double.plus(c: Complex) = Complex(this + c.re, c.im)