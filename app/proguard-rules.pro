-verbose

# Virtualcard packages that were already run through proguard
-keep class com.simplytapp.virtualcard.** { *; }
-keep class javacard.framework.** { *; }
-keep class javacardx.apdu.** { *; }

# This might be overly cautious
-keep class com.google.** { *; }
-keep class org.apache.http.** { *; }


-dontwarn rx.**
-dontwarn retrofit.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**

# Any class name that could be referenced in the manifest needs to keep its name
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider


# Preserve the special static methods that are required in all enumeration
# classes.
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepattributes *Annotation*,Signature

-keepclasseswithmembernames class * {
    native <methods>;
}
