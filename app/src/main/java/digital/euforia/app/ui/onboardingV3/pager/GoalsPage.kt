package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Goal
import digital.euforia.app.ui.onboardingV3.OnboardingV3ViewModel
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.AnimatedSizeBox
import digital.euforia.app.ui.util.widget.fadeBottom
import digital.euforia.app.ui.util.widget.fadeTop
import kotlinx.coroutines.delay

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
        verticalArrangement = spacedBy(18.dp),
        contentPadding = PaddingValues(
            start = 18.dp,
            top = 192.dp,
            end = 18.dp,
            bottom = 132.dp,
        ),
    ) {
        itemsIndexed(goals, key = { _, goal -> goal.identifier }) { index, goal ->
            V3GoalItemView(
                index = index,
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

@Composable
private fun V3GoalItemView(
    index: Int,
    goal: Goal,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(25.dp)
    val appear = remember(goal.identifier) { Animatable(0f) }
    val startOffsetY = with(LocalDensity.current) { 80.dp.toPx() }

    LaunchedEffect(goal.identifier) {
        appear.snapTo(0f)
        delay(index * 50L)
        appear.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }

    AnimatedSizeBox(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = appear.value
                    translationY = (1f - appear.value) * startOffsetY
                    scaleX = 0.8f + appear.value * 0.2f
                    scaleY = 0.8f + appear.value * 0.2f
                }
                .heightIn(min = 62.dp)
                .background(Color.White.copy(alpha = 0.04f), shape)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFE29B31),
                                    Color(0xFFFF5589),
                                    Color(0xFF204FC0),
                                ),
                            ),
                            shape = shape,
                        )
                    } else {
                        Modifier
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp, top = 18.dp, end = 16.dp, bottom = 18.dp),
                text = goal.text,
                color = White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            )
            if (isSelected) {
                Icon(
                    modifier = Modifier.padding(end = 16.dp),
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = White,
                )
            }
        }
    }
}
