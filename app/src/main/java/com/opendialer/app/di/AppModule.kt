package com.opendialer.app.di

import android.content.Context
import com.opendialer.app.core.telephony.CallManager
import com.opendialer.app.core.telephony.SimManager
import com.opendialer.app.core.updater.OtaUpdateManager
import com.opendialer.app.features.recorder.RecorderCapabilityChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Singletons are automatically provided via @Inject constructor
}
