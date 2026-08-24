package digital.euforia.app.ui.subscription;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import digital.euforia.app.domain.usecase.translation.GetTranslationUseCase;
import digital.euforia.app.ui.subscription.UserActivity;
import digital.euforia.app.ui.subscription.RC;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

public class Subscription6Config implements Serializable {

    public String button_month = "Start Premium";
    public String button_year = "Start Free Trial";
    public String year_label = "Save up to 50%";
    public String year_line2 = "Best choice";
    public String name_month = "Monthly";
    public String name_year = "Annual";
    public String offer_month = "";
    public String offer_year = "Try 7 days for free";
    public String subs_display = Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_YEAR;
    public String subs_month = Subscription1Config.SUBSCRIPTION_MONTH_SPECIAL;
    public String subs_year = Subscription1Config.SUBSCRIPTION_YEAR_TRIAL;

    public String title = "Get unlimited access to all pictures and NYMF content";
    public String subtitle = "Get unlimited access to all pictures and NYMF content";

    public List<Subscription6Item> items;

    public Subscription6Config(UserActivity activity, GetTranslationUseCase getTranslationUseCase) {
        button_month = RC.getString(activity.getRC(), "premium_6_button_month");
        button_year = RC.getString(activity.getRC(), "premium_6_button_year");
        year_label = getTranslationUseCase.resolveOrOriginal(
                RC.getString(activity.getRC(), "premium_6_year_label"));
        year_line2 = getTranslationUseCase.resolveOrOriginal(
                RC.getString(activity.getRC(), "premium_6_line2nd_year"));
        name_month = RC.getString(activity.getRC(), "premium_6_name_month");
        name_year = RC.getString(activity.getRC(), "premium_6_name_year");
        offer_month = RC.getString(activity.getRC(), "premium_6_offer_month");
        offer_year = RC.getString(activity.getRC(), "premium_6_offer_year");
        subs_display = RC.getString(activity.getRC(), "premium_6_subs_display");
        subs_month = RC.getString(activity.getRC(), "premium_6_subs_month");
        subs_year = RC.getString(activity.getRC(), "premium_6_subs_year");
        title = RC.getString(activity.getRC(), "premium_6_title");
        subtitle = RC.getString(activity.getRC(), "premium_6_subtitle");

        try {
            items = new Gson().fromJson(RC.getString(activity.getRC(), "premium_6_items"), new TypeToken<List<Subscription6Item>>() {
            }.getType());
        } catch (Exception e) {
            items = new ArrayList<>();
        }
    }

    public static class Subscription6Item {
        public String icon;
        public String text;

        public String getIcon() {
            if (icon != null)
                return icon;
            else return "";
        }

        public String getText() {
            if (text != null)
                return text;
            else return "";
        }
    }
}