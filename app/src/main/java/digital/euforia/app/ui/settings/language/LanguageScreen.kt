@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.settings.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberModalBottomSheetState
import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import digital.euforia.app.R
import digital.euforia.app.domain.model.onboarding.Language
import digital.euforia.app.ui.navigation.NavBarlessScreen
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.theme.DialogButton
import digital.euforia.app.ui.theme.NavBarBackground
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.noRippleClickable
import digital.euforia.app.ui.util.widget.titleItem
import digital.euforia.app.ui.util.LocalLocalizedRes
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SharedTransitionScope.LanguageScreen(
    navController: NavHostController,
    viewModel: LanguageViewModel,
    navBarVisibilityState: MutableState<Boolean>,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect, context)
    }
    NavBarlessScreen(navBarVisibilityState) {
        LanguageContent(
            navController = navController,
            selectedLanguage = Language.entries.firstOrNull { it.tag == state.language }
                ?: Language.EN,
            onLanguageSelected = viewModel::onLanguageChanged,
            onBackClick = {
                navController.popBackStack()
            },
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

@Composable
private fun SharedTransitionScope.LanguageContent(
    navController: NavHostController,
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onBackClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val listState = rememberLazyListState()
    val hazeState = dev.chrisbanes.haze.rememberHazeState()
    val density = LocalDensity.current
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
    var confirmShown by remember { mutableStateOf(false) }
//    var prevSelectedItem by remember { mutableStateOf<Language>(selectedLanguage) }
    var pendingLanguage by remember { mutableStateOf<Language?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
        BlurredAppBar(
            titleRes = R.string.change_language_title,
            backTitleRes = R.string.profile_title,
            shouldBlur = shouldBlur, hazeState = hazeState, onBackClick = onBackClick,
            sharedElementKeyForBackTitle = "my_euforia_title",
            animatedVisibilityScope = animatedVisibilityScope,
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
                titleRes = R.string.change_language_title
            )
            optionsItem(
                selectedLanguage = pendingLanguage?: selectedLanguage,
                onLanguageSelected = {
                    confirmShown = true
//                    prevSelectedItem = it
                    pendingLanguage = it
//                    onLanguageSelected(it)
                }
            )
        }
        if (confirmShown) {
            ConfirmBottomSheet(
                onConfirm = {
                    confirmShown = false
                    onLanguageSelected(pendingLanguage?: selectedLanguage)
                    pendingLanguage = null
                },
                onDismiss = {
                    confirmShown = false
                    pendingLanguage = null
                })
        }
    }
}


fun LazyListScope.optionsItem(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) = item {
    val localizedRes = LocalLocalizedRes.current
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(color = NavBarBackground, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        val supportedLanguages = remember {
            Language.entries.filter { it.isSupported }
        }

        supportedLanguages.forEachIndexed { index, language ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(32.dp)
                    .noRippleClickable { onLanguageSelected(language) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = localizedRes.string(language.titleRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = White
                )
                if (language == selectedLanguage) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
            if (index != supportedLanguages.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(0.5.dp)
                        .background(White.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmBottomSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = 16.dp).statusBarsPadding(),
        dragHandle = {},
        scrimColor = Color.Transparent,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.Transparent,
        tonalElevation = 12.dp,
    ) {
        val localizedRes = LocalLocalizedRes.current
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
            Text(
                modifier = Modifier.background(
                    color = NavBarBackground,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ).padding(12.dp).fillMaxWidth(),
                text = localizedRes.string(R.string.change_language_confirm_message),
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = White.copy(alpha = 0.7f),
                thickness = 1.dp
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                onClick = onConfirm,
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
                    text = localizedRes.string(R.string.yes),
                    color = DialogButton,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
                onClick = onDismiss,
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = White.copy(alpha = 0.6f),
                    containerColor = NavBarBackground
                ),
                contentPadding = PaddingValues(0.dp),
                border = null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    text = localizedRes.string(R.string.cancel),
                    color = DialogButton,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

private fun handleSideEffect(sideEffect: LanguageSideEffect, context: Context) {
    when (sideEffect) {
        LanguageSideEffect.RestartApp -> {
            restartApp(context)
        }
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
    android.os.Process.killProcess(android.os.Process.myPid())
}