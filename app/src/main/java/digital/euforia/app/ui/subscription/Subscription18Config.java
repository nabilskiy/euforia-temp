package digital.euforia.app.ui.subscription;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import digital.euforia.app.billing.BillingRepository;

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

public class Subscription18Config implements Serializable {

    public String subscription = BillingRepository.BillingSku.PREMIUM_YEARLY;
    public String subscription_related = BillingRepository.BillingSku.PREMIUM_MONTHLY;
    public String offer = "Try 7 days for free";
    public String button = "Subscribe for %price%/%period%";
    public String bottom_text = "Invest in your well-being and\\n upgrade today";

    public String title = "Get unlimited access to all pictures and NYMF content";

    public String background = "https://dubnitskiy.com/storage/manual/mp4/1/123bdd87b916e1e778b6f294edcbd90d.mp4";

    public List<String> items;

    public Subscription18Config(UserActivity activity) {
        subscription = RC.getString(activity.getRC(), "premium_3_product_id");
        subscription_related = RC.getString(activity.getRC(), "premium_3_related_product_id");
        offer = RC.getString(activity.getRC(), "premium_3_offer");
        button = RC.getString(activity.getRC(), "premium_3_button");
        bottom_text = RC.getString(activity.getRC(), "premium_3_bottom_text");
        title = RC.getString(activity.getRC(), "premium_title_4");
        background = RC.getString(activity.getRC(), "premium_3_bg");

        try {
            items = new Gson().fromJson(RC.getString(activity.getRC(), "premium_3_items"), new TypeToken<List<String>>() {
            }.getType());
        } catch (Exception e) {
            items = new ArrayList<>();
        }
    }

}
