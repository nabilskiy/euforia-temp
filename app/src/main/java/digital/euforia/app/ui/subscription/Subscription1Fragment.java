package digital.euforia.app.ui.subscription;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
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
//import digital.euforia.app.LulubyApp;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import digital.euforia.app.App;
import digital.euforia.app.R;
import digital.euforia.app.billing.localdb.AugmentedSkuDetails;
import digital.euforia.app.billing.model.SubscriptionInfoModel;
import digital.euforia.app.databinding.FragmentSubscription1Binding;
import digital.euforia.app.domain.model.subscription.PremiumBenefit;
import digital.euforia.app.domain.usecase.subscription.GetPremiumBenefitsUseCase;
import digital.euforia.app.domain.usecase.translation.GetTranslationUseCase;
import javax.inject.Inject;
//import digital.euforia.app.model.service.SubscriptionInfoModel;


/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

@UnstableApi
@AndroidEntryPoint
public class Subscription1Fragment extends SubscriptionFragment {

    @Inject
    GetTranslationUseCase getTranslationUseCase;

    @Inject
    GetPremiumBenefitsUseCase getPremiumBenefitsUseCase;

    private static final int DEFAULT_VIDEO = R.raw.vid_vibes_7;
    private static final String DEFAULT_TITLE_COLOR = "#E6FFFFFF";

    private FragmentSubscription1Binding binding;

    TextView title;
    TextView offer;
    View monthly;
    View annual;
    TextView titleMonthly;
    TextView priceMonthly;
    TextView titleYearly;
    TextView priceYearly;
    TextView label1Yearly;
    TextView label2Yearly;
    PlayerView playerView;
    ImageView imageFallback;
    LinearLayout container;
    androidx.recyclerview.widget.RecyclerView benefitsList;
    private PremiumBenefitsAdapter benefitsAdapter;
    private final Runnable benefitsAutoScroll = new Runnable() {
        @Override public void run() {
            if (benefitsList == null) return;
            benefitsList.smoothScrollBy(2, 0); // slow left scroll
            benefitsList.removeCallbacks(this);
            benefitsList.postDelayed(this, 30);
        }
    };

    private ExoPlayer player;
    private Subscription1Config config;
    private AugmentedSkuDetails selectedMonthSku = skuDetailsMonthly;
    private AugmentedSkuDetails selectedYearSku = skuDetailsYearly;
    private List<PremiumBenefit> premiumBenefits;
    public Subscription1Fragment() {
    }

