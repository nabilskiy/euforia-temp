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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.DayBlue
import digital.euforia.app.ui.theme.DialogButton
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.widget.TimePickerView
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.openSystemSettings
import digital.euforia.app.ui.util.widget.AnimatedSizeBox
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isPermissionGranted =
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                viewModel.updateNotificationPermission(isPermissionGranted)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, navController)
    }

    BackHandler { viewModel.saveSettings() }

    NavBarlessScreen(navBarVisibilityState) {
        NotificationsContent(
            navController = navController,
            isNotificationsEnabled = state.isNotificationEnabled,
            morningTime = state.morningTime,
            daytimeTime = state.dayTime,
            eveningTime = state.eveningTime,
            isMorningEnabled = state.isMorningNotificationEnabled,
            isDaytimeEnabled = state.isDayNotificationEnabled,
            isEveningEnabled = state.isEveningNotificationEnabled,
            isSensitiveEnabled = state.isTimeSensitiveEnabled,
            onMorningToggleChanged = viewModel::onMorningNotificationToggled,
            onDaytimeToggleChanged = viewModel::onDayNotificationToggled,
            onEveningToggleChanged = viewModel::onEveningNotificationToggled,
            onMorningTimeChanged = viewModel::onMorningTimeChanged,
            onDaytimeTimeChanged = viewModel::onDayTimeChanged,
            onEveningTimeChanged = viewModel::onEveningTimeChanged,
            onSensitiveChanged = viewModel::onTimeSensitiveToggled,
            onBackClick = viewModel::saveSettings,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun SharedTransitionScope.NotificationsContent(
    navController: NavHostController,
    isNotificationsEnabled: Boolean,
    morningTime: Pair<Int, Int>,
    daytimeTime: Pair<Int, Int>,
    eveningTime: Pair<Int, Int>,
    isMorningEnabled: Boolean = false,
    isDaytimeEnabled: Boolean = false,
    isEveningEnabled: Boolean = false,
    isSensitiveEnabled: Boolean = false,
    onMorningToggleChanged: (Boolean) -> Unit = {},
    onDaytimeToggleChanged: (Boolean) -> Unit = {},
    onEveningToggleChanged: (Boolean) -> Unit = {},
    onMorningTimeChanged: (Pair<Int, Int>) -> Unit = {},
    onDaytimeTimeChanged: (Pair<Int, Int>) -> Unit = {},
    onEveningTimeChanged: (Pair<Int, Int>) -> Unit = {},
    onSensitiveChanged: (Boolean) -> Unit = {},
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
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
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
            if (!isNotificationsEnabled) {
                permissionItem {
                    openSystemSettings(context)
                }
            }
            notificationSettingsItem(
                isEnabled = isMorningEnabled,
                isEditable = isNotificationsEnabled,
                timeOfDay = TimeOfDay.MORNING,
                hoursAndMinutes = morningTime,
                onToggleChanged = onMorningToggleChanged,
                onTimeChanged = onMorningTimeChanged
            )
            notificationSettingsItem(
                isEnabled = isDaytimeEnabled,
                isEditable = isNotificationsEnabled,
                timeOfDay = TimeOfDay.DAYTIME,
                hoursAndMinutes = daytimeTime,
                onToggleChanged = onDaytimeToggleChanged,
                onTimeChanged = onDaytimeTimeChanged
            )
            notificationSettingsItem(
                isEnabled = isEveningEnabled,
                isEditable = isNotificationsEnabled,
                timeOfDay = TimeOfDay.EVENING,
                hoursAndMinutes = eveningTime,
                onToggleChanged = onEveningToggleChanged,
                onTimeChanged = onEveningTimeChanged
            )
            sensitiveItem(
                isEnabled = isSensitiveEnabled,
                isEditable = isNotificationsEnabled,
                onToggleChanged = onSensitiveChanged
            )

            settingsButtonItem(
                isEditable = isNotificationsEnabled
            ) {
                openSystemSettings(context)
            }
            item {
                Spacer(
                    modifier = Modifier.height(24.dp).fillMaxWidth().navigationBarsPadding()
                )
            }
        }
    }
}


