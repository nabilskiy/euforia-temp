package digital.euforia.app.ui.plan.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants.IterateForever
import com.airbnb.lottie.compose.rememberLottieComposition
import digital.euforia.app.R
import digital.euforia.app.ui.theme.DayBlue
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.noRippleClickable

fun LazyListScope.notificationItem(
    visible: Boolean,
    onClick: () -> Unit,
    onCloseClick: () -> Unit
) = item(key = "notification") {
    val localizedRes = LocalLocalizedRes.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 48.dp)
                .background(color = NavBarBackground, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
                .animateContentSize()
                .noRippleClickable(onClick),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = localizedRes.string(R.string.request_permissions_notifications_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = White
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = localizedRes.string(R.string.request_permissions_notifications_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.7f)
                )
            }
            val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.anim_notifications))
            LottieAnimation(
                composition = composition,
                iterations = IterateForever,
                modifier = Modifier.widthIn(60.dp, 90.dp),
                contentScale = ContentScale.FillWidth,
                isPlaying = true
            )
            Icon(
                modifier = Modifier
                    .noRippleClickable(onCloseClick)
                    .size(20.dp)
                    .background(color = White.copy(alpha = 0.4f), shape = CircleShape)
                    .padding(4.dp),
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = NavBarBackground
            )
        }
    }
}