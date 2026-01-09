package digital.euforia.app.ui.subscription.subs2;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

public class PlanConfig implements Serializable {
    @SerializedName("productIdentifier")
    public String productIdentifier;
    @SerializedName("badge")
    public String badge;
    @SerializedName("title")
    public String title;
    @SerializedName("details")
    public String details;
    @SerializedName("buttonTitle")
    public String buttonTitle;
    @SerializedName("buttonSubtitle")
    public String buttonSubtitle;
}