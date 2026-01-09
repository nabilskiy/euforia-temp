package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import digital.euforia.app.R
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.White
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.model.content.CircleShape
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun CongratsPopup(
    onActionClick: () -> Unit = {}
) {
    val composition = rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.anim_confetti)
    ).value

    if (composition != null) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillWidth,
            isPlaying = true
        )
    }
//    Dialog(onDismissRequest = onDismissRequest) {
    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        val localizedRes = LocalLocalizedRes.current
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth()
                .padding(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NavBarBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = localizedRes.string(R.string.premium_popup_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Euforia MAX",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    brush = PremiumGradient
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = localizedRes.string(R.string.premium_popup_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(PremiumGradient)
                    .noRippleClickable { onActionClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = localizedRes.string(R.string.premium_popup_button),
                    style = MaterialTheme.typography.titleMedium.copy(color = White),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
//    }
}
