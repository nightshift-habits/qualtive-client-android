import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint)
}

val ktlintVersion = libs.versions.ktlint.get()

subprojects {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
}

allprojects {
    pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<KtlintExtension> {
            version.set(ktlintVersion)
            android.set(false)
            ignoreFailures.set(false)
            outputToConsole.set(true)
        }
    }
}
