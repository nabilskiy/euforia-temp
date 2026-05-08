/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.ui.soundscapes.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import digital.euforia.app.R
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val MINUTE_STEP = 5
private const val MINUTE_SLOT_COUNT = 12 // 0,5,…,55
private const val DEFAULT_TIMER_MINUTES = 60

private fun totalMinutesFromSeconds(seconds: Int?): Int =
    (((seconds ?: 0).coerceAtLeast(0)) / 60).let { minutes ->
        if (minutes > 0) minutes else DEFAULT_TIMER_MINUTES
    }

private fun minuteValueToSlot(minutes0to59: Int): Int =
    (minutes0to59.coerceIn(0, 59) / MINUTE_STEP).coerceIn(0, MINUTE_SLOT_COUNT - 1)

private fun minuteSlotToValue(slot: Int): Int =
    (slot.coerceIn(0, MINUTE_SLOT_COUNT - 1) * MINUTE_STEP).coerceIn(0, 55)

/**
 * Sleep timer dialog aligned with iOS: rounded dark surface, dual wheel columns,
 * shared selection band, Cancel + pill "Set". Minutes in 5-minute steps.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SleepTimerPickerDialog(
    initialSeconds: Int?,
    onDismiss: () -> Unit,
    onSet: (Int) -> Unit,
    onDisable: () -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val density = LocalDensity.current
    val initialTotalMinutes = totalMinutesFromSeconds(initialSeconds)
    var hours by remember(initialSeconds) {
        mutableStateOf((initialTotalMinutes / 60).coerceIn(0, 23))
    }
    var minuteSlot by remember(initialSeconds) {
        mutableStateOf(minuteValueToSlot(initialTotalMinutes % 60))
    }
    val minuteValue = minuteSlotToValue(minuteSlot)
    val canSet = (hours * 60 + minuteValue) > 0
    val hasActiveTimer = (initialSeconds ?: 0) > 0

    val itemHeight = 46.dp
    val pickerHeight = 216.dp
    val verticalPad = (pickerHeight - itemHeight) / 2

    val hoursListState = rememberLazyListState()
    val minutesListState = rememberLazyListState()
    var scrollSyncReady by remember { mutableStateOf(false) }

    LaunchedEffect(initialSeconds) {
        scrollSyncReady = false
        delay(32)
        val itemPx = with(density) { itemHeight.roundToPx() }
        var vp = 0
        var attempts = 0
        while (vp <= 0 && attempts < 8) {
            delay(16)
            vp = hoursListState.layoutInfo.viewportEndOffset - hoursListState.layoutInfo.viewportStartOffset
            attempts++
        }
        if (vp > 0) {
            val centerScrollOffset = -((vp - itemPx) / 2)
            hoursListState.scrollToItem(hours.coerceIn(0, 23), centerScrollOffset)
            minutesListState.scrollToItem(minuteSlot.coerceIn(0, MINUTE_SLOT_COUNT - 1), centerScrollOffset)
        } else {
            hoursListState.scrollToItem(hours.coerceIn(0, 23))
            minutesListState.scrollToItem(minuteSlot.coerceIn(0, MINUTE_SLOT_COUNT - 1))
        }
        delay(100)
        scrollSyncReady = true
    }

    LaunchedEffect(hoursListState, scrollSyncReady) {
        if (!scrollSyncReady) return@LaunchedEffect
        snapshotFlow { hoursListState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                delay(48)
                val idx = hoursListState.centeredItemIndex() ?: return@collect
                val clamped = idx.coerceIn(0, 23)
                if (clamped != hours) {
                    hours = clamped
                }
            }
    }

    LaunchedEffect(minutesListState, scrollSyncReady) {
        if (!scrollSyncReady) return@LaunchedEffect
        snapshotFlow { minutesListState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                delay(48)
                val idx = minutesListState.centeredItemIndex() ?: return@collect
                val clamped = idx.coerceIn(0, MINUTE_SLOT_COUNT - 1)
                if (clamped != minuteSlot) {
                    minuteSlot = clamped
                }
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(BottomSheetBackground)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = localizedRes.string(R.string.sleep_timer_title),
                        color = White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = localizedRes.string(R.string.close),
                            tint = White,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
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
                            .background(White.copy(alpha = 0.12f)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TimerWheelColumn(
                            count = 24,
                            itemHeight = itemHeight,
                            verticalPadding = verticalPad,
                            listState = hoursListState,
                            formatValue = { it.toString() },
                            modifier = Modifier
                                .widthIn(min = 44.dp, max = 56.dp)
                                .fillMaxHeight(),
                        )
                        Text(
                            text = localizedRes.string(R.string.sleep_timer_hour_label),
                            color = White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, end = 14.dp),
                        )
                        TimerWheelColumn(
                            count = MINUTE_SLOT_COUNT,
                            itemHeight = itemHeight,
                            verticalPadding = verticalPad,
                            listState = minutesListState,
                            formatValue = { slot -> minuteSlotToValue(slot).toString() },
                            modifier = Modifier
                                .widthIn(min = 44.dp, max = 56.dp)
                                .fillMaxHeight(),
                        )
                        Text(
                            text = localizedRes.string(R.string.sleep_timer_minute_label),
                            color = White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                if (hasActiveTimer) {
                    TextButton(
                        onClick = onDisable,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) {
                        Text(
                            text = localizedRes.string(R.string.audio_scene_menu_timer_stop),
                            color = White.copy(alpha = 0.65f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    Spacer(Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = localizedRes.string(R.string.cancel),
                            color = White,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                    Button(
                        onClick = {
                            val h = hoursListState.centeredItemIndex()?.coerceIn(0, 23) ?: hours
                            val slot = minutesListState.centeredItemIndex()
                                ?.coerceIn(0, MINUTE_SLOT_COUNT - 1) ?: minuteSlot
                            val mins = minuteSlotToValue(slot)
                            onSet((h * 60 + mins) * 60)
                        },
                        enabled = canSet,
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = White,
                            contentColor = Black,
                            disabledContainerColor = White.copy(alpha = 0.22f),
                            disabledContentColor = White.copy(alpha = 0.45f),
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                        modifier = Modifier.heightIn(min = 44.dp),
                    ) {
                        Text(
                            text = localizedRes.string(R.string.sleep_timer_setup_button),
                            color = Black,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimerWheelColumn(
    count: Int,
    itemHeight: Dp,
    verticalPadding: Dp,
    listState: LazyListState,
    formatValue: (Int) -> String,
    modifier: Modifier = Modifier,
) {
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val highlightIndex by remember(listState, count) {
        derivedStateOf {
            listState.centeredItemIndex()?.coerceIn(0, count - 1) ?: -1
        }
    }
    LazyColumn(
        modifier = modifier,
        state = listState,
        flingBehavior = snapFlingBehavior,
        contentPadding = PaddingValues(vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        userScrollEnabled = true,
    ) {
        items(count, key = { it }) { index ->
            val isHighlight = index == highlightIndex && highlightIndex >= 0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatValue(index),
                    color = if (isHighlight) White else White.copy(alpha = 0.28f),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = if (isHighlight) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun LazyListState.centeredItemIndex(): Int? {
    val info = layoutInfo
    val items = info.visibleItemsInfo
    if (items.isEmpty()) return null
    val viewportStart = info.viewportStartOffset.toFloat()
    val viewportEnd = info.viewportEndOffset.toFloat()
    val center = (viewportStart + viewportEnd) / 2f
    return items.minByOrNull { item ->
        val itemCenter = item.offset + item.size / 2f
        abs(itemCenter - center)
    }?.index
}
