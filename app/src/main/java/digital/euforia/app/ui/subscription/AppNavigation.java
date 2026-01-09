//package digital.euforia.app.ui.subscription;
//
////import static com.luluby.app.ui.UserActivity.EXTRA_RESUME_FRAGMENT;
////import static com.luluby.app.ui.UserActivity.EXTRA_SINGLE_FRAGMENT;
//
//import static digital.euforia.app.ui.subscription.UserActivity.EXTRA_RESUME_FRAGMENT;
//import static digital.euforia.app.ui.subscription.UserActivity.EXTRA_SINGLE_FRAGMENT;
//
//import android.content.Intent;
//import android.net.Uri;
//
////import com.luluby.app.api.APIConfig;
////import com.luluby.app.model.service.NotificationModel;
////import com.luluby.app.ui.UserActivity;
////import com.luluby.app.ui.fragment.AlbumFragment;
////import com.luluby.app.ui.fragment.AlbumsFragment;
////import com.luluby.app.ui.fragment.PlayerFragment;
////import com.luluby.app.ui.fragment.PlaylistFragment;
////import com.luluby.app.ui.fragment.PlaylistsFragment;
////import com.luluby.app.ui.fragment.SearchResultFragment;
////import com.luluby.app.ui.fragment.UpdatesFragment;
////import com.luluby.app.ui.fragment.settings.SettingsFragment;
////import com.luluby.app.ui.subscription.SubscriptionFragment;
////import com.luluby.app.util.base.AppUtils;
//
//import java.util.HashMap;
//import java.util.Objects;
//import java.util.Set;
//
///**
// * Created by ONCREATE COMPANY © 2023.
// * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
// */
//
//public class SubscriptionAppNavigation {
//
//    private UserActivity activity;
//
//    public AppNavigation(UserActivity activity) {
//        this.activity = activity;
//    }
//
//    public void navigate(Intent intent) {
//        try {
//            activity.lastSelectedItem = -1;
//
//            if (intent != null && intent.getExtras() != null) {
//                if (intent.getBooleanExtra(EXTRA_RESUME_FRAGMENT, false)) {
////                    activity.replaceFragmentSaveState(PlayerFragment.newInstance(0));
//                    return;
//                }
//                activity.activitySingleFragmentMode = intent.getBooleanExtra(EXTRA_SINGLE_FRAGMENT, false);
//            }
//
//            if (intent != null && intent.getExtras() != null && intent.hasExtra("link")) {
////                AppUtils.customTabs(activity, intent.getExtras().getString("link"));
//                return;
//            } else if (intent != null && intent.getExtras() != null
//                    && intent.hasExtra("showScreen")
//                    && intent.getStringExtra("showScreen").equals("true")) {
////                NotificationModel notificationModel = NotificationModel.createFromBundle(intent.getExtras());
////                if (notificationModel != null)
////                    activity.replaceFragmentSaveState(NotificationFragment.newInstance(notificationModel));
////                else activity.selectNavigationItem(UserActivity.NavigationItem.HomeTab);
//                return;
//            }
//
//            Uri uri = intent.getData();
//            if (uri == null || uri.getPath() == null) {
////                activity.selectNavigationItem(UserActivity.NavigationItem.HomeTab);
//            } else if (Objects.equals(uri.getHost(), APIConfig.HOST)) {
//                //
//                //Parsing links from luluby.com
//                if (uri.getPath().matches(".*album/\\d+")) {
//                    activity.replaceFragmentSaveState(AlbumFragment.newInstance(getIdParam(uri)));
//                } else if (uri.getPath().matches(".*playlist/\\d+")) {
//                    activity.replaceFragmentSaveState(PlaylistFragment.newInstance(getIdParam(uri)));
//                } else if (uri.getPath().matches(".*search")) {
//                    activity.selectNavigationItem(UserActivity.NavigationItem.SearchTab);
//                } else if (uri.getPath().matches(".*playlist")) {
//                    activity.selectNavigationItem(UserActivity.NavigationItem.SearchTab);
//                } else if (uri.getPath().matches(".*subscription")) {
//                    activity.replaceFragmentSaveState(SubscriptionFragment.newInstance(activity, SubscriptionFragment.FROM_PRIMARY));
//                }
//            } else if (Objects.equals(uri.getScheme(), APIConfig.SCHEME)) {
//                //
//                // Parsing links from scheme
//                if (Objects.equals(uri.getHost(), "subscription") && uri.getPath().matches("/\\d+")) {
//                    activity.replaceFragmentSaveState(SubscriptionFragment.newInstance(activity, SubscriptionFragment.FROM_PRIMARY, getIdParam(uri), "deeplink"));
//                } else if (Objects.equals(uri.getHost(), "subscription")) {
//                    activity.replaceFragmentSaveState(SubscriptionFragment.newInstance(activity, SubscriptionFragment.FROM_PRIMARY));
//                } else if (Objects.equals(uri.getHost(), "notification") && uri.getPath().matches("/\\d+")) {
//                    activity.replaceFragmentSaveState(NotificationFragment.newInstance(getIdParam(uri)));
//                } else if (Objects.equals(uri.getHost(), "home")) {
//                    activity.selectNavigationItem(UserActivity.NavigationItem.HomeTab);
//                } else if (Objects.equals(uri.getHost(), "search")) {
//                    activity.selectNavigationItem(UserActivity.NavigationItem.SearchTab);
//                } else if (Objects.equals(uri.getHost(), "my_playlists")) {
//                    activity.selectNavigationItem(UserActivity.NavigationItem.PlaylistsTab);
//                } else if (Objects.equals(uri.getHost(), "settings")) {
//                    activity.replaceFragmentSaveState(SettingsFragment.newInstance());
//                } else if (Objects.equals(uri.getHost(), "updates")) {
//                    activity.replaceFragmentSaveState(UpdatesFragment.newInstance());
//                } else if (Objects.equals(uri.getHost(), "search_result")) {
//                    activity.replaceFragmentSaveState(SearchResultFragment.newInstance());
//                } else if (Objects.equals(uri.getHost(), "albums") && uri.getPath().matches("/\\d+")) {
//                    activity.replaceFragmentSaveState(AlbumFragment.newInstance(getIdParam(uri)));
//                } else if (Objects.equals(uri.getHost(), "albums") && !uri.getQueryParameterNames().isEmpty()) {
//                    activity.replaceFragmentSaveState(AlbumsFragment.newInstance(null, getQueryParams(uri)));
//                } else if (Objects.equals(uri.getHost(), "albums")) {
//                    activity.replaceFragmentSaveState(AlbumsFragment.newInstance(null, new HashMap<>()));
//                } else if (Objects.equals(uri.getHost(), "playlists") && uri.getPath().matches("/\\d+")) {
//                    activity.replaceFragmentSaveState(PlaylistFragment.newInstance(getIdParam(uri)));
//                } else if (Objects.equals(uri.getHost(), "playlists") && !uri.getQueryParameterNames().isEmpty()) {
//                    activity.replaceFragmentSaveState(PlaylistsFragment.newInstance(null, getQueryParams(uri)));
//                } else if (Objects.equals(uri.getHost(), "playlists")) {
//                    activity.replaceFragmentSaveState(PlaylistsFragment.newInstance(null, new HashMap<>()));
//                }
//            } else {
//                activity.selectNavigationItem(UserActivity.NavigationItem.HomeTab);
//            }
//        } catch (Exception ignored) {
//            activity.selectNavigationItem(UserActivity.NavigationItem.HomeTab);
//        }
//    }
//
//    private static int getIdParam(Uri uri) {
//        try {
//            return Integer.parseInt(uri.getPathSegments().get(uri.getPathSegments().size() - 1));
//        } catch (Exception e) {
//            return 0;
//        }
//    }
//
//    private static HashMap<String, Object> getQueryParams(Uri uri) {
//        HashMap<String, Object> map = new HashMap<>();
//        try {
//            Set<String> params = uri.getQueryParameterNames();
//            for (String param : params) {
//                map.put(param, uri.getQueryParameter(param));
//            }
//        } catch (Exception ignored) {
//        }
//        return map;
//    }
//
//}
