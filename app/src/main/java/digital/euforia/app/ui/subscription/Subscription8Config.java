package digital.euforia.app.ui.subscription;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import digital.euforia.app.billing.BillingRepository;
import digital.euforia.app.ui.subscription.UserActivity;
import digital.euforia.app.ui.subscription.RC;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

public class Subscription8Config implements Serializable {

    public String subscription = BillingRepository.BillingSku.PREMIUM_YEARLY;
    public String subscription_related = BillingRepository.BillingSku.PREMIUM_MONTHLY;
    public String offer = "Try 7 days for free";
    public String button = "Subscribe for %price%/%period%";
    public String bottom_text = "Invest in your well-being and\\n upgrade today";

    public String title = "Get unlimited access to all pictures and NYMF content";

    public String background = "https://dubnitskiy.com/storage/manual/mp4/1/123bdd87b916e1e778b6f294edcbd90d.mp4";

    public List<Subscription8Item> items;

    public Subscription8Config(UserActivity activity) {
        subscription = RC.getString(activity.getRC(), "premium_8_product_id");
        subscription_related = RC.getString(activity.getRC(), "premium_8_related_product_id");
        offer = RC.getString(activity.getRC(), "premium_8_offer");
        button = RC.getString(activity.getRC(), "premium_8_button");
        background = RC.getString(activity.getRC(), "premium_8_header");
        title = RC.getString(activity.getRC(), "premium_8_title");
        bottom_text = RC.getString(activity.getRC(), "premium_8_bottom_text");

        try {
            items = new Gson().fromJson(RC.getString(activity.getRC(), "premium_8_items"), new TypeToken<List<Subscription8Item>>() {
            }.getType());
        } catch (Exception e) {
            items = new ArrayList<>();
        }
    }

    public static class Subscription8Item {
        public String title;
        public String text;

        public String getTitle() {
            if (title != null)
                return title;
            else return "";
        }

        public String getText() {
            if (text != null)
                return text;
            else return "";
        }
    }
}