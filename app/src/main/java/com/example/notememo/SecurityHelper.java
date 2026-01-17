package com.example.notememo;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SecurityHelper {
    private static final String PREFS_NAME = "fast_memo_security";
    private static final String KEY_PIN_ENABLED = "pin_enabled";
    private static final String KEY_PIN_HASH = "pin_hash";

    public static boolean isPinEnabled(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(KEY_PIN_ENABLED, false);
    }

    public static void setPinEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = getSharedPreferences(context);
        prefs.edit().putBoolean(KEY_PIN_ENABLED, enabled).apply();
    }

    public static void setPin(Context context, String pin) {
        SharedPreferences prefs = getSharedPreferences(context);
        String hash = hashPin(pin);
        prefs.edit()
                .putString(KEY_PIN_HASH, hash)
                .putBoolean(KEY_PIN_ENABLED, true)
                .apply();
    }

    public static boolean verifyPin(Context context, String pin) {
        SharedPreferences prefs = getSharedPreferences(context);
        String storedHash = prefs.getString(KEY_PIN_HASH, "");
        String inputHash = hashPin(pin);
        return storedHash.equals(inputHash);
    }

    private static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return pin; // Fallback (not secure, but better than crash)
        }
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        try {
            // Try to use EncryptedSharedPreferences for better security
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback to regular SharedPreferences if encryption fails
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }
}
