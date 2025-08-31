plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

tasks.withType<JavaCompile>().configureEach { options.compilerArgs.add("-Xlint:deprecation") }

subprojects {
    // ktlint applied at root; detekt configured via detekt.yml
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
// Ensure Detekt runs with a supported JVM target
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
}
