package digital.euforia.app.ui.util.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.ButtonDisabled
import digital.euforia.app.ui.theme.CircleButtonBackground
import digital.euforia.app.ui.theme.CircleButtonIcon
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White

@Composable
fun AnimatedSizeButton(
    modifier: Modifier = Modifier,
    text: String,
    isEnabled: Boolean = true,
    isVisible: Boolean = true,
    suppressInitialAnimation: Boolean = false,
    buttonColor: Color = White,
    textColor: Color = PrimaryButtonText,
    onClick: () -> Unit
) {
    val visibleState = if (suppressInitialAnimation) {
        remember {
            MutableTransitionState(true)
        }.also { it.targetState = isVisible }
    } else null

    if (visibleState != null) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            AnimatedSizeBox(
                modifier = modifier,
                onClick = onClick
            ) {
                val backgroundColor = if (isEnabled) {
                    buttonColor
                } else {
                    ButtonDisabled
                }
                Box(
                    modifier = Modifier.animateContentSize().padding(horizontal = 40.dp).fillMaxWidth()
                        .height(48.dp)
                        .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = text,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    } else {
        AnimatedVisibility(
            visible = isVisible,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
        AnimatedSizeBox(
            modifier = modifier,
            onClick = onClick
        ) {
            val backgroundColor = if (isEnabled) {
                buttonColor
            } else {
                ButtonDisabled
            }
            Box(
                modifier = Modifier.animateContentSize().padding(horizontal = 40.dp).fillMaxWidth()
                    .height(48.dp)
                    .background(color = backgroundColor, shape = RoundedCornerShape(16.dp))
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    iconRes: Int,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier.background(color = CircleButtonBackground, shape = CircleShape).size(32.dp),
        onClick = onClick
    ) {
        Icon(
            modifier = iconModifier.padding(8.dp),
            painter = painterResource(iconRes),
            tint = CircleButtonIcon,
            contentDescription = null
        )
    }
}