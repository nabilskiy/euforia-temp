# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.appsflyer.** { *; }
-keep public class com.android.installreferrer.** { *; } # Need for install referrer integration with AppsFlyer
-keep class com.adapty.** { *; }
-keep class com.pushwoosh.** { *; }
-dontwarn com.pushwoosh.**
-keep class com.arellomobile.** { *; }
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.arellomobile.**
-keep class com.google.firebase.** { *; }
-keep class com.pushwoosh.firebase.** { *; }
-keep class io.intercom.android.** { *; }
-keep class com.intercom.** { *; }
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
 # Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class digital.euforia.app.domain.util.ResultWrapper

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# keep kotlinx.coroutines android internals used by dispatcher / exceptions
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }

# Ensure Kotlin coroutines intrinsics are kept for suspend functions (fixes ClassNotFoundException for IntrinsicsKt)
-keep class kotlin.coroutines.intrinsics.** { *; }
-keep class kotlin.coroutines.SafeContinuation { *; }
-keep class kotlin.coroutines.jvm.internal.** { *; }

# Moshi: keep generated JsonAdapters and prevent obfuscation of @JsonClass models
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,Signature,Kotlin
# Keep Kotlin Metadata annotation so Moshi/KotlinJsonAdapterFactory can reflect when needed
-keep class kotlin.Metadata { *; }
# Keep names for classes annotated with @com.squareup.moshi.JsonClass(generateAdapter = true)
-keepnames @com.squareup.moshi.JsonClass class *
# Keep the generated JsonAdapter for those classes (referenced by name via reflection)
-keep class **JsonAdapter { *; }
# Keep Moshi and Kotlin reflect warnings quiet
-dontwarn com.squareup.moshi.**
# If you have enums used with Moshi, keep enum constant names
-keepclassmembers enum * { **[] $VALUES; public *; }