package digital.euforia.app.ui.subscription;


import static digital.euforia.app.ui.subscription.SubscriptionFragment.FROM_PRIMARY;
import static digital.euforia.app.ui.subscription.SubscriptionFragment.FROM_SECOND;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;

import com.android.billingclient.api.Purchase;
import androidx.media3.common.util.UnstableApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.firebase.inappmessaging.FirebaseInAppMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
//import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
//import com.lapism.searchview.SearchView;
import dagger.hilt.android.AndroidEntryPoint;
import digital.euforia.app.R;
import androidx.media3.common.util.UnstableApi;
//import digital.euforia.app.adapter.composite.HomeFooterType;
//import digital.euforia.app.adapter.composite.SettingsType;
//import digital.euforia.app.adapter.composite.album.AlbumCategoryType;
//import digital.euforia.app.adapter.composite.album.AlbumListType;
//import digital.euforia.app.adapter.composite.album.AlbumPremiumType;
//import digital.euforia.app.adapter.composite.album.AlbumType;
//import digital.euforia.app.adapter.composite.album.LastAlbumType;
//import digital.euforia.app.adapter.composite.album.RandomAlbumType;
//import digital.euforia.app.adapter.composite.composition.CompositionAlbumType;
//import digital.euforia.app.adapter.composite.composition.CompositionListType;
//import digital.euforia.app.adapter.composite.composition.CompositionPlaylistType;
//import digital.euforia.app.adapter.composite.composition.CompositionPremiumType;
//import digital.euforia.app.adapter.composite.playlist.PlaylistCategoryType;
//import digital.euforia.app.adapter.composite.playlist.PlaylistListType;
//import digital.euforia.app.adapter.composite.playlist.PlaylistPremiumType;
//import digital.euforia.app.api.API;
//import digital.euforia.app.api.APIConfig;
//import digital.euforia.app.audio.Player;
import digital.euforia.app.billing.BillingRepository;
import digital.euforia.app.billing.BillingViewModel;
//import digital.euforia.app.data.Configuration;
//import digital.euforia.app.data.PreferencesManager;
//import digital.euforia.app.data.TimeManager;
//import digital.euforia.app.data.events.SpeechFromUserEvent;
//import digital.euforia.app.data.events.app.ShowPremiumBackgroundDialog;
//import digital.euforia.app.data.provider.AlbumDataProvider;
//import digital.euforia.app.model.AlbumModel;
//import digital.euforia.app.model.CompositionModel;
//import digital.euforia.app.model.PlaylistModel;
//import digital.euforia.app.model.service.SubscriptionModel;
//import digital.euforia.app.ui.base.AppNavigation;
//import digital.euforia.app.ui.base.BaseFragment;
//import digital.euforia.app.ui.base.FragmentBaseActivity;
//import digital.euforia.app.ui.fragment.AlbumFragment;
//import digital.euforia.app.ui.fragment.HomeFragment;
//import digital.euforia.app.ui.fragment.MyPlaylistsFragment;
//import digital.euforia.app.ui.fragment.PlaylistFragment;
//import digital.euforia.app.ui.fragment.SearchFragment;
//import digital.euforia.app.util.analytics.Analytics;
//import digital.euforia.app.util.base.DialogFactory;
import digital.euforia.app.data.analytics.AnalyticSender;
import digital.euforia.app.data.store.AppPreferences;
import digital.euforia.app.domain.usecase.subscription.SyncPurchaseUseCase;
import digital.euforia.app.ui.subscription.RC;
//import com.oncreate.composite.core.CompositeController;
//import com.oncreate.composite.core.CompositeType;
//import com.oncreate.composite.core.adapter.CompositeCallback;
//import com.oncreate.composite.core.banner.BannerListType;
//import com.oncreate.composite.core.banner.BannerModel;
//import com.oncreate.composite.core.banner.BannerType;

//import org.greenrobot.eventbus.EventBus;
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import digital.euforia.app.databinding.ActivityUserBinding;
//import pro.oncreate.easynet.data.NError;
//import pro.oncreate.easynet.models.NRequestModel;
//import pro.oncreate.easynet.models.NResponseModel;
//import pro.oncreate.easynet.processing.NCallbackGson;
//import pro.oncreate.easynet.utils.NHelper;

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */

