package digital.euforia.app.ui

import android.content.Context
import android.content.Intent
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
import digital.euforia.app.domain.usecase.ParseDeepLinkUseCase
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

    @Inject
    lateinit var parseDeepLinkUseCase: ParseDeepLinkUseCase

    private lateinit var billingViewModel: BillingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBillingViewModel()
        setupEdgeToEdge()
        setContent {
            val state by viewModel.collectAsState()
            val spheresState = rememberSaveable { mutableStateOf(false) }
            val isBottomBarShown = rememberSaveable { mutableStateOf(false) }
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

                    val deeplinkDestination = intent.data?.let { uri ->
                        parseDeepLinkUseCase.invoke(uri)
                    }

                    val deepLink = intent.data

                    LocalizedScope(langTag = state.language) {
                        AppNavigation(
                            navController = navController,
                            spheresState = spheresState,
                            isBottomBarShown = isBottomBarShown,
                            deepLinkUri = deepLink.toString()
                        )
                    }
                }
            }
        }
    }

    fun Context.applyLocale(tag: String): Context {
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
                Timber.tag(logTag())
                    .d("Error observing premium status: ${ignored.localizedMessage}")
            }
        }
    }

    private fun initBillingViewModel() {
        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseRemoteConfig.fetchAndActivate()

        billingViewModel = ViewModelProvider(this).get(BillingViewModel::class.java)
        billingViewModel.premiumLiveData.observe(this, premiumObserver)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        val cmd = parseDeepLinkUseCase.invoke(uri)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // warm start
        val command = intent.data?.let { uri ->
            parseDeepLinkUseCase.invoke(uri)
        } // do smth with this
    }
}