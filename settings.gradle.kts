pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "MoviesDemoAPP"
include(":app")
include(":core:domain")
include(":core:ui")
include(":core:network")
include(":core:data")
include(":engine:sdui")
include(":engine:navigation")
include(":feature:movies")
include(":feature:banking")
include(":feature:deeplink")
include(":feature:analytics")
