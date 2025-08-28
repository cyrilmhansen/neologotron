# neologotron
Bringing a classic microcomputer experience to the mobile platform

Project status
- Kotlin Android app scaffolded with Jetpack Compose, Material 3, and Hilt.
- Gradle Kotlin DSL with version catalog.
- ktlint and detekt configured. Basic CI workflow planned.

Requirements
- Android Studio (latest stable) with SDK Platform 34 and Build-Tools 34.x
- JDK 17

Getting started
- Open the project in Android Studio and let it sync dependencies.
- Select a device/emulator and Run.

CLI builds
- Generate Gradle wrapper if missing: Tools > Gradle > Gradle Wrapper (or run `gradle wrapper`).
- Build: `./gradlew assembleDebug`
- Run unit tests: `./gradlew testDebugUnitTest`
- Lint/style: `./gradlew ktlintCheck detekt`

Module overview
- `app`: Android application module with Compose UI and Hilt setup.

Code style
- Uses ktlint (official Kotlin style) and a minimal detekt baseline (`detekt.yml`).
