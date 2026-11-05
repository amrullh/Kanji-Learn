# Keep kotlinx.serialization metadata for the dataset models.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.amr.kanjiwidget.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.amr.kanjiwidget.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
