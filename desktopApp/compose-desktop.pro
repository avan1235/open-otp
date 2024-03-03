-keep class com.arkivanov.decompose.extensions.compose.mainthread.SwingMainThreadChecker
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory
-keep class org.bridj.** { *; }
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider { *; }
-keep class kotlinx.serialization.SerialFormat { *; }
-keepattributes Annotation, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class ml.dev.kotlin.openotp.**$$serializer { *; }
-keepclassmembers class ml.dev.kotlin.openotp.** {
    *** Companion;
}
-keepclasseswithmembers class ml.dev.kotlin.openotp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class kotlinx.serialization.cbor.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.cbor.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-dontwarn com.arkivanov.decompose.extensions.compose.pages.**
-dontwarn org.xml.sax.**
-dontwarn javax.xml.**
-dontwarn org.w3c.dom.**
-dontwarn org.apache.log.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.batik.**
-dontwarn org.apache.avalon.**
-dontwarn org.mozilla.javascript.**
-dontwarn org.python.**
-dontwarn org.osgi.framework.**
-dontwarn org.slf4j.impl.**
-dontwarn ch.qos.logback.classic.**
-dontwarn javafx.**
-dontwarn com.github.eduramiba.webcamcapture.**
-dontwarn android.security.**
-dontwarn android.net.http.**
-dontwarn android.net.ssl.**
-dontwarn android.util.**
-dontwarn android.os.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.openjsse.javax.net.ssl.**
-dontwarn org.openjsse.net.ssl.**
-dontwarn org.conscrypt.**
-dontwarn java.lang.Thread$Builder$OfPlatform
-dontwarn java.lang.Thread$Builder$OfVirtual
-dontwarn java.lang.Thread$Builder
