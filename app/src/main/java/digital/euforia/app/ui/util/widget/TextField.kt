package digital.euforia.app.ui.util.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import digital.euforia.app.ui.theme.White

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun CorporateTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    maxLength: Int = 14,
    keyboardController: SoftwareKeyboardController? = null,
    focusManager: FocusManager? = null,
    onValueChanged: (TextFieldValue) -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    var textValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }
    val interactionSource = remember { MutableInteractionSource() }
    val borderWidth = if (isFocused) 2.dp else 1.dp
    val borderColor = if (isFocused) Color.White else Color.White.copy(alpha = 0.1f)

//    CompositionLocalProvider(value = LocalTextSelectionColors provides customTextSelectionColors()) {
    BasicTextField(
        value = textValue,
        onValueChange = {
            if (it.text.length <= maxLength) {
                textValue = it; onValueChanged(textValue)
            }
        },
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.labelLarge.copy(
            color = White,
            fontWeight = Bold
        ),
        singleLine = true,

        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.05f))
            .onFocusChanged { isFocused = it.hasFocus },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            focusManager?.clearFocus()
            keyboardController?.hide()
        }),
        cursorBrush = SolidColor(White),
    ) { innerTextField ->
        Box(
            modifier = Modifier
                .padding(horizontal = 26.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
            ) {
                Box {
                    if (placeholder == null || textValue.text.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 0.dp),
                            text = placeholder ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = Bold,
                                color = White.copy(alpha = 0.2f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    }
}
