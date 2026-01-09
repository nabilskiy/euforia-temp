/**
 * Copyright (C) 2018 Google Inc. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package digital.euforia.app.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import digital.euforia.app.billing.localdb.AugmentedSkuDetails
import digital.euforia.app.billing.localdb.Photo360
import digital.euforia.app.billing.localdb.Premium
import digital.euforia.app.util.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * Created by ONCREATE COMPANY © 2023.
 * Developed for LULUBY TECHNOLOGY OÜ. All rights reserved.
 */

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    val photo360LiveData: LiveData<Photo360>
    val premiumLiveData: LiveData<Premium>
    val subsSkuDetailsListLiveData: LiveData<List<AugmentedSkuDetails>>
    val inappSkuDetailsListLiveData: LiveData<List<AugmentedSkuDetails>>
    val billingStatus: SingleLiveEvent<Int>

    private val viewModelScope = CoroutineScope(Job() + Dispatchers.Main)

    companion object {
        var repository: BillingRepository? = null
    }

    init {
        if (repository == null) {
            repository = BillingRepository.getInstance(application)
            repository!!.startDataSourceConnections()
        }
        photo360LiveData = repository!!.photo360LiveData
        premiumLiveData = repository!!.premiumLiveData
        subsSkuDetailsListLiveData = repository!!.subsSkuDetailsListLiveData
        inappSkuDetailsListLiveData = repository!!.inappSkuDetailsListLiveData
        billingStatus = repository!!.billingStatus
    }

    fun queryPurchases() = repository?.queryPurchasesAsync()

    override fun onCleared() {
        super.onCleared()
        repository?.endDataSourceConnections()
        viewModelScope.coroutineContext.cancel()
    }

    fun makePurchase(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) {
        repository?.launchBillingFlow(activity, augmentedSkuDetails)
    }
}



