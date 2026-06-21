package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.delay

private val RateButtonGlow = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE29B31),
        Color(0xFFFF5589),
        Color(0xFF204FC0),
    ),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RateV3Page(
    isPageActive: Boolean,
    onSubmit: () -> Unit,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val emotions = remember {
        listOf(
            R.string.intro_rate_feel_calmer,
            R.string.intro_rate_feel_relieved,
            R.string.intro_rate_feel_inspired,
            R.string.intro_rate_feel_hopeful,
            R.string.intro_rate_feel_lighter,
            R.string.intro_rate_feel_no_change,
        )
    }
    var rating by remember { mutableIntStateOf(0) }
    var selectedEmotion by remember { mutableIntStateOf(-1) }
    var showRating by remember { mutableStateOf(false) }
    var showFeelings by remember { mutableStateOf(false) }
    var showComment by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(isPageActive) {
        if (!isPageActive) return@LaunchedEffect
        rating = 0
        selectedEmotion = -1
        showRating = false
        showFeelings = false
        showComment = false
        comment = ""
        contentAlpha.snapTo(0f)
        contentAlpha.animateTo(1f, tween(220))
        delay(180)
        showRating = true
    }

    LaunchedEffect(rating) {
        if (rating <= 0) {
            showFeelings = false
            showComment = false
            selectedEmotion = -1
            return@LaunchedEffect
        }
        delay(220)
        showFeelings = rating > 3
        showComment = rating <= 3
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18191D))
            .padding(horizontal = 32.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .graphicsLayer { alpha = contentAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = showRating,
                enter = fadeIn(tween(360)) + slideInVertically(tween(360, easing = LinearOutSlowInEasing)) { it / 5 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = localizedRes.string(R.string.intro_rate_rating),
                        color = White,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(5) { index ->
                            Image(
                                painter = painterResource(
                                    if (index < rating) R.drawable.ic_rate_star_full else R.drawable.ic_rate_star_empty,
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .noRippleClickable { rating = index + 1 },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            AnimatedVisibility(
                visible = showFeelings,
                enter = fadeIn(tween(380)) + slideInVertically(tween(380, easing = LinearOutSlowInEasing)) { it / 4 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = localizedRes.string(R.string.intro_rate_emotion),
                        color = White,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        emotions.forEachIndexed { index, resId ->
                            EmotionChip(
                                text = localizedRes.string(resId),
                                isSelected = selectedEmotion == index,
                                onClick = { selectedEmotion = index },
                            )
                        }
                        EmotionChip(
                            text = localizedRes.string(R.string.intro_rate_add_thoughts),
                            isSelected = showComment,
                            onClick = { showComment = !showComment },
                            prefix = "+",
                        )
                    }
                    AnimatedVisibility(visible = showComment, enter = fadeIn(tween(220))) {
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it.take(280) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            minLines = 3,
                            maxLines = 5,
                            placeholder = {
                                Text(
                                    text = localizedRes.string(R.string.intro_rate_comment_placeholder),
                                    color = White.copy(alpha = 0.32f),
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = White,
                                unfocusedTextColor = White,
                                focusedBorderColor = White.copy(alpha = 0.18f),
                                unfocusedBorderColor = White.copy(alpha = 0.10f),
                                cursorColor = White,
                                focusedContainerColor = White.copy(alpha = 0.08f),
                                unfocusedContainerColor = White.copy(alpha = 0.08f),
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        )
                    }
                }
            }
        }

        RateSubmitButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            text = localizedRes.string(R.string.intro_rate_next),
            isEnabled = rating > 0,
            onClick = onSubmit,
        )
    }
}

@Composable
private fun EmotionChip(
    text: String,
    isSelected: Boolean,
    prefix: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) White.copy(alpha = 0.22f) else Color(0xFF2B2D33))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        prefix?.let {
            Text(
                text = it,
                color = White,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            )
        }
        Text(
            text = text,
            color = White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun RateSubmitButton(
    modifier: Modifier,
    text: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 32.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    brush = if (isEnabled) RateButtonGlow else Brush.linearGradient(
                        listOf(Color.Transparent, Color.Transparent),
                    ),
                    shape = CircleShape,
                )
                .padding(3.dp)
                .alpha(if (isEnabled) 1f else 0.45f)
                .align(Alignment.Center)
                .clickable(enabled = isEnabled, onClick = onClick),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    color = PrimaryButtonText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}
