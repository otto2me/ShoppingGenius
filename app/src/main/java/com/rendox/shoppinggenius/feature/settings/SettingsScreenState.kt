package com.rendox.shoppinggenius.feature.settings

import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.DEFAULT_USER_PREFERENCES
import com.rendox.shoppinggenius.model.GroceryList
import com.rendox.shoppinggenius.model.UserPreferences
import com.rendox.shoppinggenius.ui.helpers.UiEvent

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


