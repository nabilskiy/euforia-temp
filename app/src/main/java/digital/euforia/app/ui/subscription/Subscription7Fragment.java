package digital.euforia.app.ui.subscription;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.datasource.RawResourceDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.google.gson.Gson;

import digital.euforia.app.App;
import digital.euforia.app.R;
import digital.euforia.app.billing.localdb.AugmentedSkuDetails;
import digital.euforia.app.billing.model.SubscriptionInfoModel;
import digital.euforia.app.domain.usecase.translation.GetTranslationUseCase;

import javax.inject.Inject;


/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

@UnstableApi
@dagger.hilt.android.AndroidEntryPoint
public class Subscription7Fragment extends SubscriptionFragment {

    @Inject
    GetTranslationUseCase getTranslationUseCase;

    private static final int DEFAULT_VIDEO = R.raw.vid_subs_4;

    TextView title;
    PlayerView playerView;
    LinearLayout items;
    CardView subscribeLayout;
    TextView offer;
    View monthly;
    View annual;
    TextView titleMonthly;
    TextView priceMonthly;
    TextView titleYearly;
    TextView priceYearly;
    TextView label1Yearly;
    TextView label2Yearly;
    LinearLayout container;

    private ExoPlayer player;
    private Subscription7Config config;
    private AugmentedSkuDetails selectedMonthSku = skuDetailsMonthly;
    private AugmentedSkuDetails selectedYearSku = skuDetailsYearly;

    public Subscription7Fragment() {
    }

