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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.billingclient.api.Purchase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.AndroidEntryPoint
import digital.euforia.app.billing.BillingViewModel
import digital.euforia.app.billing.BillingViewModel.Companion.repository
import digital.euforia.app.billing.localdb.Premium
import digital.euforia.app.data.analytics.AnalyticSender
import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.di.ApplicationCoroutineScope
import digital.euforia.app.ui.navigation.AppNavigation
import digital.euforia.app.ui.theme.EuforiaTheme
import digital.euforia.app.ui.util.LocalizedScope
import digital.euforia.app.ui.util.logTag
import digital.euforia.app.ui.util.setupEdgeToEdge
import digital.euforia.app.ui.util.widget.FloatingOrbitingBalls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import timber.log.Timber
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

    @Inject
    lateinit var profilePreferences: ProfilePreferences

    @Inject
    lateinit var analyticSender: AnalyticSender

    private lateinit var billingViewModel: BillingViewModel

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
        initBillingViewModel()
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

    private val premiumObserver: Observer<Premium?> = Observer { premium: Premium? ->
        scope.launch {
            try {
                if (premium != null && premium.entitled) {
                    analyticSender.premiumActive()
                    profilePreferences.setIsPremium(true)
                    var purchaseJson: String? = null
                    val purchases: List<Purchase>? = repository?.currentPurchases
                    purchases?.let {
                        for (p in purchases) {
                            purchaseJson = p.originalJson
                            break
                        }
                        purchaseJson?.let {
                            viewModel.syncPurchase(it)
                        }
                    }
//                PreferencesManager.setIsPro(this, true)
                } else {
                    analyticSender.premiumNotActive()
                    profilePreferences.setIsPremium(false)
//                    PreferencesManager.setIsPro(this, false)
                }
            } catch (ignored: Exception) {
                Timber.tag(logTag()).d("Error observing premium status: ${ignored.localizedMessage}")
            }
        }
    }

    private fun initBillingViewModel() {
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseRemoteConfig.fetchAndActivate()

        billingViewModel = ViewModelProvider(this).get(BillingViewModel::class.java)
        billingViewModel.premiumLiveData.observe(this, premiumObserver)
    }
}