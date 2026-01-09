@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.settings.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.DayBlue
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.widget.TimePickerView
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SharedTransitionScope.NotificationsScreen(
    navController: NavHostController,
    viewModel: NotificationsViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    BackHandler { viewModel.saveSettings() }

    NavBarlessScreen(navBarVisibilityState) {
        NotificationsContent(
            navController = navController,
            morningTime = state.morningTime,
            daytimeTime = state.dayTime,
            eveningTime = state.eveningTime,
            isMorningEnabled = state.isMorningNotificationEnabled,
            isDaytimeEnabled = state.isDayNotificationEnabled,
            isEveningEnabled = state.isEveningNotificationEnabled,
            onMorningToggleChanged = viewModel::onMorningNotificationToggled,
            onDaytimeToggleChanged = viewModel::onDayNotificationToggled,
            onEveningToggleChanged = viewModel::onEveningNotificationToggled,
            onMorningTimeChanged = viewModel::onMorningTimeChanged,
            onDaytimeTimeChanged = viewModel::onDayTimeChanged,
            onEveningTimeChanged = viewModel::onEveningTimeChanged,
            onBackClick = viewModel::saveSettings,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun SharedTransitionScope.NotificationsContent(
    navController: NavHostController,
    morningTime: Pair<Int, Int>,
    daytimeTime: Pair<Int, Int>,
    eveningTime: Pair<Int, Int>,
    isMorningEnabled: Boolean = false,
    isDaytimeEnabled: Boolean = false,
    isEveningEnabled: Boolean = false,
    onMorningToggleChanged: (Boolean) -> Unit = {},
    onDaytimeToggleChanged: (Boolean) -> Unit = {},
    onEveningToggleChanged: (Boolean) -> Unit = {},
    onMorningTimeChanged: (Pair<Int, Int>) -> Unit = {},
    onDaytimeTimeChanged: (Pair<Int, Int>) -> Unit = {},
    onEveningTimeChanged: (Pair<Int, Int>) -> Unit = {},
    onBackClick: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val localizedRes = LocalLocalizedRes.current
    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 16.dp.roundToPx() }
    val shouldBlur by remember(listState) {
        derivedStateOf {
            val firstIndex = listState.firstVisibleItemIndex
            val firstOffset = listState.firstVisibleItemScrollOffset
            // Blur when the very first list item (spacer) scrolled off enough
            // or when any next item became the first visible one.
            firstIndex > 0 || firstOffset > thresholdPx
        }
    }

    Box() {
        BlurredAppBar(
            backTitleRes = R.string.profile_title,
            titleRes = R.string.notifications_settings_title,
            shouldBlur = shouldBlur,
            hazeState = hazeState,
            onBackClick = onBackClick,
            // Shared element for back title "My Euforia"
            sharedElementKeyForBackTitle = "my_euforia_title",
            animatedVisibilityScope = animatedVisibilityScope,
            navController = navController
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().hazeSource(hazeState),
            state = listState,
            verticalArrangement = Arrangement.Absolute.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = AppBarHeightMedium + 16.dp,
                bottom = 56.dp
            ),
        ) {
            titleItem(
                titleRes = R.string.notifications_settings_title
            )
            notificationSettingsItem(
                isEnabled = isMorningEnabled,
                timeOfDay = TimeOfDay.MORNING,
                hoursAndMinutes = morningTime,
                onToggleChanged = onMorningToggleChanged,
                onTimeChanged = onMorningTimeChanged
            )
            notificationSettingsItem(
                isEnabled = isDaytimeEnabled,
                timeOfDay = TimeOfDay.DAYTIME,
                hoursAndMinutes = daytimeTime,
                onToggleChanged = onDaytimeToggleChanged,
                onTimeChanged = onDaytimeTimeChanged
            )
            notificationSettingsItem(
                isEnabled = isEveningEnabled,
                timeOfDay = TimeOfDay.EVENING,
                hoursAndMinutes = eveningTime,
                onToggleChanged = onEveningToggleChanged,
                onTimeChanged = onEveningTimeChanged
            )
            item {
                Spacer(
                    modifier = Modifier.height(1008.dp).fillMaxWidth()
                )
            }
        }
    }
}

fun LazyListScope.notificationSettingsItem(
    isEnabled: Boolean,
    timeOfDay: TimeOfDay,
    hoursAndMinutes: Pair<Int, Int>,
    onToggleChanged: (Boolean) -> Unit,
    onTimeChanged: (Pair<Int, Int>) -> Unit
) = item(key = timeOfDay.name) {

    Column(
        modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            text = LocalLocalizedRes.current.string(timeOfDay.getTitleRes()).uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light),
            color = White.copy(alpha = 0.7f)
        )
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(color = NavBarBackground, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = LocalLocalizedRes.current.string(R.string.notifications_settings_accompaniment_enabled),
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )

                CustomSwitch(
                    checked = isEnabled,
                    onCheckedChange = { onToggleChanged(it) },
                )
            }
            if (isEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(0.5.dp)
                        .background(White.copy(alpha = 0.2f))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = LocalLocalizedRes.current.string(R.string.notifications_settings_accompaniment_time),
                        style = MaterialTheme.typography.titleMedium,
                        color = White
                    )

                    TimeView(
                        hoursAndMinutes = hoursAndMinutes,
                        onTimeChanged = onTimeChanged
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isEnabled,
            enter = androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.shrinkVertically()
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = LocalLocalizedRes.current.string(timeOfDay.getDescriptionRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TimeView(
    hoursAndMinutes: Pair<Int, Int>,
    onTimeChanged: (Pair<Int, Int>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val textColor = if (expanded) DayBlue else White

    Box(modifier = Modifier.noRippleClickable { expanded = true }) {
        Text(
            modifier = Modifier.background(color = DarkGray, shape = RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            text = "%02d:%02d".format(hoursAndMinutes.first, hoursAndMinutes.second),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
        TimePickerView(
            expanded = expanded,
            initialTime = hoursAndMinutes,
            onTimeChanged = onTimeChanged,
            onExpandedChange = { expanded = it }
        )
    }
}

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 52.dp,
    height: Dp = 32.dp,
    thumbPadding: Dp = 2.dp,
    checkedColor: Color = DayBlue,
    uncheckedColor: Color = DarkGray
) {
    val thumbSize = height - thumbPadding * 2

    val backgroundColor by animateColorAsState(
        targetValue = if (checked) checkedColor else uncheckedColor,
        label = "background"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked)
            width - thumbSize - thumbPadding * 2
        else 0.dp,
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .size(width, height)
            .clip(RoundedCornerShape(percent = 50))
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onCheckedChange(!checked)
            }
            .padding(thumbPadding)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.25f)
                )
                .background(Color.White, CircleShape)
        )
    }
}


private fun TimeOfDay.getTitleRes(): Int {
    return when (this) {
        TimeOfDay.MORNING -> R.string.notifications_settings_morning_title
        TimeOfDay.DAYTIME -> R.string.notifications_settings_daytime_title
        TimeOfDay.EVENING -> R.string.notifications_settings_evening_title
    }
}

private fun TimeOfDay.getDescriptionRes(): Int {
    return when (this) {
        TimeOfDay.MORNING -> R.string.notifications_settings_morning_details
        TimeOfDay.DAYTIME -> R.string.notifications_settings_daytime_details
        TimeOfDay.EVENING -> R.string.notifications_settings_evening_details
    }
}

private fun handleSideEffect(
    sideEffect: NotificationsSideEffect,
    navController: NavHostController
) {
    when (sideEffect) {
        is NotificationsSideEffect.NavigateBack -> {
            navController.popBackStack()
        }

        else -> {}
    }
}