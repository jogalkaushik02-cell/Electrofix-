# ============================================================================
# ElectroFix ProGuard Configuration
# ============================================================================

# Preserve line numbers for crash analysis
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================================
# FIREBASE CONFIGURATION
# ============================================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.appcheck.** { *; }
-keepnames class com.google.firebase.auth.api.model.** { *; }
-dontwarn com.google.firebase.**

# ============================================================================
# ROOM DATABASE
# ============================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}
-keep class com.example.data.db.** { *; }
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** {
    public *;
    private *;
}

# ============================================================================
# RETROFIT + MOSHI JSON SERIALIZATION
# ============================================================================
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.example.data.** { *; }
-keep interface com.example.data.** { *; }

# Retrofit
-keepattributes Signature,RuntimeVisibleAnnotations
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ============================================================================
# KOTLIN SUPPORT
# ============================================================================
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class kotlin.reflect.** { *; }
-keep interface kotlin.reflect.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-keep interface kotlinx.coroutines.** { *; }

# ============================================================================
# ANDROIDX SUPPORT
# ============================================================================
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class android.** { *; }
-dontwarn androidx.**

# Compose
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# ============================================================================
# APPLICATION-SPECIFIC CLASSES
# ============================================================================
-keep class com.example.MainActivity { *; }
-keep class com.example.data.firebase.FirebaseService { *; }
-keep class com.example.ui.viewmodel.** { *; }
-keep class com.example.data.repository.** { *; }

# Keep all enums
-keepclassmembers enum com.example.** {
    public static ** [] values();
    public static ** valueOf(java.lang.String);
    public *;
}

# Keep data classes
-keep class com.example.data.model.Product { *; }
-keep class com.example.data.model.Order { *; }
-keep class com.example.data.model.CartItem { *; }
-keep class com.example.data.model.RepairRequest { *; }
-keep class com.example.data.model.AdminSettings { *; }

# ============================================================================
# NATIVE DEBUGGING (Comment out for production if needed)
# ============================================================================
# -keep,allowobfuscation interface com.example.** { *; }
# -keep,allowobfuscation class com.example.** { *; }

# ============================================================================
# OPTIMIZATION SETTINGS
# ============================================================================
-optimizationpasses 5
-verbose
