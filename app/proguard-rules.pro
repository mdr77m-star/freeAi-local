# Project Production ProGuard / R8 Rules for Qwengram AI

# 1. Strip debug logs in production
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# 2. Hide source file names and line numbers in release builds
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# 3. Obfuscate package names and class members
-repackageclasses 'com.aistudio.secure.core'
-allowaccessmodification

# 4. Keep Room Database entities and DAOs safely
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# 5. Keep Moshi / JSON Serialization models
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class com.example.data.local.** { *; }
-keep class com.example.data.model.** { *; }

# 6. Protect Core Engine Architecture
-keepclassmembers class com.example.engine.** {
    public <methods>;
}

# 7. Jetpack Compose rules
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
