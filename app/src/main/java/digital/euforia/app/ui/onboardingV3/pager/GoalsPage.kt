package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.ui.onboarding.pager.GoalItemView
import digital.euforia.app.ui.onboardingV3.OnboardingV3ViewModel
import digital.euforia.app.ui.util.widget.fadeBottom
import digital.euforia.app.ui.util.widget.fadeTop

@Composable
fun GoalsPage(
    viewModel: OnboardingV3ViewModel,
    goals: List<Goal>,
    selectedGoalId: String?,
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
        itemsIndexed(goals, key = { _, goal -> goal.identifier }) { _, goal ->
            GoalItemView(
                goal = goal,
                isSelected = goal.identifier == selectedGoalId,
                onClick = { viewModel.onGoalSelected(goal.identifier) },
            )
        }

        item {
            Spacer(Modifier.fillMaxWidth().navigationBarsPadding())
        }
    }
}
