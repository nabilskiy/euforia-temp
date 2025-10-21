package digital.euforia.app.data.repository

import digital.euforia.app.data.api.EuforiaApi
import javax.inject.Inject

class SoundsRepository @Inject constructor(
    private val api: EuforiaApi
) {

    suspend fun getSounds() = api.sounds()
}