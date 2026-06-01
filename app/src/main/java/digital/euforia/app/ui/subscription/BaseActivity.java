package digital.euforia.app.ui.subscription;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
//import com.luluby.app.R;
//import com.luluby.app.data.events.ApplyWindowInsetsEvent;
//import com.luluby.app.util.base.AnalyticHelper;
//import com.luluby.app.util.base.SelectorUtils;
//
//import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
//import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
//
//import org.greenrobot.eventbus.EventBus;
//
//import pro.oncreate.emptyview.EmptyView;

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */

public class BaseActivity extends AppCompatActivity
//        implements KeyboardVisibilityEventListener,
//        View.OnClickListener, View.OnTouchListener, EmptyView.OnClickEmptyViewListener
{

    public static final String ARGUMENT_ID = "id";
    public static final int REQUEST_PERMISSION_LOCATION = 101;
    public static final int REQUEST_PERMISSION_STORAGE = 102;

    protected FirebaseAnalytics analytics;
//    protected EmptyView emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().getDecorView()
//                .findViewById(Window.ID_ANDROID_CONTENT)
//                .setOnApplyWindowInsetsListener((v, insets) -> {
//                    EventBus.getDefault().postSticky(new ApplyWindowInsetsEvent(insets));
//                    return insets;
//                });
//        if (enableEventBus())
//            EventBus.getDefault().register(this);
    }

//    protected void enableEmptyView() {
//        emptyView = createEmptyView();
//    }
//
//    public FirebaseAnalytics getAnalytics() {
//        if (analytics == null)
//            analytics = AnalyticHelper.get(this);
//        return analytics;
//    }
//
//    @Override
//    public void onClick(View view) {
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        SelectorUtils.selectorScale(v, event);
//        return false;
//    }
//
//    protected void enableKeyVisibilityListener() {
//        KeyboardVisibilityEvent.setEventListener(this, this);
//    }
//
//    protected boolean enableEventBus() {
//        return false;
//    }
//
//    @Override
//    public void onVisibilityChanged(boolean isOpen) {
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        try {
//            Fresco.getImagePipeline().clearMemoryCaches();
//        } catch (Exception ignored) {
//        }
//        if (enableEventBus())
//            EventBus.getDefault().unregister(this);
//        super.onDestroy();
//    }
//
//    //
//    // Location
//
//    protected void requestLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            onPermissionLocationGranted();
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_PERMISSION_LOCATION);
//        }
//    }
//
//    protected boolean isLocationPermissionGranted() {
//        return ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    protected void onPermissionLocationGranted() {
//    }
//
//    //
//    // Storage
//
//    protected void requestStoragePermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            onPermissionStorageGranted();
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_PERMISSION_STORAGE);
//        }
//    }
//
//    protected boolean isStoragePermissionGranted() {
//        return ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    protected void onPermissionStorageGranted() {
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_PERMISSION_LOCATION: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    onPermissionLocationGranted();
//                }
//                return;
//            }
//
//            case REQUEST_PERMISSION_STORAGE: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    onPermissionStorageGranted();
//                }
//                return;
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    //
//    // Empty view
//    //
//
//    protected EmptyView.Builder getEmptyViewBuilder() {
//        return EmptyView.Builder.create(this)
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
//
//    @SuppressLint("ResourceType")
//    protected int getEmptyViewTextStyle() {
//        return R.style.ButtonEmptyView;
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
//
//    @Override
//    public void onEmptyViewClick(EmptyView.States state) {
//    }
}
