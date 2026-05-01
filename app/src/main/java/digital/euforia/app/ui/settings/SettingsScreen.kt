@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package digital.euforia.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
//import androidx.compose.animation.rememberSharedContentState
//import androidx.compose.animation.sharedElement
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraLight
import androidx.compose.ui.text.font.FontWeight.Companion.Light
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import digital.euforia.app.BuildConfig
import digital.euforia.app.R
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.domain.model.settings.SettingGroup
import digital.euforia.app.domain.model.settings.SettingType
import digital.euforia.app.domain.model.settings.SettingsItem
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.ui.plan.item.notificationItem
import digital.euforia.app.ui.player.audio.AppBarHeightMedium
import digital.euforia.app.ui.settings.feedback.FeedbackBottomSheet
import digital.euforia.app.ui.settings.support.SupportBottomSheet
import digital.euforia.app.ui.subscription.UserActivity
import digital.euforia.app.ui.theme.DarkGray
import digital.euforia.app.ui.theme.PrimaryBackground
import digital.euforia.app.ui.theme.SoonColor
import digital.euforia.app.ui.theme.White
import digital.euforia.app.ui.util.openDeveloperLink
import digital.euforia.app.ui.util.openFacebookAccount
import digital.euforia.app.ui.util.openInstagramAccount
import digital.euforia.app.ui.util.openPrivacyPolicy
import digital.euforia.app.ui.util.openTikTokAccount
import digital.euforia.app.ui.util.openUserAgreement
import digital.euforia.app.ui.util.openXAccount
import digital.euforia.app.ui.util.openYouTubeAccount
import digital.euforia.app.ui.util.openAboutEuforia
import digital.euforia.app.ui.util.openCopyrightNotice
import digital.euforia.app.ui.util.LocalLocalizedRes
import digital.euforia.app.ui.util.openSystemSettings
import digital.euforia.app.ui.util.widget.BlurredAppBar
import digital.euforia.app.ui.util.widget.PremiumButtonState
import digital.euforia.app.ui.settings.support.SupportSideEffect
import digital.euforia.app.ui.settings.support.SupportViewModel
import digital.euforia.app.ui.util.widget.NotificationToast
import digital.euforia.app.ui.util.widget.UpgradeView
import digital.euforia.app.ui.util.widget.applyIf
import digital.euforia.app.ui.util.widget.noRippleClickable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import androidx.hilt.navigation.compose.hiltViewModel
import digital.euforia.app.ui.util.SubscriptionActivityLauncher
import digital.euforia.app.ui.util.shareApp
import timber.log.Timber

@Composable
fun SharedTransitionScope.SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val state by viewModel.collectAsState()
    val supportViewModel: SupportViewModel = hiltViewModel()
    var isSupportSheetVisible by remember { mutableStateOf(false) }
    var isSupportToastVisible by remember { mutableStateOf(false) }
    var supportToastMessageRes by remember { mutableStateOf(R.string.sent_success) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val isPermissionGranted =
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                viewModel.updateNotificationPermission(isPermissionGranted)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        handleSideEffect(sideEffect)
    }

    supportViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SupportSideEffect.ShowToast -> {
                supportToastMessageRes = sideEffect.messageRes
                isSupportToastVisible = true
            }

            is SupportSideEffect.CloseSheet -> {
                isSupportSheetVisible = false
            }
        }
    }

    val localizedRes = LocalLocalizedRes.current
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsContent(
            navController = navController,
            settingGroups = state.settingGroups,
            animatedVisibilityScope = animatedVisibilityScope,
            isPremium = state.isPremium,
            isNotificationPermissionGranted = state.isNotificationPermissionGranted,
            showNotificationPermissionItem = state.showNotificationPermissionItem,
            onNotificationPermissionDismiss = viewModel::dismissNotificationPermissionItem,
            analyticSender = viewModel.analyticSender,
            shareMessage = state.shareMessage,
            isSupportSheetVisible = isSupportSheetVisible,
            onSupportSheetVisibilityChange = { isSupportSheetVisible = it },
            supportViewModel = supportViewModel
        )

        NotificationToast(
            text = localizedRes.string(supportToastMessageRes),
            isVisible = isSupportToastVisible,
            onDismissed = { isSupportToastVisible = false }
        )
    }
}

