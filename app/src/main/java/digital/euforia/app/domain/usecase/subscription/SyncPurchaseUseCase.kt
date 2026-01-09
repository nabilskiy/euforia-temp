package digital.euforia.app.domain.usecase.subscription

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.moshi.Moshi
import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.data.store.ProfilePreferences
import digital.euforia.app.data.model.SubscriptionRequest
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import kotlin.io.encoding.Base64
import kotlin.jvm.java

class SyncPurchaseUseCase @Inject constructor(
    private val moshi: Moshi,
    private val api: EuforiaApi,
    private val profilePreferences: ProfilePreferences,
) {
    suspend operator fun invoke(purchaseJson: String?) {
        if (purchaseJson != null && purchaseJson.isNotEmpty()) {
//            val adapter = moshi.adapter(SubscriptionRequest::class.java)
//            val data = adapter.fromJson(purchaseJson)!!
//            val body = subscriptionBody(purchaseJson)
            val adapter = Moshi.Builder().build().adapter(SubscriptionRequest::class.java)
            val request = adapter.fromJson(purchaseJson)!!
            val jsonMediaType = "text/plain".toMediaType()
            val data = Base64.encode(purchaseJson.toByteArray())
            val body = purchaseJson.toRequestBody(jsonMediaType)
            val receiptBytes = purchaseJson.toByteArray(Charsets.UTF_8)
            val base64Receipt = Base64.encode(receiptBytes)
            val contentType = "text/plain".toMediaType()
            val contentTypeJson = "application/json".toMediaType()
            val requestBody = base64Receipt.toRequestBody(contentType)

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    Timber.d("FCM Token: $fcmToken")
                } else {
                    Timber.d("Fetching FCM token failed: ${task.exception}")
                }
            }

            api.subscription(
                body = purchaseJson.trimIndent().toRequestBody("application/json".toMediaType())
//                body = request
//                receipt = requestBody,
//                body = data!!,
//                orderId = request.orderId
            ).onSuccess { networkSubscription ->
                Timber.d("Successfully synced subscription: $networkSubscription")
//                if (networkSubscription.token != null && networkSubscription.token.isNotEmpty()) {
//                    profilePreferences.setSubsToken(networkSubscription.token)
//                    profilePreferences.setIsPremium(true)
//                    profilePreferences.setIsSubscriptionValid(true)
//                }
            }.onFailure {
                Timber.d("Failed to sync subscription: ${it.localizedMessage}")
            }
        } else {
            profilePreferences.setSubsToken("")
            profilePreferences.setIsPremium(false)
            profilePreferences.setIsSubscriptionValid(false)
        }
    }

    // Java-friendly entry point
    fun execute(purchaseJson: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            invoke(purchaseJson)
        }
    }
}