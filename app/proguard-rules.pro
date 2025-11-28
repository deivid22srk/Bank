-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keep class com.bancoapp.** { *; }

-keepclassmembers class com.bancoapp.** {
    native <methods>;
}

-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keepclasseswithmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.bancoapp.data.**$$serializer { *; }
-keepclassmembers class com.bancoapp.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.bancoapp.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