    public static Subscription7Fragment newInstance(String from, String tag) {
        Subscription7Fragment fragment = new Subscription7Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("from", from);
        bundle.putString("tag", tag);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected boolean enableYearlySubscription() {
        return true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription_7, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        // Map views (replacing ButterKnife)
        title = v.findViewById(R.id.title);
        playerView = v.findViewById(R.id.video);
        items = v.findViewById(R.id.items);
        subscribeLayout = v.findViewById(R.id.subscribeLayout);
        offer = v.findViewById(R.id.terms);
        monthly = v.findViewById(R.id.monthly);
        annual = v.findViewById(R.id.annual);
        titleMonthly = v.findViewById(R.id.titleMonthly);
        priceMonthly = v.findViewById(R.id.priceMonthly);
        titleYearly = v.findViewById(R.id.titleYearly);
        priceYearly = v.findViewById(R.id.priceYearly);
        label1Yearly = v.findViewById(R.id.label1Yearly);
        label2Yearly = v.findViewById(R.id.label2Yearly);
        container = v.findViewById(R.id.subscribeContainer);
        if (annual != null) annual.setOnClickListener(view -> onAnnualClick());
        if (monthly != null) monthly.setOnClickListener(view -> onMonthlyClick());
        playerView.post(this::initializePlayer);
    }

    @Override
    protected void onPremiumPreSetup() {
        this.config = new Subscription7Config(activity, getTranslationUseCase);
    }

    void onAnnualClick() {
        annual.setSelected(true);
        monthly.setSelected(false);
        if (config.offer_year == null || config.offer_year.trim().isEmpty())
            offer.setVisibility(View.GONE);
        else {
            displayText(offer, config.offer_year, null, getYearPlanInfo(), getMonthPlanInfo());
            offer.setVisibility(View.VISIBLE);
        }
        String textButton = config.button_year;
        getPremium.setText(textButton.trim().isEmpty() ? getString(R.string.subscription_button_title) : textButton);

        displayText(getPremium, config.button_year, getString(R.string.subscription_button_title), getYearPlanInfo(), getMonthPlanInfo());
    }

    void onMonthlyClick() {
        annual.setSelected(false);
        monthly.setSelected(true);

        if (config.offer_month == null || config.offer_month.trim().isEmpty())
            offer.setVisibility(View.GONE);
        else {
            displayText(offer, config.offer_month, null, getMonthPlanInfo(), null);
            offer.setVisibility(View.VISIBLE);
        }
        displayText(getPremium, config.button_month, getString(R.string.subscription_button_title), getMonthPlanInfo(), null);
    }

    @Override
    protected String onGetSubscriptionButtonText() {
        return getString(R.string.subscription_button_title);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPremiumPostSetup() {
        try {
            displayText(title, config.title, getString(R.string.subscription_title), null, null);

            SubscriptionInfoModel infoMonthly = null, infoYearly = null;

            switch (config.subs_display) {
                case Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_MONTH:
                    infoMonthly = getMonthPlanInfo();
                    break;
                case Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_YEAR:
                    infoYearly = getYearPlanInfo();
                    break;
                case Subscription1Config.SUBSCRIPTION_DISPLAY_TYPE_OFF:
                    hideSubscriptions();
                    return;
                default:
                    infoMonthly = getMonthPlanInfo();
                    infoYearly = getYearPlanInfo();
                    break;
            }

            if (infoMonthly == null && infoYearly == null) {
                throw new NullPointerException();
            }

            if (infoMonthly != null) {
                priceMonthly.setText(infoMonthly.getPrice() + getString(R.string.price_per_month));
                displayText(titleMonthly, config.name_month, getString(R.string.subscription_title_monthly), infoMonthly, null);

                monthly.setVisibility(View.VISIBLE);
                if (infoYearly == null) {
                    onMonthlyClick();
                    container.setLayoutTransition(new LayoutTransition());
                }
            }

            if (infoYearly != null) {
                //priceYearly.setText(infoYearly.getPrice());
                displayText(priceYearly, "%price_per_month%" + getString(R.string.price_per_month), infoYearly.getPrice(), infoYearly, getMonthPlanInfo());
                displayText(titleYearly, config.name_year, getString(R.string.subscription_title_yearly), infoYearly, getMonthPlanInfo());
                displayText(label1Yearly, config.year_line2, getString(R.string.best_price_label), infoYearly, getMonthPlanInfo());
                displayText(label2Yearly, config.year_label, getString(R.string.save_label), infoYearly, getMonthPlanInfo());

                annual.setVisibility(View.VISIBLE);
                onAnnualClick();
                container.setLayoutTransition(new LayoutTransition());
            }

            if (config.items == null || config.items.isEmpty()) {
                items.setVisibility(View.GONE);
            } else {
                items.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(activity);
                for (int i = 0; i < config.items.size(); i++) {
                    View item = inflater.inflate(R.layout.view_item_subscription_8, items, false);
                    ((TextView) item.findViewById(R.id.itemTitle)).setText(config.items.get(i).getTitle().trim());
                    ((TextView) item.findViewById(R.id.itemSubtitle)).setText(config.items.get(i).getText().trim());
                    items.addView(item);
                }
                items.setVisibility(View.VISIBLE);
            }

        } catch (Exception ignored) {
            Toast.makeText(activity, R.string.text_premium_post_setup_error, Toast.LENGTH_LONG).show();
            hideSubscriptions();
        }
    }

    private void hideSubscriptions() {
        if (isViewAvailable()) {
            monthly.setVisibility(View.GONE);
            annual.setVisibility(View.GONE);
            getPremium.setEnabled(false);
            getPremium.setAlpha(0.6f);
        }
    }

    private void initializePlayer() {
        try {
            String video = config.background;

            if (video != null && URLUtil.isValidUrl(video)) {
                SimpleCache cache = App.Companion.getExoCache(activity.getApplicationContext());

                HttpDataSource.Factory httpDataSourceFactory =
                        new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true);

                DataSource.Factory cacheDataSourceFactory =
                        new CacheDataSource.Factory()
                                .setCache(cache)
                                .setUpstreamDataSourceFactory(httpDataSourceFactory);

                player = new ExoPlayer.Builder(activity).setMediaSourceFactory(new DefaultMediaSourceFactory(cacheDataSourceFactory)).build();
                playerView.setPlayer(player);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                player.setPlayWhenReady(true);
                player.setMediaItem(MediaItem.fromUri(video));
            } else {
                int localVideoId = getVideoById(video, DEFAULT_VIDEO);

                DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(localVideoId));
                final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(activity);
                try {
                    rawResourceDataSource.open(dataSpec);
                } catch (RawResourceDataSource.RawResourceDataSourceException ignored) {
                }
                DataSource.Factory factory = () -> rawResourceDataSource;

                player = new ExoPlayer.Builder(activity).setMediaSourceFactory(new DefaultMediaSourceFactory(factory)).build();
                playerView.setPlayer(player);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                player.setPlayWhenReady(true);
                player.setMediaItem(MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(localVideoId)));
            }
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.prepare();

            subscribeLayout.setTranslationY(subscribeLayout.getHeight() * 1.5f);
        } catch (Exception ignored) {
        }
    }

    private void releasePlayer() {
        try {
            if (player != null) {
                player.release();
                player = null;
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (player != null)
                player.pause();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (subscribeLayout != null)
                subscribeLayout.post(() -> {
                    try {
                        subscribeLayout.animate().setStartDelay(300).translationY(0).alpha(1f).setDuration(600).setInterpolator(new DecelerateInterpolator()).start();
                    } catch (Exception ignored) {
                    }
                });

            if (player != null)
                player.play();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        releasePlayer();
        super.onDestroyView();
    }

    @Override
    public AugmentedSkuDetails getSelectedMonthSku() {
        if (monthly.isSelected())
            return selectedMonthSku;
        else return selectedYearSku;
    }

    private SubscriptionInfoModel getMonthPlanInfo() {
        try {
            switch (config.subs_month) {
                case Subscription1Config.SUBSCRIPTION_MONTH_TRIAL:
                    selectedMonthSku = skuDetailsMonthlyTrial;
                    break;
                case Subscription1Config.SUBSCRIPTION_MONTH_SPECIAL:
                    selectedMonthSku = skuDetailsMonthlySpecial;
                    break;
                case Subscription1Config.SUBSCRIPTION_MONTH_SPECIAL_TRIAL:
                    selectedMonthSku = skuDetailsMonthlySpecialTrial;
                    break;
                default:
                    selectedMonthSku = skuDetailsMonthly;
            }
            return new Gson().fromJson(selectedMonthSku.getOriginalJson(), SubscriptionInfoModel.class);
        } catch (Exception ignored) {
            return new Gson().fromJson(skuDetailsMonthly.getOriginalJson(), SubscriptionInfoModel.class);
        }
    }

    private SubscriptionInfoModel getYearPlanInfo() {
        try {
            switch (config.subs_year) {
                case Subscription1Config.SUBSCRIPTION_YEAR_TRIAL:
                    selectedYearSku = skuDetailsYearlyTrial;
                    break;
                case Subscription1Config.SUBSCRIPTION_YEAR_SPECIAL:
                    selectedYearSku = skuDetailsYearlySpecial;
                    break;
                case Subscription1Config.SUBSCRIPTION_YEAR_SPECIAL_TRIAL:
                    selectedYearSku = skuDetailsYearlySpecialTrial;
                    break;
                default:
                    selectedYearSku = skuDetailsYearly;
            }
            return new Gson().fromJson(selectedYearSku.getOriginalJson(), SubscriptionInfoModel.class);
        } catch (Exception ignored) {
            return new Gson().fromJson(skuDetailsYearly.getOriginalJson(), SubscriptionInfoModel.class);
        }
    }
}