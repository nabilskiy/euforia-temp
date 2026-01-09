package digital.euforia.app.ui.subscription.subs2;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

public class BenefitConfig implements Serializable {
    @SerializedName("imageUrl")
    public String imageUrl;
    @SerializedName("text")
    public String text;
    @SerializedName("title")
    public String title;
}