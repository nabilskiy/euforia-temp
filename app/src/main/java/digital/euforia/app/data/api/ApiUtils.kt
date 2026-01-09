package digital.euforia.app.data.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

fun subscriptionBody(json: String) =
    json.toRequestBody("application/json".toMediaType())