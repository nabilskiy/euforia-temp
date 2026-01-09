package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.db.entity.AccompanimentWithItems
import digital.euforia.app.data.repository.AccompanimentRepository
import digital.euforia.app.data.store.AppPreferences
import digital.euforia.app.data.store.ProfilePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetAccompanimentWithItemsFlowUseCase @Inject constructor(
    private val accompanimentRepository: AccompanimentRepository,
    private val profilePreferences: ProfilePreferences
) {

    suspend operator fun invoke(): Flow<List<AccompanimentWithItems>> {
        return withContext(Dispatchers.IO) {
            val isDemo = profilePreferences.getIsDemo()
            val isPremium = profilePreferences.getIsPremium()
            val requestDemoDays = isDemo || !isPremium
            accompanimentRepository.getAllWithItemsFlow(requestDemoDays)
        }
    }
}