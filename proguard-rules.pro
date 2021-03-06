# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Evan/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn okio.**
-dontwarn retrofit.**
-dontwarn butterknife.**
-dontwarn com.squareup.**
-dontwarn com.caverock.**
-keepnames public class * extends io.realm.RealmObject
-keep class io.realm.** { *; }
-dontwarn javax.**
-dontwarn io.realm.**
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn rx.internal.**

# retrofit specific
-dontwarn com.squareup.okhttp.**
-dontwarn com.google.appengine.api.urlfetch.**
-dontwarn rx.**
-dontwarn retrofit.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-dontwarn com.firebase.client.**
-dontwarn com.google.android.gms.**
-dontwarn com.twitter.sdk.android.core.**
-dontwarn org.apache.**
-keep class org.apache.http.**
-keep interface org.apache.http.**
