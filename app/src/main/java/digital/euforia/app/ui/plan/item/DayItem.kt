package digital.euforia.app.ui.plan.item

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.ui.plan.DayTimeItemUi
import digital.euforia.app.ui.plan.DayUi
import digital.euforia.app.ui.plan.PlanViewItems
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.daytimeColors
import digital.euforia.app.ui.theme.eveningColors
import digital.euforia.app.ui.theme.morningColors
import digital.euforia.app.ui.util.canvas.timeOfDayBackgroundAnimation
import digital.euforia.app.ui.util.formatDateFromMillis
import digital.euforia.app.ui.util.shadow
import digital.euforia.app.ui.util.widget.AccompanimentBackground
import digital.euforia.app.ui.util.widget.ActionButton
import digital.euforia.app.ui.util.widget.AccompanimentButtonColors
import digital.euforia.app.ui.util.widget.AccompanimentButtonDimensions
import digital.euforia.app.ui.util.widget.MaxTextView
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


fun LazyListScope.dayItem(
    days: List<DayUi>,
    completedDays: Int,
    freeDemoDays: Int,
    isPremium: Boolean,
    isDemo: Boolean,
    selectedDayIndex: Int,
    timeOfDay: TimeOfDay,
    timeOfDayConfig: TimeOfDayConfig,
    onDaySelected: (Int) -> Unit,
    onDayTimeItemClick: (DayTimeItemUi) -> Unit
) = item(key = PlanViewItems.DAYS, contentType = PlanViewItems.DAYS) {
    val day = days.getOrNull(selectedDayIndex)

    Column(modifier = Modifier.timeOfDayBackgroundAnimation(timeOfDay)) {
        day?.let {
            Column {
                HeaderView(
                    day = day,
                    dayIndex = selectedDayIndex,
                    completedDays = completedDays,
                    isDemo = isDemo,
                    isPremium = isPremium,
                    isNextEnabled = days.lastIndex > selectedDayIndex,
                    isPrevEnabled = selectedDayIndex > 0,
                    onPrevClick = { onDaySelected(selectedDayIndex - 1) },
                    onNextClick = { onDaySelected(selectedDayIndex + 1) }
                )
            }
        }

        val pagerState = rememberPagerState { days.size }
        subscribeToPagerUpdates(
            coroutineScope = rememberCoroutineScope(),
            pagerState = pagerState,
            page = selectedDayIndex
        )
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .collect { page -> onDaySelected(page) }
        }
        HorizontalPager(
            modifier = Modifier.fillMaxWidth(1f).heightIn(min = 260.dp),
            state = pagerState,
            userScrollEnabled = true
        ) { position ->
            days.getOrNull(position)?.let { pageDay ->
                AccompanimentPagerPage(
                    day = pageDay,
                    timeOfDayConfig = timeOfDayConfig,
                    onDayTimeItemClick = onDayTimeItemClick
                )
            }
        }
    }
}

@Composable
private fun AccompanimentPagerPage(
    day: DayUi,
    timeOfDayConfig: TimeOfDayConfig,
    onDayTimeItemClick: (DayTimeItemUi) -> Unit
) {
    if (day.items.size == 3) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(260.dp),
            horizontalArrangement = spacedBy(8.dp)
        ) {
            AccompanimentButton(
                modifier = Modifier.fillMaxHeight().weight(1f),
                accompaniment = day.accompaniment,
                item = day.items.first(),
                timeOfDayConfig = timeOfDayConfig,
                isCompleted = false,
                buttonDimensions = AccompanimentButtonDimensions.PRIMARY,
                onClick = onDayTimeItemClick
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = spacedBy(8.dp)
            ) {
                AccompanimentButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    accompaniment = day.accompaniment,
                    item = day.items[1],
                    timeOfDayConfig = timeOfDayConfig,
                    isCompleted = false,
                    onClick = onDayTimeItemClick
                )
                AccompanimentButton(
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    accompaniment = day.accompaniment,
                    item = day.items.last(),
                    timeOfDayConfig = timeOfDayConfig,
                    isCompleted = true,
                    onClick = onDayTimeItemClick
                )
            }
        }
    }
}

