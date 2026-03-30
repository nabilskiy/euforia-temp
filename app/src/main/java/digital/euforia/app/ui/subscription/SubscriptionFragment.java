package digital.euforia.app.ui.subscription;

import static digital.euforia.app.ui.subscription.UserActivity.PURCHASE_SUCCESS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.LifecycleOwnerKt;
import androidx.media3.common.util.UnstableApi;

import com.google.gson.Gson;
//import com.jaychang.st.SimpleText;
import digital.euforia.app.R;
import digital.euforia.app.billing.BillingRepository;

import com.android.billingclient.api.BillingClient;

import digital.euforia.app.billing.BillingViewModel;
import digital.euforia.app.billing.localdb.AugmentedSkuDetails;
//import digital.euforia.app.data.PreferencesManager;
//import digital.euforia.app.model.service.SubscriptionInfoModel;
import digital.euforia.app.billing.model.SubscriptionInfoModel;
import digital.euforia.app.data.analytics.AnalyticSender;
import digital.euforia.app.data.store.ProfilePreferences;
import digital.euforia.app.ui.subscription.UserActivity;
import digital.euforia.app.ui.subscription.BaseFragment;
//import digital.euforia.app.ui.fragment.HomeFragment;
import digital.euforia.app.ui.subscription.subs2.Subscription2Fragment;
//import digital.euforia.app.util.analytics.Analytics;
import digital.euforia.app.ui.subscription.RC;
//import digital.euforia.app.util.base.SelectorUtils;

//import org.apache.commons.lang3.StringUtils;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import digital.euforia.app.data.store.AppPreferences;
import timber.log.Timber;


/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

public abstract class SubscriptionFragment extends BaseFragment<UserActivity> {

    public static final String FROM_PRIMARY = "primary"; // All general app screens.
    public static final String FROM_INTRO = "intro"; // From into (start offer).
    public static final String FROM_SECOND = "second"; // From secondary action; now it's a background mode.

    CoordinatorLayout coordinatorLayout;
    @Nullable
    TextView getPremium;
    @Nullable
    TextView getPremiumYearly;
    TextView terms;
    @Nullable
    View layoutSubscriptionProcessing;

    protected int screenId;
    protected String from = FROM_PRIMARY;
    protected String tag;
    protected BillingViewModel billingViewModel;
    protected List<AugmentedSkuDetails> subsSkuDetailsList;
    protected AugmentedSkuDetails skuDetailsMonthly, skuDetailsYearly,
            skuDetailsMonthlySpecial, skuDetailsYearlySpecial,
            skuDetailsMonthlyTrial, skuDetailsYearlyTrial,
            skuDetailsMonthlySpecialTrial, skuDetailsYearlySpecialTrial;

    @Inject
    protected AppPreferences appPreferences;

    @Inject
    protected ProfilePreferences profilePreferences;

    @Inject
    public AnalyticSender analyticSender;

    public SubscriptionFragment() {
    }

    protected boolean enableYearlySubscription() {
        return false;
    }

    @UnstableApi
    public static SubscriptionFragment newInstance(UserActivity activity, String from) {
        int screenId = RC.getInt(activity.getRC(), "premium_screen_variant");
        return newInstance(activity, from, screenId, null);
//        return newInstance(activity, from, screenId, null);
    }

    @UnstableApi
    public static SubscriptionFragment newInstance(UserActivity activity, String from, String tag) {
        int screenId = RC.getInt(activity.getRC(), "premium_screen_variant");
        return newInstance(activity, from, screenId, tag);
    }

    @UnstableApi
    public static SubscriptionFragment newInstance(UserActivity activity, String from, int screenId, String tag) {
        switch (screenId) {
            case 8:
                return Subscription8Fragment.newInstance(from, tag);
            case 7:
                return Subscription7Fragment.newInstance(from, tag);
            case 6:
                return Subscription6Fragment.newInstance(from, tag);
            case 3:
                return Subscription3Fragment.newInstance(from, tag);
            case 18:
                return Subscription18Fragment.newInstance(from, tag);
//            case 2:
//                return Subscription2Fragment.newInstance(from, tag);
//            case 10:
//                return Subscription10Fragment.newInstance(from, tag);
//            case 18:
//                return Subscription18Fragment.newInstance(from, tag);
            default:
                return Subscription1Fragment.newInstance(from, tag);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        billingViewModel = new ViewModelProvider(activity).get(BillingViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Initialize common views (optional per-layout)
        coordinatorLayout = v.findViewById(R.id.coordinator);
        getPremium = v.findViewById(R.id.getPremium);
        getPremiumYearly = v.findViewById(R.id.getPremiumYearly);
        terms = v.findViewById(R.id.terms);
        layoutSubscriptionProcessing = v.findViewById(R.id.layoutSubscriptionProcessing);
        View back = v.findViewById(R.id.back);
        if (back != null) back.setOnClickListener(view -> onBackClick());
        if (getPremium != null) getPremium.setOnClickListener(view -> onGetPremiumClick());
        if (getPremiumYearly != null)
            getPremiumYearly.setOnClickListener(view -> onGetPremiumYearlyClick());

        // Handle system back press: finish the hosting Activity when this fragment is on screen
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        onBackClick();
                    }
                });

