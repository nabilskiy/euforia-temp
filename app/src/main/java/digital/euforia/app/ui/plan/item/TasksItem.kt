package digital.euforia.app.ui.plan.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.ui.plan.PlanViewItems
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.DescriptionDisabled
import digital.euforia.app.ui.theme.MaxGradient
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.TasksGradient
import digital.euforia.app.ui.theme.TitleDisabled
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.AnimatedSizeBox
import digital.euforia.app.ui.util.widget.PremiumButton

fun LazyListScope.tasksItem(
    isDemo: Boolean,
    isPremium: Boolean,
    tasks: List<DailyTask>,
    completedTasks: Int,
    onPremiumClick: () -> Unit
) = item(key = PlanViewItems.TASKS, contentType = PlanViewItems.TASKS) {
    val localizedRes = LocalLocalizedRes.current
    Column {
        val titleRes =
            if (isDemo) R.string.today_progress_free_title else R.string.today_progress_premium_title
        Text(
            text = localizedRes.string(titleRes),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
            color = White,
            modifier = Modifier.padding(start = 16.dp, top = 64.dp, end = 16.dp, bottom = 4.dp)
        )
        Box {
            val heightDpMap = remember { mutableStateMapOf<Int, Dp>() }
            ProgressScaleView(
                foregroundHeightDp = heightDpMap.computeHeight(completedTasks),
                backgroundHeightDp = heightDpMap.computeHeight(tasks.size)
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                tasks.forEachIndexed { index, task ->
                    DailyTaskItem(
                        index = index, task = task, isCompleted = index < completedTasks,
                        heightMap = heightDpMap
                    )
                }
            }
        }
        if (!isPremium) {
            PremiumButton(onClick = onPremiumClick)
        }
    }
}

@Composable
fun PremiumButton(onClick: () -> Unit) {
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = Modifier
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        PremiumButton(
            text = localizedRes.string(R.string.vibes_skip_demo_period_button),
            onClick = onClick
        )
    }
//    AnimatedSizeBox(
//        onClick = onClick,
//        modifier = Modifier
//            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
//            .height(56.dp)
//            .fillMaxWidth(),
//    ) {
//        Box(
//            modifier = Modifier
//                .background(brush = PremiumGradient, shape = CircleShape)
//                .clip(CircleShape)
//                .fillMaxWidth()
//                .padding(vertical = 12.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = stringResource(R.string.vibes_skip_demo_period_button),
//                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
//                color = White
//            )
//        }
//    }
}

@Composable
private fun BoxScope.ProgressScaleView(backgroundHeightDp: Dp, foregroundHeightDp: Dp) {
    Box(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
        // background
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(backgroundHeightDp)
                .width(24.dp)
                .background(
                    color = White.copy(alpha = 0.1f), shape = CircleShape
                )
        )
        // foreground
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(foregroundHeightDp)
                .width(24.dp)
                .background(
                    brush = TasksGradient, shape = CircleShape
                )
        )
    }
}

@Composable
private fun ColumnScope.DailyTaskItem(
    index: Int,
    task: DailyTask,
    isCompleted: Boolean,
    heightMap: MutableMap<Int, Dp>
) {
    val localizedRes = LocalLocalizedRes.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    Row(
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp).onGloballyPositioned {
            val heightDp = with(density) { it.size.height.toDp() }
            heightMap[index] = heightDp
        },
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(task.iconRes),
            contentDescription = null,
            tint = if (isCompleted) White.copy(alpha = 0.7f) else White
        )
        Column(
            modifier = Modifier.padding(start = 20.dp),
            verticalArrangement = spacedBy(4.dp)
        ) {
            Text(
                modifier = Modifier.heightIn(min = 24.dp),
                text = localizedRes.string(task.titleRes),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = if (isCompleted) TitleDisabled else White
            )
            Text(
                text = localizedRes.string(task.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCompleted) DarkGray else DescriptionDisabled
            )
        }
    }
}

private fun Map<Int, Dp>.computeHeight(stopIndex: Int): Dp {
    var totalHeight = 0.dp
    forEach { (index, height) ->
        if (index <= stopIndex) {
            totalHeight += height
        }
    }

    return totalHeight
}