package digital.euforia.app.ui.settings.support

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraLight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import digital.euforia.app.ui.theme.Black
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.DayBlue
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.theme.subtitleSmall
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.sendSupportEmail
import digital.euforia.app.ui.util.widget.AnimatedSizeButton
import digital.euforia.app.ui.util.widget.CorporateTextField
import digital.euforia.app.ui.util.widget.NotificationToast
import digital.euforia.app.ui.util.widget.SettingsTextField
import digital.euforia.app.ui.util.widget.WhiteOutlinedButton
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportBottomSheet(
    title: String,
    subtitle: String,
    viewModel: SupportViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .padding(top = 32.dp)
            .statusBarsPadding(),
        dragHandle = {},
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BottomSheetBackground,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        SupportContent(
            email = state.email,
            message = state.message,
            emailError = state.emailError,
            messageError = state.messageError,
            title = title,
            subtitle = subtitle,
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
    emailError: String? = null,
    messageError: String? = null,
    title: String,
    subtitle: String,
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
    Box(modifier = Modifier.fillMaxSize().background(BottomSheetBackground)) {
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
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .size(28.dp)
                    .padding(6.dp),
                onClick = { onDismiss() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    tint = White.copy(alpha = 0.7f),
                    contentDescription = null
                )
            }
            if (shouldBlur) {
                Text(
                    text = title,
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
            titleItem(title)
            subtitleItem(subtitle)
            emailItem(
                email = email,
                errorMessage = emailError,
                onEmailChanged = onEmailChanged
            )
            messageItem(
                message = message,
                errorMessage = messageError,
                onMessageChanged = onMessageChanged
            )
            footerItem(
                isEnabled = true,
                onClick = onSendClicked
            )
        }
    }
}

private fun LazyListScope.titleItem(title: String) = item(key = "title") {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = title,
        style = MaterialTheme.typography.displaySmall,
        color = Color.White
    )
}

fun LazyListScope.subtitleItem(subtitle: String) = item(key = "subtitle") {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = subtitle,
        style = subtitleSmall,
        color = Color.White.copy(alpha = 0.4f)
    )
}

fun LazyListScope.emailItem(
    email: String?,
    errorMessage: String?,
    onEmailChanged: (String) -> Unit
) = item(key = "icon") {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    SettingsTextField(
        modifier = Modifier.fillMaxWidth(),
        value = email.orEmpty(),
        placeholder = LocalLocalizedRes.current.string(R.string.email),
        onValueChanged = { onEmailChanged(it.text) },
        keyboardController = keyboardController,
        focusManager = focusManager,
        errorMessage = errorMessage,
        isError = errorMessage != null,
        onClearClick = { onEmailChanged("") },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Email
        )
    )
}

fun LazyListScope.messageItem(
    message: String?,
    errorMessage: String?,
    onMessageChanged: (String) -> Unit
) = item(key = "message") {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    SettingsTextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = message.orEmpty(),
        placeholder = LocalLocalizedRes.current.string(R.string.feedback_write_here),
        onValueChanged = { onMessageChanged(it.text) },
        maxLength = 1000,
        heightDp = 156.dp,
        isSingleLine = false,
        keyboardController = keyboardController,
        focusManager = focusManager,
        errorMessage = errorMessage,
        isError = errorMessage != null,
        onClearClick = { onMessageChanged("") },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Text
        )
    )
}


private fun LazyListScope.footerItem(
    isEnabled: Boolean,
    onClick: () -> Unit
) =
    item(key = "footer_spacer") {
        val context = LocalContext.current
        val localizedRes = LocalLocalizedRes.current
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            AnimatedSizeButton(
//                modifier = Modifier.fillMaxWidth(),
//                text = localizedRes.string(R.string.send),
//                isEnabled = isEnabled,
//                horizontalPadding = 0.dp,
//                onClick = onClick
//            )
            WhiteOutlinedButton(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 24.dp),
                text = localizedRes.string(R.string.send),
                onClick = onClick,
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
//                    textDecoration = TextDecoration.Underline
                ),
                color = DayBlue
            )

        }
    }
