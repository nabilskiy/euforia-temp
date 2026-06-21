package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.onboardingV3.OnboardingV3PreviewType
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.MediaPlayerHelper
import digital.euforia.app.ui.util.widget.noRippleClickable

private data class FirstExperienceItem(
    val type: OnboardingV3PreviewType,
    val titleRes: Int,
    val subtitleRes: Int,
    val timeRes: Int,
    val imageRes: Int,
)

private val FirstExperienceGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE29B31),
        Color(0xFFFF5589),
        Color(0xFF204FC0),
    ),
)

@Composable
fun FirstExperiencePage(
    isPageActive: Boolean,
    selectedPreviewType: OnboardingV3PreviewType,
    onPreviewTypeSelected: (OnboardingV3PreviewType) -> Unit,
    onPlayNowClick: () -> Unit,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val context = LocalContext.current
    val items = remember {
        listOf(
            FirstExperienceItem(
                type = OnboardingV3PreviewType.Accompaniment,
                titleRes = R.string.intro_content_accompaniment,
                subtitleRes = R.string.intro_content_accompaniment_subtitle,
                timeRes = R.string.intro_content_accompaniment_time,
                imageRes = R.drawable.img_intro_accompaniment_2,
            ),
            FirstExperienceItem(
                type = OnboardingV3PreviewType.Meditation,
                titleRes = R.string.intro_content_meditation,
                subtitleRes = R.string.intro_content_meditation_subtitle,
                timeRes = R.string.intro_content_meditation_time,
                imageRes = R.drawable.img_intro_meditation_2,
            ),
            FirstExperienceItem(
                type = OnboardingV3PreviewType.Soundscape,
                titleRes = R.string.intro_content_soundscape,
                subtitleRes = R.string.intro_content_soundscape_subtitle,
                timeRes = R.string.intro_content_soundscape_time,
                imageRes = R.drawable.img_intro_soundscape_2,
            ),
        )
    }
    var showReminderAlert by remember { mutableStateOf(false) }
    val appear = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(700))
    }

    LaunchedEffect(Unit) {
        MediaPlayerHelper.play(context, R.raw.snd_intro_pick_experience_female)
    }

    DisposableEffect(Unit) {
        onDispose { MediaPlayerHelper.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17191F)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 78.dp, bottom = 126.dp)
                .graphicsLayer {
                    alpha = appear.value
                    translationY = (1f - appear.value) * 24f
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = localizedRes.string(R.string.intro_final_subtitle_experience),
                color = White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 25.sp,
                    lineHeight = 33.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Box(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .size(width = 64.dp, height = 1.dp)
                    .background(White.copy(alpha = 0.18f), RoundedCornerShape(999.dp)),
            )
            Spacer(modifier = Modifier.height(76.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ExperienceCard(
                    item = items[0],
                    isSelected = selectedPreviewType == items[0].type,
                    modifier = Modifier.weight(1f),
                    onClick = { onPreviewTypeSelected(items[0].type) },
                )
                ExperienceCard(
                    item = items[1],
                    isSelected = selectedPreviewType == items[1].type,
                    modifier = Modifier.weight(1f),
                    onClick = { onPreviewTypeSelected(items[1].type) },
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ExperienceCard(
                    item = items[2],
                    isSelected = selectedPreviewType == items[2].type,
                    modifier = Modifier.weight(1f),
                    onClick = { onPreviewTypeSelected(items[2].type) },
                )
                ScheduleCard(
                    modifier = Modifier.weight(1f),
                    onClick = { showReminderAlert = true },
                )
            }
        }

        FirstExperienceBottomButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = onPlayNowClick,
        )

        AnimatedVisibility(
            visible = showReminderAlert,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(450),
            ) + fadeIn(tween(450)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300),
            ) + fadeOut(tween(220)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ReminderAlert(
                onSetReminderClick = { showReminderAlert = false },
                onPlayNowClick = {
                    showReminderAlert = false
                    onPlayNowClick()
                },
            )
        }
    }
}

@Composable
private fun ExperienceCard(
    item: FirstExperienceItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val shape = RoundedCornerShape(25.dp)

    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .then(
                if (isSelected) {
                    Modifier
                        .blur(0.dp)
                        .background(FirstExperienceGradient, shape)
                        .padding(2.dp)
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(White.copy(alpha = 0.06f), shape)
            .noRippleClickable(onClick),
    ) {
        Image(
            painter = painterResource(item.imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFF17191F).copy(alpha = 0.20f),
                            0.55f to Color.Transparent,
                            1f to Color(0xFF17191F).copy(alpha = 0.86f),
                        ),
                    ),
                ),
        )
        TimeBadge(
            text = localizedRes.string(item.timeRes),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = localizedRes.string(item.titleRes),
                color = White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = localizedRes.string(item.subtitleRes),
                color = White,
                modifier = Modifier
                    .background(
                        if (item.type == OnboardingV3PreviewType.Accompaniment) {
                            Color(0xFFB9FFE5).copy(alpha = 0.5f)
                        } else {
                            White.copy(alpha = 0.20f)
                        },
                        RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            )
        }
    }
}

@Composable
private fun ScheduleCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFF1F2228))
            .noRippleClickable(onClick)
            .padding(16.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_timer_large),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(76.dp)
                .graphicsLayer { alpha = 0.55f },
        )
        Text(
            text = localizedRes.string(R.string.intro_final_schedule),
            color = White,
            modifier = Modifier.align(Alignment.BottomStart),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 17.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun TimeBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(White.copy(alpha = 0.20f), RoundedCornerShape(6.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_clock),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = text,
            color = White,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        )
    }
}

@Composable
private fun FirstExperienceBottomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xFF17191F)),
                ),
            )
            .padding(horizontal = 40.dp, vertical = 30.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(FirstExperienceGradient, CircleShape)
                .blur(5.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(White, CircleShape)
                .noRippleClickable(onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = LocalLocalizedRes.current.string(R.string.intro_final_start),
                color = PrimaryButtonText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
private fun ReminderAlert(
    onSetReminderClick: () -> Unit,
    onPlayNowClick: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFF1F2228))
            .padding(horizontal = 25.dp)
            .padding(top = 30.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = localizedRes.string(R.string.intro_final_timer_title),
            color = White,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 24.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = localizedRes.string(R.string.intro_final_timer_message),
            color = White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ReminderButton(
                text = localizedRes.string(R.string.intro_final_timer_ok),
                isPrimary = true,
                onClick = onSetReminderClick,
            )
            ReminderButton(
                text = localizedRes.string(R.string.intro_final_timer_cancel),
                isPrimary = false,
                onClick = onPlayNowClick,
            )
        }
    }
}

@Composable
private fun ReminderButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                if (isPrimary) White else White.copy(alpha = 0.10f),
                CircleShape,
            )
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (isPrimary) PrimaryButtonText else White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
