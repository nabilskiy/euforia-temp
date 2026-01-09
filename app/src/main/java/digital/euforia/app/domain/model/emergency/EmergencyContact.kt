package digital.euforia.app.domain.model.emergency

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(
    val country: String,
    @SerialName("country_native") val countryNative: String,
    val code: String,
    val contacts: List<Contact>
) {
    @Serializable
    data class Contact(
        val name: String,
        val phone: String
    )
}