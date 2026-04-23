package com.example.astroids

import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import kotlin.math.min

class AstroidApplication : Application() {
    // Erstelle ein Canvas mit einer Größe von 800x600 Pixeln
    private val canvas = Canvas(800.0, 600.0)
    val asteroids = mutableListOf<Asteroid>()
    val maxAsteroid = 10
    var level = 1
    val radiusClick = 15.0 // radius um den klickpunkt
    var clickCount = 0
    var hitCount = 0
    var mouseInside = true
    private var lastTime = 0L
    private var mouseX = 0.0
    private var mouseY = 0.0
    //event handler für mausklicks
     init {
        canvas.setOnMouseClicked { event -> onCanvasClick(event) }
        canvas.onMouseMoved = javafx.event.EventHandler { event ->
            onMouseMove(event.x, event.y)
        }
     }

    override fun start(primaryStage: Stage) {
        gameLoop.start()
        // Fülle das Canvas mit einer Farbe
        fillCanvas()
        // Zeichne zufällige Asteroiden
        for (i in 1.. maxAsteroid) {
            val cx = Math.random() * canvas.width
            val cy = Math.random() * canvas.height
            val r = 20 + Math.random() * 60
            val n = 5 + (Math.random() * 10).toInt()
            val winkelUnregel = 0.4 // 0 = perfekt, 1 = sehr unregelmäßig
            val radiusUnregel = r * 0.4 // max Abweichung vom Radius
            val asteroid = Asteroid(cx, cy, r, n, winkelUnregel, radiusUnregel,level.toDouble())
            asteroids.add(asteroid)
            drawPolygon(asteroid.punkte)
        }

        // Erstelle eine Gruppe und füge das Canvas hinzu
        val root = Group(canvas)

        // Erstelle eine neue Scene und füge die Gruppe hinzu
        val scene = Scene(root)

        // Setze den Titel des Stages
        primaryStage.title = "Asteroid Game"

        // Setze die Szene für den Stage
        primaryStage.scene = scene
        // --- Cursor-Handling ---
                  // merkt, ob die Maus über der Scene ist
        scene.cursor = Cursor.NONE       // standardmäßig verstecken

        // Wenn das Fenster den Fokus verliert -> Cursor zeigen
        primaryStage.focusedProperty().addListener { _, _, focused ->
            scene.cursor = if (focused && mouseInside) Cursor.NONE else Cursor.DEFAULT
        }

        // Wenn die Maus die Scene verlässt -> Cursor zeigen
        scene.setOnMouseExited {
            mouseInside = false
            scene.cursor = Cursor.DEFAULT
        }

        // Wenn die Maus die Scene betritt & das Fenster Fokus hat -> wieder verstecken
        scene.setOnMouseEntered {
            mouseInside = true
            if (primaryStage.isFocused) scene.cursor = Cursor.NONE
        }
        // --- Ende Cursor-Handling ---

        //canvas.cursor = Cursor.NONE
        // Zeige den Stage an
        primaryStage.show()
    }

    private fun fillCanvas() {
        // Hole den GraphicsContext für das Canvas
        val gc: GraphicsContext = canvas.graphicsContext2D

        // Fülle das gesamte Canvas mit einer Farbe (z.B. Blau)
        gc.fill = Color.DARKBLUE
        gc.fillRect(0.0, 0.0, canvas.width, canvas.height)
    }

    private fun drawLine(fromX: Double,fromY: Double,toX: Double,toY: Double,color: Color) {
        // Hole den GraphicsContext für das Canvas
        val gc: GraphicsContext = canvas.graphicsContext2D

        // Setze die Strokkfarbe (Farbe der Linie) auf Weiß
        gc.stroke = color
        gc.lineWidth = 2.0

        // Beginne einen neuen Pfad für das Zeichnen
        gc.beginPath()

        // Bewege den "Stift" an den Startpunkt der Linie
        gc.moveTo(fromX, fromY)

        // Zeichne eine Linie zum Endpunkt der Linie
        gc.lineTo(toX, toY)

        // Zeichne den Pfad mit der definierten Strokkfarbe
        gc.stroke()
    }

    private fun drawPolygon(points: Array<Pair<Double,Double>>) {
        val gc = canvas.graphicsContext2D
        val n = points.size
        val xs = DoubleArray(n) { points[it].first }
        val ys = DoubleArray(n) { points[it].second }
        //Liste mit Farben je nach level
        var colors = listOf(Color.LIGHTGRAY, Color.LIGHTGREEN, Color.LIGHTYELLOW, Color.LIGHTPINK, Color.LIGHTCORAL, Color.LIGHTSEAGREEN)
        val fillColor = colors[(level -1) % colors.size]
        gc.fill = fillColor
        gc.fillPolygon(xs, ys, n)

        gc.stroke = Color.BLACK
        gc.lineWidth = 3.0
        gc.strokePolygon(xs, ys, n)
    }


