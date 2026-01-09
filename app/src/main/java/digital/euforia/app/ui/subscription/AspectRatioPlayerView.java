package digital.euforia.app.ui.subscription;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.media3.ui.PlayerView;


import javax.annotation.Nullable;

import digital.euforia.app.R;

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */

public class AspectRatioPlayerView extends PlayerView {

    private float ratio = 1f;
    private boolean useRatio = true;

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    public boolean isUseRatio() {
        return useRatio;
    }

    public void setUseRatio(boolean useRatio) {
        this.useRatio = useRatio;
    }

    public AspectRatioPlayerView(Context context) {
        super(context);
        init(context, null);
    }

    public AspectRatioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AspectRatioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray at = null;
            try {
                at = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioPlayerView);
                ratio = at.getFloat(R.styleable.AspectRatioPlayerView_ratio, 1f);
                useRatio = at.getBoolean(R.styleable.AspectRatioPlayerView_aspectEnabled, true);
            } catch (Exception ignored) {
            } finally {
                if (at != null) {
                    at.recycle();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!useRatio) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (measuredWidth * (ratio));

        int measureMode = MeasureSpec.getMode(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, measureMode);

        super.onMeasure(widthMeasureSpec, height > 0 ? heightMeasureSpec : widthMeasureSpec);
    }
}
