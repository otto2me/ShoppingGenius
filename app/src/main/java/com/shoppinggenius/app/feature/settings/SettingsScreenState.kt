package com.shoppinggenius.app.feature.settings

import com.shoppinggenius.app.model.Category
import com.shoppinggenius.app.model.DEFAULT_USER_PREFERENCES
import com.shoppinggenius.app.model.GroceryList
import com.shoppinggenius.app.model.UserPreferences
import com.shoppinggenius.app.ui.helpers.UiEvent

data class SettingsScreenState(
    val userPreferences: UserPreferences = DEFAULT_USER_PREFERENCES,
    val groceryLists: List<GroceryList> = emptyList(),
    val categories: List<Category> = emptyList(),
    val duckDuckGoImageSearchTestInProgress: Boolean = false,
    val duckDuckGoImageSearchTestSucceeded: Boolean? = null,
    val isLoading: Boolean = true,
    val exportInProgress: Boolean = false,
    val importInProgress: Boolean = false,
    val backupMessageEvent: UiEvent<String>? = null
)