@AndroidEntryPoint
public class UserActivity extends digital.euforia.app.ui.base.FragmentBaseActivity
//        implements BottomNavigationView.OnNavigationItemSelectedListener,
//        CompositeCallback, CompositeController.SubscriptionStatus
{

    public static final String EXTRA_SINGLE_FRAGMENT = "activitySingleFragmentMode";
    public static final String EXTRA_RESUME_FRAGMENT = "activityResumeFragmentMode";
    public static final String EXTRA_SCREEN_ID = "extra_screen_id";
    public static final int PURCHASE_SUCCESS = 754;

    private BillingViewModel billingViewModel;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
//    private AlbumDataProvider albumProvider;
//    private Player player;
//    private CompositeController compositeController;
//    private AppNavigation appNavigation;
    private boolean isSubscriptionValid = false;

//    public enum NavigationItem {
//        HomeTab, SearchTab, PlaylistsTab, PremiumTab
//    }

    private ActivityUserBinding binding;
    View layout;
//    BottomNavigationViewEx bottomNavigation;
    View bottomNavigationLayout;

    public int lastSelectedItem = -1;
    public boolean activitySingleFragmentMode = false;

    private AlertDialog premiumBackgroundDialog;

    @Inject
    protected SyncPurchaseUseCase syncPurchaseUseCase;

    @Inject
    protected AnalyticSender analyticSender;

    @Override
    @UnstableApi
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.colorPrimary);
        updateNavigationBarColor();

//        appNavigation = new AppNavigation(this);
//
//        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(false);
//        FirebaseInAppMessaging.getInstance().triggerEvent("main_launch");
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        Analytics.main_screen_show(getAnalytics());

        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Map views formerly bound by ButterKnife
        layout = binding.layout;
//        bottomNavigation = binding.bottomNavigation;
        bottomNavigationLayout = binding.bottomNavigationLayout;
//        PreferencesManager.launchIncrement(this);

//        setupBottomNavigation();
//        setupComposite();

//        player = new Player(this);
//        player.setMiniPlayer(findViewById(R.id.miniPlayer));
//        player.onCreate();

        billingViewModel = new ViewModelProvider(this).get(BillingViewModel.class);
        billingViewModel.getPremiumLiveData().observe(this, premium -> {
            if (premium != null && premium.getEntitled()) {
//                PreferencesManager.setIsPro(this, true);
                syncPurchaseToken();
//                removeSubscriptionTab();
                p = true;
            } else {
//                PreferencesManager.setIsPro(this, false);
//                PreferencesManager.setSubsToken(this, "");
                onSubscriptionUpdated(false, false);
            }
        });

//        PreferencesManager.setRCUpdateInterval(this, RC.getInt(getRC(), "remote_config_expiration_duration"));
        getSupportFragmentManager().addOnBackStackChangedListener(this::updateNavigationBarColor);

//        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation, (v, insets) -> {
//            Object lpOriginalHeight = v.getTag("original_height".hashCode());
//            ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
//            int originalHeight;
//            if (!(lpOriginalHeight instanceof Integer)) {
//                originalHeight = layoutParams.height;
//                v.setTag("original_height".hashCode(), originalHeight);
//            } else {
//                originalHeight = (int) lpOriginalHeight;
//            }
//            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
//            layoutParams.height = originalHeight + bottomInset;
//            v.setPadding(0, 0 ,0, bottomInset);
//            v.forceLayout();
//            return insets;
//        });
        // If screenId was provided via Intent, open appropriate subscription screen
        int providedScreenId = getIntent() != null ? getIntent().getIntExtra(EXTRA_SCREEN_ID, -1) : -1;
        if (providedScreenId > 0) {
            replaceFragmentSaveState(SubscriptionFragment.newInstance(this, SubscriptionFragment.FROM_PRIMARY, providedScreenId, null));
        } else {
            replaceFragmentSaveState(SubscriptionFragment.newInstance(this, SubscriptionFragment.FROM_PRIMARY));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        premiumBackgroundDialog = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        player.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        player.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        appNavigation.navigate(intent);
    }

    public void onSubscriptionUpdated(boolean hasPremium, boolean isUpdated) {
        try {
//            if (getBackStackCount() <= 1)
//                appNavigation.navigate(getIntent());
//            else {
//                lastSelectedItem = -1;
//                selectNavigationItem(NavigationItem.HomeTab, true);
//            }
        } catch (Exception ignored) {
        }
    }
