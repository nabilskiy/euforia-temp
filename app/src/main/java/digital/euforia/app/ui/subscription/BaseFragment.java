package digital.euforia.app.ui.subscription;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.Fade;
import androidx.transition.Transition;

import com.google.android.material.snackbar.Snackbar;
import digital.euforia.app.R;
//import digital.euforia.app.ui.UserActivity;
import digital.euforia.app.ui.subscription.anim.DetailsTransition;
//import digital.euforia.app.util.base.KeyboardHelper;
//import digital.euforia.app.util.base.SelectorUtils;

//import pro.oncreate.emptyview.EmptyView;

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */
public abstract class BaseFragment<T extends digital.euforia.app.ui.base.FragmentBaseActivity> extends Fragment
        implements  View.OnTouchListener, View.OnClickListener,
        Transition.TransitionListener {

    public static final String ARGUMENT_ID = "id";

    protected T activity;
//    protected EmptyView emptyView;
    private float scaleFactor = 0.9f;
    private boolean isViewAvailable = false;

    @Override
    public void onAttach(@NonNull Context context) {
        this.activity = (T) getActivity();
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        this.activity = (T) getActivity();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.isViewAvailable = true;
//        KeyboardHelper.hide(activity);
//        if (enableEmptyView())
//            emptyView = createEmptyView();

//        updateStatusBarViewOffset();
    }

    @Override
    public void onDestroyView() {
        try {
//            if (!isChildFragment())
//                KeyboardHelper.hide(activity);
        } catch (Exception ignored) {
        }
        super.onDestroyView();
        this.isViewAvailable = false;
    }

    protected void setBackToolbar(Toolbar toolbar) {
        if (toolbar != null && !activity.isBackStackEmpty() && !isChildFragment()) {
            toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
        }
    }

    protected void enableKeyboardListener() {
//        KeyboardVisibilityEvent.setEventListener(activity, this);
    }

    protected void noInternet() {
        if (getContainer() != null) {
//            Snackbar snackbar = Snackbar.make(getContainer(), R.string.text_no_internet, Snackbar.LENGTH_SHORT);
//            if (getSnackBarAnchor() != null)
//                snackbar.setAnchorView(getSnackBarAnchor());
//            snackbar.show();
        }
    }

    protected View getSnackBarAnchor() {
        return null;
    }

    protected View getContainer() {
        return null;
    }

    public boolean isViewAvailable() {
        return isViewAvailable;
    }

//    @Override
    public void onVisibilityChanged(boolean isOpen) {

    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        SelectorUtils.selectorScale(v, event, scaleFactor);
        return false;
    }

    @Override
    public void onClick(View view) {
    }

    protected boolean enableEventBus() {
        return false;
    }

    public boolean bottomNavigationRequired() {
        return false;
    }

    public boolean isChildFragment() {
        return false;
    }

//    public UserActivity.NavigationItem getNavigationItem() {
//        return null;
//    }

    public void onTabAlreadySelected() {

    }

    //
    // Empty view
    //


//    protected EmptyView.Builder getEmptyViewBuilder() {
//        return EmptyView.Builder.create(getContext())
//                .where(getEmptyViewContainer())
//                .empty(getEmptyViewOption())
//                .connection(getConnectionViewOption())
//                .custom(getCustomViewOption())
//                .accessDenied(getAccessDeniedViewOption())
//                .setTextStyle(getEmptyViewTextStyle())
//                .setButtonStyle(getEmptyViewButtonStyle())
//                .setOnClickListener(this);
//    }
//
//    protected EmptyView createEmptyView() {
//        return getEmptyViewBuilder().build();
//    }
//
//    protected EmptyView.EmptyViewOption getEmptyViewOption() {
//        return null;
//    }
//
//    protected EmptyView.EmptyViewOption getCustomViewOption() {
//        return null;
//    }
//
//    protected EmptyView.EmptyViewOption getAccessDeniedViewOption() {
//        return null;
//    }
//
//    protected EmptyView.EmptyViewOption getConnectionViewOption() {
//        return new EmptyView.EmptyViewOption(getString(R.string.text_no_internet));
//    }

//    @SuppressLint("ResourceType")
//    protected int getEmptyViewTextStyle() {
//        return R.style.TextViewEmptyView;
//    }
//
//    @SuppressLint("ResourceType")
//    protected int getEmptyViewButtonStyle() {
//        return R.style.ButtonEmptyView;
//    }
//
//    protected ViewGroup getEmptyViewContainer() {
//        return null;
//    }

    /**
     * @return false, if you don't want to create empty view for this fragment instance.
     * Default value is true.
     */
    protected boolean enableEmptyView() {
        return true;
    }

//    @Override
//    public void onEmptyViewClick(EmptyView.States state) {
//    }


    // Permission

    public static final int REQUEST_PERMISSION_GALLERY = 22;
    public static final int REQUEST_PERMISSION_CALENDAR = 23;

    protected void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGalleryGranted();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_GALLERY);
        }
    }

    protected void onPermissionGalleryGranted() {
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_PERMISSION_GALLERY: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    onPermissionGalleryGranted();
//                }
//                return;
//            }
//            case REQUEST_PERMISSION_CALENDAR: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    onPermissionCalendarGranted();
//                }
//                return;
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }


//    protected void requestCalendarPermission() {
//        if (isCalendarPermissionGranted()) {
//            onPermissionCalendarGranted();
//        } else {
//            requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
//                    REQUEST_PERMISSION_CALENDAR);
//        }
//    }
//
//    protected boolean isCalendarPermissionGranted() {
//        return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    protected void onPermissionCalendarGranted() {
//    }

    protected long getArgumentLong(String name, long defValue) {
        if (getArguments() != null)
            return getArguments().getLong(name, defValue);
        else return defValue;
    }

    protected String getArgumentString(String name, String defValue) {
        if (getArguments() != null)
            return getArguments().getString(name, defValue);
        else return defValue;
    }

    @Override
    public void onTransitionStart(@NonNull Transition transition) {
    }

    @Override
    public void onTransitionEnd(@NonNull Transition transition) {
    }

    @Override
    public void onTransitionCancel(@NonNull Transition transition) {
    }

    @Override
    public void onTransitionPause(@NonNull Transition transition) {
    }

    @Override
    public void onTransitionResume(@NonNull Transition transition) {
    }

    public static Fragment setupTransition(Fragment fragment) {
        fragment.setSharedElementEnterTransition(new DetailsTransition());
        fragment.setSharedElementReturnTransition(new DetailsTransition());
        fragment.setEnterTransition(new Fade());
        return fragment;
    }

//    public int getStatusBarHeight() {
//        Rect rectangle = new Rect();
//        Window window = activity.getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
//
//        if (rectangle.top > 0)
//            return rectangle.top;
//
//        int result = 0;
//        try {
//            @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
//            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//            if (resourceId > 0) {
//                result = getResources().getDimensionPixelSize(resourceId);
//            }
//        } catch (Exception ignored) {
//        }
//        if (result > 0)
//            return result;
//        else return getResources().getDimensionPixelOffset(R.dimen.size_24);
//    }
//
//    protected void updateStatusBarViewOffset() {
//        try {
//            View statusBarOffsetView = getView().findViewById(R.id.statusBarOffsetView);
//            if (!isViewAvailable() || getView() == null || statusBarOffsetView == null)
//                return;
//            ViewGroup.LayoutParams lp = statusBarOffsetView.getLayoutParams();
//            lp.height = getStatusBarHeight();
//            statusBarOffsetView.setLayoutParams(lp);
//        } catch (Exception ignored) {
//        }
//    }
}
