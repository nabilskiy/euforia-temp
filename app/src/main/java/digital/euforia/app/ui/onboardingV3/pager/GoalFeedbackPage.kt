package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes

@Composable
fun GoalFeedbackPage(
    selectedGoal: Goal?,
    isPageActive: Boolean,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val title = localizedRes.string(R.string.intro_v3_goals_feedback_title)
    val feedback = selectedGoal?.feedback.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
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

        Text(
            text = "🫶",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        IntroBoldText(
            text = feedback,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
