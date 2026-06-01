package digital.euforia.app.ui.subscription;

import android.os.Environment;


import java.io.File;

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */
public class Configuration {

    public static final String APP_NAME = "Luluby";
    public static final String LOG = APP_NAME + "_APP_LOG";
    public static final int MAX_SOUND_POOL_STREAMS = 50;
    public static final int MAX_SOCIAL_LINK_DESCRIPTION_LENGTH = 150;
    public static final int SYNC_PURCHASE_TOKEN_INTERVAL = 1000 * 60 * 60; // 1 hour
    public static final int RATE_APP_LAUNCH_COUNT = 3;
    public static final int RATE_APP_FEEDBACK_COUNT = 7;

    public static final int MAX_ALBUM_PLAYLIST_DEFAULT = 3;

    // Video
    public static final long DEFAULT_VIDEO_CACHE_SIZE_IN_BYTES = 1024L * 1024 * 1024;
    public static final long MAX_VIDEO_CACHE_SIZE_IN_BYTES = 2048L * 1024 * 1024;
    public static final long MIN_VIDEO_CACHE_SIZE_IN_BYTES = 512 * 1024 * 1024;
    public static final long OFF_VIDEO_CACHE_SIZE_IN_BYTES = 20 * 1024 * 1024;

    // Fresco & Images
    public static final int CACHE_IMG_VERSION = 1;
    public static final String CACHE_IMG_DIR = "cache-v1";
//    public static final int MAX_CACHE_SIZE = 512 * ByteConstants.MB;
//    public static final int MAX_CACHE_SIZE_LOW_DISK = 25 * ByteConstants.MB;
//    public static final int MAX_CACHE_SIZE_VERY_LOW_DISK = 5 * ByteConstants.MB;

    // Notification
    public static final int NOTIFICATIONS_ACTIVE_DELAY = 1000 * 60 * 3; // 3 minutes
    public static final long[] VIBRATE_TIME_INTERVAL = {777, 777, 777};
    public static final int LIGHT_TIME_ON = 3000;
    public static final int LIGHT_TIME_OFF = 3000;

    // Text and links
    public static final String URL_TERMS = "https://luluby.com/info/terms";
    public static final String URL_PRIVACY = "https://luluby.com/info/privacy";
    public static final String URL_COPYRIGHT = "https://luluby.com/info/copyright";
    public static final String URL_ABOUT_APP = "https://luluby.com/info/about";
    public static final String URL_ABOUT_BILLING = "https://luluby.com/info/subscriptions";
    public static final String URL_ONCREATE = "https://oncreate.com";

    public static final String URL_YOUTUBE = "https://www.youtube.com/@luluby_com";
    public static final String URL_TIKTOK = "https://www.tiktok.com/@luluby.com";
    public static final String URL_INSTAGRAM = "https://www.instagram.com/luluby_com";
    public static final String URL_TWITTER = "https://twitter.com/Luluby_Tech";
    public static final String URL_FACEBOOK = "https://facebook.com/lulubycom";
    public static final String APP_LINK_GOOGLE = "https://play.google.com/store/apps/details?id=com.luluby.app";
    public static final String APP_LINK_APPLE = "https://apps.apple.com/app/id6444841793";
    public static final String APP_LINK_DYNAMIC = "https://luluby.page.link/share";
    public static final String SUPPORT_EMAIL = "support@euforia.digital";

    // Methods
    public static File getDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsolutePath() + "/" + APP_NAME);
    }
}