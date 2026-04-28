// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint) apply false
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.4" apply false
}

// ── KTLint — applied and configured for every submodule ──────────────────────

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.3.1")
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
            exclude("**/SDUIComponents.kt") // intentionally empty placeholder
        }
    }
}

// ── Git hook installation ─────────────────────────────────────────────────────

tasks.register<Copy>("installGitHooks") {
    description = "Installs pre-commit and pre-push hooks into .git/hooks/"
    from("scripts/pre-commit", "scripts/pre-push")
    into(".git/hooks")
    filePermissions { unix("755") }
}

// Wire hook installation into every Android module's preBuild so it runs
// automatically on the first build after any clone.
subprojects {
    tasks.configureEach {
        if (name == "preBuild") {
            dependsOn(rootProject.tasks.named("installGitHooks"))
        }
    }
}
