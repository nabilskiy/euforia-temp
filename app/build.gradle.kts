import java.util.Properties
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.android.build.gradle.internal.api.ApkVariantOutputImpl

//Ваш додаток не підтримує сторінки пам’яті розміром 16 КБ.
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
    buildFeatures {
        viewBinding = true
    }
    namespace = "digital.euforia.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "digital.euforia.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 13
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APP_API_URL", "\"https://euforia.digital/api/\"")
        buildConfigField("String", "API_KEY", "\"3i6o2ko}AFGM1,LTj8xn/FmRw\\\\]@NI7\"")
        buildConfigField("String", "UNSPLASH_ACCESS_KEY", "\"-JndjsO7pVs0b_dnz2asryatqI2Tt0Ol9j2Dls1yU_c\"")
        buildConfigField("String", "UNSPLASH_SECRET_KEY", "\"_OwTN939rfzjMGrMSbOxs3L9reeQZENlsIjMu_F-JQw\"")
        buildConfigField("String", "PEXELS_API_KEY", "\"gEKEloETni2msv2SEueSxlqG69I0qZP0evewtJAFsKwmLU6RFqczMVxd\"")
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

    val keystoreProps = Properties().apply {
        val propsFile = rootProject.file("euforia.properties")
        if (propsFile.exists()) {
            load(propsFile.inputStream())
        }
    }

    signingConfigs {
        create("release") {
            // Якщо файла нема (наприклад на CI без секретів) — не падаємо
            val hasProps = keystoreProps.isNotEmpty()
            if (hasProps) {
                val keystorePath = keystoreProps["releaseKeystore"] as String
                storeFile = rootProject.file(keystorePath)
                storePassword = keystoreProps["releaseKeystorePassword"] as String
                keyAlias = keystoreProps["releaseKeyAlias"] as String
                keyPassword = keystoreProps["releaseKeyPassword"] as String
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isDebuggable = false
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

    applicationVariants.all {
        if (buildType.name != "debug") return@all
        val buildDate = ZonedDateTime.now(ZoneId.of("UTC"))
            .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        val appId = applicationId
        val vName = versionName ?: "0.0"
        val vCode = versionCode
        outputs.all {
            (this as? ApkVariantOutputImpl)?.outputFileName =
                "${appId}-v${vName}(${vCode})-${buildDate}-${buildType.name}.apk"
        }
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
    implementation(libs.androidx.transition)
    implementation(libs.androidx.work.runtime.ktx)
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
    // Use direct coordinate to ensure availability even if version catalog accessor isn't generated yet
    implementation("androidx.media3:media3-exoplayer-hls:1.8.0")
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
    implementation(libs.play.review)
    implementation(libs.play.review.ktx)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.appsflyer)
    implementation(libs.installreferrer)

    // Billing
    implementation(libs.billing.client)
    implementation(libs.billing.ktx)
    implementation(libs.gson)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.material.icons.extended)
    implementation("com.google.android.material:material:1.12.0")
    // Fresco for image pipeline used in BaseActivity and UserActivity
        //  implementation("com.facebook.fresco:fresco:3.3.0")
    // ExpandableTextView for album/playlist descriptions (JitPack)
   // implementation("com.github.giangpham96:expandable-text:2.0.1")
    implementation("com.github.anhaki:PickTime-Compose:1.1.5")
//    implementation("com.arnyminerz.markdowntext:markdowntext:1.3.1")
    implementation("com.colintheshots:twain:0.3.2")
    implementation("io.noties.markwon:core:4.6.2")
//    implementation("com.mikepenz:multiplatform-markdown-renderer-android:0.39.1")
//    implementation("com.github.jeziellago:compose-markdown:0.2.6")

}

