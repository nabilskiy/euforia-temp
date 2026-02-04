package digital.euforia.app.ui.util.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import digital.euforia.app.R
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.LocalizedScope

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun CorporateTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String? = null,
    placeholder: String? = null,
    isSingleLine: Boolean = true,
    isError: Boolean = false,
    maxLength: Int = 14,
    heightDp: Dp = 56.dp,
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

    // Keep internal state in sync with external value changes.
    // Without this, when the caller updates `value` (e.g., from ViewModel after returning to screen),
    // the field may still display the old text from its internal state.
    LaunchedEffect(value) {
        if (textValue.text != value) {
            textValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
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
        singleLine = isSingleLine,

        modifier = modifier
            .height(heightDp)
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
            modifier = Modifier.applyIf(!isSingleLine) {
                padding(vertical = 20.dp)
            }
                .padding(horizontal = 26.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(if (isSingleLine) Alignment.CenterStart else Alignment.TopStart)
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

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String,
    heightDp: Dp = 48.dp,
    keyboardController: SoftwareKeyboardController? = null,
    focusManager: FocusManager? = null,
    isCancelEnabled: Boolean = true,
    onValueChanged: (TextFieldValue) -> Unit = {},
    onClearClick: () -> Unit,
    onCancelClick: () -> Unit = {},
) {
    val localizedRes = LocalLocalizedRes.current
    var isFocused by remember { mutableStateOf(false) }
    var textValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (textValue.text != value) {
            textValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val animationSpec = spring<IntSize>()

    Row(
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = textValue,
            onValueChange = { textValue = it; onValueChanged(textValue) },
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = White),
            singleLine = true,
            modifier = modifier
                .height(heightDp)
                .weight(1f)
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f)
                )
                .onFocusChanged { isFocused = it.hasFocus },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager?.clearFocus()
                keyboardController?.hide()
            }),
            cursorBrush = SolidColor(White),
        ) { innerTextField ->
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (textValue.text.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 0.dp),
                            text = localizedRes.string(R.string.search_title),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = White.copy(
                                    alpha = 0.4f
                                )
                            )
                        )
                    }
                    innerTextField()
                }

                if (textValue.text.isNotEmpty()) {
                    Icon(
                        modifier = Modifier
                            .noRippleClickable(onClearClick)
                            .size(20.dp)
                            .background(color = White.copy(alpha = 0.4f), shape = CircleShape)
                            .padding(4.dp),
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null,
                        tint = NavBarBackground
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = White.copy(alpha = 0.4f)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = (isCancelEnabled && isFocused) || textValue.text.isNotEmpty(),
            enter = fadeIn() + expandHorizontally(
                animationSpec = spring(),
                expandFrom = Alignment.End
            ),
            exit = fadeOut() + shrinkHorizontally(
                animationSpec = spring(),
                shrinkTowards = Alignment.End
            )
        ) {
            Text(
                modifier = Modifier
                    .noRippleClickable {
                        onCancelClick()
                        focusManager?.clearFocus()
                    }.padding(start = 8.dp, end = 16.dp),
                text = localizedRes.string(R.string.cancel),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = White
                )
            )
        }
    }
}

@Preview()
@Composable
fun TextFieldPreview() {
    LocalizedScope(langTag = "en") {
        EuforiaTheme() {
            var text by remember { mutableStateOf("some text") }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                SearchTextField(
                    value = text,
                    onValueChanged = { text = it.text },
                    onClearClick = { text = "" }
                )
                SearchTextField(
                    value = "",
                    onValueChanged = { text = it.text },
                    onClearClick = { text = "" }
                )
            }
        }
    }
}