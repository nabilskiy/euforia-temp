package digital.euforia.app.ui.onboarding.pager

import android.R.attr.lineHeight
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.widget.CorporateTextField
import kotlinx.coroutines.delay

@Composable
fun NamePage(
    name: String?,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    isPageOpened: () -> Boolean,
    onNameUpdated: (String?) -> Unit
) {

    NamePageContent(
        name = name,
        focusManager = focusManager,
        keyboardController = keyboardController,
        isPageOpened = isPageOpened,
        onNameUpdated = onNameUpdated,
    )
}

@Composable
private fun NamePageContent(
    name: String?,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?,
    isPageOpened: () -> Boolean,
    onNameUpdated: (String?) -> Unit
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
            value = name ?: "",
            placeholder = localizedRes.string(id = R.string.name_placeholder),
            onValueChanged = { onNameUpdated(it.text) },
            keyboardController = keyboardController,
            focusManager = focusManager,
            maxLength = 140,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            )
        )

        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = White.copy(alpha = 0.4f),
            text = localizedRes.string(R.string.intro_name_info),
            style = MaterialTheme.typography.titleSmall.copy(lineHeight = 16.sp)
        )
    }
}

@Preview
@Composable
private fun NamePageDarkPreview() {
    EuforiaTheme {
        CorporateTextField(placeholder = "name", label = "Name")
    }
}
