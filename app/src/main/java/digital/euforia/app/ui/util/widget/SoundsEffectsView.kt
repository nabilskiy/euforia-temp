package digital.euforia.app.ui.util.widget

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import kotlinx.coroutines.delay

@Composable
fun ColumnScope.SoundsEffectsView(
    soundsEffects: List<SoundEffectUi>,
    selectedIndex: Int,
    avatarPreviewUrl: String?,
    isHintShown: Boolean = false,
    isAvatarItemShown: Boolean = true,
    onMuteClick: () -> Unit,
    onClick: (Int) -> Unit,
    onAvatarClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    LaunchedEffect(selectedIndex, soundsEffects.size) {
        if (selectedIndex >= 0 && soundsEffects.isNotEmpty()) {
            val targetIndex =
                selectedIndex + if (isAvatarItemShown) 2 else 1 // account for Avatar + Mute items

            var viewportWidth =
                listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            var attempts = 0
            while (viewportWidth <= 0 && attempts < 5) {
                delay(16)
                attempts++
                viewportWidth =
                    listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            }

            if (viewportWidth > 0) {
                val itemWidthPx = with(density) { 86.dp.roundToPx() }
                val desiredStartPx = (viewportWidth / 2) - (itemWidthPx / 2)
                listState.animateScrollToItem(index = targetIndex, scrollOffset = -desiredStartPx)
            } else {
                listState.animateScrollToItem(index = targetIndex)
            }
        }
    }

    if (selectedIndex >= 0 && soundsEffects.isNotEmpty()) {
        Text(
            text = soundsEffects[selectedIndex].title,
            color = White,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = SemiBold),
            textAlign = TextAlign.Center,
        )

    }
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
        if (isAvatarItemShown) {
            item {
                AvatarPreviewItem(
                    url = avatarPreviewUrl,
                    isHintShown = isHintShown,
                    onClick = onAvatarClick
                )
            }
        }
        item {
            MuteItem(isSelected = selectedIndex == -1, onClick = onMuteClick)
        }
        itemsIndexed(items = soundsEffects, key = { index, item -> item.id }) { index, item ->
            SoundItem(
                soundEffectUi = item,
                isSelected = index == selectedIndex,
                onClick = {
                    onClick(index)
                }
            )
        }
    }
}

@Composable
private fun SoundItem(
    soundEffectUi: SoundEffectUi,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val borderWidth = if (isSelected) 3.dp else 1.dp
    val borderColor = if (isSelected) White else White.copy(alpha = 0.3f)
    val size by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 48.dp,
        animationSpec = tween(durationMillis = 300),
        label = "size"
    )
    Box(
        modifier = Modifier.size(56.dp)
            .noRippleClickable(onClick = onClick)
    ) {
        AsyncImage(
            modifier = Modifier.clip(CircleShape)
                .border(width = borderWidth, color = borderColor, shape = CircleShape)
                .align(Alignment.Center)
                .size(size),
            model = soundEffectUi.imageUrl,
            contentDescription = null
        )
        if (isSelected) {
            Box(
                modifier = Modifier.align(Alignment.Center).size(size)
                    .background(color = White.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.Center).size(24.dp),
                    painter = painterResource(id = R.drawable.ic_slider),
                    contentDescription = null,
                    tint = White
                )
            }
        }
    }
}

@Composable
fun MuteItem(isSelected: Boolean, onClick: () -> Unit) {
    val borderWidth = if (isSelected) 3.dp else 1.dp
    val borderColor = if (isSelected) White else White.copy(alpha = 0.3f)

    Box(
        modifier = Modifier.size(56.dp)
            .noRippleClickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.align(Alignment.Center).size(48.dp)
                .background(color = White.copy(alpha = 0.2f), shape = CircleShape)
                .border(width = borderWidth, color = borderColor, shape = CircleShape)
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center).size(24.dp),
                painter = painterResource(id = R.drawable.ic_sound_off),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarPreviewItem(
    url: String?,
    isHintShown: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clip(CircleShape).size(56.dp)
            .background(color = White.copy(alpha = 0.2f), shape = CircleShape)
            .border(width = 1.dp, color = White.copy(alpha = 0.3f), shape = CircleShape)
            .noRippleClickable(onClick = onClick)
    ) {
        if (url != null) {
            AsyncImage(
                modifier = Modifier.clip(CircleShape)
                    .align(Alignment.Center)
                    .fillMaxSize(),
                model = url,
                contentDescription = null
            )
        } else {
            Icon(
                modifier = Modifier.align(Alignment.Center).size(24.dp),
                painter = painterResource(id = R.drawable.ic_custom_photo),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
        val tooltipState = rememberTooltipState(isPersistent = true)

        LaunchedEffect(isHintShown) {
            if (isHintShown) {
                tooltipState.show()
            } else {
                tooltipState.dismiss()
            }
        }
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                TriangleTooltipBubble(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = LocalLocalizedRes.current.string(R.string.vibes_avatar_change_hint),
                    caretWidth = 12.dp,
                    caretHeight = 8.dp,
                    caretOffsetX = 28.dp,
                )
            },
            state = tooltipState,
        ) {}
    }
}

@Immutable
data class SoundEffectUi(
    val id: Int,
    val title: String,
    val imageUrl: String,
    val audioUrl: String,
    val maxVolume: Float = 0.5f
)