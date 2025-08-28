// This file, settings.gradle.kts, configures the project settings for Neologotron.
// It defines where to find plugins, how to resolve dependencies, the root project name,
// and includes the sub-projects that are part of this build.

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "neologotron"
include(":app")
