@file:OptIn(ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.plan.item

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.db.entity.Accompaniment
import digital.euforia.app.data.db.entity.getCompletedPhrases
import digital.euforia.app.data.db.entity.getCurrentPhrase
import digital.euforia.app.domain.model.TimeOfDay
import digital.euforia.app.domain.model.config.TimeOfDayConfig
import digital.euforia.app.ui.plan.DayTimeItemUi
import digital.euforia.app.ui.plan.DayUi
import digital.euforia.app.ui.plan.TodayOffset
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.MaxGradient
import digital.euforia.app.ui.theme.MaxGradientReversed
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.daytimeColors
import digital.euforia.app.ui.theme.eveningColors
import digital.euforia.app.ui.theme.morningColors
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.canvas.timeOfDayBackgroundAnimation
import digital.euforia.app.ui.util.formatDateFromMillis
import digital.euforia.app.ui.util.shadow
import digital.euforia.app.ui.util.widget.AccompanimentBackground
import digital.euforia.app.ui.util.widget.ActionButton
import digital.euforia.app.ui.util.widget.AccompanimentButtonColors
import digital.euforia.app.ui.util.widget.AccompanimentButtonDimensions
import digital.euforia.app.ui.util.widget.MaxTextView
import digital.euforia.app.ui.util.widget.ProgressIndicator
import digital.euforia.app.ui.util.widget.noRippleClickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun SharedTransitionScope.dayItem(
    modifier: Modifier,
    isLoading: Boolean,
    days: List<DayUi>,
    completedDays: Int,
    freeDemoDays: Int,
    isPremium: Boolean,
    isDemo: Boolean,
    selectedDayIndex: Int,
    timeOfDay: TimeOfDay,
    timeOfDayConfig: TimeOfDayConfig,
    todayOffset: TodayOffset,
    animatedVisibilityScope: AnimatedVisibilityScope,
    analyticSender: AnalyticSender,
    onPremiumClick: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onDayTimeItemClick: (DayTimeItemUi) -> Unit
) {
    val day = days.getOrNull(selectedDayIndex)

    Box(modifier = Modifier.timeOfDayBackgroundAnimation(timeOfDay)) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { state ->
            if (!state) {
                Column() {
                    day?.let {
                        Column {
                            HeaderView(
                                day = day,
                                dayIndex = selectedDayIndex,
                                completedDays = completedDays,
                                isDemo = isDemo,
                                isPremium = isPremium,
                                todayOffset = todayOffset,
                                isNextEnabled = days.lastIndex > selectedDayIndex,
                                isPrevEnabled = selectedDayIndex > 0,
                                onPrevClick = {
                                    analyticSender.todayPrevDayClick()
                                    onDaySelected(selectedDayIndex - 1)
                                },
                                onNextClick = {
                                    analyticSender.todayNextDayClick()
                                    onDaySelected(selectedDayIndex + 1)
                                }
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
                            if (!pageDay.isSubscriptionDay) {
                                AccompanimentPagerPage(
                                    day = pageDay,
                                    timeOfDayConfig = timeOfDayConfig,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    onDayTimeItemClick = onDayTimeItemClick
                                )
                            } else {
                                PremiumDayPage(onClick = onPremiumClick)
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .heightIn(min = 260.dp + 106.dp)
                ) {
                    ProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumDayPage(onClick: () -> Unit) {
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp)),
    ) {

        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = R.drawable.img_8day_bg,
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(),
                text = localizedRes.string(R.string.vibes_skip_demo_period).uppercase(),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
                color = White,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = localizedRes.string(R.string.vibes_skip_demo_period_message),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = Normal),
                color = White,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = { onClick() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Black
                ),
            ) {
                Text(
                    text = localizedRes.string(R.string.vibes_skip_demo_period_button),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        brush = MaxGradientReversed
                    ),
                )
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.AccompanimentPagerPage(
    day: DayUi,
    timeOfDayConfig: TimeOfDayConfig,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onDayTimeItemClick: (DayTimeItemUi) -> Unit
) {
    if (day.items.isEmpty()) return

    val (first, second, third) = day.items
    val shared1 =
        rememberSharedContentState(key = "${first.item.accompanimentId}+${first.item.timeOfDay.name}")
    val shared2 =
        rememberSharedContentState(key = "${second.item.accompanimentId}+${second.item.timeOfDay.name}")
    val shared3 =
        rememberSharedContentState(key = "${third.item.accompanimentId}+${third.item.timeOfDay.name}")

    if (day.items.size == 3) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(260.dp),
            horizontalArrangement = spacedBy(8.dp)
        ) {
            AccompanimentButton(
                modifier = Modifier.fillMaxHeight().weight(1f)
                    .sharedElement(shared1, animatedVisibilityScope),
                accompaniment = day.accompaniment ?: return,
                item = first,
                timeOfDayConfig = timeOfDayConfig,
                buttonDimensions = AccompanimentButtonDimensions.PRIMARY,
                onClick = onDayTimeItemClick
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = spacedBy(8.dp)
            ) {
                AccompanimentButton(
                    modifier = Modifier.fillMaxHeight().weight(1f)
                        .sharedElement(shared2, animatedVisibilityScope),
                    accompaniment = day.accompaniment,
                    item = second,
                    timeOfDayConfig = timeOfDayConfig,
                    onClick = onDayTimeItemClick
                )
                AccompanimentButton(
                    modifier = Modifier.fillMaxHeight().weight(1f)
                        .sharedElement(shared3, animatedVisibilityScope),
                    accompaniment = day.accompaniment,
                    item = third,
                    timeOfDayConfig = timeOfDayConfig,
                    onClick = onDayTimeItemClick
                )
            }
        }
    }
}

@Composable
fun AccompanimentButton(
    modifier: Modifier,
    accompaniment: Accompaniment,
    item: DayTimeItemUi,
    timeOfDayConfig: TimeOfDayConfig,
    isLocked: Boolean = false,
    isToday: Boolean = false,
    buttonDimensions: AccompanimentButtonDimensions = AccompanimentButtonDimensions.SECONDARY,
    onClick: (DayTimeItemUi) -> Unit
) {
    Box(
        modifier = modifier.noRippleClickable(onClick = {
            onClick(item)
        })
    ) {
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
            accompaniment = accompaniment,
            item = item,
            dayTimeItem = item,
            buttonDimensions = buttonDimensions,
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
    accompaniment: Accompaniment,
    item: DayTimeItemUi,
    dayTimeItem: DayTimeItemUi,
    buttonDimensions: AccompanimentButtonDimensions,
) {
    val localizedRes = LocalLocalizedRes.current
    val title = accompaniment.getTitleByTimeOfDay(item)

    Column(
        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
        verticalArrangement = spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = CenterVertically
        ) {
            val completedCount =
                if (dayTimeItem.item.timeOfDay != TimeOfDay.DAYTIME/* || dayTimeItem.state*/) {
                    ""
                } else {
                    val completed = accompaniment.getCompletedPhrases(item.item).size + 1
                    val total = accompaniment.phrases.size
                    if (completed > total) "" else " $completed/$total"
                }

            if (dayTimeItem.state == DayTimeItemUi.State.COMPLETED) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_checkbox_full),
                    contentDescription = null,
                    tint = White.copy(alpha = 0.2f)
                )
            }
            Text(
                text = "${
                    localizedRes.string(dayTimeItem.item.timeOfDay.getTitleRes()).uppercase()
                }$completedCount",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = White.copy(alpha = 0.2f)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = buttonDimensions.titleFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = buttonDimensions.titleLineHeight
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
    todayOffset: TodayOffset,
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
                modifier = Modifier
                    .weight(1f, fill = false)
                    .basicMarquee(iterations = Int.MAX_VALUE),
                dayIndex = dayIndex,
                isDemo = isDemo,
                isPremium = isPremium,
                isToday = day.isToday,
                daysOffset = dayIndex - completedDays,
                todayOffset = todayOffset
//                daysOffset = dayIndex - completedDays
            )
            MaxBadge(
                isMax = day.isLockedByPremium(),
                modifier = Modifier.wrapContentWidth(unbounded = true)
            )
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
            day = day,
            isDemo = isDemo,
            isPremium = isPremium
        )
    }
}

