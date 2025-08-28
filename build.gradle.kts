plugins {
    // Version catalog-driven; no root plugins required
}

allprojects {
    // Common configuration if needed later
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

