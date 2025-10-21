package digital.euforia.app.ui

import android.content.Context
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
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.di.ApplicationCoroutineScope
import digital.euforia.app.ui.navigation.AppNavigation
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.util.LocalizedScope
import digital.euforia.app.ui.util.setupEdgeToEdge
import digital.euforia.app.ui.util.widget.FloatingOrbitingBalls
import kotlinx.coroutines.CoroutineScope
import org.orbitmvi.orbit.compose.collectAsState
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    @ApplicationCoroutineScope
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var euforiaRemoteConfigFetcher: EuforiaRemoteConfigFetcher

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        updateRemoteConfig()

        // Apply the saved language to the activity before setting content
//        val appLang = compositionLocalOf { "en" } // default
//
//        scope.launch {
//            appPreferences.getLanguageFlow().collectLatest {
//                appLang.provides(it ?: "en")
//
//            }
//        }
        setupEdgeToEdge()
        setContent {
            val state by viewModel.collectAsState()
            val spheresState = rememberSaveable { mutableStateOf(false) }
            applyLocale(state.language)
            EuforiaTheme {
                val navController: NavHostController = rememberNavController()

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
//                    var lang by rememberSaveable { mutableStateOf("en") }

//                    CompositionLocalProvider(appLang provides lang) {

                    LocalizedScope(langTag = state.language) {
                        AppNavigation(navController = navController, spheresState = spheresState)
                    }
                }
//                }
            }
        }
    }

    private fun applyLanguage(context: Context) {
        // Run in the main thread since we're updating the UI
//        scope.launch(Dispatchers.Main) {
//            appPreferences.getLanguageFlow().collect { newLanguage ->
//                val locale = Locale.forLanguageTag(newLanguage)
//                val newContext = context.updateLocale(locale)
//                val localizedResources = newContext.resources
////                val localizedString = localizedResources.getString(R.string.some_text)
//            }
//            val language = getLanguage()
//            Timber.tag("LANG_BUG").d("Lang in prefs: $language")
//
//            config.setLocale(locale)
//
//
//            Locale.setDefault(locale)
//
//            config.setLayoutDirection(locale)
//
//            // Update the configuration
////            resources.updateConfiguration(config, resources.displayMetrics)
//            // Set up a listener for language changes
//            scope.launch {
//                appPreferences.getLanguageFlow().collect { newLanguage ->
//                    Timber.tag("LANG_BUG").d("flow collected lang: $newLanguage")
//                    if (newLanguage != null && newLanguage != language) {
//                        Timber.tag("LANG_BUG").d("recreating")
//                        // Language changed, recreate the activity
//                        recreate()
//                    }
//                }
//            }
//        }
    }


    fun Context.applyLocale(tag: String): Context {
//        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
//        val localeCode = prefs.getString("locale", "en") ?: "en" // дефолт

        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        return createConfigurationContext(config)
    }

//    private suspend fun getLanguage(): String {
//        return scope.async(Dispatchers.IO) {
//            appPreferences.getLanguage() ?: Locale.getDefault().language
//        }.await()
//    }

//    private fun getLanguageFlow(): Flow<String> = appPreferences.getLanguageFlow().map { language ->
//        language ?: Locale.getDefault().language
//    }
}