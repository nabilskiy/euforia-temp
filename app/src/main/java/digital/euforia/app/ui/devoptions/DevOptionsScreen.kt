package digital.euforia.app.ui.devoptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DevOptionsScreen(
    navController: NavHostController,
    viewModel: DevOptionsViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    DevOptionsContent(
        isPremium = state.isPremium,
        isDemo = state.isDemoUser,
        isOnboardingCompleted = state.isOnboardingCompleted,
        onPremiumChange = viewModel::onIsPremiumChanged,
        onDemoChange = viewModel::onIsDemoChanged,
        onOnboardingCompletedChange = viewModel::onIsOnboardingCompletedChanged
    )
}

@Composable
private fun DevOptionsContent(
    isPremium: Boolean,
    isDemo: Boolean,
    isOnboardingCompleted: Boolean,
    onPremiumChange: (Boolean) -> Unit,
    onDemoChange: (Boolean) -> Unit,
    onOnboardingCompletedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
            .systemBarsPadding(),
        verticalArrangement = spacedBy(12.dp)
    ) {
        OptionView(
            text = "Is Premium",
            isChecked = isPremium,
            onCheckedChange = onPremiumChange
        )

        OptionView(
            text = "Is Demo",
            isChecked = isDemo,
            onCheckedChange = onDemoChange
        )

        OptionView(
            text = "Is Onboarding Completed",
            isChecked = isOnboardingCompleted,
            onCheckedChange = onOnboardingCompletedChange
        )

    }
}

@Composable
fun OptionView(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = Modifier.fillMaxWidth().noRippleClickable(onClick = {onCheckedChange(!isChecked)}),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Magenta,
                uncheckedColor = Color.Gray,
                checkmarkColor = Color.White
            )
        )

        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = text,
            color = White
        )
    }
}

private fun handleSideEffect(sideEffect: DevOptionsSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}