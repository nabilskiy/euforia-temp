// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}


buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
        classpath(libs.detekt.gradle.plugin)
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.androidx.navigation.safe.args.plugin)
    }
}

subprojects {
     apply(plugin = "io.gitlab.arturbosch.detekt")

     dependencies {
         "detekt"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
         "detekt"("io.gitlab.arturbosch.detekt:detekt-cli:1.23.8")
     }

     configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
         toolVersion = "1.23.3"
         config.setFrom("$rootDir/config/detekt/detekt.yml")
         baseline = file("$rootDir/config/detekt/baseline.xml")
         ignoredBuildTypes = listOf("release")
         ignoredFlavors = listOf("release")
         tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
             reports {
                 html.required.set(true)
                 xml.required.set(false)
                 txt.required.set(false)
                 sarif.required.set(false)
                 md.required.set(false)
             }
         }
     }
}