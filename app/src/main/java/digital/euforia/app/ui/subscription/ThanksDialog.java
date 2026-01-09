package digital.euforia.app.ui.subscription;


import static digital.euforia.app.ui.subscription.Configuration.MAX_SOUND_POOL_STREAMS;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import digital.euforia.app.R;
//import digital.euforia.app.AppUtils;
import digital.euforia.app.databinding.DialogThanksBinding;

/**
 * Developed by oncreate.com.
 * Copyright © 2019-2023 David Dubnitskiy. All Rights Reserved.
 */

public class ThanksDialog extends Dialog {

    TextView title;
    TextView subtitle;
    TextView skip;
    FrameLayout layout;
    ImageView image;
    LinearLayout layoutTitle;

    private FirebaseRemoteConfig remoteConfig;
    private int soundIdExplosion;
    private boolean soundPoolLoaded = false;
    private SoundPool soundPool;
//    private MediaPlayer mediaPlayer;

    private DialogThanksBinding binding;

    public ThanksDialog(Context context, FirebaseRemoteConfig remoteConfig) {
        super(context, R.style.DialogStyle);
        this.remoteConfig = remoteConfig;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogThanksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Map fields for minimal code changes
        title = binding.title;
        subtitle = binding.subtitle;
        skip = binding.skip;
        layout = binding.layout;
//        image = binding.image;
        layoutTitle = binding.layoutTitle;

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layoutTitle.getLayoutParams();
//        lp.width = (int) (AppUtils.getScreenWidth(context) * 0.62f);
        lp.height = lp.width;
        layoutTitle.setLayoutParams(lp);

        if (image != null) image.setOnClickListener(v -> this.dismiss());

        initSoundPool();

        setOnDismissListener(dialog -> {
            releaseSoundPool();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.colorDark20)));
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    private void initSoundPool() {
        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttributes).setMaxStreams(MAX_SOUND_POOL_STREAMS);
            this.soundPool = builder.build();

//            this.soundIdExplosion = this.soundPool.load(this.getContext(), R.raw.sound_thanks, 10);

//            mediaPlayer = MediaPlayer.create(getContext(), R.raw.sound_thanks);
//            mediaPlayer.setLooping(false);
//            mediaPlayer.setVolume(0.8f, 0.8f);
//            mediaPlayer.setOnPreparedListener(mediaPlayer -> {
//                soundPoolLoaded = true;
//                playSound();
//            });
        } catch (Exception ignored) {
        }
    }

    public void playSound() {
        try {
            if (this.soundPoolLoaded && soundPool != null) {
                soundPool.play(this.soundIdExplosion, 0.8f, 0.8f, 10, 0, 1f);
            }
        } catch (Exception ignored) {
        }
    }

    public void stopSound() {
        try {
//            if (mediaPlayer != null && soundPoolLoaded)
//                mediaPlayer.pause();
        } catch (Exception ignored) {
        }
    }

    public void releaseSoundPool() {
        try {
            if (soundPoolLoaded && soundPool != null) {
                soundPool.release();
            }
//            if (soundPoolLoaded && mediaPlayer != null) {
//                mediaPlayer.stop();
//                mediaPlayer.release();
//            }
        } catch (Exception ignored) {
        }
    }
}