        if (getArguments() != null) {
            from = getArguments().getString("from", FROM_PRIMARY);
            tag = getArguments().getString("tag", null);
        }
        screenId = RC.getInt(activity.getRC(), "premium_screen_variant_android");
        analyticSender.premiumShow(from, screenId, tag);
        //Analytics.premium_show(AppsFlyerLib.getInstance(), activity, from, screenId, tag);
        onGetSubscriptionsInfo();
//        onUISetup();

        try {
//            if (PreferencesManager.readIsPro(activity)) {
//                if (!activity.isActivitySingleFragmentMode())
//                    Toast.makeText(activity, R.string.you_are_already_pro, Toast.LENGTH_LONG).show();
//                v.post(() -> activity.onBackPressed());
//            }
        } catch (Exception ignored) {
        }
    }

    protected String onGetSubscriptionButtonText() {
        return getString(R.string.subscription_button_title);
    }

    protected String onGetSubscriptionYearlyButtonText() {
        return getString(R.string.subscription_button_title);
    }

    protected void onGetSubscriptionsInfo() {
        onPremiumPreSetup();
        activity.getBillingViewModel().getSubsSkuDetailsListLiveData().observe(getViewLifecycleOwner(), augmentedSkuDetails -> {
            this.subsSkuDetailsList = augmentedSkuDetails;
            for (AugmentedSkuDetails skuDetails : subsSkuDetailsList) {
                if (skuDetails.getSku().equals(BillingRepository.BillingSku.PREMIUM_MONTHLY)) {
                    this.skuDetailsMonthly = skuDetails;
                    onPremiumMonthlySetup();
                } else if (enableYearlySubscription() && skuDetails.getSku().equals(BillingRepository.BillingSku.PREMIUM_YEARLY)) {
                    this.skuDetailsYearly = skuDetails;
                    onPremiumYearlySetup();
                } else if (skuDetails.getSku().equals(BillingRepository.BillingSku.PREMIUM_SPECIAL_MONTHLY)) {
                    this.skuDetailsMonthlySpecial = skuDetails;
                } else if (skuDetails.getSku().equals(BillingRepository.BillingSku.PREMIUM_SPECIAL_YEARLY)) {
                    this.skuDetailsYearlySpecial = skuDetails;
                } else if (skuDetails.getSku().equals(BillingRepository.BillingSku.PREMIUM_MONTHLY_TRIAL)) {
                    this.skuDetailsMonthlyTrial = skuDetails;
                } else if (skuDetails.getSku().equals(BillingRepository.BillingSku.PREMIUM_YEARLY_TRIAL)) {
                    this.skuDetailsYearlyTrial = skuDetails;
                } else if (skuDetails.getSku().equals(BillingRepository.BillingSku.PREMIUM_SPECIAL_MONTHLY_TRIAL)) {
                    this.skuDetailsMonthlySpecialTrial = skuDetails;
                } else if (skuDetails.getSku().equals(BillingRepository.BillingSku.PREMIUM_SPECIAL_YEARLY_TRIAL)) {
                    this.skuDetailsYearlySpecialTrial = skuDetails;
                }
            }
            onPremiumPostSetup();
        });
    }

    protected void onPremiumMonthlySetup() {
        if (getPremium != null)
            getPremium.setText(String.format(
                    Locale.getDefault(),
                    onGetSubscriptionButtonText(),
                    skuDetailsMonthly.getPrice()));
    }

    protected void onPremiumYearlySetup() {
        if (getPremiumYearly != null)
            getPremiumYearly.setText(String.format(
                    Locale.getDefault(),
                    onGetSubscriptionYearlyButtonText(),
                    skuDetailsYearly.getPrice()));
    }

    protected void onPremiumPreSetup() {

    }

    protected void onPremiumPostSetup() {
    }

