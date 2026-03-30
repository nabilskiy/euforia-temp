package digital.euforia.app.ui.settings.subscription.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.DialogButton
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.NotificationToast
import digital.euforia.app.ui.util.widget.titleItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DeviceInfoScreen(
    navController: NavHostController,
    viewModel: DeviceInfoViewModel,
    navBarVisibilityState: MutableState<Boolean>
) {
    val state by viewModel.collectAsState()
    var isToastVisible by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val localizedRes = LocalLocalizedRes.current

    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is DeviceInfoSideEffect.ShowCopyNotification -> {
                clipboardManager.setText(AnnotatedString(sideEffect.info))
                isToastVisible = true
            }

            is DeviceInfoSideEffect.SendEmail -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@euforia.digital"))
                    putExtra(Intent.EXTRA_SUBJECT, "Euforia Debug Info")
                    putExtra(Intent.EXTRA_TEXT, sideEffect.info)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    val shareIntent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@euforia.digital"))
                        putExtra(Intent.EXTRA_SUBJECT, "Euforia App Support - Debug Info")
                        putExtra(Intent.EXTRA_TEXT, sideEffect.info)
                    }, null)
                    context.startActivity(shareIntent)
                }
            }
        }
    }

    NavBarlessScreen(navBarVisibilityState) {
        Box(modifier = Modifier.fillMaxSize()) {
            DeviceInfoContent(
                navController = navController,
                info = state.info,
                deviceToken = state.deviceToken,
                subsToken = state.subsToken,
                isSubsTokenValid = state.isSubscriptionValid,
                version = state.version,
                languageTag = state.languageTag,
                dateOffset = state.dateOffset,
                isDemo = state.isDemo,
                firstLaunch = state.firstLaunch,
                timeOfDay = state.timeOfDay,
                systemInfo = state.systemInfo,
                onBackClick = {
                    navController.popBackStack()
                },
                onCopyClick = {
                    viewModel.copyToClipboard()
                },
                onSendClick = {
                    viewModel.sendSupportEmail()
                }
            )

            NotificationToast(
                text = localizedRes.string(R.string.copied),
                isVisible = isToastVisible,
                onDismissed = { isToastVisible = false }
            )
        }
    }
}

@Composable
private fun DeviceInfoContent(
    navController: NavHostController,
    info: String,
    deviceToken: String,
    subsToken: String,
    isSubsTokenValid: Boolean,
    version: String,
    languageTag: String,
    dateOffset: String,
    isDemo: Boolean,
    firstLaunch: String,
    timeOfDay: String,
    systemInfo: String,
    onBackClick: () -> Unit,
    onCopyClick: () -> Unit,
    onSendClick: () -> Unit
) {
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
            titleRes = R.string.debug_information,
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
                titleRes = R.string.debug_information
            )
            infoItem(
                deviceToken = deviceToken,
                subsToken = subsToken,
                isSubsTokenValid = isSubsTokenValid,
                version = version,
                languageTag = languageTag,
                dateOffset = dateOffset,
                isDemo = isDemo,
                firstLaunch = firstLaunch,
                timeOfDay = timeOfDay,
                systemInfo = systemInfo
            )
            buttonsItem(
                onSend = onSendClick,
                onCopy = onCopyClick
            )
        }
    }
}

private fun LazyListScope.infoItem(
    deviceToken: String,
    subsToken: String,
    isSubsTokenValid: Boolean,
    version: String,
    languageTag: String,
    dateOffset: String,
    isDemo: Boolean,
    firstLaunch: String,
    timeOfDay: String,
    systemInfo: String,
) = item(key = "info") {
    Column(
        modifier = Modifier.background(color = NavBarBackground, shape = RoundedCornerShape(16.dp)),
    ) {

        InfoText("Device ID", deviceToken)
        InfoText("Subscription ID", subsToken)
        InfoText("Subscription Valid", isSubsTokenValid.toString())
        InfoText("Demo", isDemo.toString())
        InfoText("Time Of Day", timeOfDay)
        InfoText("First Launch", firstLaunch)
        InfoText("Date Offset", dateOffset)
        InfoText("System", version)
        InfoText("Device", systemInfo)
        InfoText("Locale", languageTag, false)
    }
}

@Composable
private fun ColumnScope.InfoText(title: String, text: String, hasDivider: Boolean = true) {
    Text(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        text = title,
        color = White,
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        text = text,
        color = White,
        style = MaterialTheme.typography.bodyMedium
    )
    if (hasDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 16.dp).fillMaxWidth(),
            color = White.copy(alpha = 0.2f),
            thickness = 1.dp
        )
    }
}

private fun LazyListScope.buttonsItem(
    onSend: () -> Unit,
    onCopy: () -> Unit,
) = item(key = "buttons") {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp).background(
            color = NavBarBackground,
            shape = RoundedCornerShape(12.dp)
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        val localizedRes = LocalLocalizedRes.current

        OutlinedButton(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            onClick = onSend,
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = White.copy(alpha = 0.6f),
                containerColor = NavBarBackground
            ),
            contentPadding = PaddingValues(0.dp),
            border = null,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                text = localizedRes.string(R.string.debug_information_share),
                color = DialogButton,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 16.dp).fillMaxWidth(),
            color = White.copy(alpha = 0.2f),
            thickness = 1.dp
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            onClick = onCopy,
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = White.copy(alpha = 0.6f),
                containerColor = NavBarBackground
            ),
            contentPadding = PaddingValues(0.dp),
            border = null,
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                text = localizedRes.string(R.string.copy),
                color = DialogButton,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