//
//    private void setupComposite() {
//        compositeController = CompositeController.newInstance()
//                .setCallback(this)
//                .register(new BannerType())
//                .register(new BannerListType(this))
//                .register(new HomeFooterType())
//                .register(new SettingsType(this))
//
//                .register(new AlbumType(this))
//                .register(new LastAlbumType(this))
//                .register(new RandomAlbumType(this))
//                .register(new AlbumListType(this))
//                .register(new AlbumCategoryType(this))
//                .register(new AlbumPremiumType(this))
//
//                .register(new PlaylistListType(this))
//                .register(new PlaylistCategoryType(this))
//                .register(new PlaylistPremiumType(this))
//
//                .register(new CompositionListType(this))
//                .register(new CompositionPlaylistType(this))
//                .register(new CompositionAlbumType(this))
//                .register(new CompositionPremiumType(this));
//
//        CompositeController.addClass("album", AlbumModel.class);
//        CompositeController.addClass("playlist", PlaylistModel.class);
//        CompositeController.addClass("composition", CompositionModel.class);
//        CompositeController.addClassByType("banner", BannerModel.class);
//        CompositeController.addClassByType("banner_list", BannerModel.class);
//    }

//    public Player getPlayer() {
//        return player;
//    }
//
//    @Override
//    public void onItemClick(CompositeType type, Object object) {
//        if (type.getType().equals("album_category")) {
//            try {
//                AlbumModel album = (AlbumModel) object;
//                replaceFragmentSaveState(AlbumFragment.newInstance(album.getId(), album));
//            } catch (Exception ignored) {
//            }
//        } else if (type.getType().equals("playlist_category")) {
//            try {
//                PlaylistModel playlist = (PlaylistModel) object;
//                replaceFragmentSaveState(PlaylistFragment.newInstance(playlist.getId(), playlist));
//            } catch (Exception ignored) {
//            }
//        }
//    }

//    public CompositeController getCompositeController() {
//        if (compositeController == null)
//            setupComposite();
//        return compositeController;
//    }

    @Override
    public void clearBackStack() {
        super.clearBackStack();
        lastSelectedItem = -1;
    }

//    private void setupBottomNavigation() {
//        bottomNavigation.enableAnimation(false);
//        bottomNavigation.enableShiftingMode(false);
//        bottomNavigation.enableItemShiftingMode(false);
//        bottomNavigation.setTextVisibility(true);
//        bottomNavigation.setTextSize(12);
//
//        for (int i = 0; i < bottomNavigation.getItemCount(); i++) {
//            bottomNavigation.setIconTintList(i, ContextCompat.getColorStateList(this,
//                    R.color.bottom_navigation_item_color));
//            bottomNavigation.setTextTintList(i, ContextCompat.getColorStateList(this,
//                    R.color.bottom_navigation_item_color));
//        }
//        bottomNavigation.setOnNavigationItemSelectedListener(this);
//    }

//    private void removeSubscriptionTab() {
//        try {
//            if (PreferencesManager.readIsPro(this)) {
//                bottomNavigation.getMenu().clear();
//                bottomNavigation.inflateMenu(R.menu.menu_bottom_navigation_premium);
//                setupBottomNavigation();
//            }
//        } catch (Exception ignored) {
//        }
//    }
//
//    public void selectNavigationItem(NavigationItem navigationItem) {
//        selectNavigationItem(navigationItem, false);
//    }

//    public void selectNavigationItem(NavigationItem navigationItem, boolean clearStack) {
//        if (clearStack) {
//            bottomNavigation.setCurrentItem(getBottomPositionByItemId(getItemIdByNavigationItem(navigationItem)));
//        } else {
//            selectTab(navigationItem);
//            onNavigationItemSelected(bottomNavigation.getSelectedItemId(), false);
//        }
//    }

