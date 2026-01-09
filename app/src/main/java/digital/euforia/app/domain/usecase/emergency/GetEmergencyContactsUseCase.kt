package digital.euforia.app.domain.usecase.emergency

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import digital.euforia.app.R
import digital.euforia.app.domain.model.emergency.EmergencyContact
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class GetEmergencyContactsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }

    suspend operator fun invoke(): List<EmergencyContact> = withContext(Dispatchers.IO) {
        val rawFiles = listOf(
            R.raw.sos_numbers_africa,
            R.raw.sos_numbers_asia,
            R.raw.sos_numbers_australia,
            R.raw.sos_numbers_europe,
            R.raw.sos_numbers_latin_america,
            R.raw.sos_numbers_north_america
        )

        val result = mutableListOf<EmergencyContact>()
        val serializer = ListSerializer(EmergencyContact.serializer())

        for (resId in rawFiles) {
            val parsed: List<EmergencyContact> = runCatching {
                context.resources.openRawResource(resId)
                    .bufferedReader()
                    .use { it.readText() }
                    .let { json.decodeFromString(serializer, it) }
            }.getOrElse { emptyList() }
            result.addAll(parsed)
        }

        result
    }
}