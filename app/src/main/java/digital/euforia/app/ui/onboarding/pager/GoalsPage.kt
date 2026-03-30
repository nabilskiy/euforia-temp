package digital.euforia.app.ui.onboarding.pager

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.overscroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.ui.onboarding.OnboardingViewModel
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.AnimatedListCheckItem
import digital.euforia.app.ui.util.widget.fadeBottom
import digital.euforia.app.ui.util.widget.fadeTop

@Composable
fun GoalsPage(
    viewModel: OnboardingViewModel,
    goals: List<Goal>,
    selectedGoals: Set<Int>,
) {
    GoalsPageContent(
        selectedGoals = selectedGoals,
        goals = goals,
        onGoalToggled = viewModel::onGoalToggled
    )
}

@Composable
private fun GoalsPageContent(
    goals: List<Goal>,
    selectedGoals: Set<Int>,
    onGoalToggled: (Int) -> Unit
) {
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
            bottom = 108.dp
        ),
    ) {
        itemsIndexed(goals, key = { index, goal -> goal.code }) { index, goal ->
            GoalItemView(
                goal = goal,
                isSelected = index in selectedGoals,
                onClick = { onGoalToggled(index) }
            )
        }

        item {
            Spacer(Modifier.fillMaxWidth().navigationBarsPadding())
        }
    }
}

@Composable
fun GoalItemView(
    goal: Goal,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val checkIconRes =
        if (isSelected) R.drawable.ic_checkbox_checked else R.drawable.ic_checkbox_unchecked

    AnimatedListCheckItem(
        isSelected = isSelected,
        minHeight = 64.dp,
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = goal.text,
            color = White,
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