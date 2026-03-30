package digital.euforia.app.ui.util.widget

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun TermsAndPrivacyText(
    modifier: Modifier,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    val baseColor = White.copy(0.7f)
    val pressedColor = White

    var pressedTag by remember { mutableStateOf<String?>(null) }

    val privacyColor by animateColorAsState(
        targetValue = if (pressedTag == "PRIVACY") pressedColor else baseColor,
        animationSpec = tween(durationMillis = 100),
        label = "privacyColor"
    )
    val termsColor by animateColorAsState(
        targetValue = if (pressedTag == "TERMS") pressedColor else baseColor,
        animationSpec = tween(durationMillis = 100),
        label = "termsColor"
    )

    val localizedRes = LocalLocalizedRes.current

    val text: AnnotatedString = buildAnnotatedString {
        append(localizedRes.string(R.string.intro_terms_1))

        // Privacy Policy
        val privacyText = localizedRes.string(R.string.intro_terms_privacy)
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(
            SpanStyle(
                color = privacyColor,
                textDecoration = TextDecoration.Underline
            )
        ) { append(privacyText) }
        pop()

        append(" ${localizedRes.string(R.string.intro_terms_2)} ")

        // Terms of Use
        val termsText = localizedRes.string(R.string.intro_terms_terms)
        pushStringAnnotation(tag = "TERMS", annotation = "terms")
        withStyle(
            SpanStyle(
                color = termsColor,
                textDecoration = TextDecoration.Underline
            )
        ) { append(termsText) }
        pop()
        append(".")
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicText(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    val layoutResult = textLayoutResult ?: return@detectTapGestures
                    val position = layoutResult.getOffsetForPosition(offset)

                    val terms = text.getStringAnnotations(tag = "TERMS", start = position, end = position).firstOrNull()
                    val privacy = text.getStringAnnotations(tag = "PRIVACY", start = position, end = position).firstOrNull()

                    if (terms != null) {
                        pressedTag = "TERMS"
                    } else if (privacy != null) {
                        pressedTag = "PRIVACY"
                    }

                    tryAwaitRelease()
                    pressedTag = null
                },
                onTap = { offset ->
                    val layoutResult = textLayoutResult ?: return@detectTapGestures
                    val position = layoutResult.getOffsetForPosition(offset)

                    text.getStringAnnotations(tag = "TERMS", start = position, end = position).firstOrNull()?.let {
                        onTermsClick()
                    }
                    text.getStringAnnotations(tag = "PRIVACY", start = position, end = position).firstOrNull()?.let {
                        onPrivacyClick()
                    }
                }
            )
        },
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(color = baseColor, textAlign = TextAlign.Center),
        onTextLayout = { textLayoutResult = it }
    )
}
