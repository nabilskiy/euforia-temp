package digital.euforia.app.ui.onboardingV3.pager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import digital.euforia.app.R
import digital.euforia.app.ui.onboardingV3.OnboardingV3PreviewType
import digital.euforia.app.ui.onboardingV3.components.V3GradientTitleButton
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.MediaPlayerHelper
import digital.euforia.app.ui.util.widget.noRippleClickable
import java.time.Instant
import java.time.ZoneId

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
    reminderDayOffset: Int,
    reminderHour: Int,
    reminderMinute: Int,
    reminderScheduledAt: Long?,
    isReminderScheduled: Boolean,
    onPreviewTypeSelected: (OnboardingV3PreviewType) -> Unit,
    onReminderTimeChanged: (Int, Int, Int) -> Unit,
    onReminderScheduleClick: (Boolean) -> Unit,
    onReminderProceedClick: () -> Unit,
    onReminderCancelClick: () -> Unit,
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
    var reminderPickerExpanded by remember { mutableStateOf(false) }
    val appear = remember { Animatable(0f) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            onReminderScheduleClick(true)
            showReminderAlert = false
            reminderPickerExpanded = false
        }
    }

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
        if (isReminderScheduled) {
            FirstExperienceScheduledScreen(
                scheduledAtMillis = reminderScheduledAt,
                modifier = Modifier.graphicsLayer {
                    alpha = appear.value
                    translationY = (1f - appear.value) * 24f
                },
                onProceedClick = onReminderProceedClick,
                onCancelClick = onReminderCancelClick,
            )
        } else {
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
                        onClick = {
                            reminderPickerExpanded = false
                            showReminderAlert = true
                        },
                    )
                }
            }

            FirstExperienceBottomButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = onPlayNowClick,
            )
        }

        if (showReminderAlert && !isReminderScheduled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        ),
                    )
                    .noRippleClickable {
                        showReminderAlert = false
                        reminderPickerExpanded = false
                    },
            )
        }

        AnimatedVisibility(
            visible = showReminderAlert && !isReminderScheduled,
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
                isPickerExpanded = reminderPickerExpanded,
                dayOffset = reminderDayOffset,
                hour = reminderHour,
                minute = reminderMinute,
                onDaySelected = { onReminderTimeChanged(it, reminderHour, reminderMinute) },
                onTimeChanged = { hour, minute ->
                    onReminderTimeChanged(reminderDayOffset, hour, minute)
                },
                onSetReminderClick = { reminderPickerExpanded = true },
                onScheduleClick = {
                    val isGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) == PackageManager.PERMISSION_GRANTED
                    if (isGranted) {
                        onReminderScheduleClick(true)
                        showReminderAlert = false
                        reminderPickerExpanded = false
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
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
                            0f to Color(0xFF17191F).copy(alpha = 0.25f),
                            0.30f to Color.Transparent,
                            0.65f to Color.Transparent,
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
            ExperienceTag(
                text = localizedRes.string(item.subtitleRes),
                backgroundColor = if (item.type == OnboardingV3PreviewType.Accompaniment) {
                    Color(0xFFB9FFE5).copy(alpha = 0.5f)
                } else {
                    White.copy(alpha = 0.20f)
                },
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(White.copy(alpha = 0.20f), RoundedCornerShape(6.dp))
                .blur(10.dp),
        )
        Row(
            modifier = Modifier
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
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                ),
            )
        }
    }
}

@Composable
private fun ExperienceTag(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(backgroundColor, RoundedCornerShape(6.dp))
                .blur(2.dp),
        )
        Text(
            text = text,
            color = White,
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 16.sp,
            ),
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
        V3GradientTitleButton(
            text = LocalLocalizedRes.current.string(R.string.intro_final_start),
            onClick = onClick,
        )
    }
}

