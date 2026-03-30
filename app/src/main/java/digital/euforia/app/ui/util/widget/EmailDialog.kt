package digital.euforia.app.ui.util.widget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import digital.euforia.app.R
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.theme.PrimaryButtonText
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.theme.dialogButton
import digital.euforia.app.ui.theme.dialogMessage
import digital.euforia.app.ui.theme.dialogTitle
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.LocalizedScope


@Composable
fun EmailDialog(
    hazeState: HazeState,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val localizedRes = LocalLocalizedRes.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var email by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(false) }
    val validateEmailUseCase = remember { digital.euforia.app.domain.usecase.onboarding.ValidateEmailUseCase() }

    AlertDialog(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .hazeEffect(
                hazeState,
                style = HazeMaterials.regular(AppBarBackground)
            )
            .background(color = BottomSheetBackground, shape = RoundedCornerShape(24.dp)),
        containerColor = Color.Transparent,
        onDismissRequest = onCancel,
        title = {
            Text(
                text = localizedRes.string(R.string.intro_email_step_title),
                style = dialogTitle.copy(fontSize = 20.sp)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    placeholder = localizedRes.string(R.string.email_placeholder),
                    onValueChanged = {
                        email = it.text
                        isValid = validateEmailUseCase.invoke(email)
                    },
                    keyboardController = keyboardController,
                    focusManager = focusManager,
                    onClearClick = { email = "" },
                )
                Text(
                    text = localizedRes.string(R.string.intro_email_info),
                    style = dialogMessage.copy(color = White.copy(alpha = 0.7f))
                )

            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                LaterButton(
                    text = localizedRes.string(R.string.enter_email_alert_cancel),
                    onClick = onCancel,
                    isEnabled = true
                )

                ConfirmButton(
                    text = localizedRes.string(R.string.save),
                    onClick = {
                        if (isValid) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onConfirm(email)
                        }
                    },
                    isEnabled = isValid
                )
            }
        }
    )
}

@Composable
private fun RowScope.ConfirmButton(
    modifier: Modifier = Modifier,
    text: String,
    heightDp: Dp = 44.dp,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "button-scale"
    )

    OutlinedButton(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .weight(1f)
            .height(heightDp)
        ,
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.Black.copy(alpha = 0.1f),
            containerColor = White,
            disabledContentColor = Color.Black.copy(alpha = 0.1f),
            disabledContainerColor = White.copy(alpha = 0.5f),
        ),
        interactionSource = interactionSource,
        border = null
    ) {
        Text(
            modifier = Modifier,
            text = text,
            color = PrimaryButtonText,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}


@Composable
private fun RowScope.LaterButton(
    modifier: Modifier = Modifier,
    text: String,
    heightDp: Dp = 44.dp,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "button-scale"
    )

    OutlinedButton(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .weight(1f)
            .height(heightDp)
        ,
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = Color.Transparent,
            disabledContentColor = White,
            disabledContainerColor = Color.Transparent,
        ),
        interactionSource = interactionSource,
        border = null
    ) {
        Text(
            modifier = Modifier,
            text = text,
            color = White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        )
    }
}

@Preview()
@Composable
fun DialogPreview() {
    LocalizedScope(langTag = "en") {
        EuforiaTheme() {

            EmailDialog(
                hazeState = HazeState(),
                onCancel = {},
                onConfirm = { _ -> }
            )
        }
    }
}