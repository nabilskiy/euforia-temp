package digital.euforia.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import digital.euforia.app.ui.navigation.AppNavigation
import digital.euforia.app.ui.navigation.HomeDestination
import digital.euforia.app.service.soundscapes.SoundscapeNavigationEvents
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.util.LocalizedScope
import digital.euforia.app.ui.util.setupEdgeToEdge
import digital.euforia.app.ui.util.widget.FloatingOrbitingBalls
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var soundscapeNavigationEvents: SoundscapeNavigationEvents

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        setContent {
            val state by viewModel.collectAsState()
            val spheresState = rememberSaveable { mutableStateOf(false) }
            val isBottomBarShown = rememberSaveable { mutableStateOf(false) }

            applyLocale(state.language)
            EuforiaTheme {
                val navController: NavHostController = rememberNavController()

                viewModel.collectSideEffect { sideEffect ->
                    when (sideEffect) {
                        is MainSideEffect.NavigateDeepLink -> {
                            Timber.tag("NAVIGATION").d("Navigating to deep link destination: ${sideEffect.destination}")
                            navController.navigate(sideEffect.destination)
                        }
                    }
                }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    soundscapeNavigationEvents.openScene.collect { sceneId ->
                        navController.navigate(HomeDestination.SoundscapesScene(sceneId = sceneId))
                    }
                }

                // Use the activity context directly for AppNavigation
                Box {
                    AnimatedVisibility(
                        visible = spheresState.value,
                        enter = fadeIn(animationSpec = tween(durationMillis = 2500)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 2500))
                    ) {
                        FloatingOrbitingBalls(
                            modifier = Modifier.Companion.fillMaxSize(),
                            ballRadius = 1000f,
                            orbitRadius = 1100f,
                            driftRadiusX = 40f,
                            driftRadiusY = 80f,
                        )
                    }

                    LocalizedScope(langTag = state.language) {
                        AppNavigation(
                            navController = navController,
                            spheresState = spheresState,
                            isBottomBarShown = isBottomBarShown,
                            deepLinkUri = intent.data?.toString()
                        )
                    }
                }
            }
        }
    }

    fun applyLocale(tag: String): android.content.Context {
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        return createConfigurationContext(config)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            viewModel.handleDeepLink(uri)
        }
    }
}