package digital.euforia.app.ui.base;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import digital.euforia.app.R;
import digital.euforia.app.ui.subscription.BaseActivity;
//import digital.euforia.app.util.base.L;


/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */

@SuppressLint("Registered")
public class FragmentBaseActivity extends BaseActivity {

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void replaceFragmentSaveState(Fragment fragment, @NonNull String backStackName) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment, backStackName)
                .addToBackStack(backStackName)
                .commitAllowingStateLoss();
    }

    public void replaceFragmentSaveState(Fragment fragment, @NonNull String backStackName, View sharedElement, String sharedName) {
        getSupportFragmentManager()
                .beginTransaction()
//                .setReorderingAllowed(true)
//                .setCustomAnimations(R.anim.fade_in,
//                        R.anim.fragment_fade_out,
//                        R.anim.fade_in,
//                        R.anim.fragment_fade_out)
                .replace(R.id.content, fragment, backStackName)
                .addToBackStack(backStackName)
                .addSharedElement(sharedElement, sharedName)
                .commitAllowingStateLoss();
    }

    public void replaceFragmentSaveState(Fragment fragment) {
        replaceFragmentSaveState(fragment, fragment.getClass().getSimpleName());
    }

    public void replaceFragmentSaveState(Fragment fragment, View sharedElement, String sharedName) {
        replaceFragmentSaveState(fragment, fragment.getClass().getSimpleName(), sharedElement, sharedName);
    }

    public void replaceFragmentSaveState(Fragment fragment, @NonNull String backStackName,
                                         View sh1, String sn1, View sh2, String sn2) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment, backStackName)
                .addToBackStack(backStackName)
                .addSharedElement(sh1, sn1)
                .addSharedElement(sh2, sn2)
                .commitAllowingStateLoss();
    }

    public void replaceFragmentSaveState(Fragment fragment, @NonNull String backStackName,
                                         View sh1, String sn1, View sh2, String sn2, View sh3, String sn3) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment, backStackName)
                .addToBackStack(backStackName)
                .addSharedElement(sh1, sn1)
                .addSharedElement(sh2, sn2)
                .addSharedElement(sh3, sn3)
                .commitAllowingStateLoss();
    }

    public void replaceFragmentSaveState(Fragment fragment, @NonNull String backStackName,
                                         View sh1, String sn1, View sh2, String sn2, View sh3, String sn3, View sh4, String sn4) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, fragment, backStackName)
                .addToBackStack(backStackName)
                .addSharedElement(sh1, sn1)
                .addSharedElement(sh2, sn2)
                .addSharedElement(sh3, sn3)
                .addSharedElement(sh4, sn4)
                .commitAllowingStateLoss();
    }

    public void replaceFragmentSaveState(Fragment fragment, boolean uniqueBackStackName) {
        String uniqueTag = "";
        if (uniqueBackStackName && fragment.getArguments() != null)
            uniqueTag += fragment.getArguments().getLong(ARGUMENT_ID);
        replaceFragmentSaveState(fragment, fragment.getClass().getSimpleName() + uniqueTag);
    }

    public void clearBackStack() {
        try {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception ignored) {
        }
    }

    public void popBackStack(int count) {
        try {
            for (int i = 0; i < count; i++)
                getSupportFragmentManager().popBackStackImmediate();
        } catch (Exception ignored) {
        }
    }

    public int getBackStackCount() {
//        L.d("getBackStackCount=", getSupportFragmentManager().getBackStackEntryCount());
        return getSupportFragmentManager().getBackStackEntryCount();
    }

    public boolean isBackStackEmpty() {
        return getBackStackCount() == 0;
    }

    public Fragment findFragment(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.content);
    }

//    @Override
//    public void onBackPressed() {
//        if (getBackStackCount() <= 1) {
//            this.finish();
//        } else {
//            super.onBackPressed();
//        }
//    }
}
