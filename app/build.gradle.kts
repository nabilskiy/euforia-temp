plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Use the modern Hilt Gradle plugin ID only
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.detekt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}

android {
    namespace = "digital.euforia.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "digital.euforia.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APP_API_URL", "\"https://euforia.digital/api/\"")
        buildConfigField("String", "API_KEY", "\"3i6o2ko}AFGM1,LTj8xn/FmRw\\\\]@NI7\"")
        buildConfigField("boolean", "IS_SANDBOX", "true")
    }

//    signingConfigs {
//        create("release") {
////            releaseConfig(project)
//        }
//        create("innerTest") {
//            storeFile = file("debugkeystore")
//            storePassword = "sampleappdebug"
//            keyAlias = "debug"
//            keyPassword = "sampleappdebug"
//        }
//        getByName("debug") {
//            storeFile = file("debugkeystore")
//            storePassword = "sampleappdebug"
//            keyAlias = "debug"
//            keyPassword = "sampleappdebug"
//        }
//    }
//
//    buildTypes {
//        getByName("release") {
//            isMinifyEnabled = true
//            isDebuggable = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//            signingConfig = signingConfigs.getByName("release")
//            buildConfigField("String", "APP_API_URL", "\"https://euforia.digital/api/\"")
//            buildConfigField("boolean", "ENABLED_DEV_MENU", "false")
//
//        }
//
//        getByName("debug") {
//            isDebuggable = true
//            isMinifyEnabled = false
//            applicationIdSuffix = ".dev"
//            versionNameSuffix = "-dev"
//            signingConfig = signingConfigs.getByName("debug")
//            buildConfigField("String", "APP_API_URL", "\"https://euforia.digital/api/\"")
//            buildConfigField("boolean", "ENABLED_DEV_MENU", "true")
//        }
//    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    lint {
        abortOnError = true
        checkAllWarnings = true
        baseline = file("$rootDir/config/lint/lint-baseline.xml")
        disable += listOf(
            "JvmStaticProvidesInObjectDetector",
            "FieldSiteTargetOnQualifierAnnotation",
            "ModuleCompanionObjects",
            "ModuleCompanionObjectsNotInModuleParent"
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.animation) {
//        version {
//            strictly("1.7.0") // або будь-яку 1.7.x/1.8.x, яка відповідає твоєму BOM
//        }
    }
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    coreLibraryDesugaring(libs.desugar.jdk)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.jdk8)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.play)
    implementation(libs.kotlinx.serialization)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.orbit.core)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.core)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.airbnb.lottie)
    implementation(libs.airbnb.lottie.compose)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.android.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.core)
    implementation(libs.moshi.kotlin)
    implementation(libs.converter.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.ui)
    implementation(libs.timber)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.mock)
    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.ui.tooling)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.haze)
    implementation(libs.haze.materials)
}
