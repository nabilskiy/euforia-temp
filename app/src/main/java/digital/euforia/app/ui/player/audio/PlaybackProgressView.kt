package digital.euforia.app.ui.player.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
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
import java.util.Calendar
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
    val sliderState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(clampedExternal) }
    val isSeekingState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
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

        Slider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
            ,
            value = sliderState.value,
            onValueChange = { v ->
                isSeekingState.value = true
                sliderState.value = min(v, safeDuration)
            },
            onValueChangeFinished = {
                val target = sliderState.value
                onSeek(target)
                // Keep local control for a short grace period to avoid a visual jump
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