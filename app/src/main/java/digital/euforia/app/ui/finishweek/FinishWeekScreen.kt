package digital.euforia.app.ui.finishweek

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.Brown
import digital.euforia.app.ui.theme.CalendarColors
import digital.euforia.app.ui.theme.CalendarGradient
import digital.euforia.app.ui.theme.DarkBlue
import digital.euforia.app.ui.theme.DarkPink
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.DayBlue
import digital.euforia.app.ui.theme.MaxGradient
import digital.euforia.app.ui.theme.PremiumGradient
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.dashedCircleBorder
import digital.euforia.app.ui.util.px
import digital.euforia.app.ui.util.widget.PremiumButton
import digital.euforia.app.ui.util.widget.applyIf
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import java.time.temporal.ChronoUnit
import kotlin.math.min

@Composable
fun FinishWeekScreen(
    navController: NavHostController,
    navBarVisibilityState: MutableState<Boolean>,
    viewModel: FinishWeekViewModel = hiltViewModel(),
    hazeState: HazeState,
    onBackClick: () -> Unit,
    onPremiumClick: () -> Unit
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    NavBarlessScreen(navBarVisibilityState) {
        FinishWeekContent(
            hazeState = hazeState,
            onBackClick = onBackClick,
            onPremiumClick = onPremiumClick
        )
    }
}

@Composable
private fun FinishWeekContent(
    hazeState: HazeState,
    onBackClick: () -> Unit,
    onPremiumClick: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    Box(
        modifier = Modifier.hazeEffect(
            hazeState,
            style = HazeMaterials.regular(AppBarBackground)
        ).padding(horizontal = 16.dp)
    ) {
        LazyColumn() {
            progressItem()
            calendarItem()
            item {
                Spacer(modifier = Modifier.height(320.dp).fillMaxWidth())
            }
        }

        Icon(
            modifier = Modifier.noRippleClickable(onBackClick).statusBarsPadding()
                .padding(top = 16.dp),
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = null,
            tint = Color.White,
        )

        PremiumButton(
            modifier = Modifier.align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            text = localizedRes.string(R.string.vibes_skip_demo_period_button),
            gradient = CalendarGradient,
            onClick = {
                onBackClick()
                onPremiumClick()
            }
        )
    }
}

fun LazyListScope.progressItem() = item(key = "progress") {
    val localizedRes = LocalLocalizedRes.current

    Column(
        modifier = Modifier.statusBarsPadding().padding(top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(16.dp)
    ) {
        Text(
            text = localizedRes.string(R.string.vibes_upgrade_without_subscription),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val days = listOf(1, 2, 3, 4, 5, 6, 7)
            days.forEach { day ->
                ProgressDay(day = day, isLocked = day != 1)
            }
        }

        Text(
            text = localizedRes.string(R.string.vibes_upgrade_details),
            style = MaterialTheme.typography.bodyMedium,
            color = White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
fun RowScope.ProgressDay(day: Int = 1, isLocked: Boolean = false) {
    val localizedRes = LocalLocalizedRes.current
    val backgroundColor = if (isLocked) White.copy(alpha = 0.1f) else DayBlue

    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
//                .heightIn(min = 56.dp)
//                .aspectRatio(1f)
                .background(color = backgroundColor, shape = CircleShape)
                .applyIf(isLocked) {
                    dashedCircleBorder(color = White.copy(alpha = 0.5f), strokeWidth = 1.dp)
//                    border(width = 1.dp, color = White.copy(alpha = 0.5f), shape = CircleShape)
                }
                .padding(8.dp),
        ) {
            if (!isLocked) {
                Icon(
                    modifier = Modifier.align(Alignment.Center).size(16.dp),
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            } else {
                Spacer(
                    modifier = Modifier.align(Alignment.Center).size(16.dp)
                )
            }
        }
        Text(
            text = localizedRes.string(R.string.vibes_upgrade_day_index, day),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isLocked) White.copy(alpha = 0.5f) else White,
            fontSize = 10.sp
        )
    }
}

fun LazyListScope.calendarItem() = item(key = "calendar") {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.navigationBarsPadding()
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizedRes.string(R.string.vibes_upgrade_with_subscription),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = White
            )
            Text(
                text = "MAX",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    brush = Brush.horizontalGradient(CalendarColors)
                ),
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = White
            )
        }
        Column(
            modifier = Modifier.border(
                width = 2.dp,
                brush = Brush.verticalGradient(CalendarColors.reversed()),
                shape = RoundedCornerShape(12.dp)
            ).padding(4.dp)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedRes.string(R.string.vibes_upgrade_everyday),
                    style = MaterialTheme.typography.titleMedium.copy(
                        //                    fontWeight = FontWeight.SemiBold
                    ),
                    color = White
                )
                Text(
                    text = "∞",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        brush = Brush.horizontalGradient(CalendarColors)
                    ),
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(1.dp)
                    .background(color = Black.copy(alpha = 0.2f))
            )
            CalendarView()
        }
    }

}

