package digital.euforia.app.domain.usecase.home

import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.domain.model.home.NavBarItem
import digital.euforia.app.domain.model.subscription.SubscriptionLevel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GetNavBarItemsFlowUseCase @Inject constructor(
    private val profilePreferences: ProfilePreferences
) {
    suspend operator fun invoke(): Flow<List<NavBarItem>> {
        return withContext(Dispatchers.IO) {
            profilePreferences.getIsPremiumFlow().map { isPremium ->
                listOfNotNull(
                    NavBarItem.PLAN,
                    NavBarItem.PROGRAMS,
                    NavBarItem.SOUNDSCAPES,
                    if (isPremium) NavBarItem.SETTINGS_MAX else NavBarItem.SETTINGS
                )
            }
//            profilePreferences.getSubscriptionLevelFlow().map { subscriptionLevel ->
//                listOfNotNull(
//                    NavBarItem.PLAN,
//                    NavBarItem.PROGRAMS,
//                    NavBarItem.SOUNDSCAPES,
//                    if (subscriptionLevel == SubscriptionLevel.PREMIUM) NavBarItem.SETTINGS_MAX else NavBarItem.SETTINGS
//                )
//            }
        }
    }
}