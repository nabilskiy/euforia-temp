package digital.euforia.app.ui.onboarding.pager

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.Error
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.CorporateTextField

@Composable
fun EmailPage(
    email: String?,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    isPageOpened: () -> Boolean,
    onEmailUpdated: (String?) -> Unit,
    isValid: Boolean
) {
    EmailPageContent(
        email = email,
        isValid = isValid,
        focusManager = focusManager,
        keyboardController = keyboardController,
        isPageOpened = isPageOpened,
        onEmailUpdated = onEmailUpdated,
    )
}

@Composable
private fun EmailPageContent(
    email: String?,
    isValid: Boolean,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    isPageOpened: () -> Boolean,
    onEmailUpdated: (String?) -> Unit
) {
//    var name by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
//    val focusManager = LocalFocusManager.current
//    val keyboardController = LocalSoftwareKeyboardController.current
    val localizedRes = LocalLocalizedRes.current

    val isOpened = isPageOpened()
    LaunchedEffect(isOpened) {
        if (isOpened) {
            focusRequester.requestFocus()
        }
    }


    Column(
        modifier = Modifier
//            .clickable {
//                focusManager.clearFocus()
//                keyboardController?.hide()
//            }
            .fillMaxSize()
            .padding(start = 16.dp, top = 30.dp, end = 16.dp),
        verticalArrangement = spacedBy(12.dp)
    ) {
        CorporateTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = email ?: "",
            placeholder = localizedRes.string(id = R.string.name_placeholder),
            //        label = stringResource(id = R.string.onboarding_name_subtitle),
            onValueChanged = { onEmailUpdated(it.text) },
            keyboardController = keyboardController,
            focusManager = focusManager,
            maxLength = 140
        )

        val textColor = if (isValid) {
            White.copy(alpha = 0.4f)
        } else {
            Error
        }

        val textRes = if (isValid) {
            R.string.intro_email_info
        } else {
            R.string.email_validation_text
        }
        Text(
            modifier = Modifier.animateContentSize().padding(horizontal = 8.dp),
            color = textColor,
            text = localizedRes.string(textRes),
            style = MaterialTheme.typography.titleSmall.copy(lineHeight = 16.sp)
        )
    }
}