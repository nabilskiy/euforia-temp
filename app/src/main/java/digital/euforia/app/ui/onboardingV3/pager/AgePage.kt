package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgePage(
    selectedAge: Int?,
    isPageActive: Boolean,
    onAgeSelected: (Int) -> Unit,
) {
    if (!isPageActive) return

    val locale = Locale.getDefault()
    val calendar = remember { Calendar.getInstance(locale) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val maxYear = remember { Calendar.getInstance().get(Calendar.YEAR) - 6 }
    val years = remember(maxYear) { (1900..maxYear).toList() }
    val months = remember(locale) {
        DateFormatSymbols(locale).months.take(12).map { month ->
            month.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        }
    }

    var selectedDay by remember { mutableIntStateOf(1) }
    var selectedMonth by remember { mutableIntStateOf(0) }
    var selectedYear by remember {
        mutableIntStateOf((currentYear - (selectedAge ?: 38)).coerceIn(1900, maxYear))
    }
    val daysInMonth = remember(selectedMonth, selectedYear) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (selectedDay > daysInMonth) selectedDay = daysInMonth

    val dayItems = remember(daysInMonth) { (1..daysInMonth).toList() }
    val dateLabel = remember(selectedDay, selectedMonth, selectedYear, locale) {
        calendar.set(selectedYear, selectedMonth, selectedDay)
        SimpleDateFormat("d MMMM yyyy", locale).format(calendar.time)
    }

    val itemHeight = 46.dp
    val pickerHeight = 216.dp
    val verticalPad = (pickerHeight - itemHeight) / 2
    val dayState = rememberCircularWheelState(selectedDay - 1, dayItems.size)
    val monthState = rememberCircularWheelState(selectedMonth, months.size)
    val yearState = rememberCircularWheelState((selectedYear - years.first()).coerceIn(0, years.lastIndex), years.size)
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(selectedMonth, selectedYear, daysInMonth) {
        delay(80)
        val itemPx = with(density) { itemHeight.roundToPx() }
        val dayOffset = dayState.centerOffsetFor(itemPx)
        val targetDayIndex = (selectedDay - 1).coerceIn(0, daysInMonth - 1)
        dayState.scrollToItem(
            index = dayState.nearestCircularIndex(targetDayIndex, daysInMonth),
            scrollOffset = dayOffset,
        )
    }

    PickerSelectionEffect(dayState, dayItems.size) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        selectedDay = dayItems[it]
    }
    PickerSelectionEffect(monthState, months.size) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        selectedMonth = it
    }
    PickerSelectionEffect(yearState, years.size) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        selectedYear = years[it]
    }
    LaunchedEffect(selectedYear) {
        onAgeSelected((currentYear - selectedYear).coerceAtLeast(6))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 145.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dateLabel,
                color = White,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Bold,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 84.dp)
                    .width(210.dp),
                thickness = 0.5.dp,
                color = White.copy(alpha = 0.18f),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(278.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(BottomSheetBackground),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(pickerHeight),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(48.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                        .background(White.copy(alpha = 0.10f)),
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    DateWheelColumn(
                        items = dayItems.map { it.toString() },
                        state = dayState,
                        itemHeight = itemHeight,
                        verticalPadding = verticalPad,
                        width = 54.dp,
                    )
                    DateWheelColumn(
                        items = months,
                        state = monthState,
                        itemHeight = itemHeight,
                        verticalPadding = verticalPad,
                        width = 150.dp,
                    )
                    DateWheelColumn(
                        items = years.map { it.toString() },
                        state = yearState,
                        itemHeight = itemHeight,
                        verticalPadding = verticalPad,
                        width = 76.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerSelectionEffect(
    listState: LazyListState,
    itemCount: Int,
    onSelected: (Int) -> Unit,
) {
    LaunchedEffect(listState, itemCount) {
        var isInitialEmission = true
        snapshotFlow { listState.centeredItemIndex()?.floorMod(itemCount) }
            .distinctUntilChanged()
            .collect { idx ->
                idx ?: return@collect
                if (!isInitialEmission && listState.isScrollInProgress) {
                    onSelected(idx)
                    return@collect
                }
                if (isInitialEmission) {
                    isInitialEmission = false
                    return@collect
                }
                delay(32)
                onSelected(idx)
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DateWheelColumn(
    items: List<String>,
    state: LazyListState,
    itemHeight: Dp,
    verticalPadding: Dp,
    width: Dp,
) {
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val selectedIndex by remember(state, items.size) {
        derivedStateOf { state.centeredItemIndex()?.floorMod(items.size) ?: -1 }
    }
    val viewportCenter by remember(state) {
        derivedStateOf {
            val layoutInfo = state.layoutInfo
            (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) * 0.5f
        }
    }

    LazyColumn(
        modifier = Modifier
            .width(width)
            .fillMaxHeight(),
        state = state,
        flingBehavior = snapFlingBehavior,
        contentPadding = PaddingValues(vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(Int.MAX_VALUE, key = { index -> index }) { index ->
            val itemIndex = index.floorMod(items.size)
            val item = items[itemIndex]
            val selected = itemIndex == selectedIndex
            val itemInfo = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
            val distanceFromCenter = if (itemInfo != null && itemHeightPx > 0f) {
                ((itemInfo.offset + itemInfo.size * 0.5f) - viewportCenter) / itemHeightPx
            } else {
                0f
            }
            val clampedDistance = distanceFromCenter.coerceIn(-3f, 3f)
            val absoluteDistance = abs(clampedDistance)
            val rowAlpha = if (selected) 1f else (0.68f - absoluteDistance * 0.12f).coerceAtLeast(0.32f)
            val rowScale = 1f - min(absoluteDistance * 0.055f, 0.16f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .padding(horizontal = 2.dp)
                    .graphicsLayer {
                        cameraDistance = 14f * density.density
                        rotationX = -clampedDistance * 24f
                        scaleX = rowScale
                        scaleY = rowScale
                        alpha = rowAlpha
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item,
                    color = if (selected) White.copy(alpha = 0.98f) else White.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                )
            }
        }
    }
}

private fun LazyListState.centeredItemIndex(): Int? {
    val layoutInfo = layoutInfo
    if (layoutInfo.visibleItemsInfo.isEmpty()) return null
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
    return layoutInfo.visibleItemsInfo.minByOrNull { item ->
        abs((item.offset + item.size / 2) - viewportCenter)
    }?.index
}

@Composable
private fun rememberCircularWheelState(selectedIndex: Int, itemCount: Int): LazyListState {
    val initialIndex = remember(selectedIndex, itemCount) {
        val center = Int.MAX_VALUE / 2
        center - center.floorMod(itemCount) + selectedIndex
    }
    return rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
}

private fun LazyListState.nearestCircularIndex(itemIndex: Int, itemCount: Int): Int {
    val current = firstVisibleItemIndex
    val base = current - current.floorMod(itemCount)
    return listOf(base - itemCount + itemIndex, base + itemIndex, base + itemCount + itemIndex)
        .minBy { abs(it - current) }
}

private fun LazyListState.centerOffsetFor(itemPx: Int): Int {
    val viewport = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    return if (viewport > 0) -((viewport - itemPx) / 2) else 0
}

private fun Int.floorMod(other: Int): Int {
    return ((this % other) + other) % other
}
