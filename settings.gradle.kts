pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("unityLibrary/unityLibrary/libs")
        }
    }
}

rootProject.name = "Kimyasal"

include(":launcher")
project(":launcher").projectDir = File("app")

include(":unityLibrary")
project(":unityLibrary").projectDir = File("unityLibrary/unityLibrary")
