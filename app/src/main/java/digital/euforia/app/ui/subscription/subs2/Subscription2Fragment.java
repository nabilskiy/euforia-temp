package digital.euforia.app.ui.subscription.subs2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import androidx.recyclerview.widget.RecyclerView;

//import digital.euforia.app.LulubyApp;
//import digital.euforia.app.R;
//import digital.euforia.app.adapter.recycler.ExtraSubscription2Adapter;
//import digital.euforia.app.adapter.recycler.ReviewsAdapter;
import digital.euforia.app.R;
import digital.euforia.app.billing.localdb.AugmentedSkuDetails;
import digital.euforia.app.ui.subscription.LockableScrollView;
import digital.euforia.app.ui.subscription.SubscriptionFragment;
//import digital.euforia.app.model.service.SubscriptionInfoModel;
//import digital.euforia.app.ui.subscription.SubscriptionFragment;
//import digital.euforia.app.ui.view.LockableScrollView;
//import digital.euforia.app.util.base.AppUtils;


/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

@UnstableApi
@dagger.hilt.android.AndroidEntryPoint
public class Subscription2Fragment extends SubscriptionFragment {

//    private static final int DEFAULT_VIDEO = R.raw.vid_subs_1;
//
//    CoordinatorLayout coordinatorLayout;
//    View topLayout;
//    LockableScrollView scrollView;
//
//    TextView title;
//    TextView primaryButtonSubtitle;
//    View more;
//
//    LinearLayout benefits;
//    RecyclerView recyclerReviews;
//    RecyclerView recyclerExtra;
//
//    TextView benefitsTitle;
//    TextView reviewsTitle;
//    TextView reviewsSubtitle;
//    TextView plansTitle;
//    TextView extraTitle;
//
//    TextView getPremiumTop;
//    TextView titleBottom;
//    TextView getPremiumBottom;
//    TextView bottomButtonSubtitle;
//
//    View planYearCard;
//    TextView planYearTitle;
//    TextView planYearButton;
//    TextView planYearSubtitle;
//    TextView planYearButtonOffer;
//    TextView planYearLabel;
//
//    View planMonthCard;
//    TextView planMonthTitle;
//    TextView planMonthButton;
//    TextView planMonthSubtitle;
//    TextView planMonthButtonOffer;
//
//    TextView subscribe;
//    PlayerView playerView;
//    ImageView imageFallback;
//
//
//    private ReviewsAdapter reviewsAdapter;
//    private ExtraSubscription2Adapter extraAdapter;
//    private ExoPlayer player;
//    private Subscription2Config config;
//    private AugmentedSkuDetails selectedMonthSku = skuDetailsMonthly;
//    private AugmentedSkuDetails selectedYearSku = skuDetailsYearly;
//
//    @Override
//    public AugmentedSkuDetails getSelectedMonthSku() {
//        return selectedMonthSku;
//    }
//
//    @Override
//    public AugmentedSkuDetails getSelectedYearSku() {
//        return selectedYearSku;
//    }
//
//    public Subscription2Fragment() {
//    }
//
//    public static Subscription2Fragment newInstance(String from, String tag) {
//        Subscription2Fragment fragment = new Subscription2Fragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("from", from);
//        bundle.putString("tag", tag);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//    @Override
//    protected boolean enableYearlySubscription() {
//        return true;
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_subscription_2, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
//        super.onViewCreated(v, savedInstanceState);
//
//        // Set click listeners previously handled by ButterKnife @OnClick
//        View btnSubscribe = v.findViewById(R.id.subscribe);
//        if (btnSubscribe != null) btnSubscribe.setOnClickListener(view -> onGetPremiumYearlyClick());
//        View btnGetPremiumBottom = v.findViewById(R.id.getPremiumBottom);
//        if (btnGetPremiumBottom != null) btnGetPremiumBottom.setOnClickListener(view -> onGetPremiumYearlyClick());
//        View btnGetPremiumTop = v.findViewById(R.id.getPremiumTop);
//        if (btnGetPremiumTop != null) btnGetPremiumTop.setOnClickListener(view -> onGetPremiumYearlyClick());
//
//        coordinatorLayout.post(() -> {
//            try {
//                ViewGroup.LayoutParams lp = topLayout.getLayoutParams();
//                lp.height = coordinatorLayout.getHeight() != 0 ? coordinatorLayout.getHeight() : AppUtils.getScreenHeight(activity);
//                topLayout.setLayoutParams(lp);
//            } catch (Exception ignored) {
//            }
//        });
//
//        playerView.post(this::initializePlayer);
//    }
//
//    @Override
//    protected void onPremiumPreSetup() {
//        this.config = Subscription2Config.newInstance(activity);
//    }
//
//    @Override
//    protected String onGetSubscriptionButtonText() {
//        return getString(R.string.subscription_button_title);
//    }
//
//    @SuppressLint("SetTextI18n")
//    @Override
//    protected void onPremiumPostSetup() {
//        try {
//            PlanConfig planMonth = null, planYear = null;
//            if (config.plans != null && !config.plans.isEmpty()) {
//                for (PlanConfig plan : config.plans)
//                    if (plan.productIdentifier.contains("month")) {
//                        planMonth = plan;
//                        selectedMonthSku = getSkuDetails(plan.productIdentifier);
//                    } else if (plan.productIdentifier.contains("year")) {
//                        planYear = plan;
//                    }
//            }
//            selectedYearSku = getSkuDetails(config.primaryProduct);
//            SubscriptionInfoModel infoMonth = getPlanInfo(selectedMonthSku);
//            SubscriptionInfoModel infoYear = getPlanInfo(selectedYearSku);
//            if (infoMonth == null || infoYear == null)
//                throw new NullPointerException();
//
//            displayText(title, config.title, getString(R.string.subscription_title), infoYear, infoMonth);
//            displayText(getPremiumTop, config.primaryButtonTitle, getString(R.string.subscription_button_title), infoYear, infoMonth);
//            displayText(primaryButtonSubtitle, config.primaryButtonSubtitle, getString(R.string.save_label), infoYear, infoMonth);
//
//            displayText(benefitsTitle, config.benefitsTitle, "", infoYear, infoMonth);
//            displayText(reviewsTitle, getString(R.string.subscription_reviews_title), "", infoYear, infoMonth);
//            displayText(reviewsSubtitle, getString(R.string.subscription_reviews_subtitle), "", infoYear, infoMonth);
//            displayText(plansTitle, config.plansTitle, "", infoYear, infoMonth);
//            displayText(extraTitle, config.extraBenefitsTitle, "", infoYear, infoMonth);
//
//            displayText(titleBottom, config.footerText, getString(R.string.subscription_title), infoYear, infoMonth);
//            displayText(getPremiumBottom, config.primaryBottomButtonTitle, getString(R.string.subscription_button_title), infoYear, infoMonth);
//            displayText(bottomButtonSubtitle, config.primaryButtonSubtitle, getString(R.string.save_label), infoYear, infoMonth);
//
//            // Lists
//
//            if (config.benefits == null || config.benefits.isEmpty()) {
//                benefits.setVisibility(View.GONE);
//            } else {
//                LayoutInflater inflater = LayoutInflater.from(activity);
//                for (int i = 0; i < config.benefits.size(); i++) {
//                    TextView item = (TextView) inflater.inflate(R.layout.view_item_subscription_3, benefits, false);
//                    item.setText(config.benefits.get(i).trim());
//                    benefits.addView(item);
//                }
//                benefits.setVisibility(View.VISIBLE);
//            }
//
//            if (config.reviews != null && !config.reviews.isEmpty()) {
//                if (reviewsAdapter == null || reviewsAdapter.isEmpty()) {
//                    reviewsAdapter = new ReviewsAdapter(activity);
//                    reviewsAdapter.set(config.reviews);
//                }
//                recyclerReviews.setAdapter(reviewsAdapter);
//                recyclerReviews.setVisibility(View.VISIBLE);
//            } else {
//                reviewsTitle.setVisibility(View.GONE);
//                reviewsSubtitle.setVisibility(View.GONE);
//                recyclerReviews.setVisibility(View.GONE);
//            }
//
//            if (config.extraBenefits != null && !config.extraBenefits.isEmpty()) {
//                if (extraAdapter == null || extraAdapter.isEmpty()) {
//                    extraAdapter = new ExtraSubscription2Adapter(activity);
//                    extraAdapter.set(config.extraBenefits);
//                }
//                recyclerExtra.setAdapter(extraAdapter);
//                recyclerExtra.setNestedScrollingEnabled(false);
//                recyclerExtra.setVisibility(View.VISIBLE);
//            } else {
//                extraTitle.setVisibility(View.GONE);
//                recyclerExtra.setVisibility(View.GONE);
//            }
//
//            // Plans
//
//            if (planYear != null) {
//                displayText(planYearTitle, planYear.title, getString(R.string.subscription_title_yearly), infoYear, infoMonth);
//                displayText(planYearButton, planYear.buttonTitle, getString(R.string.subscription_button_title), infoYear, infoMonth);
//                displayText(planYearSubtitle, planYear.details, null, infoYear, infoMonth);
//                displayText(planYearButtonOffer, planYear.buttonSubtitle, null, infoYear, infoMonth);
//                displayText(planYearLabel, planYear.badge, null, infoYear, infoMonth);
//                planYearButton.setOnClickListener(v -> onGetPremiumYearlyClick());
//            } else {
//                ((View) planYearCard.getParent()).setVisibility(View.GONE);
//            }
//
//            if (planMonth != null) {
//                displayText(planMonthTitle, planMonth.title, getString(R.string.subscription_title_yearly), infoMonth, null);
//                displayText(planMonthButton, planMonth.buttonTitle, getString(R.string.subscription_button_title), infoMonth, null);
//                displayText(planMonthSubtitle, planMonth.details, null, infoMonth, null);
//                displayText(planMonthButtonOffer, planMonth.buttonSubtitle, null, infoMonth, null);
//                planMonthButton.setOnClickListener(v -> onGetPremiumClick());
//            } else {
//                planYearCard.setVisibility(View.GONE);
//            }
//
//            // Animation
//
//            animateUp();
//            more.setOnClickListener(v -> {
//                try {
//                    int topHeight = coordinatorLayout.getHeight() != 0 ? coordinatorLayout.getHeight() : AppUtils.getScreenHeight(activity);
//                    scrollView.smoothScrollTo(0, topHeight - getResources().getDimensionPixelOffset(R.dimen.size_56));
//                } catch (Exception ignored) {
//                }
//            });
//
//        } catch (Exception ignored) {
//            Toast.makeText(activity, R.string.text_premium_post_setup_error, Toast.LENGTH_LONG).show();
//            hideSubscriptions();
//        }
//    }
//
//    private void animateUp() {
//        if (isViewAvailable())
//            more.animate()
//                    .translationY(getResources().getDimensionPixelOffset(R.dimen.size_5))
//                    .alpha(0.5f)
//                    .setInterpolator(new DecelerateInterpolator())
//                    .setDuration(1500)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            animateDown();
//                        }
//                    })
//                    .start();
//    }
//
//    private void animateDown() {
//        if (isViewAvailable())
//            more.animate()
//                    .translationY(-getResources().getDimensionPixelOffset(R.dimen.size_5))
//                    .alpha(1f)
//                    .setInterpolator(new DecelerateInterpolator())
//                    .setDuration(1500)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            animateUp();
//                        }
//                    })
//                    .start();
//    }
//
//    private void hideSubscriptions() {
//        if (isViewAvailable()) {
//            getPremiumTop.setEnabled(false);
//            getPremiumTop.setAlpha(0.6f);
//            scrollView.setScrollingEnabled(false);
//            subscribe.setEnabled(false);
//            subscribe.setAlpha(0.6f);
//            more.setVisibility(View.GONE);
//        }
//    }
//
//    private void initializePlayer() {
//        try {
//            String video = config.video;
//
//            if (video != null && URLUtil.isValidUrl(video)) {
//                SimpleCache cache = LulubyApp.getExoCache(activity.getApplicationContext());
//
//                HttpDataSource.Factory httpDataSourceFactory =
//                        new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true);
//
//                DataSource.Factory cacheDataSourceFactory =
//                        new CacheDataSource.Factory()
//                                .setCache(cache)
//                                .setUpstreamDataSourceFactory(httpDataSourceFactory);
//
//                player = new ExoPlayer.Builder(activity).setMediaSourceFactory(new DefaultMediaSourceFactory(cacheDataSourceFactory)).build();
//                playerView.setPlayer(player);
//                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//                player.setPlayWhenReady(true);
//                player.setMediaItem(MediaItem.fromUri(video));
//            } else {
//                int localVideoId = getVideoById(video, DEFAULT_VIDEO);
//
//                DataSpec dataSpec = new DataSpec(RawResourceDataSource.buildRawResourceUri(localVideoId));
//                final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(activity);
//                try {
//                    rawResourceDataSource.open(dataSpec);
//                } catch (RawResourceDataSource.RawResourceDataSourceException ignored) {
//                }
//                DataSource.Factory factory = () -> rawResourceDataSource;
//
//                player = new ExoPlayer.Builder(activity).setMediaSourceFactory(new DefaultMediaSourceFactory(factory)).build();
//                playerView.setPlayer(player);
//                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//                player.setPlayWhenReady(true);
//                player.setMediaItem(MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(localVideoId)));
//            }
//            player.setRepeatMode(Player.REPEAT_MODE_ALL);
//            player.prepare();
//        } catch (Exception ignored) {
//            try {
//                if (isViewAvailable()) {
//                    imageFallback.setImageResource(R.drawable.img_premium_bg);
//                    imageFallback.setVisibility(View.VISIBLE);
//                }
//            } catch (Exception ignored1) {
//            }
//        }
//    }
//
//    private void releasePlayer() {
//        try {
//            if (player != null) {
//                player.release();
//                player = null;
//            }
//        } catch (Exception ignored) {
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        try {
//            if (player != null)
//                player.pause();
//        } catch (Exception ignored) {
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        try {
//            if (player != null)
//                player.play();
//        } catch (Exception ignored) {
//        }
//    }
//
//    @Override
//    public void onDestroyView() {
//        releasePlayer();
//        super.onDestroyView();
//    }

}
