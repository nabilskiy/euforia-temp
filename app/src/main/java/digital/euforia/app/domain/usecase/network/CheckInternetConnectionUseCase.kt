package digital.euforia.app.domain.usecase.network

import digital.euforia.app.data.repository.NetworkStatusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckInternetConnectionUseCase @Inject constructor(
    private val networkStatusRepository: NetworkStatusRepository
) {
    suspend operator fun invoke(): Boolean = withContext(Dispatchers.IO) {
        networkStatusRepository.hasInternetConnection()
    }
}
