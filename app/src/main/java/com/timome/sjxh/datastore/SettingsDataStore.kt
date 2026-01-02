package com.timome.sjxh.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    
    object PreferencesKeys {
        val START_NUMBER = stringPreferencesKey("start_number")
        val END_NUMBER = stringPreferencesKey("end_number")
        val ALLOW_DUPLICATES = booleanPreferencesKey("allow_duplicates")
        val ENABLE_TRANSITION_ANIMATION = booleanPreferencesKey("enable_transition_animation")
        val ENABLE_TTS = booleanPreferencesKey("enable_tts")
        val TTS_TEXT = stringPreferencesKey("tts_text")
        val ANIMATION_DELAY = stringPreferencesKey("animation_delay")
    }
    
    val startNumber: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.START_NUMBER] ?: "1"
        }
    
    val endNumber: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.END_NUMBER] ?: "30"
        }
    
    val allowDuplicates: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ALLOW_DUPLICATES] ?: false
        }
    
    val enableTransitionAnimation: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_TRANSITION_ANIMATION] ?: true
        }
    
    val enableTts: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENABLE_TTS] ?: false
        }
    
    val ttsText: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TTS_TEXT] ?: "恭喜%学号号同学成功被抽中！"
        }
    
    val animationDelay: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ANIMATION_DELAY] ?: "10"
        }
    
    suspend fun saveStartNumber(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.START_NUMBER] = value
        }
    }
    
    suspend fun saveEndNumber(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.END_NUMBER] = value
        }
    }
    
    suspend fun saveAllowDuplicates(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_DUPLICATES] = value
        }
    }
    
    suspend fun saveEnableTransitionAnimation(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_TRANSITION_ANIMATION] = value
        }
    }
    
    suspend fun saveEnableTts(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_TTS] = value
        }
    }
    
    suspend fun saveTtsText(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TTS_TEXT] = value
        }
    }
    
    suspend fun saveAnimationDelay(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATION_DELAY] = value
        }
    }
}