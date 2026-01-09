package digital.euforia.app.ui.subscription;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import digital.euforia.app.BuildConfig;


/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */
public class TimeManager {

    private static final String PREFERENCES_NAME = "time" + BuildConfig.APPLICATION_ID;

    // Basic events •••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••••

    public static void set(Context context, String event, long time) {
        try {
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(event, time)
                    .apply();
        } catch (Exception ignored) {
        }
    }

    public static void set(Context context, String event) {
        set(context, event, System.currentTimeMillis());
    }

    public static long get(Context context, String event) {
        try {
            return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getLong(event, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long between(Context context, String event, long timeTo) {
        return timeTo - get(context, event);
    }

}