//    public void selectTab(NavigationItem navigationItem) {
//        try {
//            bottomNavigation.setOnNavigationItemSelectedListener(null);
//            int index = getBottomPositionByItemId(getItemIdByNavigationItem(navigationItem));
//            bottomNavigation.setCurrentItem(index);
//            bottomNavigation.setOnNavigationItemSelectedListener(this);
//        } catch (Exception ignored) {
//        }
//    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        onNavigationItemSelected(item.getItemId(), true);
//        return true;
//    }
//
//    public synchronized void onNavigationItemSelected(int itemId, boolean clearStack) {
//        if (lastSelectedItem == itemId) {
//            try {
//                BaseFragment currentFragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.content);
//                currentFragment.onTabAlreadySelected();
//            } catch (Exception ignored) {
//            }
//            return;
//        }
//
//        if (clearStack) {
//            clearBackStack();
//            Fresco.getImagePipeline().clearMemoryCaches();
//        }
//
//        switch (itemId) {
//            case R.id.home:
//                replaceFragmentSaveState(HomeFragment.newInstance());
//                Analytics.tab_home_click(getAnalytics());
//                break;
//            case R.id.search:
//                replaceFragmentSaveState(SearchFragment.newInstance());
//                Analytics.tab_search_click(getAnalytics());
//                break;
//            case R.id.playlists:
//                replaceFragmentSaveState(MyPlaylistsFragment.newInstance());
//                Analytics.tab_my_playlists_click(getAnalytics());
//                break;
//            case R.id.premium:
//                int v = RC.getInt(getRC(), "tab_premium_screen_variant");
//                replaceFragmentSaveState(SubscriptionFragment.newInstance(this, FROM_PRIMARY, v, "tab"));
//                Analytics.tab_premium_click(getAnalytics());
//                break;
//        }
//        lastSelectedItem = itemId;
//    }
//
//    public int getBottomPositionByItemId(int itemId) {
//        try {
//            for (int i = 0; i < bottomNavigation.getMenu().size(); i++) {
//                if (bottomNavigation.getMenu().getItem(i).getItemId() == itemId)
//                    return i;
//            }
//        } catch (Exception ignored) {
//        }
//        return 0;
//    }

