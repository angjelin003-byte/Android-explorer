# Vertical 3D Island Explorer

A realistic, optimized 3D exploration game simulator designed explicitly for Android mobile devices. Developed to run beautifully in portrait/vertical mode (9:16 aspect ratio), focusing on a slow-paced, atmospheric wilderness exploration experience.

## Game Overview
Players explore a procedural 1 km × 1 km island containing forests, plains, cliffs, and cave systems. The goal is atmosphere, survival, and exploration rather than arcade-style combat. 

## Technical Architecture (Kotlin Native)

This project has been scaffolded completely natively in Kotlin to prepare it for integration with a mobile 3D engine (such as Google Filament or Rajawali) avoiding overhead from massive generic game engines.

The architecture is heavily modularized across 25 dedicated subsystems mapped inside `app/src/main/java/com/islandexplorer/`:

### Core Mechanics
* **`GameManager`**: The master update loop. Manages Delta Time and sequentially executes the physics, input, and environment updates.
* **`Vectors`**: Custom lightweight 2D and 3D math interpolation (`lerp`, `magnitude`).

### Player & Controls
* **`PlayerMovement` & `PlayerStamina`**: Physics-based acceleration. Characters speed up/slow down smoothly. Running drains stamina (15/s), walking slowly regenerates it (3/s), and standing rests (10/s).
* **`PlayerAnimation`**: State machine mapping movement velocity to skeletal IK triggers.
* **`TouchInputManager`**: Dual-zone touch parsing. Left side for movement joystick, right side for camera rotation.

### Camera System
* **`ThirdPersonCamera`**: Orbits the player using Pitch and Yaw mathematics. Smoothly interpolates positions to create realistic "camera lag" while clamping underground clipping.

### Environment & World Generation
* **`TerrainManager`**: Analyzes the terrain to return `SurfaceTypes` (Grass, Mud, Stone, Water) for footsteps and interaction.
* **`DayNightSystem` & `WeatherSystem`**: A full 24-hour cycle altering skybox colors and dynamic directional lighting. Weather randomly shifts between Fog, Clear, and Mist every 5–15 minutes.
* **`WorldChunkManager` & `LODManager`**: Grid-based distance calculations to aggressively cull distant geometry (NEAR, MEDIUM, FAR, VERY_FAR) to ensure 60fps on mobile.

### Survival Equipment
* **`TentSystem`**: Deployable campsites that smoothly advance the day/night cycle by 6 hours to simulate resting.
* **`TorchSystem`**: Handheld dynamic point-light with battery tracking for navigating caves.
* **`MapSystem` & `CompassSystem`**: Calculates exact North/South headings and tracks a 100x100 Boolean grid for fog-of-war map exploration.

### Persistence & UI
* **`SaveSystem`**: Serializes Vector3 positions, time of day, and inventory state.
* **`SettingsManager`**: Scalable Low/Medium/High graphics presets.

## CI/CD Pipeline
This repository contains a pre-configured `.github/workflows/android-build.yml` file. 

Every push to the `main` branch will trigger an Ubuntu runner to compile the Gradle project using JDK 17, automatically outputting a testable `app-debug.apk` artifact.
