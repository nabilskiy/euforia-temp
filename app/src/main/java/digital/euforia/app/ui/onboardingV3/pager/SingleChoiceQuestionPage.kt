package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.euforia.app.domain.model.onboarding.IntroAnswerItem
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.AnimatedListCheckItem
import digital.euforia.app.ui.util.widget.fadeBottom
import digital.euforia.app.ui.util.widget.fadeTop

@Composable
fun SingleChoiceQuestionPage(
    answers: List<IntroAnswerItem>,
    selectedId: String?,
    onAnswerSelected: (IntroAnswerItem) -> Unit,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fadeTop()
            .fadeBottom(),
        verticalArrangement = spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 30.dp,
            end = 16.dp,
            bottom = 108.dp,
        ),
    ) {
        items(answers, key = { it.identifier }) { answer ->
            QuestionAnswerItemView(
                answer = answer,
                isSelected = answer.identifier == selectedId,
                onClick = { onAnswerSelected(answer) },
            )
        }

        item {
            Spacer(Modifier.fillMaxWidth().navigationBarsPadding())
        }
    }
}

@Composable
private fun QuestionAnswerItemView(
    answer: IntroAnswerItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    AnimatedListCheckItem(
        isSelected = isSelected,
        minHeight = 64.dp,
        onClick = onClick,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnswerTitle(answer),
                color = White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )

            if (isSelected && !answer.subtitle.isNullOrBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.1f),
                )
                IntroBoldText(
                    text = answer.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.6f),
                    boldColor = White,
                )
            }
        }
    }
}

private fun buildAnswerTitle(answer: IntroAnswerItem): String {
    return if (!answer.icon.isNullOrBlank()) {
        "${answer.icon} ${answer.text}"
    } else {
        answer.text
    }
}
