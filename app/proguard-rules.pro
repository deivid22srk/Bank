-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keep class com.bancoapp.** { *; }

-keepclassmembers class com.bancoapp.** {
    native <methods>;
}

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keepclasseswithmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }
