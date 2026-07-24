# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.example.japanmap.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.japanmap.** {
    kotlinx.serialization.KSerializer serializer(...);
}