    public static Subscription1Fragment newInstance(String from, String tag) {
        Subscription1Fragment fragment = new Subscription1Fragment();
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
        binding = digital.euforia.app.databinding.FragmentSubscription1Binding.inflate(inflater, container, false);
        // Map existing fields for minimal code changes
        title = binding.title;
        offer = binding.offer;
        monthly = binding.monthly;
        annual = binding.annual;
        titleMonthly = binding.titleMonthly;
        priceMonthly = binding.priceMonthly;
        titleYearly = binding.titleYearly;
        priceYearly = binding.priceYearly;
        label1Yearly = binding.label1Yearly;
        label2Yearly = binding.label2Yearly;
        playerView = binding.video;
        imageFallback = binding.imageFallback;
        container = binding.container;
        benefitsList = binding.benefitsList;
        // Click listeners replacing ButterKnife
        annual.setOnClickListener(v -> onAnnualClick());
        monthly.setOnClickListener(v -> onMonthlyClick());
        premiumBenefits = getPremiumBenefitsUseCase.getNow();

        // Setup benefits recycler
        if (benefitsList != null) {
            androidx.recyclerview.widget.LinearLayoutManager lm = new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false);
            benefitsList.setLayoutManager(lm);
            benefitsAdapter = new PremiumBenefitsAdapter(requireContext());
            // infinite scrolling unless list size < 2
            boolean infinite = premiumBenefits != null && premiumBenefits.size() > 1;
            benefitsAdapter.setInfinite(infinite);
            benefitsList.setAdapter(benefitsAdapter);
            benefitsAdapter.submitList(premiumBenefits);
            if (infinite) {
                int start = Integer.MAX_VALUE / 2;
                // align to first item boundary
                if (premiumBenefits != null && !premiumBenefits.isEmpty()) {
                    start = start - (start % premiumBenefits.size());
                }
                benefitsList.scrollToPosition(start);
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        playerView.post(this::initializePlayer);
        // start auto-scroll when view is laid out
        if (benefitsList != null && benefitsAdapter != null) {
            boolean canScroll = premiumBenefits != null && premiumBenefits.size() > 1;
            if (canScroll) {
                benefitsList.removeCallbacks(benefitsAutoScroll);
                benefitsList.postDelayed(benefitsAutoScroll, 800);
            }
        }
    }

    @Override
    protected void onPremiumPreSetup() {
        this.config = new Subscription1Config(activity, getTranslationUseCase);
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
            String currentTitle = getTitle();
            displayText(title,
                    currentTitle.trim().isEmpty() ? getString(R.string.subscription_title) : currentTitle,
                    null, null, null);
            title.setAllCaps(config.title_caps);
            title.setMaxLines(config.title_max_lines > 0 ? config.title_max_lines : 2);

            int color;
            try {
                color = Color.parseColor(config.title_color);
            } catch (Exception e) {
                color = Color.parseColor(DEFAULT_TITLE_COLOR);
            }
            title.setTextColor(color);

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
                priceMonthly.setText(infoMonthly.getPrice());
                displayText(titleMonthly, config.name_month, getString(R.string.subscription_title_monthly), infoMonthly, null);

                monthly.setVisibility(View.VISIBLE);
                if (infoYearly == null) {
                    onMonthlyClick();
                    container.setLayoutTransition(new LayoutTransition());
                }
            }

            if (infoYearly != null) {
                priceYearly.setText(infoYearly.getPrice());
                displayText(titleYearly, config.name_year, getString(R.string.subscription_title_yearly), infoYearly, getMonthPlanInfo());
                displayText(label1Yearly, config.year_line2, getString(R.string.best_price_label), infoYearly, getMonthPlanInfo());
                displayText(label2Yearly, config.year_label, getString(R.string.save_label), infoYearly, getMonthPlanInfo());

                annual.setVisibility(View.VISIBLE);
                onAnnualClick();
//                container.setLayoutTransition(new LayoutTransition());
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
            String video = getVideo();

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
        } catch (Exception ignored) {
            try {
                if (isViewAvailable()) {
                    imageFallback.setImageResource(R.drawable.img_premium_bg);
                    imageFallback.setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored1) {
            }
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
        try {
            if (benefitsList != null) {
                benefitsList.removeCallbacks(benefitsAutoScroll);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (player != null)
                player.play();
        } catch (Exception ignored) {
        }
        try {
            boolean canScroll = premiumBenefits != null && premiumBenefits.size() > 1;
            if (benefitsList != null && canScroll) {
                benefitsList.removeCallbacks(benefitsAutoScroll);
                benefitsList.postDelayed(benefitsAutoScroll, 600);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        releasePlayer();
        try {
            if (benefitsList != null) {
                benefitsList.removeCallbacks(benefitsAutoScroll);
                benefitsList.setAdapter(null);
            }
        } catch (Exception ignored) {
        }
        super.onDestroyView();
    }

    @Override
    public AugmentedSkuDetails getSelectedMonthSku() {
        if (monthly.isSelected())
            return selectedMonthSku;
        else return selectedYearSku;
    }

    private String getTitle() {
        switch (from) {
            case SubscriptionFragment.FROM_INTRO:
                return config.title_from_intro;
            case SubscriptionFragment.FROM_SECOND:
                return config.title_from_second;
            case SubscriptionFragment.FROM_PRIMARY:
            default:
                return config.title_from_primary;
        }
    }

    private String getVideo() {
        switch (from) {
            case SubscriptionFragment.FROM_INTRO:
                return config.video_from_intro;
            case SubscriptionFragment.FROM_SECOND:
                return config.video_from_second;
            case SubscriptionFragment.FROM_PRIMARY:
            default:
                return config.video_from_primary;
        }
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