//    public int getItemIdByNavigationItem(NavigationItem navigationItem) {
//        switch (navigationItem) {
//            case HomeTab:
//                return R.id.home;
//            case SearchTab:
//                return R.id.search;
//            case PlaylistsTab:
//                return R.id.playlists;
//            case PremiumTab:
//                return R.id.premium;
//        }
//        return R.id.home;
//    }

    public FirebaseRemoteConfig getRC() {
        if (mFirebaseRemoteConfig == null)
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return mFirebaseRemoteConfig;
    }

    public void updateRCImmediately() {
        try {
            mFirebaseRemoteConfig.fetch(0)
                    .addOnSuccessListener(unused -> {
                        try {
                            mFirebaseRemoteConfig.activate();
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception ignored) {
        }
    }


//    public AlbumDataProvider getAlbumProvider() {
//        if (albumProvider == null) {
//            albumProvider = new AlbumDataProvider();
//            albumProvider.enableSerialization(this);
//        }
//        return albumProvider;
//    }

//    @Override
//    public void onBackPressed() {
//        try {
//            if (getBackStackCount() <= 1) {
//                Fragment fragment = findFragment(HomeFragment.class.getSimpleName());
//                if (fragment == null && !activitySingleFragmentMode)
//                    selectNavigationItem(NavigationItem.HomeTab, true);
//                else super.onBackPressed();
//            } else super.onBackPressed();
//        } catch (Exception ignored) {
//        }
//    }

    public BillingViewModel getBillingViewModel() {
        return billingViewModel;
    }

    private void syncPurchaseToken() {
        try {
            if (TimeManager.between(this, "syncPurchaseToken",
                    System.currentTimeMillis()) < Configuration.SYNC_PURCHASE_TOKEN_INTERVAL) {
                onSubscriptionUpdated(true, false);
                return;
            }

            String purchaseJson = null;
            List<Purchase> purchases = BillingViewModel.Companion.getRepository().getCurrentPurchases();
            for (Purchase p : purchases) {
                purchaseJson = p.getOriginalJson();
                break;
            }
            syncPurchaseUseCase.execute(purchaseJson);
            if (purchaseJson != null) {
//                API.subscription(purchaseJson)
//                        //.bind(new ProgressDialog(UserActivity.this))
//                        .start(new NCallbackGson<SubscriptionModel>(SubscriptionModel.class) {
//                            @Override
//                            public void onSuccess(SubscriptionModel model, NResponseModel responseModel) {
//                                try {
//                                    if (model.getToken() != null && !model.getToken().isEmpty()) {
//                                        PreferencesManager.setSubsToken(UserActivity.this, model.getToken());
//                                        TimeManager.set(UserActivity.this, "syncPurchaseToken");
//                                        onSubscriptionUpdated(true, true);
//
//                                        setSubscriptionValid(NHelper.getIntHeader(responseModel, APIConfig.HEADER_SUBSCRIPTION_VALID));
//                                    } else {
//                                        onSubscriptionUpdated(true, false);
//                                    }
//                                } catch (Exception ignored) {
//                                    onSubscriptionUpdated(true, false);
//                                }
//                                Analytics.subscription_sent(getAnalytics());
//                            }
//
//                            @Override
//                            public void onFailed(NRequestModel nRequestModel, NError error) {
//                                onSubscriptionUpdated(true, false);
//                            }
//
//                            @Override
//                            public void onError(NResponseModel responseModel) {
//                                onSubscriptionUpdated(true, false);
//                            }
//                        });
            } else {
//                PreferencesManager.setSubsToken(this, "");
//                onSubscriptionUpdated(true, false);
            }
        } catch (Exception ignored) {
//            PreferencesManager.setSubsToken(this, "");
//            onSubscriptionUpdated(true, false);
        }
    }

    public void refreshPurchases() {
        BillingRepository billingRepository = BillingRepository.Companion.getInstance(getApplication());
        // if connectToPlayBillingService() returns true, billing client is NOT ready
        if (!billingRepository.connectToPlayBillingService()) {
            billingRepository.refreshPurchases(purchases -> {
                runOnUiThread(() -> {
                    if (!isFinishing() && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        if (purchases.isEmpty()) {
                            Toast.makeText(this, R.string.purchases_restore_failed_no_active_subscriptions,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.purchases_restore_success,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        } else {
            Toast.makeText(this, R.string.receipt_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
//            if (requestCode == SearchView.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
//                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                if (results != null && results.size() > 0) {
//                    String searchWrd = results.get(0);
//                    if (!TextUtils.isEmpty(searchWrd))
//                        EventBus.getDefault().post(new SpeechFromUserEvent(searchWrd));
//                }
//            }
        } catch (Exception ignored) {
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateNavigationBarColor() {
        Fragment currentFragment = getCurrentFragment();
        boolean blackNavigation = false;

//        getWindow().setNavigationBarColor(blackNavigation ? Color.BLACK
//                : ContextCompat.getColor(this, R.color.colorNavigation));

        getWindow().getDecorView().setSystemUiVisibility(blackNavigation
                ? getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                : getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        try {
            if (blackNavigation) {
                bottomNavigationLayout.setVisibility(View.GONE);
                return;
            }

            BaseFragment cf = (BaseFragment) currentFragment;
            if (!cf.isChildFragment()) {
//                if (cf.getNavigationItem() != null) {
//                    selectTab(cf.getNavigationItem());
//                }
                bottomNavigationLayout.setVisibility(cf.bottomNavigationRequired() ? View.VISIBLE : View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

//    @Deprecated
//    public int getBottomNavigationHeight() {
//        return bottomNavigation.getVisibility() == View.VISIBLE ? bottomNavigationLayout.getHeight() : 0;
//    }

    public boolean isActivitySingleFragmentMode() {
        return activitySingleFragmentMode;
    }

    public boolean isSubscriptionValid() {
        return isSubscriptionValid;
    }

    public void setSubscriptionValid(int subscriptionValid) {
        isSubscriptionValid = subscriptionValid == 1;
    }

    private boolean p;

    public boolean isPremium() {
        return p;
    }

//    @Override
//    public boolean isSubscriptionActive() {
//        try {
//            return PreferencesManager.readIsPro(this);
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void showPremiumBackgroundDialog(ShowPremiumBackgroundDialog event) {
//        Analytics.music_dialog_premium_bg_show(getAnalytics());
//        if (premiumBackgroundDialog == null) {
//            premiumBackgroundDialog = new AlertDialog.Builder(this)
//                    .setMessage(R.string.need_premium_to_background_music)
//                    .setPositiveButton(R.string.get_premium, (dialog12, id) -> {
//                        Analytics.music_dialog_premium_bg_subscribe(getAnalytics());
//                        replaceFragmentSaveState(SubscriptionFragment.newInstance(this, FROM_SECOND));
//                    })
//                    .setNegativeButton(R.string.continue_without_premium, (dialog12, id) -> {
//                        Analytics.music_dialog_premium_bg_continue(getAnalytics());
//                    })
//                    .create();
//            DialogFactory.setFont(premiumBackgroundDialog, this, R.font.lexend_deca);
//        }
//        if (!premiumBackgroundDialog.isShowing()) {
//            premiumBackgroundDialog.show();
//        }
//    }
}