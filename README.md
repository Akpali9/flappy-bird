# 🐦 Flappy Bird - Android App

A fully functional Flappy Bird clone for Android, built in Kotlin.

## Features
- 🎮 Tap-to-flap bird mechanics
- 🌿 Scrolling pipe obstacles with randomized gaps
- 🏆 Score tracking with high score persistence
- 🥇 Medal system (Bronze: 5+, Silver: 10+, Gold: 20+)
- 🎨 Polished visuals — gradient sky, animated bird, clouds
- 📱 Fullscreen portrait mode

## How to Play
- **Tap** the screen to make the bird flap its wings
- Fly through the gaps between the green pipes
- Each pipe you pass earns **1 point**
- Don't hit the pipes, ground, or ceiling!
- After a game over, tap **PLAY AGAIN** to restart

## Project Structure
```
FlappyBird/
├── app/src/main/java/com/example/flappybird/
│   ├── MainActivity.kt    — Entry point, sets up fullscreen
│   ├── GameView.kt        — Main game canvas & touch input
│   ├── GameThread.kt      — 60 FPS game loop
│   ├── Bird.kt            — Player character with animation
│   └── Pipe.kt            — Scrolling obstacle pipes
├── app/src/main/res/
│   └── values/
│       ├── strings.xml
│       └── themes.xml
└── app/src/main/AndroidManifest.xml
```
