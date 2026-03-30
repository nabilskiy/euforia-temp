package digital.euforia.app.ui.util.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.VibesBackground
import digital.euforia.app.ui.theme.White
import kotlinx.coroutines.delay

import androidx.compose.ui.tooling.preview.Preview
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.theme.NavBarBackground

@Composable
fun NotificationToast(
    text: String,
    isVisible: Boolean,
    onDismissed: () -> Unit = {}
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(3000)
            onDismissed()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.background(color = White, shape = CircleShape).padding(16.dp),
                text = text,
                color = NavBarBackground,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun NotificationToastPreview() {
    EuforiaTheme {
        NotificationToast(
            text = "Hello, this is a notification toast!",
            isVisible = true
        )
    }
}

