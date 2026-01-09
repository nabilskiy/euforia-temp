package digital.euforia.app.ui.player.audio.components
// build.gradle: implementation "androidx.compose.material3:material3:<latest>"

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.AnimatedSizeButton
import digital.euforia.app.ui.util.LocalLocalizedRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeBottomSheet(
    visible: Boolean,
    title: String,
    value: Float,                           // 0f..1f
    onValueChange: (Float) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
//        dragHandle = { SheetDragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BottomSheetBackground,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val localizedRes = LocalLocalizedRes.current
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
            Box(
                Modifier
                    .padding(vertical = 16.dp)
                    .width(88.dp)
                    .height(2.dp)
                    .background(White.copy(alpha = 0.3f), RoundedCornerShape(1.dp))
            )
            Text(
                color = White.copy(alpha = 0.7f),
                text = localizedRes.string(R.string.sound_volume),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Slider(
                value = value.coerceIn(0f, 1f),
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                steps = 0,
                modifier = Modifier
                    .fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = White,
                    inactiveTrackColor = White.copy(alpha = 0.3f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                thumb = {
                    Box(
                        modifier = Modifier.size(16.dp)
                            .background(color = White, shape = CircleShape)
                    )
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
                        // Right-side mask that hides the unplayed portion
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(it.value)
                                .align(Alignment.CenterStart)
                                .background(White)
                        )
                    }
                }
            )

            Spacer(Modifier.height(28.dp))

            AnimatedSizeButton(
                modifier = Modifier.fillMaxWidth(),
                text = localizedRes.string(R.string.done),
                onClick = onDone
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}