fun LazyListScope.permissionItem(
    onClick: () -> Unit,
) = item(key = "permission") {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(color = NavBarBackground, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = localizedRes.string(R.string.notifications_settings_permission_denied_text),
            style = MaterialTheme.typography.bodyMedium,
            color = White.copy(alpha = 0.7f)
        )

        OutlinedButton(
            modifier = Modifier,
            onClick = onClick,
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = White,
                containerColor = DayBlue
            ),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
            border = null,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                modifier = Modifier,
                text = localizedRes.string(R.string.request_permissions_notifications_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = White,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun LazyListScope.notificationSettingsItem(
    isEnabled: Boolean,
    isEditable: Boolean,
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
            color = if (isEditable) White.copy(alpha = 0.7f) else White.copy(alpha = 0.3f)
        )
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(
                    color = if (isEditable) NavBarBackground else NavBarBackground.copy(
                        alpha = 0.5f
                    ), shape = RoundedCornerShape(16.dp)
                )
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
                    color = if (isEditable) White else White.copy(alpha = 0.5f)
                )

                CustomSwitch(
                    checked = isEnabled,
                    isEditable = isEditable,
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
                        color = if (isEditable) White else White.copy(alpha = 0.5f)
                    )

                    TimeView(
                        isEditable = isEditable,
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
                color = if (isEditable) White.copy(alpha = 0.7f) else White.copy(0.4f)
            )
        }
    }
}

fun LazyListScope.sensitiveItem(
    isEnabled: Boolean,
    isEditable: Boolean,
    onToggleChanged: (Boolean) -> Unit,
) = item(key = "sensitive") {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(
                color = if (isEditable) NavBarBackground else NavBarBackground.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
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
                text = LocalLocalizedRes.current.string(R.string.notifications_settings_time_sensitive),
                style = MaterialTheme.typography.titleMedium,
                color = if (isEditable) White else White.copy(alpha = 0.5f)
            )

            CustomSwitch(
                checked = isEnabled,
                isEditable = isEditable,
                onCheckedChange = { onToggleChanged(it) },
            )
        }
    }
    Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = localizedRes.string(R.string.notifications_settings_time_sensitive_details),
        style = MaterialTheme.typography.bodyMedium,
        color = if (isEnabled) White.copy(alpha = 0.7f) else White.copy(alpha = 0.4f)
    )
}

fun LazyListScope.settingsButtonItem(
    isEditable: Boolean,
    onClick: () -> Unit,
) = item(key = "system settings") {
    val localizedRes = LocalLocalizedRes.current
    OutlinedButton(
        enabled = isEditable,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp),
        onClick = onClick,
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = White.copy(alpha = 0.6f),
            containerColor = NavBarBackground,
            disabledContainerColor = NavBarBackground.copy(alpha = 0.5f),
            disabledContentColor = White.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(0.dp),
        border = null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            text = localizedRes.string(R.string.profile_system_settings),
            color = if (isEditable) DialogButton else DialogButton.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun TimeView(
    isEditable: Boolean,
    hoursAndMinutes: Pair<Int, Int>,
    onTimeChanged: (Pair<Int, Int>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val textColor = if (expanded) DayBlue else {
        if (isEditable) White else White.copy(0.5f)
    }

    Box(modifier = Modifier.noRippleClickable {
        if (isEditable) {
            expanded = true
        }
    }) {
        Text(
            modifier = Modifier.background(
                color = if (isEditable) DarkGray else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
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
    isEditable: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 52.dp,
    height: Dp = 32.dp,
    thumbPadding: Dp = 2.dp,
) {

    val checkedColor: Color = if (isEditable) DayBlue else DayBlue.copy(alpha = 0.5f)
    val uncheckedColor: Color = if (isEditable) DarkGray else DarkGray.copy(alpha = 0.5f)
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
                enabled = enabled && isEditable,
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
                .background(if (isEditable) Color.White else White.copy(alpha = 0.7f), CircleShape)
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