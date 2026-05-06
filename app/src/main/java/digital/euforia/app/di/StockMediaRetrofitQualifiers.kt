/**
 * Developed by www.euforia.digital.
 * Copyright © 2019-2026 EUFORIA MENTAL HEALTH APPS LTD. All Rights Reserved.
 */

package digital.euforia.app.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnsplashRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PexelsRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StockMediaOkHttpClient
