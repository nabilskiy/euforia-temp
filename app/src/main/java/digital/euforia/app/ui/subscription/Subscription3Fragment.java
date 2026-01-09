package digital.euforia.app.ui.subscription;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import digital.euforia.app.App;
import digital.euforia.app.R;
import digital.euforia.app.billing.localdb.AugmentedSkuDetails;
import digital.euforia.app.billing.model.SubscriptionInfoModel;


/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 ONCREATE. All Rights Reserved.
 */

@UnstableApi
@dagger.hilt.android.AndroidEntryPoint
public class Subscription3Fragment extends SubscriptionFragment {

    private static final int DEFAULT_VIDEO = R.raw.vid_subs_1;

    TextView title;
    TextView subtitle;
    TextView offer;
    PlayerView playerView;
    ImageView imageFallback;
    LinearLayout items;

    private ExoPlayer player;
    private Subscription3Config config;
    private AugmentedSkuDetails selectedSku = skuDetailsYearly;

    @Override
    public AugmentedSkuDetails getSelectedMonthSku() {
        return selectedSku;
    }

    public Subscription3Fragment() {
    }

    public static Subscription3Fragment newInstance(String from, String tag) {
        Subscription3Fragment fragment = new Subscription3Fragment();
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
        return inflater.inflate(R.layout.fragment_subscription_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        // Map views (replacing ButterKnife)
        title = v.findViewById(R.id.title);
        subtitle = v.findViewById(R.id.subtitle);
        offer = v.findViewById(R.id.offer);
        playerView = v.findViewById(R.id.video);
        imageFallback = v.findViewById(R.id.imageFallback);
        items = v.findViewById(R.id.items);
        playerView.post(this::initializePlayer);
    }

    @Override
    protected void onPremiumPreSetup() {
        this.config = new Subscription3Config(activity);
    }

    @Override
    protected String onGetSubscriptionButtonText() {
        return getString(R.string.subscription_button_title);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPremiumPostSetup() {
        try {
            selectedSku = getSkuDetails(config.subscription);
            SubscriptionInfoModel info = getPlanInfo(selectedSku);
            if (info == null)
                throw new NullPointerException();

            displayText(title, config.title, getString(R.string.subscription_title), info, null);
            displayText(getPremium, config.button, getString(R.string.subscription_button_title), info, null);

            if (config.offer == null || config.offer.trim().isEmpty()) {
                offer.setVisibility(View.GONE);
            } else {
                displayText(offer, config.offer, null, info, null);
                offer.setVisibility(View.VISIBLE);
            }

            if (config.bottom_text == null || config.bottom_text.trim().isEmpty()) {
                subtitle.setVisibility(View.GONE);
            } else {
                displayText(subtitle, config.bottom_text, null, info, null);
                subtitle.setVisibility(View.VISIBLE);
            }

            if (config.items == null || config.items.isEmpty()) {
                items.setVisibility(View.GONE);
            } else {
                items.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(activity);
                for (int i = 0; i < config.items.size(); i++) {
                    TextView item = (TextView) inflater.inflate(R.layout.view_item_subscription_3, items, false);
                    item.setText(config.items.get(i).trim());
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
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
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
}