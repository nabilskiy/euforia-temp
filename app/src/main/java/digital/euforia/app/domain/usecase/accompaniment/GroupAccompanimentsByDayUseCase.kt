package digital.euforia.app.domain.usecase.accompaniment

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Accompaniment
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class GroupAccompanimentsByDayUseCase @Inject constructor(
    private val config: EuforiaRemoteConfigFetcher,

) {
    operator fun invoke(accompaniment: List<Accompaniment>, isDemo: Boolean) : Unit {
        val currentHour: Int = LocalTime.now(ZoneId.systemDefault()).hour
        val timeOfDayConfig = config.getTimeOfDayConfig()


        val range = 1..3
    }
}