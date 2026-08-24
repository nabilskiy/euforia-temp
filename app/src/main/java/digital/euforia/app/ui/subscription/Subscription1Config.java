package digital.euforia.app.ui.subscription;

import digital.euforia.app.domain.usecase.translation.GetTranslationUseCase;
import digital.euforia.app.ui.subscription.UserActivity;
import digital.euforia.app.ui.subscription.RC;

import java.io.Serializable;

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

public class Subscription1Config implements Serializable {

    public static final String SUBSCRIPTION_DISPLAY_TYPE_ALL = "all";
    public static final String SUBSCRIPTION_DISPLAY_TYPE_MONTH = "month";
    public static final String SUBSCRIPTION_DISPLAY_TYPE_YEAR = "year";
    public static final String SUBSCRIPTION_DISPLAY_TYPE_OFF = "off";

    public static final String SUBSCRIPTION_MONTH_DEFAULT = "default";
    public static final String SUBSCRIPTION_MONTH_TRIAL = "trial";
    public static final String SUBSCRIPTION_MONTH_SPECIAL = "special";
    public static final String SUBSCRIPTION_MONTH_SPECIAL_TRIAL = "special_trial";

    public static final String SUBSCRIPTION_YEAR_DEFAULT = "default";
    public static final String SUBSCRIPTION_YEAR_TRIAL = "trial";
    public static final String SUBSCRIPTION_YEAR_SPECIAL = "special";
    public static final String SUBSCRIPTION_YEAR_SPECIAL_TRIAL = "special_trial";

    public String subs_display = SUBSCRIPTION_DISPLAY_TYPE_ALL;
    public String subs_month = SUBSCRIPTION_MONTH_SPECIAL;
    public String subs_year = SUBSCRIPTION_YEAR_SPECIAL;

    public String button_month = "Start Premium";
    public String button_year = "Start Free Trial";

    public String name_month = "Monthly";
    public String name_year = "Annual Special";

    public String offer_month = "";
    public String offer_year = "Try 7 days for free";

    public String title_from_intro = "Get unlimited access to all pictures and NYMF content";
    public String title_from_primary = "Get unlimited access to all NYMF content";
    public String title_from_second = "Get unlimited access to all stories and NYMF content";

    public String video_from_intro = "https://dubnitskiy.com/storage/manual/mp4/1/123bdd87b916e1e778b6f294edcbd90d.mp4"; // id or URL
    public String video_from_primary = "video1";
    public String video_from_second = "https://dubnitskiy.com/storage/manual/mp4/2/2003e69a36ce5183b7a2cedca4251087.mp4";

    public String year_label = "Save up to 50%";
    public String year_line2 = "Best choice";

    public boolean title_caps = false;
    public String title_color = "#ffffff";
    public int title_max_lines = 2;

    public Subscription1Config(UserActivity activity, GetTranslationUseCase getTranslationUseCase) {
        subs_display = RC.getString(activity.getRC(), "premium_1_subs_display");
        subs_month = RC.getString(activity.getRC(), "premium_1_subs_month");
        subs_year = RC.getString(activity.getRC(), "premium_1_subs_year");

        button_month = RC.getString(activity.getRC(), "premium_1_button_month");
        button_year = RC.getString(activity.getRC(), "premium_1_button_year");

        name_month = getTranslationUseCase.resolveOrOriginal(
                RC.getString(activity.getRC(), "premium_1_name_month"));
        name_year = getTranslationUseCase.resolveOrOriginal(
                RC.getString(activity.getRC(), "premium_1_name_year"));

        offer_month = RC.getString(activity.getRC(), "premium_1_offer_month");
        offer_year = RC.getString(activity.getRC(), "premium_1_offer_year");

        title_from_intro = RC.getString(activity.getRC(), "premium_1_title_from_intro");
        title_from_primary = RC.getString(activity.getRC(), "premium_1_title_from_primary");
        title_from_second = RC.getString(activity.getRC(), "premium_1_title_from_second");

        video_from_intro = RC.getString(activity.getRC(), "premium_1_video_from_intro");
        video_from_primary = RC.getString(activity.getRC(), "premium_1_video_from_primary");
        video_from_second = RC.getString(activity.getRC(), "premium_1_video_from_second");

        year_label = getTranslationUseCase.resolveOrOriginal(
                RC.getString(activity.getRC(), "premium_1_year_label"));
        year_line2 = getTranslationUseCase.resolveOrOriginal(
                RC.getString(activity.getRC(), "premium_1_line2nd_year"));

        title_caps = RC.getBoolean(activity.getRC(), "premium_1_title_caps");
        title_color = "#" + RC.getString(activity.getRC(), "premium_1_title_color");
        title_max_lines = RC.getInt(activity.getRC(), "premium_1_title_max_lines");
    }

}