//    @SuppressLint("ClickableViewAccessibility")
//    protected void onUISetup() {
//        if (getPremium != null)
//            getPremium.setOnTouchListener((view, motionEvent) -> {
//                SelectorUtils.selectorScale(view, motionEvent);
//                return false;
//            });
//        if (enableYearlySubscription() && getPremiumYearly != null)
//            getPremiumYearly.setOnTouchListener((view, motionEvent) -> {
//                SelectorUtils.selectorScale(view, motionEvent);
//                return false;
//            });
//    }

    protected AugmentedSkuDetails getSelectedMonthSku() {
        return skuDetailsMonthly;
    }

    protected AugmentedSkuDetails getSelectedYearSku() {
        return skuDetailsYearly;
    }

    public void onGetPremiumClick() {
        analyticSender.premiumBuyClick(getSelectedMonthSku().getSku(), from, screenId, tag);
        //Analytics.premium_buy_click(AppsFlyerLib.getInstance(), activity, getSelectedMonthSku().getSku(), from, screenId, tag);
        performPurchase(getSelectedMonthSku());
    }

    public void onGetPremiumYearlyClick() {
        analyticSender.premiumBuyClick(getSelectedYearSku().getSku(), from, screenId, tag);
        //Analytics.premium_buy_click(AppsFlyerLib.getInstance(), activity, getSelectedYearSku().getSku(), from, screenId, tag);
        performPurchase(getSelectedYearSku());
    }

    protected void performPurchase(AugmentedSkuDetails skuDetails) {
        Timber.tag("PURCHASE").d("Performing purchase for SKU: %s", skuDetails != null ? skuDetails.getSku() : "null");
        if (skuDetails != null) {
            billingViewModel.getPremiumLiveData().removeObservers(getViewLifecycleOwner());
            billingViewModel.getBillingStatus().removeObservers(getViewLifecycleOwner());

            if (layoutSubscriptionProcessing != null)
                layoutSubscriptionProcessing.setVisibility(View.VISIBLE);
            billingViewModel.getBillingStatus().observe(getViewLifecycleOwner(), status -> {
                if (layoutSubscriptionProcessing != null)
                    layoutSubscriptionProcessing.setVisibility(View.GONE); // status 0 for purchased success
            });

            billingViewModel.getPremiumLiveData().observe(getViewLifecycleOwner(), premium -> {
                Timber.tag("PURCHASE").d("Purchase result received for SKU: %s, entitled: %s", skuDetails.getSku(), premium != null ? premium.getEntitled() : "null");
                if (premium != null && premium.getEntitled()) {
                    Timber.tag("PURCHASE").d("Purchase successful for SKU: %s", skuDetails.getSku());
                    activity.updateRCImmediately();
                    if (profilePreferences != null) {
                        profilePreferences.setIsPremiumAsync(true, LifecycleOwnerKt.getLifecycleScope(getViewLifecycleOwner()));
                    } else {
                        Timber.tag("PURCHASE").e("ProfilePreferences is null; Hilt injection likely missing on this fragment variant");
                    }
                    analyticSender.premiumBuySuccess(skuDetails.getSku(), from, screenId, tag);
                    showThanks();
                }
            });

            activity.getBillingViewModel().makePurchase(activity, skuDetails);
        } else {
            Toast.makeText(activity, R.string.text_payment_not_available, Toast.LENGTH_LONG).show();
        }
    }

    protected void showThanks() {
        try {
            Intent data = new android.content.Intent();

            requireActivity().setResult(PURCHASE_SUCCESS, data); // <- your custom result code
            requireActivity().finish();
////            Analytics.thanks_dialog_show(activity.getAnalytics());
//            ThanksDialog thanksDialog = new ThanksDialog(activity, activity.getRC());
//            thanksDialog.setOnDismissListener(dialog -> {
//                try {
////                    if (!activity.isActivitySingleFragmentMode())
////                        activity.replaceFragment(HomeFragment.newInstance());
//                } catch (Exception ignored) {
//                }
//            });
//            thanksDialog.show();
        } catch (Exception ignored) {
        }
    }

    public void onBackClick() {
        // Use the OnBackPressedDispatcher to correctly navigate back to the previous
        // place (e.g., back to MainActivity where the user was before opening subscription).
        // This finishes the hosting activity or pops the appropriate back stack entry.
//        Analytics.premium_back(activity.getAnalytics());

        analyticSender.premiumBack();

        requireActivity().finish();
//        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    protected void displayText(TextView textView, String text, String defaultText,
                               SubscriptionInfoModel info,
                               SubscriptionInfoModel infoRelativeMonth) {
        if (textView == null)
            return;
        else if (text == null || text.trim().isEmpty())
            text = defaultText;

        if (text == null) {
            textView.setVisibility(View.GONE);
            return;
        }

        try {
            String var_price = "%price%";
            String var_period = "%period%";
            String var_price_per_month = "%price_per_month%";
            String var_discount = "%discount%";
            String var_relative_price = "%relative_price%";
            String var_trial_period = "%trial_period%"; // not used

            if (text.contains(var_price)) {
                if (info != null)
                    text = text.replace(var_price, info.getPrice());
                else text = text.replace(var_price, "");
            }

            if (text.contains(var_period)) {
                if (info != null)
                    text = text.replace(var_period, getPeriod(info));
                else text = text.replace(var_period, "");
            }

            if (text.contains(var_trial_period)) {
                if (info != null)
                    text = text.replace(var_trial_period, getTrialPeriod(info));
                else text = text.replace(var_trial_period, "");
            }

            if (text.contains(var_discount)) {
                if (info != null && infoRelativeMonth != null) {
                    int saleValue = 100 - (int) (((double) info.getPriceAmountMicros() / (infoRelativeMonth.getPriceAmountMicros() * 12d) * 100d));
                    text = text.replace(var_discount, String.valueOf(saleValue));
                } else text = text.replace(var_discount, "");
            }

            if (text.contains(var_price_per_month)) {
                if (info != null) {
                    float priceYearlyPerMonth = (float) (info.getPriceAmountMicros() / 12d / 1000000d);
                    int priceYearlyPerMonthUnit = (int) priceYearlyPerMonth;
                    float rem = priceYearlyPerMonth - priceYearlyPerMonthUnit;

                    if (rem >= 0.0f && rem < 0.15f) {
                        priceYearlyPerMonth = (priceYearlyPerMonthUnit - 1) + 0.99000f;
                    } else if (rem >= 0.15f && rem < 0.40f) {
                        priceYearlyPerMonth = priceYearlyPerMonthUnit + 0.19000f;
                    } else if (rem >= 0.40f && rem < 0.65f) {
                        priceYearlyPerMonth = priceYearlyPerMonthUnit + 0.49000f;
                    } else if (rem >= 0.65f && rem < 0.90f) {
                        priceYearlyPerMonth = priceYearlyPerMonthUnit + 0.75000f;
                    } else if (rem >= 0.90f && rem <= 0.99f) {
                        priceYearlyPerMonth = priceYearlyPerMonthUnit + 0.99000f;
                    }

                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                    currencyFormat.setMinimumFractionDigits(2);
                    currencyFormat.setMaximumFractionDigits(2);
                    currencyFormat.setRoundingMode(RoundingMode.FLOOR);
                    currencyFormat.setCurrency(Currency.getInstance(info.getPriceCurrencyCode()));
                    String pricePerMonth = currencyFormat.format(priceYearlyPerMonth);

                    text = text.replace(var_price_per_month, pricePerMonth);
                } else text = text.replace(var_price_per_month, "");
            }

            if (text.contains(var_relative_price)) {
                if (info != null && infoRelativeMonth != null) {
                    double priceYearlyRelative = infoRelativeMonth.getPriceAmountMicros() * 12d / 1000000;

                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
                    currencyFormat.setCurrency(Currency.getInstance(infoRelativeMonth.getPriceCurrencyCode()));
                    String pricePerMonth = currencyFormat.format(priceYearlyRelative);

                    text = text.replace(var_relative_price, pricePerMonth);
                } else text = text.replace(var_relative_price, "");
            }

            if (text.contains("&amp;")) {
                text = text.replace("&amp;", "&");
            }

            if (text.contains("<p>") && text.contains("</p>")) {
//                String formText = StringUtils.substringBetween(text, "<p>", "</p>");
//                text = text.replace("<p>", "");
//                text = text.replace("</p>", "");
//
//                SimpleText simpleText = SimpleText.from(text)
//                        .first(formText)
//                        .bold()
//                        .textColor(R.color.colorAccent);
//                textView.setText(simpleText);
                return;
            }

            if (text.contains("~~") && text.contains("~~")) {
//                String formText = StringUtils.substringBetween(text, "~~", "~~");
//                text = text.replace("~~", "");
//
//                SimpleText simpleText = SimpleText.from(text)
//                        .first(formText)
//                        .strikethrough();
//                textView.setText(simpleText);
                return;
            }

        } catch (Exception ignored) {
        }
        textView.setText(text.trim());
    }

    protected String getPeriod(SubscriptionInfoModel info) {
        if (info == null)
            return "";
        try {
            if (info.getSubscriptionPeriod().contains("p1m"))
                return activity.getString(R.string.subscription_period_month);
            else if (info.getSubscriptionPeriod().contains("p1y"))
                return activity.getString(R.string.subscription_period_year);
        } catch (Exception ignored) {
        }
        return "";
    }

    protected String getTrialPeriod(SubscriptionInfoModel info) {
        if (info == null)
            return "";
        try {
            if (info.getSubscriptionPeriod().contains("3dt"))
                return activity.getString(R.string.trial_period_3_days);
            else if (info.getSubscriptionPeriod().contains("7dt"))
                return activity.getString(R.string.trial_period_7_days);
        } catch (Exception ignored) {
        }
        return "";
    }

    protected int getVideoById(String video, @RawRes int defaultVideo) {
        if (video == null || video.trim().isEmpty())
            return defaultVideo;

        switch (video) {
            case "vid_intro_main":
                return R.raw.vid_subs_1;
            case "vid_intro_1":
                return R.raw.vid_subs_1;
            case "vid_intro_2":
                return R.raw.vid_subs_1;
            case "vid_subs_1":
                return R.raw.vid_subs_1;
            case "vid_subs_2":
                return R.raw.vid_subs_2;
            case "vid_subs_3":
                return R.raw.vid_subs_3;
            default:
                return defaultVideo;
        }
    }

    protected SubscriptionInfoModel getPlanInfo(AugmentedSkuDetails selectedSku) {
        try {
            return new Gson().fromJson(selectedSku.getOriginalJson(), SubscriptionInfoModel.class);
        } catch (Exception ignored) {
            return new Gson().fromJson(skuDetailsYearly.getOriginalJson(), SubscriptionInfoModel.class);
        }
    }

    protected AugmentedSkuDetails getSkuDetails(String sku) {
        try {
            sku = checkIOSCompatibility(sku);
            for (AugmentedSkuDetails skuDetails : subsSkuDetailsList)
                if (skuDetails.getSku().equals(sku))
                    return skuDetails;
        } catch (Exception ignored) {
        }
        return skuDetailsYearly;
    }

    protected String checkIOSCompatibility(String sku) {
        try {
            if (sku.trim().equals(BillingRepository.BillingSku.IOS_PREMIUM_MONTHLY))
                return BillingRepository.BillingSku.PREMIUM_MONTHLY;
            if (sku.trim().equals(BillingRepository.BillingSku.IOS_PREMIUM_YEARLY))
                return BillingRepository.BillingSku.PREMIUM_YEARLY;
            if (sku.trim().equals(BillingRepository.BillingSku.IOS_PREMIUM_SPECIAL_MONTHLY))
                return BillingRepository.BillingSku.PREMIUM_SPECIAL_MONTHLY;
            if (sku.trim().equals(BillingRepository.BillingSku.IOS_PREMIUM_SPECIAL_YEARLY))
                return BillingRepository.BillingSku.PREMIUM_SPECIAL_YEARLY;
            if (sku.trim().equals(BillingRepository.BillingSku.IOS_PREMIUM_MONTHLY_TRIAL))
                return BillingRepository.BillingSku.PREMIUM_MONTHLY_TRIAL;
            if (sku.trim().equals(BillingRepository.BillingSku.IOS_PREMIUM_YEARLY_TRIAL))
                return BillingRepository.BillingSku.PREMIUM_YEARLY_TRIAL;
            if (sku.trim().equals(BillingRepository.BillingSku.IOS_PREMIUM_SPECIAL_MONTHLY_TRIAL))
                return BillingRepository.BillingSku.PREMIUM_SPECIAL_MONTHLY_TRIAL;
            if (sku.trim().equals(BillingRepository.BillingSku.IOS_PREMIUM_SPECIAL_YEARLY_TRIAL))
                return BillingRepository.BillingSku.PREMIUM_SPECIAL_YEARLY_TRIAL;
        } catch (Exception ignored) {
        }
        return sku;
    }

    @Deprecated
    protected SubscriptionInfoModel getPlanInfo(String sku) {
        try {
            AugmentedSkuDetails selectedSku = skuDetailsYearly;
            for (AugmentedSkuDetails skuDetails : subsSkuDetailsList) {
                if (skuDetails.getSku().equals(sku)) {
                    selectedSku = skuDetails;
                    break;
                }
            }
            return new Gson().fromJson(selectedSku.getOriginalJson(), SubscriptionInfoModel.class);
        } catch (Exception ignored) {
            return new Gson().fromJson(skuDetailsYearly.getOriginalJson(), SubscriptionInfoModel.class);
        }
    }
}