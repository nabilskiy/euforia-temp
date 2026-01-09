package digital.euforia.app.domain.usecase.network

import digital.euforia.app.data.repository.NetworkStatusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveInternetConnectionUseCase @Inject constructor(
    private val networkStatusRepository: NetworkStatusRepository
) {
    operator fun invoke(): Flow<Boolean> =
        networkStatusRepository.observeInternetConnection()
}
