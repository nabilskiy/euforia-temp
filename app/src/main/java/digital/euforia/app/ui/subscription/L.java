package digital.euforia.app.ui.subscription;


import android.util.Log;

import digital.euforia.app.BuildConfig;


/**
 * Created by ONCREATE COMPANY © 2023.
 * <a href="https://oncreate.com">Developer Website</a>
 * Copyright © 2014-2023 ONCREATE. All rights reserved.
 */

public class L {

    public static boolean isNeed() {
        return BuildConfig.DEBUG;
    }

    public static void d(Object message) {
        if (isNeed())
            Log.d(Configuration.LOG, message.toString());
    }

    public static void d(Object... messages) {
        if (isNeed())
            for (Object m : messages)
                Log.d(Configuration.LOG, m.toString() + " ");
    }

    public static void e(Object message) {
        if (isNeed())
            Log.e(Configuration.LOG, message.toString());
    }

    public static void i(Object message) {
        if (isNeed())
            Log.i(Configuration.LOG, message.toString());
    }

    public static void wft(Object message) {
        if (isNeed())
            Log.wtf(Configuration.LOG, message.toString());
    }

    public static void print(Object message) {
        if (isNeed())
            System.out.print(message.toString());
    }

    public static void print(Object... messages) {
        if (isNeed())
            for (Object m : messages)
                System.out.print(m.toString() + " ");
    }

    public static void println(Object message) {
        if (isNeed())
            System.out.println(message.toString());
    }

    public static void println(Object... messages) {
        if (isNeed())
            for (Object m : messages)
                System.out.println(m.toString() + "\n");
    }

    public static void println() {
        if (isNeed())
            System.out.println();
    }
}