@Composable
private fun SharedTransitionScope.SettingsContent(
    navController: NavHostController,
    settingGroups: List<SettingGroup>,
    isPremium: Boolean = false,
    isNotificationPermissionGranted: Boolean,
    showNotificationPermissionItem: Boolean,
    onNotificationPermissionDismiss: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    analyticSender: AnalyticSender,
    shareMessage: String? = null,
    isSupportSheetVisible: Boolean,
    onSupportSheetVisibilityChange: (Boolean) -> Unit,
    supportViewModel: SupportViewModel
) {
    val localizedRes = LocalLocalizedRes.current
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    val context = LocalContext.current
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
    var isFeedbackSheetVisible by remember { mutableStateOf(false) }
    SubscriptionActivityLauncher { launchSubscriptionActivity ->
        Box(modifier = Modifier.fillMaxSize().background(PrimaryBackground)) {
            BlurredAppBar(
                titleRes = R.string.profile_title,
                hazeState = hazeState,
                shouldBlur = shouldBlur,
                onUpgradeClick = {
                    launchSubscriptionActivity()
                },
                isBackAllowed = false,
                navController = navController,
                premiumButtonState = if (isPremium) PremiumButtonState.MAX else PremiumButtonState.UPGRADE
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().hazeSource(hazeState),
                verticalArrangement = spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = AppBarHeightMedium,
                    bottom = 56.dp
                ),
            ) {
                settingsSharedTitleItem(this@SettingsContent, animatedVisibilityScope)
                notificationItem(
                    visible = !isNotificationPermissionGranted && showNotificationPermissionItem,
                    onClick = {
                        openSystemSettings(context)
                        onNotificationPermissionDismiss()
                    },
                    onCloseClick = onNotificationPermissionDismiss
                )
                for ((index, settingGroup) in settingGroups.withIndex()) {
                    if (index != 0) dividerItem(index)
                    settingGroupItem(
                        settingGroup = settingGroup,
                        onItemClick = {
                            handleSettingClick(
                                settingItem = it,
                                navController = navController,
                                context = context,
                                analyticSender = analyticSender,
                                showFeedbackSheet = {
                                    isFeedbackSheetVisible = true
                                },
                                showSupportSheet = {
                                    onSupportSheetVisibilityChange(true)
                                },
                                shareApp = {
                                    shareApp(context, shareMessage)
                                })
                        })
                }
                developerItem(
                    analyticSender = analyticSender,
                    navController = navController
                )
            }


            if (isFeedbackSheetVisible) {
                FeedbackBottomSheet() {
                    isFeedbackSheetVisible = false
                }
            }

            if (isSupportSheetVisible) {
                val localizedRes = LocalLocalizedRes.current
                SupportBottomSheet(
                    title = localizedRes.string(R.string.feedback_support_title),
                    subtitle = localizedRes.string(R.string.feedback_support_subtitle),
                    viewModel = supportViewModel,
                ) {
                    onSupportSheetVisibilityChange(false)
                }
            }
        }
    }
}

private fun LazyListScope.settingsSharedTitleItem(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    item(key = "title_shared_my_euforia") {
        with(sharedTransitionScope) {
            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "my_euforia_title"),
                        animatedVisibilityScope
                    ),
                text = LocalLocalizedRes.current.string(R.string.profile_title),
                style = MaterialTheme.typography.displaySmall,
                color = White
            )
        }
    }
}


private fun LazyListScope.settingGroupItem(
    settingGroup: SettingGroup,
    onItemClick: (SettingsItem) -> Unit,
) = item(key = settingGroup.titleRes) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = spacedBy(12.dp)
    ) {
        val context = LocalContext.current
        Text(
            modifier = Modifier.fillMaxWidth().padding(bottom = 0.dp),
            text = LocalLocalizedRes.current.string(settingGroup.titleRes).uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = Medium,
                fontSize = 12.sp
            ),
            color = DarkGray
        )
        Text(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            text = LocalLocalizedRes.current.string(settingGroup.subtitleRes),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = Bold),
            color = White
        )

        for (setting in settingGroup.settingItems) {
            SettingItem(settingsItem = setting, oncClick = { onItemClick(setting) })
        }
    }
}

fun LazyListScope.developerItem(
    modifier: Modifier = Modifier,
    analyticSender: AnalyticSender,
    navController: NavHostController,
) =
    item(key = "dev") {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 12.dp),
            verticalArrangement = spacedBy(16.dp),
        ) {
            val context = LocalContext.current
            Row(
                modifier = Modifier.noRippleClickable {
                    analyticSender.settingsOncreateClick()
                    openDeveloperLink(context)
                }.fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_developer),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = LocalLocalizedRes.current.string(R.string.profile_developer),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = Light),
                    color = White.copy(alpha = 0.6f)
                )
                Text(
                    modifier = Modifier,
                    text = "oncreate.com",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = ExtraLight,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = Cyan.copy(alpha = 0.8f)
                )
            }

            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .noRippleClickable {
                        navController.navigate(HomeDestination.DevOptions)
                    },
                text = "© 2025, EUFORIA MENTAL HEALTH APPS LTD\nBuild ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = Light),
                color = White.copy(alpha = 0.3f),
                textAlign = TextAlign.Center
            )
        }
    }

