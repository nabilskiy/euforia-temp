package digital.euforia.app.ui.plan

import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.BannerConfig
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.domain.model.plan.DailyTask
import digital.euforia.app.domain.model.plan.ExtraPackage
import digital.euforia.app.domain.model.plan.RankedPackage
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.plan.item.dayItem
import digital.euforia.app.ui.plan.item.continuousItem
import digital.euforia.app.ui.plan.item.extraItem
import digital.euforia.app.ui.plan.item.soundscapesItem
import digital.euforia.app.ui.plan.item.tasksItem
import digital.euforia.app.ui.plan.item.topProgramsItem
import digital.euforia.app.ui.player.audio.AudioPlayerEntryPoint
import digital.euforia.app.ui.player.audio.AudioPlayerScreen
import digital.euforia.app.ui.theme.PrimaryBackground
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PlanScreen(
    navController: NavHostController,
    viewModel: PlanViewModel
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    PlanContent(
        days = state.days,
        completedDays = state.completedDays,
        selectedDayIndex = state.selectedDayIndex,
        freeDemoDays = state.freeDemoDays,
        isPremium = state.isPremium,
        isDemo = state.isDemo,
        timeOfDay = state.timeOfDay,
        timeOfDayConfig = state.timeOfDayConfig,
        completedDailyTasks = state.completedDailyTasks,
        dailyTasks = state.dailyTasks,
        continuousDays = state.continuousDays,
        topPrograms = state.topPackages,
        bannerConfig = state.bannerConfig,
        extraPackage = state.extraPackage,
        onDaySelected = viewModel::onDaySelected,
        onDayTimeItemClick = viewModel::onDayTimeItemClick
    )
}

@Composable
private fun PlanContent(
    days: List<DayUi>,
    completedDays: Int,
    selectedDayIndex: Int,
    freeDemoDays: Int,
    isPremium: Boolean,
    isDemo: Boolean,
    timeOfDay: TimeOfDay,
    timeOfDayConfig: TimeOfDayConfig,
    completedDailyTasks: Int,
    dailyTasks: List<DailyTask>,
    continuousDays: Int,
    topPrograms: List<RankedPackage>,
    bannerConfig: BannerConfig?,
    extraPackage: ExtraPackage?,
    onDaySelected: (Int) -> Unit,
    onDayTimeItemClick: (DayTimeItemUi) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(color = PrimaryBackground)) {
        dayItem(
            days = days,
            completedDays = completedDays,
            freeDemoDays = freeDemoDays,
            isPremium = isPremium,
            isDemo = isDemo,
            selectedDayIndex = selectedDayIndex,
            timeOfDay = timeOfDay,
            timeOfDayConfig = timeOfDayConfig,
            onDaySelected = onDaySelected,
            onDayTimeItemClick = onDayTimeItemClick
        )
        tasksItem(
            isDemo = isDemo,
            tasks = dailyTasks,
            completedTasks = completedDailyTasks
        )
        if (!isDemo) {
            continuousItem(days = continuousDays)
        }
        extraItem(extraPackage = extraPackage) {}
        topProgramsItem(
            isDemo = isDemo,
            topPrograms = topPrograms
        )
        soundscapesItem(
            isDemo = isDemo,
            isPremium = isPremium,
            bannerConfig = bannerConfig,
            onSoundscapesClick = {},
            onBannerClick = {},
            onFAQClick = {},
            onSupportClick = {},
            onSOSClick = {}

        )
        item {
            Spacer(modifier = Modifier.fillMaxWidth().height(200.dp))
        }
    }
}

private fun handleSideEffect(sideEffect: PlanSideEffect, navController: NavHostController) {
    when (sideEffect) {
        is PlanSideEffect.NavigateAudioPlayer -> {
            navController.navigate(
                HomeDestination.AudioPlayer(
                    accompanimentId = sideEffect.accompanimentId,
                    timeOfDay = sideEffect.timeOfDay,
                    entryPoint = AudioPlayerEntryPoint.DAY
                )
            )
        }
        else -> {}
    }
}

@Keep
enum class PlanViewItems { DAYS, TASKS, EXTRA, STREAK, TOP, SOUNDSCAPES, SOS }