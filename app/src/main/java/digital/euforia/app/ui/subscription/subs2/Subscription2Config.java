package digital.euforia.app.ui.subscription.subs2;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import digital.euforia.app.R;
import digital.euforia.app.ui.subscription.RC;
import digital.euforia.app.ui.subscription.UserActivity;
import digital.euforia.app.ui.subscription_c.config.ReviewSubscriptionModel;
//import digital.euforia.app.model.service.ReviewSubscriptionModel;
//import digital.euforia.app.ui.UserActivity;
//import digital.euforia.app.util.base.RC;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

public class Subscription2Config implements Serializable {

    @SerializedName("title")
    public String title;
    @SerializedName("video")
    public String video;
    @SerializedName("music")
    public String music;
    @SerializedName("primaryButtonTitle")
    public String primaryButtonTitle;
    @SerializedName("primaryBottomButtonTitle")
    public String primaryBottomButtonTitle;
    @SerializedName("primaryButtonSubtitle")
    public String primaryButtonSubtitle;
    @SerializedName("primaryProduct")
    public String primaryProduct;
    @SerializedName("footerText")
    public String footerText;
    @SerializedName("plansTitle")
    public String plansTitle;
    @SerializedName("benefitsTitle")
    public String benefitsTitle;
    @SerializedName("extraBenefitsTitle")
    public String extraBenefitsTitle;
    @SerializedName("benefits")
    public ArrayList<String> benefits;
    @SerializedName("plans")
    public ArrayList<PlanConfig> plans;
    @SerializedName("extraBenefits")
    public ArrayList<BenefitConfig> extraBenefits;

    public ArrayList<ReviewSubscriptionModel> reviews;

    public static Subscription2Config newInstance(UserActivity activity) {
        try {
            Subscription2Config config = new Gson().fromJson(RC.getString(activity.getRC(), "premium_2_configuration"), Subscription2Config.class);

            InputStream inputStream = activity.getResources().openRawResource(R.raw.premium_reviews);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }

            String jsonReviews = writer.toString().trim();

            config.reviews = new Gson().fromJson(jsonReviews, new TypeToken<ArrayList<ReviewSubscriptionModel>>() {
            }.getType());
            return config;
        } catch (Exception e) {
            return new Subscription2Config();
        }
    }
}