@Composable
private fun TitleText(
    modifier: Modifier = Modifier,
    dayIndex: Int,
    isDemo: Boolean,
    isPremium: Boolean,
    isToday: Boolean,
    daysOffset: Int,
    todayOffset: TodayOffset
) {
    val localizedRes = LocalLocalizedRes.current

    val todayText =
        if (isDemo) {
            localizedRes.string(R.string.vibes_title_demo_date, dayIndex + 1)
        } else if (isToday) {
            localizedRes.string(R.string.today_title)
        } else {
            if (dayIndex < todayOffset.daysBefore) {
                val offset = todayOffset.daysBefore - dayIndex
                formatDateFromMillis(System.currentTimeMillis() - (offset * 86400000L))
            } else {
                val offset = dayIndex - todayOffset.daysBefore
                formatDateFromMillis(System.currentTimeMillis() + (offset * 86400000L))

            }
//            todayOffset.daysBefore
//            formatDateFromMillis(System.currentTimeMillis() + (daysOffset * 86400000L))
        }

    Text(
        modifier = modifier,
        text = todayText,
        style = MaterialTheme.typography.displaySmall,
        color = White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun LockText(
    index: Int, day: DayUi, isDemo: Boolean,
    isPremium: Boolean
) {
    val localizedRes = LocalLocalizedRes.current
    val text = if (day.isLockedByPrevDay()) {
        if (isDemo) {
            localizedRes.string(R.string.vibes_unavailable_text, index)
        } else {
//            if (isPremium) {
            null
//            }
//            stringResource(R.string.vibes_unavailable_text_2)
        }
    } else if (day.isLockedByPremium()) {
        localizedRes.string(R.string.vibes_unavailable_get_max_text)
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
fun MaxBadge(isMax: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        modifier = modifier,
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

@Composable
private fun Accompaniment.getTitleByTimeOfDay(
    item: DayTimeItemUi
): String {
    val localizedRes = LocalLocalizedRes.current
    return when (item.item.timeOfDay) {
        TimeOfDay.MORNING -> morningTitle
        TimeOfDay.DAYTIME -> {
            getCurrentPhrase(item.item)?.daytimeTitle
                ?: localizedRes.string(R.string.vibes_daytime_completed)
//
////            val phrasesList = phrases
//            val phrasesIds = phrases.map { it.id }
//            if (phrases.isEmpty()) return daytimeTitle
//
//            val viewedId = item.item.viewedPhraseId
//            if (viewedId == null) {
//                return phrases.first().daytimeTitle
//            }
//
//            val phrase = phrases.firstOrNull { it != phrasesIds }
////                phrases.indexOfFirst { it.id == viewedId }
//            return phrase?.// Return the next phrase's daytime title
//            daytimeTitle ?: // If the viewed ID is not found, default to the first phrase title
//            phrases.first().daytimeTitle
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