fun LazyListScope.dividerItem(index: Int) = item(key = "divider$index") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(0.5.dp)
            .background(White.copy(alpha = 0.2f))
    )
}

@Composable
private fun SettingItem(settingsItem: SettingsItem, oncClick: () -> Unit) {
    Row(
        modifier = Modifier.noRippleClickable(oncClick).fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val textColor = if (settingsItem.isSoon) {
            White.copy(alpha = 0.3f)
        } else {
            White.copy(alpha = 0.7f)
        }
        Icon(
            modifier = Modifier.size(24.dp).applyIf(settingsItem.isSoon) {
                graphicsLayer {
                    alpha = 0.3f
                }
            },
            painter = painterResource(settingsItem.settingType.iconRes),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Text(
            modifier = Modifier.weight(1f),
            text = LocalLocalizedRes.current.string(settingsItem.settingType.titleRes),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = Light),
            color = textColor
        )
        if (!settingsItem.isSoon) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_next),
                contentDescription = null,
                tint = Color.Unspecified
            )
        } else {
            Text(
                modifier = Modifier.background(SoonColor, shape = RoundedCornerShape(4.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = LocalLocalizedRes.current.string(R.string.soon).uppercase(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = SemiBold,
                    fontSize = 10.sp
                ),
                color = White
            )
        }
    }
}

private fun handleSettingClick(
    settingItem: SettingsItem,
    navController: NavHostController,
    context: Context,
    analyticSender: AnalyticSender,
    showFeedbackSheet: () -> Unit,
    showSupportSheet: () -> Unit,
    shareApp: () -> Unit
) {
    when (settingItem.settingType) {
        SettingType.NAME -> {
            analyticSender.settingsChangeName()
            navController.navigate(HomeDestination.Name)
        }

        SettingType.VOICE -> {
            analyticSender.settingsChangeGender()
            navController.navigate(HomeDestination.Voice)
        }

        SettingType.LANGUAGE -> {
            analyticSender.settingsChangeLang()
            navController.navigate(HomeDestination.Language)
        }

        SettingType.NOTIFICATIONS -> {
            analyticSender.settingsNotificationsSettings()
            navController.navigate(HomeDestination.Notifications)
        }

        SettingType.EMAIL -> {
            analyticSender.settingsChangeEmail()
            navController.navigate(HomeDestination.Email)
        }

        SettingType.SUBSCRIPTIONS -> {
            analyticSender.settingsAboutSubscriptions()
            navController.navigate(HomeDestination.Subscription)
        }

        SettingType.SYSTEM_SETTINGS -> {
            analyticSender.settingsSystemSettings()
            openSystemSettings(context)
        }

        SettingType.PERSONAL_DATA -> {
            analyticSender.settingsPersonalData()
            navController.navigate(HomeDestination.PersonalData)
        }

        SettingType.USER_AGREEMENT -> {
            analyticSender.settingsTerms()
            openUserAgreement(context)
        }

        SettingType.PRIVACY_POLICY -> {
            analyticSender.settingsPrivacy()
            openPrivacyPolicy(context)
        }

        SettingType.COPYRIGHT -> {
            analyticSender.settingsCopyright()
            openCopyrightNotice(context)
        }

        SettingType.ABOUT -> {
            analyticSender.settingsAboutApp()
            openAboutEuforia(context)
        }

        SettingType.INSTAGRAM -> {
            analyticSender.settingsInstagramClick()
            openInstagramAccount(context)
        }

        SettingType.FACEBOOK -> {
            analyticSender.settingsFacebookClick()
            openFacebookAccount(context)
        }

        SettingType.YOUTUBE -> {
            analyticSender.settingsYoutubeClick()
            openYouTubeAccount(context)
        }

        SettingType.X -> {
            analyticSender.settingsTwitterClick()
            openXAccount(context)
        }

        SettingType.TIKTOK -> {
            analyticSender.settingsTiktokClick()
            openTikTokAccount(context)
        }

        SettingType.FAQ -> {
            analyticSender.settingsFaq()
            navController.navigate(HomeDestination.FAQ)
        }

        SettingType.FEEDBACK -> {
            analyticSender.settingsFeedback()
            showFeedbackSheet()
        }

        SettingType.SUPPORT -> {
            analyticSender.settingsSupport()
            showSupportSheet()
        }

        SettingType.DOWNLOADED -> {
            analyticSender.settingsDownloadsClick()
            navController.navigate(HomeDestination.Downloads)
        }

        SettingType.SHARE_APP -> {
            analyticSender.settingsShare()
            shareApp()
        }

        else -> {}
    }
}

private fun handleSideEffect(sideEffect: SettingsSideEffect) {
    when (sideEffect) {
        else -> {}
    }
}