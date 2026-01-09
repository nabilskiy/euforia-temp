package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.ui.theme.NavBarBackground
import androidx.compose.ui.platform.LocalDensity
import com.anhaki.picktime.PickHourMinute
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.White
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun TimePickerView(
    expanded: Boolean,
    initialTime: Pair<Int, Int>,
    fromHour: Int = 0,
    toHour: Int = 23,
    onTimeChanged: (Pair<Int, Int>) -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {
    DropdownMenu(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        containerColor = DarkGray,
        shape = RoundedCornerShape(20.dp),
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) }
    ) {
        PickHourMinute(
            initialHour = initialTime.first,
            initialMinute = initialTime.second,
            onHourChange = {
                onTimeChanged(it to initialTime.second)
            },
            onMinuteChange = {
                onTimeChanged(initialTime.first to it)
            },
            focusIndicator = PickTimeFocusIndicator(
                enabled = true,
                widthFull = false,
                background = White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
//                border = BorderStroke(2.dp, Color(0xFF87CDE6)),
            ),
            containerColor = DarkGray,
            selectedTextStyle = PickTimeTextStyle(
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            ),
        )
//        // Sizes
//        val itemHeight = 40.dp
//        val visibleItemsCount = 5 // odd number, center is selected
//        val wheelHeight = itemHeight * visibleItemsCount
//
//        val (initHour, initMinute) = remember(initialTime) { parseInitialTime(initialTime) }
//
//        // Respect provided hour bounds
//        val hourRange = remember(fromHour, toHour) {
//            val start = fromHour.coerceIn(0, 23)
//            val end = toHour.coerceIn(0, 23)
//            if (start <= end) start..end else end..start
//        }
//
//        var selectedHour by remember { mutableIntStateOf(initHour.coerceIn(hourRange.first, hourRange.last)) }
//        var selectedMinute by remember { mutableIntStateOf(initMinute) }
//
//        // Emit combined selection upstream as total minutes since midnight (distinct until changed)
//        var lastEmitted by remember { mutableStateOf<Long?>(null) }
//        LaunchedEffect(selectedHour, selectedMinute) {
//            val totalMinutes = (selectedHour * 60 + selectedMinute).toLong()
//            if (lastEmitted != totalMinutes) {
//                lastEmitted = totalMinutes
//                onTimeChanged(totalMinutes)
//            }
//        }
//
//        Box(
//            modifier = Modifier
//                .padding(8.dp)
//        ) {
//            Row(
//                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                WheelColumn(
//                    range = hourRange,
//                    selected = selectedHour,
//                    onSelected = {
//                        selectedHour = it
//
//                        Timber.d("Selected hour: $it")
//                    },
//                    itemHeight = itemHeight,
//                    visibleItemsCount = visibleItemsCount,
//                    modifier = Modifier.width(90.dp).height(wheelHeight),
//                    labelFormatter = { it.toString().padStart(2, '0') }
//                )
//
//                Text(
//                    text = ":",
//                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
//                    modifier = Modifier.padding(bottom = 2.dp)
//                )
//
//                WheelColumn(
//                    range = 0..59,
//                    selected = selectedMinute,
//                    onSelected = { selectedMinute = it },
//                    itemHeight = itemHeight,
//                    visibleItemsCount = visibleItemsCount,
//                    modifier = Modifier.width(90.dp).height(wheelHeight),
//                    labelFormatter = { it.toString().padStart(2, '0') }
//                )
//            }
//
//            // Center selection overlay
//            SelectionOverlay(
//                itemHeight = itemHeight,
//                wheelHeight = wheelHeight
//            )
//        }
    }
}

@Composable
private fun SelectionOverlay(
    itemHeight: Dp,
    wheelHeight: Dp,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
) {
    Box(
        modifier = Modifier
            .height(wheelHeight)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(itemHeight)
                .background(color = color, shape = RoundedCornerShape(10.dp))
        )
        // Top and bottom subtle lines
        Column(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Transparent)
            )
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .background(borderColor)
                    .padding(horizontal = 0.dp)
            )
            Spacer(modifier = Modifier.height(itemHeight - 2.dp))
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .background(borderColor)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Transparent)
            )
        }
    }
}