    private fun onMouseMove(x: Double, y: Double) {
        // Aktualisiere die Position des Spielers basierend auf den Mauskoordinaten
        //println("Mouse moved to: ($x, $y)")
        mouseX = x
        mouseY = y
    }

    private fun checkClickCollision(x: Double, y: Double,Asteroid: Asteroid): Boolean {
        // Überprüfe on der Mausposition ein Asteroid getroffen wurde (Simpel nur mit Kreis)
        /* val dx = x - Asteroid.cx
        val dy = y - Asteroid.cy
        val distanceSquared = dx * dx + dy * dy
        return (distanceSquared <= Asteroid.r * Asteroid.r)
        */
        val dx = x - Asteroid.cx
        val dy = y - Asteroid.cy
        val distanceSquared = dx * dx + dy * dy
        val combinedRadius = Asteroid.r + radiusClick
        return (distanceSquared <= combinedRadius * combinedRadius)
    }

    private fun onCanvasClick(event: MouseEvent) {
        val clickX = event.x
        val clickY = event.y
        val button = event.button
        println(button.toString())
        clickCount += 1
        //Liste für zu entfernende und hinzuzufügende Asteroiden
        var asteroidsToRemove: MutableList<Asteroid> = mutableListOf()
        var asteroidstoAdd: MutableList<Asteroid> = mutableListOf()

        asteroids.forEach {
            if (checkClickCollision(clickX, clickY, it)) {
                // Asteroid getroffen erstelle zwei neue kleinere Asteroiden
                asteroidsToRemove.add(it)
                hitCount += 1
                if (it.r > 15) { // nur teilen wenn groß genug
                    for (i in 1..2) {
                        val cx = it.cx + (Math.random() * 20 -10)
                        val cy = it.cy + (Math.random() * 20 -10)
                        val r = it.r / 2
                        val n = it.punkte.size
                        val winkelUnregel = 0.4 // 0 = perfekt, 1 = sehr unregelmäßig
                        val radiusUnregel = r * 0.4 // max Abweichung vom Radius
                        val asteroid = Asteroid(cx, cy, r, n, winkelUnregel, radiusUnregel,level.toDouble())
                        asteroidstoAdd.add(asteroid)
                    }
                }
                //println("Asteroid at (${it.cx}, ${it.cy}) clicked!")
            }
        }
        asteroids.removeAll(asteroidsToRemove)
        asteroidstoAdd.forEach {
            asteroids.add(it)
        }
    }

    private val gameLoop = object : AnimationTimer() {
        override fun handle(now: Long) {
            if (lastTime == 0L) { lastTime = now; return }
            val dt = min((now - lastTime) / 1_000_000_000.0, 0.05) // Sekunden, auf 50 ms gekappt
            lastTime = now
            val deadzone = 50.0 // Bereich außerhalb des Bildschirms zum Respawn
            fillCanvas()
            //move Asteroids
            if(asteroids.isNotEmpty()){
                asteroids.forEach {
                    it.update(dt)
                    //Falls der Asteroid aus dem Bildschirm fliegt, soll er auf der anderen Seite wieder auftauchen
                    if (it.cx < - deadzone) it.cx = canvas.width + deadzone // links raus, rechts rein
                    if (it.cx > canvas.width + deadzone ) it.cx = -deadzone // rechts raus, links rein
                    if (it.cy < -deadzone) it.cy = canvas.height + deadzone // oben raus, unten rein
                    if (it.cy > canvas.height + deadzone) it.cy = -deadzone // unten raus, oben rein
                    drawPolygon(it.punkte)
                }
            }
            else{
                //Create new asteroids
                level += 1 //Da keine Asteroiden mehr übrig sind, nächstes Level
                for (i in 1.. maxAsteroid) {
                    val cx = Math.random() * canvas.width
                    val cy = Math.random() * canvas.height
                    val r = 20 + Math.random() * 50
                    val n = 5 + (Math.random() * 10).toInt()
                    val winkelUnregel = 0.4 // 0 = perfekt, 1 = sehr unregelmäßig
                    val radiusUnregel = r * 0.4 // max Abweichung vom Radius
                    val asteroid = Asteroid(cx, cy, r, n, winkelUnregel, radiusUnregel, level.toDouble())
                    asteroids.add(asteroid)
                    drawPolygon(asteroid.punkte)
                }
            }
            //Schreibe Level oben rechts in den Canvas
            val gc = canvas.graphicsContext2D
            gc.fill = Color.WHITE
            gc.font = javafx.scene.text.Font("Arial", 24.0)
            gc.fillText("Level $level", canvas.width - 100, 30.0)
            //Hit und Click count unten rechts
            gc.fillText("Hits: $hitCount / Clicks: $clickCount", canvas.width - 250, canvas.height - 20)

            if(mouseInside) {
                //Zeichnet ein Fadenkreuz an die Mausposition
                drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY, Color.WHITE)
                drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10, Color.WHITE)
            }
        }
    }
}


fun main() {
    Application.launch(AstroidApplication::class.java)
}