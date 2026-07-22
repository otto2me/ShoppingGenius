package com.rendox.grocerygenius.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.rendox.grocerygenius.feature.widget.ActiveGroceryListWidgetProvider
import com.rendox.grocerygenius.model.DEFAULT_USER_PREFERENCES
import com.rendox.grocerygenius.model.DarkThemeConfig
import com.rendox.grocerygenius.model.GroceryGeniusColorScheme
import com.rendox.grocerygenius.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val dataStore: DataStore<Preferences>
) {

    /**
     * The id of the list that is opened on app startup.
     * When the user navigates to a different list, this property gets updated.
     * Alternatively, the user can explicitly set the list id in the settings.
     */
    val userPreferencesFlow = dataStore.data.map { preferences ->
        // unspecified list id is saved as an empty string since preferences data store
        // doesn't allow nullable values
        val defaultListId = preferences[DEFAULT_LIST_ID_KEY] ?: ""
        UserPreferences(
            defaultListId = defaultListId.ifEmpty { null },
            lastOpenedListId = preferences[LAST_OPENED_LIST_ID_KEY],
            darkThemeConfig = preferences[DARK_THEME_CONFIG_KEY]?.let {
                DarkThemeConfig.entries[it]
            } ?: DEFAULT_USER_PREFERENCES.darkThemeConfig,
            useSystemAccentColor = preferences[USE_SYSTEM_ACCENT_COLOR_KEY]
                ?: DEFAULT_USER_PREFERENCES.useSystemAccentColor,
            openLastViewedList = preferences[OPEN_LAST_VIEWED_LIST_KEY]
                ?: DEFAULT_USER_PREFERENCES.openLastViewedList,
            useListViewForGroceries = preferences[USE_LIST_VIEW_FOR_GROCERIES_KEY]
                ?: DEFAULT_USER_PREFERENCES.useListViewForGroceries,
            widgetBackgroundOpacityPercent =
                (preferences[WIDGET_BACKGROUND_OPACITY_PERCENT_KEY]
                    ?: DEFAULT_USER_PREFERENCES.widgetBackgroundOpacityPercent)
                    .coerceIn(0, 100),
            selectedTheme = preferences[SELECTED_THEME_KEY]?.let {
                GroceryGeniusColorScheme.entries[it]
            } ?: DEFAULT_USER_PREFERENCES.selectedTheme,
            selectedLanguageTag = preferences[SELECTED_LANGUAGE_TAG_KEY].orEmpty().ifBlank { null },
            autoDeleteCompletedAfterHours = (preferences[AUTO_DELETE_COMPLETED_AFTER_HOURS_KEY]
                ?: DEFAULT_USER_PREFERENCES.autoDeleteCompletedAfterHours)
                .coerceIn(1, 120)
        )
    }

    suspend fun updateDefaultListId(listId: String?) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_LIST_ID_KEY] = listId ?: ""
        }
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    suspend fun updateLastOpenedListId(listId: String) {
        dataStore.edit { preferences ->
            preferences[LAST_OPENED_LIST_ID_KEY] = listId
        }
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    suspend fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        dataStore.edit { preferences ->
            preferences[DARK_THEME_CONFIG_KEY] = darkThemeConfig.ordinal
        }
    }

    suspend fun updateUseSystemAccentColor(useSystemAccentColor: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_SYSTEM_ACCENT_COLOR_KEY] = useSystemAccentColor
        }
    }

    suspend fun updateOpenLastViewedList(openLastViewedList: Boolean) {
        dataStore.edit { preferences ->
            preferences[OPEN_LAST_VIEWED_LIST_KEY] = openLastViewedList
        }
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    suspend fun updateUseListViewForGroceries(useListViewForGroceries: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_LIST_VIEW_FOR_GROCERIES_KEY] = useListViewForGroceries
        }
    }

    suspend fun updateWidgetBackgroundOpacityPercent(opacityPercent: Int) {
        dataStore.edit { preferences ->
            preferences[WIDGET_BACKGROUND_OPACITY_PERCENT_KEY] = opacityPercent.coerceIn(0, 100)
        }
        ActiveGroceryListWidgetProvider.refreshAllWidgets(appContext)
    }

    suspend fun updateSelectedTheme(selectedTheme: GroceryGeniusColorScheme) {
        dataStore.edit { preferences ->
            preferences[SELECTED_THEME_KEY] = selectedTheme.ordinal
        }
    }

    suspend fun updateSelectedLanguageTag(selectedLanguageTag: String?) {
        dataStore.edit { preferences ->
            if (selectedLanguageTag.isNullOrBlank()) {
                preferences.remove(SELECTED_LANGUAGE_TAG_KEY)
            } else {
                preferences[SELECTED_LANGUAGE_TAG_KEY] = selectedLanguageTag
            }
        }
    }

    suspend fun updateAutoDeleteCompletedAfterHours(hours: Int) {
        dataStore.edit { preferences ->
            preferences[AUTO_DELETE_COMPLETED_AFTER_HOURS_KEY] = hours.coerceIn(1, 120)
        }
    }

    companion object {
        val DEFAULT_LIST_ID_KEY = stringPreferencesKey("default_list_id")
        val DARK_THEME_CONFIG_KEY = intPreferencesKey("dark_theme_config")
        val USE_SYSTEM_ACCENT_COLOR_KEY = booleanPreferencesKey("use_system_accent_color")
        val OPEN_LAST_VIEWED_LIST_KEY = booleanPreferencesKey("open_last_viewed_list")
        val USE_LIST_VIEW_FOR_GROCERIES_KEY = booleanPreferencesKey("use_list_view_for_groceries")
        val WIDGET_BACKGROUND_OPACITY_PERCENT_KEY =
            intPreferencesKey("widget_background_opacity_percent")
        val SELECTED_THEME_KEY = intPreferencesKey("selected_theme")
        val LAST_OPENED_LIST_ID_KEY = stringPreferencesKey("last_opened_list_id")
        val SELECTED_LANGUAGE_TAG_KEY = stringPreferencesKey("selected_language_tag")
        val AUTO_DELETE_COMPLETED_AFTER_HOURS_KEY = intPreferencesKey("auto_delete_completed_after_hours")
    }
}