@Composable
private fun WheelColumn(
    range: IntRange,
    selected: Int,
    onSelected: (Int) -> Unit,
    itemHeight: Dp,
    visibleItemsCount: Int,
    modifier: Modifier = Modifier,
    labelFormatter: (Int) -> String = { it.toString() }
) {
    val items = remember(range) { range.toList() }
    // Number of padding items to keep the real item centered
    val paddingItems = visibleItemsCount / 2
    // Create the lazy list state with an initial index that centers the currently selected item
    val state = rememberLazyListState(initialFirstVisibleItemIndex = (selected + paddingItems))
    val scope = rememberCoroutineScope()
    // Always read the latest selected value inside collectors to avoid stale captures
    val selectedRef = rememberUpdatedState(selected)
    val localDensity = LocalDensity.current
    val itemHeightPx = with(localDensity) { itemHeight.toPx() }

    // Keep the list centered on 'selected' when it changes from the outside
    LaunchedEffect(selected) {
        val target = selected + paddingItems
        if (target != state.firstVisibleItemIndex || state.firstVisibleItemScrollOffset != 0) {
            state.animateScrollToItem(target)
        }
    }

    // Snap to closest item once when scrolling transitions from true -> false
    LaunchedEffect(state) {
        var wasScrolling = false
        var snappingInProgress = false
        snapshotFlow { state.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling && wasScrolling && !snappingInProgress) {
                    val firstIndex = state.firstVisibleItemIndex
                    val offsetPx = state.firstVisibleItemScrollOffset
                    // How many full items are scrolled past the first visible one
                    val offsetItems = (offsetPx / itemHeightPx).roundToInt()
                    val centeredCandidate = firstIndex - paddingItems + offsetItems
                    val bounded = centeredCandidate.coerceIn(0, items.lastIndex)
                    val target = bounded + paddingItems
                    // Only animate if we're not already perfectly centered at the target
                    val needsAnimation =
                        target != state.firstVisibleItemIndex || state.firstVisibleItemScrollOffset != 0
                    if (needsAnimation) {
                        snappingInProgress = true
                        scope.launch {
                            try {
                                state.animateScrollToItem(target)
                            } finally {
                                snappingInProgress = false
                            }
                        }
                    }
                    // Emit selection only if it actually changed compared to latest selected
                    val newVal = items[bounded]
                    if (newVal != selectedRef.value) onSelected(newVal)
                }
                wasScrolling = scrolling
            }
    }

    // Observe scroll to compute rotation
    var currentCenterFloat by remember { mutableStateOf(0f) }
    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .collect { (idx, offset) ->
                val topIndex = idx
                val fractional = offset / itemHeightPx
                val center = topIndex - paddingItems + fractional
                currentCenterFloat = center
            }
    }

    LazyColumn(
        modifier = modifier,
        state = state,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top padding
        items(paddingItems) { Spacer(modifier = Modifier.height(itemHeight)) }

        itemsIndexed(items) { index, value ->
            val distance = abs(index.toFloat() - currentCenterFloat)
            val maxRotation = 35f
            val rotation = (distance.coerceAtMost(1.5f) / 1.5f) * maxRotation
            val minAlpha = 0.35f
            val alpha = (1f - (distance / 3f)).coerceIn(minAlpha, 1f)
            val isCenter = index == currentCenterFloat.roundToInt()
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .graphicsLayer {
                        // 'density' here refers to GraphicsLayerScope's Float density, not LocalDensity
                        cameraDistance = 12f * density
                        rotationX =
                            if (index.toFloat() < currentCenterFloat) rotation else -rotation
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelFormatter(value),
                    style = MaterialTheme.typography.titleLarge.copy(
                        // Visual selection is the center item, not the previously selected value
                        fontWeight = if (isCenter) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    // Color/alpha also depend on distance from the center item
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alpha(alpha)
                )
            }
        }

        // Bottom padding
        items(paddingItems) { Spacer(modifier = Modifier.height(itemHeight)) }
    }
}

private fun parseInitialTime(initialTime: Long): Pair<Int, Int> {
    return if (initialTime in 0..(24 * 60)) {
        val h = (initialTime / 60).toInt()
        val m = (initialTime % 60).toInt()
        h.coerceIn(0, 23) to m.coerceIn(0, 59)
    } else {
        val cal = Calendar.getInstance()
        cal.timeInMillis = initialTime
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        h.coerceIn(0, 23) to m.coerceIn(0, 59)
    }
}