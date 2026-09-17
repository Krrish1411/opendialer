package com.opendialer.app.di

import android.content.Context
import androidx.room.Room
import com.opendialer.app.core.common.Constants
import com.opendialer.app.data.local.DialerDatabase
import com.opendialer.app.data.local.dao.BlockedNumberDao
import com.opendialer.app.data.local.dao.ContactSimPreferenceDao
import com.opendialer.app.data.local.dao.FavoritesDao
import com.opendialer.app.data.local.dao.RecordingDao
import com.opendialer.app.data.local.dao.SpeedDialDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DialerDatabase {
        return Room.databaseBuilder(
            context,
            DialerDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideFavoritesDao(database: DialerDatabase): FavoritesDao = database.favoritesDao()

    @Provides
    fun provideSpeedDialDao(database: DialerDatabase): SpeedDialDao = database.speedDialDao()

    @Provides
    fun provideBlockedNumberDao(database: DialerDatabase): BlockedNumberDao = database.blockedNumberDao()

    @Provides
    fun provideRecordingDao(database: DialerDatabase): RecordingDao = database.recordingDao()

    @Provides
    fun provideContactSimPreferenceDao(database: DialerDatabase): ContactSimPreferenceDao = database.contactSimPreferenceDao()
}
