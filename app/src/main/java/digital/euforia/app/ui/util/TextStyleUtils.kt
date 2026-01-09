package digital.euforia.app.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import digital.euforia.app.ui.theme.White

class TextStyleUtils {
}

private val BaseTextColor = Color.White.copy(alpha = 0.6f)
private val HighlightTextColor = Color.White

fun AnnotatedString.Builder.normal(text: String) {
    withStyle(
        SpanStyle(
            color = BaseTextColor
        )
    ) {
        append(text.trim())
    }
}

fun AnnotatedString.Builder.highlight(text: String) {
    withStyle(
        SpanStyle(
            color = White,
            fontWeight = FontWeight.SemiBold
        )
    ) {
        append(text.trim())
    }
}
