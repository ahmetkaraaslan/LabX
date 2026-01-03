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
        flatDir {
            dirs("unityLibrary/libs")
        }
    }
}

rootProject.name = "LabX"

include(":app")
include(":unityLibrary")
project(":unityLibrary").projectDir = File("unityLibrary")