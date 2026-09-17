# Room Proguard
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Libphonenumber
-keep class com.google.i18n.phonenumbers.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
