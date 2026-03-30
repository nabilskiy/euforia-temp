package digital.euforia.app.ui.settings.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import digital.euforia.app.R
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.BottomSheetBackground
import digital.euforia.app.ui.theme.AppBarBackground
import digital.euforia.app.ui.theme.appbarMedium
import digital.euforia.app.ui.theme.subtitleSmall
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.data.db.entity.FeedbackQuestionType
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.AnimatedSizeButton
import digital.euforia.app.ui.util.widget.CorporateTextField
import digital.euforia.app.ui.util.widget.SettingsTextField
import digital.euforia.app.ui.util.widget.WhiteOutlinedButton
import digital.euforia.app.ui.util.widget.applyIf
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackBottomSheet(
    viewModel: FeedbackViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            FeedbackSideEffect.Close -> {
                onDismiss()
                viewModel.resetForm()
            }
        }
    }

    LaunchedEffect(state.errorMessage, state.errorResId) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        state.errorResId?.let {
            Toast.makeText(context, context.getString(it), Toast.LENGTH_SHORT).show()
        }
    }

    if (state.showCloseConfirmation) {
        FeedbackCloseDialog(
            hazeState = rememberHazeState(),
            onCancel = viewModel::onDismissCloseConfirmation,
            onSkip = viewModel::onConfirmClose
        )
    }

    if (state.showThanksDialog) {
        FeedbackThanksDialog(
            hazeState = rememberHazeState(),
            onConfirm = viewModel::onConfirmThanks
        )
    }

    ModalBottomSheet(
        onDismissRequest = viewModel::onCloseAttempt,
        sheetState = sheetState,
        modifier = Modifier.padding(top = 32.dp).statusBarsPadding(),
        dragHandle = {},
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = BottomSheetBackground,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        state.form?.let { form ->
            FeedbackFormContent(
                form = form,
                onSubmit = viewModel::onSubmitForm,
                onOptionSelected = viewModel::onOptionSelected,
                onDismiss = viewModel::onCloseAttempt,
                onAnswerTextChanged = viewModel::onAnswerTextChanged
            )
        }
    }

}

@Composable
fun FeedbackFormContent(
    form: UiFeedbackForm,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    onOptionSelected: (UiOption) -> Unit,
    onAnswerTextChanged: (Int, String) -> Unit
) {
// Setup haze/scroll state for applying blur to dragHandle when content scrolls under it
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    // Blur only when the first item is fully hidden (i.e., second becomes first visible)
    val shouldBlur by remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }
    Box(modifier = Modifier.background(BottomSheetBackground)) {
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
                    text = form.title,
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
            titleItem(form.title)
            subtitleItem(form.description.orEmpty())
            form.questions.forEach { question ->
                questionItem(question, onOptionSelected, onAnswerTextChanged)
            }
            footerItem(form) {
                onSubmit()
            }
        }
    }
}

fun LazyListScope.subtitleItem(text: String) = item(key = "subtitle_$text") {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        style = subtitleSmall.copy(fontWeight = FontWeight.Light),
        color = Color.White.copy(alpha = 0.4f)
    )
}

private fun LazyListScope.titleItem(text: String) = item(key = "title_$text") {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        style = MaterialTheme.typography.displaySmall,
        color = Color.White
    )
}

private fun LazyListScope.questionItem(
    question: UiQuestion,
    onOptionSelected: (UiOption) -> Unit,
    onAnswerTextChanged: (Int, String) -> Unit
) = item(key = "question_${question.id}") {
//    val question = questionWithOptions.question
//    val selectedOptions = questionWithOptions.options.filter { it.isSelected }
    Column(verticalArrangement = spacedBy(12.dp)) {
        QuestionTitle(title = question.text)

        when (question.type) {
            FeedbackQuestionType.TEXT -> {
                TextFeedbackView(question, onAnswerTextChanged)
            }

            else -> {
                question.options.forEach { option ->
                    OptionView(
                        option = option,
                        isSelected = question.selectedOptionIds.contains(option.id),
                        onClick = {
                            onOptionSelected(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionTitle(title: String) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White
    )
}

@Composable
fun TextFeedbackView(question: UiQuestion, onAnswerTextChanged: (Int, String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isRequired = question.isRequired
    SettingsTextField(
        modifier = Modifier.fillMaxWidth(),
        value = question.answerText.orEmpty(),
        placeholder = question.hint.orEmpty(),
        onValueChanged = { onAnswerTextChanged(question.id, it.text) },
        maxLength = 1000,
        heightDp = 156.dp,
        isSingleLine = false,
        keyboardController = keyboardController,
        focusManager = focusManager,
        onClearClick = { focusManager.clearFocus() },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Text
        )
    )
}

@Composable
fun OptionView(option: UiOption, isSelected: Boolean, onClick: () -> Unit) {
    val shape = remember { RoundedCornerShape(20.dp) }
    Row(
        modifier = Modifier.fillMaxWidth()
            .noRippleClickable { onClick() }
            .background(color = White.copy(alpha = 0.05f), shape = shape)
            .applyIf(isSelected) {
                border(
                    width = 1.dp,
                    color = White,
                    shape = shape
                )
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconRes = if (isSelected) {
            R.drawable.ic_checkbox_checked
        } else {
            R.drawable.ic_checkbox_unchecked
        }

        Icon(
            modifier = Modifier,
            painter = painterResource(id = iconRes),
            tint = Color.Unspecified,
            contentDescription = null
        )

        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = option.text,
            style = subtitleSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

private fun LazyListScope.footerItem(form: UiFeedbackForm, onClick: () -> Unit) =
    item(key = "footer_spacer") {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            form.footerText?.let { footerText ->
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = footerText,
                    style = subtitleSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
            form.buttonTitle?.let { text ->
                WhiteOutlinedButton(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    text = text,
                    onClick = onClick
                )
            }
        }
    }