package digital.euforia.app.ui.plan.item

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.ui.plan.PlanViewItems

fun LazyListScope.topProgramsItem(
    isDemo: Boolean,
) = item(key = PlanViewItems.TASKS, contentType = PlanViewItems.TASKS) {

}