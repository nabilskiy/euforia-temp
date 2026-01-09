package digital.euforia.app.domain.usecase.resources

import digital.euforia.app.data.repository.ResourceRepository
import digital.euforia.app.domain.util.ResultWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncResourcesUseCase @Inject constructor(
    private val resourceRepository: ResourceRepository
) {

    suspend operator fun invoke(): ResultWrapper<Unit> {
        return withContext(Dispatchers.IO) {
            resourceRepository.syncAll()
        }
    }
}