@Composable
private fun ReminderAlert(
    isPickerExpanded: Boolean,
    dayOffset: Int,
    hour: Int,
    minute: Int,
    onDaySelected: (Int) -> Unit,
    onTimeChanged: (Int, Int) -> Unit,
    onSetReminderClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onPlayNowClick: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val sheetShape = RoundedCornerShape(if (isPickerExpanded) 32.dp else 30.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isPickerExpanded) 4.dp else 24.dp)
            .padding(bottom = if (isPickerExpanded) 0.dp else 24.dp)
            .clip(sheetShape)
            .background(Color(0xFF1F2228))
            .noRippleClickable { }
            .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing))
            .padding(horizontal = if (isPickerExpanded) 38.dp else 25.dp)
            .padding(
                top = if (isPickerExpanded) 48.dp else 30.dp,
                bottom = if (isPickerExpanded) 28.dp else 26.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(if (isPickerExpanded) 28.dp else 20.dp),
    ) {
        Text(
            text = localizedRes.string(R.string.intro_final_timer_title),
            color = White,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = if (isPickerExpanded) 28.sp else 24.sp,
                lineHeight = if (isPickerExpanded) 35.sp else 31.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        if (!isPickerExpanded) {
            Text(
                text = localizedRes.string(R.string.intro_final_timer_message),
                color = White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
        if (isPickerExpanded) {
            ReminderDaySelector(
                selectedDayOffset = dayOffset,
                onDaySelected = onDaySelected,
            )
            ReminderTimePicker(
                hour = hour,
                minute = minute,
                onTimeChanged = onTimeChanged,
            )
        }
        Column(
            modifier = Modifier.padding(top = if (isPickerExpanded) 0.dp else 10.dp),
            verticalArrangement = Arrangement.spacedBy(if (isPickerExpanded) 24.dp else 20.dp),
        ) {
            ReminderButton(
                text = localizedRes.string(
                    if (isPickerExpanded) {
                        R.string.intro_final_timer_schedule
                    } else {
                        R.string.intro_final_timer_ok
                    },
                ),
                isPrimary = true,
                onClick = if (isPickerExpanded) onScheduleClick else onSetReminderClick,
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
private fun ReminderDaySelector(
    selectedDayOffset: Int,
    onDaySelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF30333A), RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        ReminderDayChip(
            text = LocalLocalizedRes.current.string(R.string.intro_final_timer_today),
            isSelected = selectedDayOffset == 0,
            modifier = Modifier.weight(1f),
            onClick = { onDaySelected(0) },
        )
        ReminderDayChip(
            text = LocalLocalizedRes.current.string(R.string.intro_final_timer_tomorrow),
            isSelected = selectedDayOffset == 1,
            modifier = Modifier.weight(1f),
            onClick = { onDaySelected(1) },
        )
    }
}

@Composable
private fun ReminderDayChip(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isSelected) Color(0xFF73747D) else Color.Transparent, RoundedCornerShape(7.dp))
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = White.copy(alpha = if (isSelected) 1f else 0.82f),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun ReminderTimePicker(
    hour: Int,
    minute: Int,
    onTimeChanged: (Int, Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .background(Color(0xFF17191F).copy(alpha = 0.36f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        PickHourMinute(
            initialHour = hour,
            initialMinute = minute,
            onHourChange = { onTimeChanged(it, minute) },
            onMinuteChange = { onTimeChanged(hour, it) },
            focusIndicator = PickTimeFocusIndicator(
                enabled = true,
                widthFull = true,
                background = Color(0xFF3B3E48),
                shape = RoundedCornerShape(12.dp),
            ),
            containerColor = Color.Transparent,
            selectedTextStyle = PickTimeTextStyle(
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun FirstExperienceScheduledScreen(
    scheduledAtMillis: Long?,
    modifier: Modifier = Modifier,
    onProceedClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val scheduled = scheduledAtMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
    }
    val dayLabel = if (scheduled?.toLocalDate() == java.time.LocalDate.now()) {
        localizedRes.string(R.string.intro_final_timer_today)
    } else {
        localizedRes.string(R.string.intro_final_timer_tomorrow)
    }
    val timeLabel = scheduled?.let { "%d:%02d".format(it.hour, it.minute) }.orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
            .padding(top = 138.dp, bottom = 34.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WaitingReminderVisual()
        Spacer(modifier = Modifier.height(76.dp))
        Text(
            text = localizedRes.string(R.string.intro_final_scheduled_title, dayLabel, timeLabel),
            color = White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 34.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = localizedRes.string(R.string.intro_final_scheduled_subtitle),
            color = White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 18.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        ReminderButton(
            text = localizedRes.string(R.string.intro_final_scheduled_proceed),
            isPrimary = true,
            onClick = onProceedClick,
        )
        Text(
            text = localizedRes.string(R.string.intro_final_scheduled_cancel),
            color = White.copy(alpha = 0.45f),
            modifier = Modifier
                .padding(top = 20.dp)
                .noRippleClickable(onCancelClick),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun WaitingReminderVisual() {
    val infiniteTransition = rememberInfiniteTransition(label = "firstExperienceReminderWaiting")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "waitingRotation",
    )

    Box(
        modifier = Modifier.size(142.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(FirstExperienceGradient, CircleShape)
                .blur(28.dp)
                .graphicsLayer { alpha = 0.45f },
        )
        Box(
            modifier = Modifier
                .size(118.dp)
                .background(White.copy(alpha = 0.08f), CircleShape)
                .border(BorderStroke(1.dp, White.copy(alpha = 0.12f)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_timer_large),
                contentDescription = null,
                modifier = Modifier
                    .size(68.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                        alpha = 0.85f
                    },
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
