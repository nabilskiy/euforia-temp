package digital.euforia.app.ui.player.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.White
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlin.math.max
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackProgressView(
    currentTime: Float,
    duration: Float,
    colors: List<Color>,
    onSeek: (Float) -> Unit
) {
    val safeDuration = if (duration <= 0f || duration.isNaN()) 1f else duration

    // Local slider state to decouple UI from external updates while the user drags
    val clampedExternal = min(currentTime, safeDuration)
    val sliderState = remember { mutableStateOf(clampedExternal) }
    val isSeekingState = remember { mutableStateOf(false) }
    var showTimeTooltip by remember { mutableStateOf(false) }
    var hideTooltipJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    // Keep local state in sync with external time when not seeking
    if (!isSeekingState.value) {
        sliderState.value = clampedExternal
    }

    val progressRaw = sliderState.value / safeDuration
    val currentProgress = min(1f, max(0f, progressRaw))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "",
            color = Color.Gray,
            fontSize = 14.sp
        )

        // Slider with an overlaid tooltip that shows current playback time above the thumb
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(60.dp) // Set a fixed height to prevent vertical jumps
        ) {
            if (showTimeTooltip) {
                // Position the tooltip horizontally by using weighted spacers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                ) {
                    val p = currentProgress.coerceIn(0f, 1f)
                    // Compose RowScope.weight requires weight > 0f, clamp extremes to a tiny epsilon
                    val epsilon = 0.0001f
                    val leftWeight = if (p <= 0f) epsilon else p
                    val rightWeight = if ((1f - p) <= 0f) epsilon else (1f - p)
                    // Left spacer takes 'p' width of the row (with epsilon at 0)
                    Spacer(modifier = Modifier.weight(leftWeight))
                    // Tooltip text
                    Box {
                        Text(
                            text = formatMsToTime(sliderState.value),
                            color = White,
                            fontSize = 12.sp,
                        )
                    }
                    // Right spacer fills the rest
                    Spacer(modifier = Modifier.weight(rightWeight))
                }
            }

            Slider(
                modifier = Modifier.align(Alignment.BottomCenter),
                value = sliderState.value,
                onValueChange = { v ->
                    isSeekingState.value = true
                    showTimeTooltip = true
                    hideTooltipJob?.cancel()
                    sliderState.value = min(v, safeDuration)
                },
                onValueChangeFinished = {
                    val target = sliderState.value
                    onSeek(target)
                    // Hide tooltip after 3 seconds, but allow slider to sync after 300ms
                    hideTooltipJob?.cancel()
                    hideTooltipJob = scope.launch {
                        delay(3000)
                        showTimeTooltip = false
                    }
                    scope.launch {
                        delay(300)
                        isSeekingState.value = false
                    }
                },
                valueRange = 0f..safeDuration,
                colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = DarkGray
                ),
                thumb = {
                    Box(
                        modifier = Modifier.background(
                            color = White.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(5.dp).align(Alignment.Center)
                                .background(White, CircleShape)
                                .size(12.dp)
                                .clip(CircleShape)
                        )
                    }
                },
                track = {
                    // Full track container with rounded corners
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(DarkGray) // Base inactive background
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(colors))
                        )
                        // Right-side mask that hides the unplayed portion
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(1f - currentProgress)
                                .align(Alignment.CenterEnd)
                                .background(DarkGray)
                        )
                    }
                }
            )
        }
    }
}

private fun formatMsToTime(ms: Float): String {
    val totalSeconds = (ms / 1000f).toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}