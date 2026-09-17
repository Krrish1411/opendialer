package com.opendialer.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.opendialer.app.core.common.Constants
import com.opendialer.app.core.designsystem.theme.ThemeId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val THEME_ID = stringPreferencesKey("selected_theme_id")
        val DEFAULT_SIM_MODE = intPreferencesKey("default_sim_mode")
        val AUTO_RECORD_ENABLED = booleanPreferencesKey("auto_record_enabled")
        val RECORD_CONSENT_ACCEPTED = booleanPreferencesKey("record_consent_accepted")
        val RECORD_SPEAKER_BOOST = booleanPreferencesKey("record_speaker_boost")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val INCOMING_ANIMATION = booleanPreferencesKey("incoming_animation")
        val DND_SILENCE_UNKNOWN = booleanPreferencesKey("dnd_silence_unknown")
        val DND_VIP_ONLY = booleanPreferencesKey("dnd_vip_only")
    }

    val selectedTheme: Flow<ThemeId> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.THEME_ID] ?: ThemeId.AURA_MODERN.name
        try {
            ThemeId.valueOf(raw)
        } catch (e: Exception) {
            ThemeId.AURA_MODERN
        }
    }

    val defaultSimMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_SIM_MODE] ?: Constants.SIM_MODE_ASK
    }

    val autoRecordEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_RECORD_ENABLED] ?: false
    }

    val recordConsentAccepted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECORD_CONSENT_ACCEPTED] ?: false
    }

    val recordSpeakerBoost: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECORD_SPEAKER_BOOST] ?: true
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_MODE] ?: true
    }

    val incomingAnimationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.INCOMING_ANIMATION] ?: true
    }

    val dndSilenceUnknown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DND_SILENCE_UNKNOWN] ?: false
    }

    val dndVipOnly: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DND_VIP_ONLY] ?: false
    }

    suspend fun setTheme(themeId: ThemeId) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_ID] = themeId.name
        }
    }

    suspend fun setDefaultSimMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SIM_MODE] = mode
        }
    }

    suspend fun setAutoRecordEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_RECORD_ENABLED] = enabled
        }
    }

    suspend fun setRecordConsentAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECORD_CONSENT_ACCEPTED] = accepted
        }
    }

    suspend fun setRecordSpeakerBoost(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECORD_SPEAKER_BOOST] = enabled
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun setIncomingAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCOMING_ANIMATION] = enabled
        }
    }

    suspend fun setDndSilenceUnknown(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DND_SILENCE_UNKNOWN] = enabled
        }
    }

    suspend fun setDndVipOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DND_VIP_ONLY] = enabled
        }
    }
}
