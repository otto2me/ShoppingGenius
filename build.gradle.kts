// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlin) apply false
    alias(libs.plugins.hiltPlugin) apply false
    alias(libs.plugins.roomPlugin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidTest) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.gradlePlayPublisher) apply false
    alias(libs.plugins.firebaseAppDistribution) apply false
}

val enableBenchmark = gradle.startParameter.projectProperties.containsKey("enableBenchmark")

if (!enableBenchmark) {
    allprojects {
        tasks.configureEach {
            val taskName = name
            if (
                taskName.contains("Benchmark") ||
                taskName.contains("NonMinified") ||
                taskName.contains("baselineProfile", ignoreCase = true)
            ) {
                enabled = false
            }
        }
    }
}