@Composable
private fun AccompanimentButton(
    modifier: Modifier,
    accompaniment: Accompaniment,
    item: DayTimeItemUi,
    isCompleted: Boolean,
    timeOfDayConfig: TimeOfDayConfig,
    isLocked: Boolean = false,
    isToday: Boolean = false,
    buttonDimensions: AccompanimentButtonDimensions = AccompanimentButtonDimensions.SECONDARY,
    onClick: (DayTimeItemUi) -> Unit
) {
    Box(modifier = modifier.noRippleClickable(onClick = { onClick(item) })) {
        DayTimeBackground(item, buttonDimensions)

        when (item.state) {
            DayTimeItemUi.State.SCHEDULED -> {
                TimeView(
                    timeOfDay = item.item.timeOfDay,
                    config = timeOfDayConfig
                )
            }

            DayTimeItemUi.State.COMPLETED -> {
                DayTimeActionButton(buttonDimensions, R.drawable.ic_repeat)
            }

            DayTimeItemUi.State.AVAILABLE -> {
                DayTimeActionButton(buttonDimensions, R.drawable.ic_play_variant)
            }

            else -> LockIcon()
        }

        TimeOfDayTitleView(
            title = accompaniment.getTitleByTimeOfDay(item),
            timeOfDay = item.item.timeOfDay,
            buttonDimensions = buttonDimensions,
            isCompleted = isCompleted,
            completedItems = 0,
            totalItems = 0,
        )
    }
}

@Composable
fun BoxScope.LockIcon() {
    Icon(
        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            .size(24.dp),
        painter = painterResource(R.drawable.ic_lock),
        contentDescription = null,
        tint = White.copy(alpha = 0.3f)
    )
}

@Composable
private fun BoxScope.DayTimeActionButton(
    buttonDimensions: AccompanimentButtonDimensions,
    iconRes: Int
) {
    Icon(
        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            .size(buttonDimensions.iconSize)
            .background(color = White.copy(alpha = 0.15f), shape = CircleShape)
            .padding(buttonDimensions.iconPadding),
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = White
    )
}

@Composable
private fun DayTimeBackground(
    item: DayTimeItemUi,
    buttonDimensions: AccompanimentButtonDimensions
) {
    when (item.state) {
        DayTimeItemUi.State.AVAILABLE, DayTimeItemUi.State.COMPLETED -> {
            AccompanimentBackground(
                colors = item.item.timeOfDay.getButtonColors(),
                dimensions = buttonDimensions,
            )
        }

        else -> {
            LockedItemBackground()
        }
    }
}

@Composable
fun BoxScope.TimeView(timeOfDay: TimeOfDay, config: TimeOfDayConfig) {
    val hour = when (timeOfDay) {
        TimeOfDay.MORNING -> config.morningBegin
        TimeOfDay.DAYTIME -> config.daytimeBegin
        TimeOfDay.EVENING -> config.eveningBegin
    }

    Row(
        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp)
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            painter = painterResource(R.drawable.ic_clock),
            contentDescription = null,
            tint = White.copy(alpha = 0.3f)
        )
        Text(
            text = "$hour:00",
            style = MaterialTheme.typography.labelMedium.copy(),
            color = White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LockedItemBackground() {
    Card(
        modifier = Modifier.fillMaxSize()
            .shadow(spread = 2.dp, color = Black.copy(alpha = 0.2f), blurRadius = 32.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGray.copy(alpha = 0.2f)),
//        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .blur(35.dp)
//                .background(backgroundColor)
        ) {
        }
    }
}

@Composable
private fun BoxScope.TimeOfDayTitleView(
    title: String,
    timeOfDay: TimeOfDay,
    buttonDimensions: AccompanimentButtonDimensions,
    isCompleted: Boolean,
    completedItems: Int,
    totalItems: Int,
) {
    Column(
        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
        verticalArrangement = spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = CenterVertically
        ) {
            val completedText = if (completedItems > 0 && totalItems > 0) {
                " $completedItems/$totalItems"
            } else {
                ""
            }

            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.drawable.ic_checkbox_full),
                contentDescription = null,
                tint = White.copy(alpha = 0.2f)
            )
            Text(
                text = "${stringResource(id = timeOfDay.getTitleRes()).uppercase()}$completedText",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = White.copy(alpha = 0.2f)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = buttonDimensions.titleFontSize,
                fontWeight = FontWeight.Bold
            ),
            color = White
        )
    }

}

