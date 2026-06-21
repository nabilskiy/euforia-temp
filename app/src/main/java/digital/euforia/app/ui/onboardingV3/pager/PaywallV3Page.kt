package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.noRippleClickable

@Composable
fun PaywallV3Page(
    isPageActive: Boolean,
    onNextClick: () -> Unit,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17191F)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 252.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PaywallHeroImage()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 42.dp)
                    .padding(top = 42.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = localizedRes.string(R.string.intro_paywall_title),
                    color = White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 25.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = Modifier.height(42.dp))
                PaywallBenefitRow(text = localizedRes.string(R.string.intro_paywall_benefit_sessions))
                Spacer(modifier = Modifier.height(26.dp))
                PaywallBenefitRow(text = localizedRes.string(R.string.intro_paywall_benefit_content))
                Spacer(modifier = Modifier.height(26.dp))
                PaywallBenefitRow(text = localizedRes.string(R.string.intro_paywall_benefit_soundscapes))
                Spacer(modifier = Modifier.height(26.dp))
                PaywallBenefitRow(text = localizedRes.string(R.string.intro_paywall_benefit_guide))
                Spacer(modifier = Modifier.height(26.dp))
                PaywallBenefitRow(text = localizedRes.string(R.string.intro_paywall_benefit_therapy))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 44.dp)
                .size(44.dp)
                .background(White.copy(alpha = 0.14f), CircleShape)
                .noRippleClickable(onNextClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = White.copy(alpha = 0.65f),
                modifier = Modifier.size(28.dp),
            )
        }

        TrialCard(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = onNextClick,
        )
    }
}

@Composable
private fun PaywallHeroImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(286.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.img_hands_bloom),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFF17191F),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun PaywallBenefitRow(
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaxLikeGradient, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = null,
                tint = Color(0xFFFFFF73),
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = text,
            color = White.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun TrialCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF20232B))
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF5167D9))) {
                    append(localizedRes.string(R.string.intro_paywall_trial_prefix))
                }
                withStyle(SpanStyle(color = Color(0xFFFF5A88))) {
                    append(localizedRes.string(R.string.intro_paywall_trial_highlight))
                }
                withStyle(SpanStyle(color = Color(0xFFEBC35D))) {
                    append(localizedRes.string(R.string.intro_paywall_trial_suffix))
                }
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaxLikeGradient, CircleShape)
                    .blur(7.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(White, CircleShape)
                    .noRippleClickable(onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = localizedRes.string(R.string.intro_paywall_try_button),
                    color = PrimaryButtonText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = localizedRes.string(R.string.intro_paywall_price_note),
            color = White.copy(alpha = 0.62f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

private val MaxLikeGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF204FC0),
        Color(0xFFFF5589),
        Color(0xFFE29B31),
    ),
)
