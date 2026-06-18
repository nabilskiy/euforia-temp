package digital.euforia.app.ui.onboardingV3.pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import kotlinx.coroutines.delay

@Composable
fun NameV3Page(
    name: String?,
    isPageActive: Boolean,
    onNameUpdated: (String) -> Unit,
) {
    if (!isPageActive) return

    val localizedRes = LocalLocalizedRes.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(220)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 206.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicTextField(
            value = name.orEmpty(),
            onValueChange = { onNameUpdated(it.take(30)) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .focusRequester(focusRequester)
                .background(White.copy(alpha = 0.04f), RoundedCornerShape(25.dp))
                .padding(horizontal = 24.dp),
            decorationBox = { innerTextField ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (name.isNullOrBlank()) {
                        Text(
                            text = localizedRes.string(R.string.name_placeholder),
                            color = White.copy(alpha = 0.2f),
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        )
                    }
                    innerTextField()
                }
            },
        )

        Text(
            text = localizedRes.string(R.string.intro_name_info),
            color = White.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                lineHeight = 17.sp,
            ),
        )
    }
}
