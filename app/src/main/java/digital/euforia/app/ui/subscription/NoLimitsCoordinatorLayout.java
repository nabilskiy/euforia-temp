package digital.euforia.app.ui.subscription;

import android.content.Context;
import android.util.AttributeSet;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */

public class NoLimitsCoordinatorLayout extends CoordinatorLayout {
    public NoLimitsCoordinatorLayout(Context context) {
        super(context);
        init();
    }

    public NoLimitsCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoLimitsCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnApplyWindowInsetsListener((view, windowInsets) -> windowInsets.replaceSystemWindowInsets(0, 0, 0, windowInsets.getSystemWindowInsetBottom()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}