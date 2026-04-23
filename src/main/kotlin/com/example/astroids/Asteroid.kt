package com.example.astroids

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

class Asteroid(
    cx: Double, cy: Double, r: Double, n: Int,
    winkelUnregel: Double, radiusUnregel: Double, velocityScale: Double
) {
    var cx = cx
    var cy = cy
    val r = r
    val winkel = makeAsteroidAngles(cx, cy, r, n, winkelUnregel, radiusUnregel)
    val radien = makeAsteroidRadii(r, n, radiusUnregel)
    var startWinkel = Math.random() * 2 * PI
    var punkte = createPoints(winkel, n, radien, cx, cy, startWinkel)

    // -- NEU: Kurvenbasiertes Speed-Model ------------------------------------

    // Basisspeed bei scale = 1.0 (tune diese Werte)
    private val BASE_MIN_SPEED = 30.0
    private val BASE_MAX_SPEED = 80.0

    /**
     * Wachstumskurve für die Geschwindigkeit.
     *
     * Mathematisch Potenzfunktion (ohne Kappung):
     *   g(scale) = scale^alpha
     *
     *
     * Parameter:
     * - scale ... Eingabe (z. B. Level + 1)
     * - alpha ... Exponent, steuert die Krümmung
     * - cap   ... optionale Obergrenze; die Funktion liefert min(g(scale), cap)
     *
     * Verhalten je nach alpha:
     * - alpha > 1     → überlinear/konvex: startet sanft, steigt später stark.
     * - alpha = 1     → linear.
     * - 0 < alpha < 1 → unterlinear/konkav: früh stark, später flacher.
     *
     * Eigenschaften:
     * - Für alpha > 0 und scale ≥ 0 ist g monoton steigend.
     * - g(1) = 1  (bei scale = 1 bleibt der Faktor unverändert).
     * - g'(scale) = alpha * scale^(alpha − 1); insbesondere g'(1) = alpha.
     */

    private fun growth(scale: Double, alpha: Double = 1.1, cap: Double = 10.0): Double {
        val g = scale.pow(alpha)
        return min(g, cap)
    }

    private fun speedRange(scale: Double): Pair<Double, Double> {
        val g = growth(scale)
        val minS = BASE_MIN_SPEED * ((g /10) + 1)                 // hebt die Mindestgeschwindigkeit
        val maxS = BASE_MAX_SPEED * ((g /10) + 1)  * 1.15          // und streckt die Obergrenze etwas
        println("Calculated maxS: ${maxS} / minS: ${minS} for scale ${scale} (growth factor: ${g})")
        return Pair(minS, maxS)
    }

    private fun randomBetween(a: Double, b: Double): Double = a + (b - a) * Math.random()

    private fun makeLinearVelocity(scale: Double): Pair<Double, Double> {
        val (minS, maxS) = speedRange(scale)
        val speed = randomBetween(minS, maxS)
        val theta = Math.random() * 2 * PI // zufällige Richtung [0, 2π)
        println("Created Asteroid with speed ${speed}")
        return Pair(cos(theta) * speed, sin(theta) * speed)
    }

    val linearVelocity = makeLinearVelocity(velocityScale)
    val angularVelocity = (Math.random() * 2 - 1) * (3.5 + 1.5 * (growth(velocityScale, alpha = 0.8, cap = 2.5) - 1.0))

    // ------------------------------------------------------------------------

    private fun makeAsteroidAngles(cx: Double, cy: Double, r: Double, n: Int, winkelUnregel: Double, radiusUnregel: Double): DoubleArray {
        val BASISSCHRITT = (2 * PI) / n
        val MAX_WINKEL_JITTER = BASISSCHRITT * winkelUnregel
        val schritte = DoubleArray(n)
        for (i in 0 until n) {
            val jitter = (Math.random() * 2 - 1) * MAX_WINKEL_JITTER
            schritte[i] = BASISSCHRITT + jitter
        }
        val summeSchritte = schritte.sum()
        val factor = (2 * PI) / summeSchritte
        for (i in 0 until n) {
            schritte[i] = schritte[i] * factor
        }
        return schritte
    }

    private fun makeAsteroidRadii(r: Double, n: Int, radiusUnregel: Double): DoubleArray {
        val radien = DoubleArray(n)
        for (i in 0 until n) {
            val abfall = (Math.random()) * radiusUnregel
            radien[i] = r - abfall
        }
        return radien
    }

    private fun createPoints(schritte: DoubleArray, n: Int, radien: DoubleArray, cx: Double, cy: Double, startWinkel: Double): Array<Pair<Double, Double>> {
        val punkte = Array(n) { Pair(0.0, 0.0) }
        for (i in 0 until n) {
            val winkel = startWinkel + schritte.slice(0 until i).sum()
            val radius = radien[i]
            val x = cx + radius * kotlin.math.cos(winkel)
            val y = cy + radius * kotlin.math.sin(winkel)
            punkte[i] = Pair(x, y)
        }
        return punkte
    }

    fun update(dt: Double) {
        startWinkel += angularVelocity * dt
        cx += linearVelocity.first * dt
        cy += linearVelocity.second * dt
        punkte = createPoints(winkel, punkte.size, radien, cx, cy, startWinkel)
    }
}
