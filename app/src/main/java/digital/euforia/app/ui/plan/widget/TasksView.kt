package digital.euforia.app.ui.plan.widget

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.unit.dp
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.ui.plan.PlanViewItems
import digital.euforia.app.ui.theme.TitleDisabled
import digital.euforia.app.ui.theme.White

fun LazyListScope.tasksItem(
    tasks: List<DailyTask>,
    completedTasks: Int
) = item(key = PlanViewItems.TASKS, contentType = PlanViewItems.TASKS) {
    Column(modifier = Modifier.fillMaxWidth()) {
        tasks.forEachIndexed { index, task ->
            DailyTaskItem(task = task, isCompleted = index < completedTasks)
        }
    }
}

@Composable
private fun ColumnScope.DailyTaskItem(task: DailyTask, isCompleted: Boolean) {
    Row(modifier = Modifier.padding(top = 12.dp)) {

        Icon(
            painter = painterResource(task.iconRes),
            contentDescription = null,
            tint = if (isCompleted) White.copy(alpha = 0.7f) else White
        )
        Column(
            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
            verticalArrangement = spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(task.titleRes),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = SemiBold),
                color = White
            )
            Text(
                text = stringResource(task.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCompleted) TitleDisabled else White
            )
        }
    }
}