package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
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
    val accentColor = White.copy(0.8f)
    val localizedRes = LocalLocalizedRes.current

    val text: AnnotatedString = buildAnnotatedString {
        append(localizedRes.string(R.string.intro_terms_1))

        // Privacy Policy
        val privacyText = localizedRes.string(R.string.intro_terms_privacy)
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(
            SpanStyle(
                color = accentColor,
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
                color = accentColor,
                textDecoration = TextDecoration.Underline
            )
        ) { append(termsText) }
        pop()
        append(".")
    }

    ClickableText(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(color = White.copy(0.7f), textAlign = TextAlign.Center),
        onClick = { offset ->
            text.getStringAnnotations(start = offset, end = offset).firstOrNull()?.let { ann ->
                when (ann.tag) {
                    "TERMS" -> onTermsClick()
                    "PRIVACY" -> onPrivacyClick()
                }
            }
        }
    )
}
