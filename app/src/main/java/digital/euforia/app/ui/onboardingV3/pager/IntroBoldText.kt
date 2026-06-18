package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import digital.euforia.app.ui.theme.White

@Composable
fun IntroBoldText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = White.copy(alpha = 0.65f),
    boldColor: Color = White,
    textAlign: TextAlign = TextAlign.Center,
) {
    val annotated = buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val start = remaining.indexOf("**")
            if (start == -1) {
                append(remaining)
                break
            }
            append(remaining.substring(0, start))
            remaining = remaining.substring(start + 2)
            val end = remaining.indexOf("**")
            if (end == -1) {
                append("**")
                append(remaining)
                break
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                append(remaining.substring(0, end))
            }
            remaining = remaining.substring(end + 2)
        }
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = style.copy(color = color, textAlign = textAlign),
    )
}
