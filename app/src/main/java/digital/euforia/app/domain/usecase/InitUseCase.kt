package digital.euforia.app.domain.usecase

import digital.euforia.app.data.network.AuthToken
import digital.euforia.app.data.network.AuthTokenProvider
import digital.euforia.app.data.network.DeviceToken
import digital.euforia.app.data.network.DeviceTokenProvider
import digital.euforia.app.data.network.TokensProvider
import jakarta.inject.Inject

class InitUseCase @Inject constructor(
//    @DeviceToken private val deviceTokenProvider: DeviceTokenProvider,
//    @AuthToken private val authTokenProvider: AuthTokenProvider
    private val tokensProvider: TokensProvider,
) {
    suspend operator fun invoke() {
        tokensProvider.initTokens()
    }
}