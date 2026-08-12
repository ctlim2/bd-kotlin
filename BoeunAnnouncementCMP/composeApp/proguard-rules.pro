# Compose Multiplatform Proguard Rules
-keep class androidx.compose.** { *; }
-keep class com.boeun.announcement.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn androidx.compose.**
-dontwarn io.ktor.**
-dontwarn kotlinx.datetime.**
