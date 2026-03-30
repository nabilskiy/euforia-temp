package digital.euforia.app.ui.settings.personaldata.cleardata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.Red
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ClearDataScreen(
    navController: NavHostController,
    viewModel: ClearDataViewModel,
    navBarVisibilityState: MutableState<Boolean>,
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, context)
    }

    NavBarlessScreen(navBarVisibilityState) {
        ClearDataContent(
            navController = navController,
            onDeleteClick = viewModel::onDeleteClick,
            onBackClick = {
                navController.popBackStack()
            },
        )
    }
}

@Composable
private fun ClearDataContent(
    navController: NavHostController,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    var dialogShown by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
    val density = LocalDensity.current
    val context = LocalContext.current
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

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            titleRes = R.string.personal_data_title,
            backTitleRes = R.string.back,
            shouldBlur = shouldBlur, hazeState = hazeState, onBackClick = onBackClick,
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
                titleRes = R.string.personal_data_title
            )
            deleteItem(onClick = {
                dialogShown = true
            })
        }

        if (dialogShown) {
            ClearDataDialog(
                onCancel = { dialogShown = false },
                onDelete = onDeleteClick
            )
        }
    }
}

private fun LazyListScope.deleteItem(
    onClick: () -> Unit,
) = item {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = localizedRes.string(R.string.personal_data_clear_details),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Light,
                color = White.copy(alpha = 0.6f)
            )
        )

        FilledTonalButton(
            modifier = Modifier.fillMaxSize().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = White.copy(alpha = 0.1f),
                contentColor = Red
            ),
            onClick = { onClick() },
        ) {
            Text(
                text = localizedRes.string(R.string.personal_data_delete),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    lineHeight = 18.sp,
                    fontWeight = Medium,
                    color = Red
                )
            )
        }
    }
    // Implementation of optionsItem goes here
}

private fun handleSideEffect(sideEffect: ClearDataSideEffect, context: Context) {
    when (sideEffect) {
        is ClearDataSideEffect.RestartApp -> {
            restartApp(context)
        }
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
    android.os.Process.killProcess(android.os.Process.myPid())
}