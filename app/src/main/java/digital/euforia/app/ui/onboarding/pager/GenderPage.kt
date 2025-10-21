package digital.euforia.app.ui.onboarding.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Gender
import digital.euforia.app.ui.onboarding.OnboardingViewModel
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.AnimatedListCheckItem

@Composable
fun GenderPage(
    viewModel: OnboardingViewModel,
    selectedGender: Gender?,
) {
    GenderPageContent(
        selectedGender = selectedGender,
        onGenderSelected = viewModel::onGenderSelected,
    )
}

@Composable
private fun GenderPageContent(
    selectedGender: Gender? = null,
    onGenderSelected: (Gender) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding()
            .padding(start = 16.dp, top = 30.dp, end = 16.dp, bottom = 30.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = spacedBy(16.dp)
        ) {
            for (genderOption in Gender.entries) {
                GenderItemView(
                    gender = genderOption,
                    isSelected = genderOption == selectedGender,
                    onSelect = onGenderSelected
                )

            }
        }
    }
}

@Composable
fun GenderItemView(
    gender: Gender,
    isSelected: Boolean = false,
    onSelect: (Gender) -> Unit = {}
) {
    val checkIconRes =
        if (isSelected) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked
    val lres = LocalLocalizedRes.current

    AnimatedListCheckItem(
        isSelected = isSelected,
        onClick = { onSelect(gender) }
    ) {
        gender.iconRes?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = lres.string(gender.textRes),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Icon(
            painter = painterResource(id = checkIconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1116)
@Composable
private fun GenderSelectionPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(Modifier.background(Color(0xFF0E1116), RectangleShape)) {
            GenderPageContent()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1116)
@Composable
private fun GenderOptionViewPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val cardBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF201B24),
                Color(0xFF52245E),
                Color(0xFFB017B3)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .background(cardBrush)
                .padding(16.dp)
                .then(Modifier)
        ) {
            GenderPageContent { }
        }
    }
}
