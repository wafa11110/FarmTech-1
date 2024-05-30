package com.example.farmtech;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class SharedPreferencesHelper {

    private static final String SHARED_PREFS_NAME = "shared_preferences";

    public static void saveProfileImageUri(Context context, Uri imageUri) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileImageUri", imageUri.toString());
        editor.apply();
    }

    public static Uri getProfileImageUri(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String imageUriString = sharedPreferences.getString("profileImageUri", null);
        return imageUriString != null ? Uri.parse(imageUriString) : null;
    }
}
