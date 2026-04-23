# Astroids

A modern take on the classic Asteroids arcade game, built with Kotlin and JavaFX.

Click to destroy procedurally generated asteroids, progress through levels of increasing difficulty, and track your accuracy.

## Features

- **Procedurally generated asteroids** with irregular polygon shapes for visual variety
- **Click-to-destroy gameplay** with proximity-based hit detection
- **Level progression** — clear all asteroids to advance; speed scales with each level
- **Asteroid fragmentation** — large asteroids split into smaller pieces when hit
- **Real-time stats** — hit/click ratio displayed during gameplay

## Prerequisites

- Java 21

## Build & Run

```bash
# Run the game
./mvnw javafx:run

# Compile only
./mvnw clean compile

# Run tests
./mvnw clean test

# Build JAR
./mvnw clean package
```

## Tech Stack

- **Kotlin 1.9** — primary language
- **JavaFX 21** — graphics and UI rendering
- **Maven** — build system

## How to Play

- Move your mouse to aim
- Click on asteroids to destroy them
- Larger asteroids break into smaller fragments
- Clear all asteroids to advance to the next level
- Each level increases asteroid speed
