package digital.euforia.app.ui.settings.subscription

import android.content.Context
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.Purchase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.billing.BillingRepository
import digital.euforia.app.billing.BillingRepository.Companion.getInstance
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val billingRepository: BillingRepository
) : ViewModel(),
    ContainerHost<SubscriptionState, SubscriptionSideEffect> {
    override val container = container<SubscriptionState, SubscriptionSideEffect>(
        initialState = SubscriptionState(),
        onCreate = {


        }
    )

}

data class SubscriptionState(
    val errorMessage: String? = null,
    val currentPurchases: List<Purchase> = emptyList()
)

sealed class SubscriptionSideEffect {}