@Composable
fun HeaderView(
    day: DayUi,
    dayIndex: Int,
    completedDays: Int,
    isDemo: Boolean,
    isPremium: Boolean,
    isNextEnabled: Boolean,
    isPrevEnabled: Boolean,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 106.dp)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(16.dp),
            verticalAlignment = CenterVertically
        ) {
            TitleText(
                dayIndex = dayIndex,
                isDemo = isDemo,
                isPremium = isPremium,
                isToday = day.isToday,
                daysOffset = dayIndex - completedDays
            )

            Box(
                modifier = Modifier.weight(1f)
            ) {
                MaxBadge(isMax = day.isLockedByPremium())
            }
            ActionButton(
                iconRes = R.drawable.ic_arrow_back,
                isEnabled = isPrevEnabled,
                onClick = onPrevClick
            )
            ActionButton(
                iconModifier = Modifier.rotate(180f),
                iconRes = R.drawable.ic_arrow_back,
                isEnabled = isNextEnabled,
                onClick = onNextClick
            )
        }
        LockText(
            index = dayIndex,
            day = day
        )
    }
}

@Composable
private fun TitleText(
    dayIndex: Int,
    isDemo: Boolean,
    isPremium: Boolean,
    isToday: Boolean,
    daysOffset: Int
) {

    val todayText =
        if (isDemo) {
            stringResource(R.string.vibes_title_demo_date, dayIndex + 1)
        } else if (isToday) {
            stringResource(R.string.today_title)
        } else {
            formatDateFromMillis(System.currentTimeMillis() + (daysOffset * 86400000L))
        }

    Text(
        text = todayText,
        style = MaterialTheme.typography.displaySmall,
        color = White
    )
}

@Composable
private fun LockText(index: Int, day: DayUi) {
    val text = if (day.isLockedByPrevDay()) {
        stringResource(R.string.vibes_unavailable_text, index)
    } else if (day.isLockedByPremium()) {
        stringResource(R.string.vibes_unavailable_get_max_text)
    } else {
        null
    }
    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = text.orEmpty().uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = White.copy(0.7f)
        )
    }
//    }
//
//    if (!isPremium) {
//        val text = if (isDemo) {
//            stringResource(R.string.vibes_title_demo_date, dayIndex)
//        } else {
//            stringResource(R.string.vibes_unavailable_get_max_text)
//        }
//        Text(
//            modifier = Modifier.padding(top = 24.dp),
//            text = text,
//            style = MaterialTheme.typography.displaySmall,
//            color = White
//        )
//    }
}

@Composable
fun MaxBadge(isMax: Boolean) {
    AnimatedVisibility(
        visible = isMax,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        MaxTextView()
    }
}

fun subscribeToPagerUpdates(
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    page: Int
) {
    coroutineScope.launch {
        if (pagerState.currentPage != page) {
            pagerState.animateScrollToPage(page)
        }
    }
}

private fun Accompaniment.getTitleByTimeOfDay(
    item: DayTimeItemUi
): String {
    return when (item.item.timeOfDay) {
        TimeOfDay.MORNING -> morningTitle
        TimeOfDay.DAYTIME -> {
            val phrasesList = phrases
            if (phrasesList.isEmpty()) return daytimeTitle

            val viewedId = item.item.viewedPhraseId
            if (viewedId == null) {
                return phrasesList.first().daytimeTitle
            }

            val idx = phrasesList.indexOfFirst { it.id == viewedId }
            return if (idx == -1) {
                // If the viewed ID is not found, default to the first phrase title
                phrasesList.first().daytimeTitle
            } else if (idx < phrasesList.lastIndex) {
                // Return the next phrase's daytime title
                phrasesList[idx + 1].daytimeTitle
            } else {
                // If the current is the last, return the last one's title
                phrasesList.last().daytimeTitle
            }
        }

        TimeOfDay.EVENING -> eveningTitle
    }
}

private fun TimeOfDay.getTitleRes(): Int {
    return when (this) {
        TimeOfDay.MORNING -> R.string.vibes_morning_title
        TimeOfDay.DAYTIME -> R.string.vibes_daytime_title
        TimeOfDay.EVENING -> R.string.vibes_evening_title
    }
}

private fun TimeOfDay.getButtonColors(): AccompanimentButtonColors {
    return when (this) {
        TimeOfDay.MORNING -> AccompanimentButtonColors.MORNING
        TimeOfDay.DAYTIME -> AccompanimentButtonColors.DAYTIME
        TimeOfDay.EVENING -> AccompanimentButtonColors.EVENING
    }
}

private fun TimeOfDay.getColor(ordinal: Int): Color {
    return when (this) {
        TimeOfDay.MORNING -> morningColors.getOrElse(ordinal) { morningColors.last() }
        TimeOfDay.DAYTIME -> daytimeColors.getOrElse(ordinal) { daytimeColors.last() }
        TimeOfDay.EVENING -> eveningColors.getOrElse(ordinal) { eveningColors.last() }
    }
}