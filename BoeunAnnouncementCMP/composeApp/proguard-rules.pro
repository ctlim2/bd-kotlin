# Compose Multiplatform Proguard Rules
-keep class androidx.compose.** { *; }
-keep class com.boeun.announcement.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn androidx.compose.**
-dontwarn io.ktor.**
-dontwarn kotlinx.datetime.**

# WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Room (Transitive from WorkManager)
-dontwarn androidx.room.**
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.WorkerFactory { *; }

# Fix for java.lang.NoSuchMethodException: androidx.work.impl.WorkDatabase_Impl.<init> []
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>(...);
}

# Startup and Core
-keep class androidx.startup.** { *; }
-dontwarn androidx.startup.**
-keep class androidx.core.app.CoreComponentFactory { *; }
