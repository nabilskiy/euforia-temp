package digital.euforia.app.ui.onboarding.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Language
import digital.euforia.app.ui.onboarding.OnboardingViewModel
import digital.euforia.app.ui.theme.LabelText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.AnimatedListCheckItem
import digital.euforia.app.ui.util.widget.AudioEqualizerView
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun LanguagePage(
    viewModel: OnboardingViewModel,
    languages: List<Language>,
    selectedLanguage: Language?,
    isPlaying: Boolean = false,
) {
    LanguagePageContent(
        languages = languages,
        selectedLanguage = selectedLanguage ?: languages.first(),
        isPlaying = isPlaying,
        onLanguageSelected = viewModel::onLanguageSelected
    )
}

@Composable
private fun LanguagePageContent(
    languages: List<Language>,
    selectedLanguage: Language,
    isPlaying: Boolean = false,
    onLanguageSelected: (Language) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp)
            .fillMaxSize().navigationBarsPadding()/*.overscroll(overscrollEffect = null)*/,
        verticalArrangement = spacedBy(16.dp),
        contentPadding = PaddingValues(top = 30.dp, bottom = 108.dp),
        flingBehavior = ScrollableDefaults.flingBehavior()
    ) {
        items(languages, key = { it.tag }) { languageOption ->
            LanguageItemView(
                language = languageOption,
                isSelected = languageOption == selectedLanguage,
                isPlaying = isPlaying,
                onSelect = onLanguageSelected
            )
        }
    }
}

@Composable
fun LanguageItemView(
    language: Language,
    isSelected: Boolean = false,
    isPlaying: Boolean = false,
    onSelect: (Language) -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    val checkIconRes =
        if (isSelected) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
    val titleColor = if (language.isSupported) Color.White else Color.White.copy(alpha = 0.3f)
    val onClick = if (language.isSupported) {
        { onSelect(language) }
    } else null
    AnimatedListCheckItem(
        isSelected = isSelected,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = language.iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(50.dp)
        )
        Text(
            modifier = Modifier.weight(1f),
            text = localizedRes.string(language.titleRes),
            color = titleColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        if (isSelected && isPlaying && language.hasSample) {
            AudioEqualizerView()
        }

        if (language.isSupported) {
            Icon(
                painter = painterResource(id = checkIconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp)
            )
        } else {
            language.getSoonTextRes()?.let { soonTextRes ->
                SoonTextView(textRes = soonTextRes)
            }
        }
    }
}

@Composable
fun SoonTextView(textRes: Int) {
    val localizedRes = LocalLocalizedRes.current
    Text(
        modifier = Modifier.background(White, RoundedCornerShape(4.dp))
            .padding(vertical = 2.dp, horizontal = 8.dp),
        text = localizedRes.string(textRes).uppercase(),
        color = LabelText,
        style = MaterialTheme.typography.displaySmall.copy(fontSize = 10.sp)
    )
}

private fun Language.getSoonTextRes(): Int? {
    return when (this) {
        Language.PT -> R.string.lang_soon_pt
        Language.FR -> R.string.lang_soon_fr
        Language.DE -> R.string.lang_soon_de
        else -> null
    }
}