@Composable
fun CalendarView(modifier: Modifier = Modifier, today: LocalDate = LocalDate.now()) {
    val firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val configuration = LocalConfiguration.current

    data class CalendarRow(
        val cells: List<LocalDate?>,
        val containsToday: Boolean
    )

    fun weekStart(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))

    fun weekEnd(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.nextOrSame(firstDayOfWeek.minus(1)))

    fun monthFirst(date: LocalDate): LocalDate = date.withDayOfMonth(1)
    fun monthLast(date: LocalDate): LocalDate = date.withDayOfMonth(date.lengthOfMonth())

    fun buildRow(dateWithinRow: LocalDate): CalendarRow {
        val monthStart = monthFirst(dateWithinRow)
        val monthEnd = monthLast(dateWithinRow)
        val rowStart = maxOf(weekStart(dateWithinRow), monthStart)
        val rowEnd = minOf(weekEnd(dateWithinRow), monthEnd)

        val visibleDays = generateSequence(rowStart) { prev ->
            val next = prev.plusDays(1)
            if (next.isAfter(rowEnd)) null else next
        }.toList()

        val leadingSlots = ChronoUnit.DAYS.between(weekStart(rowStart), rowStart).toInt()
        val trailingSlots = 7 - leadingSlots - visibleDays.size
        val cells: MutableList<LocalDate?> = mutableListOf()
        repeat(leadingSlots) { cells.add(null) }
        cells.addAll(visibleDays)
        repeat(trailingSlots.coerceAtLeast(0)) { cells.add(null) }

        return CalendarRow(cells = cells.take(7), containsToday = visibleDays.contains(today))
    }

    // Build 6 rows: row 2 contains today; row 1 is before it; others after
    val currentRow = buildRow(today)

    // previous row: take the day before the start of current row
    val prevRowDate =
        currentRow.cells.firstOrNull { it != null }?.minusDays(1) ?: today.minusDays(1)
    val row0 = buildRow(prevRowDate)

    // next rows chaining from the end of each row
    fun nextRowDate(fromRow: CalendarRow): LocalDate {
        val lastDay = fromRow.cells.lastOrNull { it != null } ?: today
        return lastDay.plusDays(1)
    }

    val row2 = buildRow(nextRowDate(currentRow))
    val row3 = buildRow(nextRowDate(row2))
    val row4 = buildRow(nextRowDate(row3))
    val row5 = buildRow(nextRowDate(row4))

    val rows = listOf(row0, currentRow, row2, row3, row4, row5)
//    val centerY = 12.px
//    val offsetY = with(configuration) { 18.dp }
    val colorStops = arrayOf(
        0f to Brown,
        0.4f to Brown,
//        0.4f to DarkPink,
        0.8f to DarkPink,
        1f to DarkBlue,
    )

    val todayCircleBrush = Brush.radialGradient(
        colorStops = colorStops,
//colorStops = 0.1f to DarkBlue,
//        colors = CalendarColors.reversed(),
        center = Offset(20.px.toFloat(), 30.px.toFloat()),
    )
    val followingRowBrush = Brush.horizontalGradient(colors = CalendarColors)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = spacedBy(8.dp)
    ) {
        rows.forEachIndexed { index, row ->
            val isFollowingRow = index > 1 // rows after current row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(vertical = 6.dp, horizontal = 8.dp)
                    .drawBehind {
                        if (isFollowingRow) {
                            val startIndex = row.cells.indexOfFirst { it != null }
                            val endIndex = row.cells.indexOfLast { it != null }
                            if (startIndex != -1 && endIndex != -1 && endIndex >= startIndex) {
                                val spacingPx = 6.dp.toPx()
                                val slots = 7
                                val totalSpacing = spacingPx * (slots - 1)
                                val slotWidth = (size.width - totalSpacing) / slots

                                val left = startIndex * (slotWidth + spacingPx)
                                val right = (endIndex + 1) * slotWidth + endIndex * spacingPx
                                val width = (right - left).coerceAtLeast(0f)

                                val radius = 8.dp.toPx()
                                val vInset =
                                    6.dp.toPx()                 // vertical padding inside the row
                                val top = vInset                         // start below the top
                                val height =
                                    (size.height - vInset * 2).coerceAtLeast(0f) // shorter strip
                                val corner = min(
                                    8.dp.toPx(),
                                    height / 2f
                                )                // keep rounding sane

                                drawRoundRect(
                                    brush = followingRowBrush,
                                    topLeft = Offset(left, top),
                                    size = Size(width, height),
                                    cornerRadius = CornerRadius(corner, corner)
                                )
                            }
                        } else if (row.containsToday) {
                            // For the current week: draw gradient only for the items AFTER today,
                            // limited to real (non-null) day cells.
                            val todayIdx = row.cells.indexOfFirst { it == today }
                            val endIndex = row.cells.indexOfLast { it != null }
                            if (todayIdx != -1) {
                                val startIndex = (todayIdx + 1)
                                if (endIndex != -1 && endIndex >= startIndex) {
                                    val spacingPx = 6.dp.toPx()
                                    val slots = 7
                                    val totalSpacing = spacingPx * (slots - 1)
                                    val slotWidth = (size.width - totalSpacing) / slots

                                    val left = startIndex * (slotWidth + spacingPx)
                                    val right = (endIndex + 1) * slotWidth + endIndex * spacingPx
                                    val width = (right - left).coerceAtLeast(0f)

                                    val radius = 8.dp.toPx()
                                    val vInset =
                                        8.dp.toPx()                 // vertical padding inside the row
                                    val top = vInset                         // start below the top
                                    val height =
                                        (size.height - vInset * 2).coerceAtLeast(0f) // shorter strip
                                    val corner = min(
                                        8.dp.toPx(),
                                        height / 2f
                                    )                // keep rounding sane

                                    drawRoundRect(
                                        brush = followingRowBrush,
                                        topLeft = Offset(left, top),
                                        size = Size(width, height),
                                        cornerRadius = CornerRadius(corner, corner)
                                    )
                                }
                            }
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.cells.forEach { cellDate ->
                    val isToday = cellDate == today
                    val textColor = when {
                        isToday -> Color.White
                        cellDate != null && cellDate.isBefore(today) -> DarkGray
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cellDate != null) {
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(todayCircleBrush)
                                ) {}
                            }
                            Text(
                                text = "%02d".format(cellDate.dayOfMonth),
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    lineHeight = 18.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleSideEffect(sideEffect: FinishWeekSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}