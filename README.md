# Vertical 3D Island Explorer (Android / Kotlin)

A realistic, optimized 3D island exploration game designed natively in Kotlin for Android mobile devices. Tailored for portrait / vertical gameplay (9:16 aspect ratio).

## Project Structure

This repository is a native Android project built with Kotlin and Gradle:

```
├── .github/
│   └── workflows/
│       └── build.yml          # GitHub Actions CI/CD building app-debug.apk
├── app/
│   ├── build.gradle.kts       # App module configuration (Kotlin, SDK 34)
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/islandexplorer/
│           │   ├── MainActivity.kt        # Main SurfaceView render & game loop
│           │   ├── audio/AudioManager.kt
│           │   ├── camera/ThirdPersonCamera.kt
│           │   ├── core/GameManager.kt
│           │   ├── core/Vectors.kt
│           │   ├── environment/DayNightSystem.kt
│           │   ├── environment/WeatherSystem.kt
│           │   ├── input/TouchInputManager.kt
│           │   ├── inventory/BackpackSystem.kt
│           │   ├── inventory/CompassSystem.kt
│           │   ├── inventory/MapSystem.kt
│           │   ├── inventory/TentSystem.kt
│           │   ├── inventory/TorchSystem.kt
│           │   ├── player/PlayerMovement.kt
│           │   ├── player/PlayerStamina.kt
│           │   ├── player/PlayerAnimation.kt
│           │   ├── save/SaveSystem.kt
│           │   ├── ui/UIManager.kt
│           │   └── world/TerrainManager.kt
│           └── res/                       # Android resources (themes, icons)
├── build.gradle.kts           # Root Gradle build script
├── settings.gradle.kts        # Root Gradle settings script
├── gradle.properties          # JVM and AndroidX settings
└── gradlew                    # Gradle wrapper script
```

## GitHub Actions Automated Build (`build.yml`)

Every push or pull request to the `main` branch triggers `.github/workflows/build.yml`:
1. Launches an Ubuntu runner with JDK 17 and Gradle 8.7.
2. Compiles the native Kotlin codebase via `gradle assembleDebug`.
3. Packages and uploads `app-debug.apk` directly as a downloadable GitHub Actions artifact.

## Local Development & Building

To build the APK locally via command line:
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```
