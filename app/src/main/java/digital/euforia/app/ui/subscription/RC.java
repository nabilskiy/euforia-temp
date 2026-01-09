package digital.euforia.app.ui.subscription;


import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Created by ONCREATE COMPANY © 2023.
 * <a href="https://oncreate.com">Developer Website</a>
 * Copyright © 2014-2023 ONCREATE. All rights reserved.
 */

@SuppressWarnings("unused")
public class RC {

    public static int getInt(FirebaseRemoteConfig firebaseRemoteConfig, String key) {
        return (int) firebaseRemoteConfig.getLong(key);
    }

    public static boolean getBoolean(FirebaseRemoteConfig firebaseRemoteConfig, String key) {
        try {
            return firebaseRemoteConfig.getBoolean(key);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getString(FirebaseRemoteConfig firebaseRemoteConfig, String key) {
        return firebaseRemoteConfig.getString(key).replace("\\n", "\n");
    }

    public static boolean isEmptyString(FirebaseRemoteConfig firebaseRemoteConfig, String key) {
        return firebaseRemoteConfig.getString(key).trim().isEmpty();
    }


}