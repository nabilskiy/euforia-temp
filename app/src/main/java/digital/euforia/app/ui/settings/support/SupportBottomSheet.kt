package digital.euforia.app.ui.settings.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraLight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.BuildConfig
import digital.euforia.app.R
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.settings.feedback.UiFeedbackForm
import digital.euforia.app.ui.subscription.Configuration.SUPPORT_EMAIL
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.theme.subtitleSmall
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.sendSupportEmail
import digital.euforia.app.ui.util.widget.AnimatedSizeButton
import digital.euforia.app.ui.util.widget.CorporateTextField
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportBottomSheet(
    viewModel: SupportViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = 32.dp).statusBarsPadding(),
        dragHandle = {},
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BottomSheetBackground,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        SupportContent(
            email = state.email,
            message = state.message,
            onEmailChanged = viewModel::onEmailChanged,
            onMessageChanged = viewModel::onMessageChanged,
            onSendClicked = viewModel::sendFeedback,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun SupportContent(
    email: String? = null,
    message: String? = null,
    onEmailChanged: (String) -> Unit,
    onMessageChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizedRes = LocalLocalizedRes.current
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    // Blur only when the first item is fully hidden (i.e., second becomes first visible)
    val shouldBlur by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }
    Box {
        val appBarModifier = if (shouldBlur) {
            Modifier
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.regular(AppBarBackground)
                )
                .zIndex(1f)
        } else {
            Modifier.zIndex(1f)
        }
        Box(
            modifier = appBarModifier
                .fillMaxWidth()
                .height(AppBarHeightMedium)
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                modifier = Modifier
                    .noRippleClickable { onDismiss() }
                    .align(Alignment.CenterStart),
                painter = painterResource(R.drawable.ic_close),
                tint = Color.Unspecified,
                contentDescription = null
            )
            if (shouldBlur) {
                Text(
                    text = localizedRes.string(R.string.feedback_support_title),
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    style = appbarMedium,
                    color = Color.White
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().hazeSource(hazeState),
            state = listState,
            verticalArrangement = spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp + AppBarHeightMedium,
                bottom = 56.dp
            ),
        ) {
            titleItem()
            subtitleItem()
            emailItem(
                email = email,
                onEmailChanged = onEmailChanged
            )
            messageItem(
                message = message,
                onMessageChanged = onMessageChanged
            )
            footerItem(
                onClick = onSendClicked
            )
        }
    }
}

private fun LazyListScope.titleItem() = item(key = "title") {
    val localizedRes = LocalLocalizedRes.current
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = localizedRes.string(R.string.feedback_support_title),
        style = MaterialTheme.typography.displaySmall,
        color = Color.White
    )
}

fun LazyListScope.subtitleItem() = item(key = "subtitle") {
    val localizedRes = LocalLocalizedRes.current
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = localizedRes.string(R.string.feedback_support_subtitle),
        style = subtitleSmall,
        color = Color.White.copy(alpha = 0.4f)
    )
}

fun LazyListScope.emailItem(
    email: String?,
    onEmailChanged: (String) -> Unit
) = item(key = "icon") {
    val focusRequester = remember { FocusRequester() }

    CorporateTextField(
        modifier = Modifier.fillMaxWidth(),
        value = email.orEmpty(),
        placeholder = LocalLocalizedRes.current.string(R.string.email),
        onValueChanged = { onEmailChanged(it.text) },
        maxLength = 150,
    )
}

fun LazyListScope.messageItem(
    message: String?,
    onMessageChanged: (String) -> Unit
) = item(key = "message") {
    val focusRequester = remember { FocusRequester() }

    CorporateTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp),
        value = message.orEmpty(),
        placeholder = LocalLocalizedRes.current.string(R.string.feedback_write_here),
        onValueChanged = { onMessageChanged(it.text) },
        maxLength = 1000,
        isSingleLine = false,
        heightDp = 156.dp
    )
}


private fun LazyListScope.footerItem(onClick: () -> Unit) =
    item(key = "footer_spacer") {
        val context = LocalContext.current
        val localizedRes = LocalLocalizedRes.current
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedSizeButton(
                modifier = Modifier.fillMaxWidth(),
                text = localizedRes.string(R.string.send),
                onClick = onClick
            )

            Text(
                text = localizedRes.string(R.string.feedback_or),
                style = subtitleSmall,
                color = Color.White.copy(alpha = 0.4f)
            )

            Text(
                text = localizedRes.string(R.string.feedback_extra),
                style = subtitleSmall,
                color = Color.White.copy(alpha = 0.4f)
            )

            Text(
                modifier = Modifier.noRippleClickable {
                    sendSupportEmail(
                        context = context,
                        subject = context.getString(
                            R.string.support_email_subject,
                            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                        )
                    )
                },
                text = SUPPORT_EMAIL,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = ExtraLight,
                    textDecoration = TextDecoration.Underline
                ),
                color = Blue.copy(alpha = 0.8f)
            )

        }
    }

private fun handleSideEffect(sideEffect: SupportSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}