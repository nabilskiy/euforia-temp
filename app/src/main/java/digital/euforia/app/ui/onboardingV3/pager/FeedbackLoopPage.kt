package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.White

@Composable
fun FeedbackLoopPage(
    title: String,
    text: String,
    glowColor: Color,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF021233)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .blur(100.dp)
                .clip(CircleShape)
                .background(glowColor),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.35f),
                                Color(0xFF002269),
                            ),
                        ),
                    ),
            )

            IntroBoldText(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
