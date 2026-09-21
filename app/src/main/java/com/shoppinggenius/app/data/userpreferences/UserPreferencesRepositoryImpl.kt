package com.shoppinggenius.app.data.userpreferences

import com.shoppinggenius.app.data.grocerylist.GroceryListRepository
import com.shoppinggenius.app.datastore.UserPreferencesDataSource
import com.shoppinggenius.app.model.DarkThemeConfig
import com.shoppinggenius.app.model.ShoppingGeniusColorScheme
import com.shoppinggenius.app.model.UserPreferences
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource,
    private val groceryListRepository: GroceryListRepository
) : UserPreferencesRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val userPreferencesFlow: Flow<UserPreferences>
        get() = userPreferencesDataSource.userPreferencesFlow
            .flatMapLatest { userPreferences ->
                if (userPreferences.defaultListId != null) {
                    groceryListRepository
                        .getGroceryListById(userPreferences.defaultListId)
                        .map { groceryList ->
                            userPreferences.copy(defaultListId = groceryList?.id)
                        }
                } else {
                    flowOf(userPreferences)
                }
            }

    override suspend fun updateDefaultListId(listId: String?) {
        userPreferencesDataSource.updateDefaultListId(listId)
    }

    override suspend fun updateLastOpenedListId(listId: String) {
        userPreferencesDataSource.updateLastOpenedListId(listId)
    }

    override suspend fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferencesDataSource.updateDarkThemeConfig(darkThemeConfig)
    }

    override suspend fun updateUseSystemAccentColor(useSystemAccentColor: Boolean) {
        userPreferencesDataSource.updateUseSystemAccentColor(useSystemAccentColor)
    }

    override suspend fun updateOpenLastViewedList(openLastViewedList: Boolean) {
        userPreferencesDataSource.updateOpenLastViewedList(openLastViewedList)
    }

    override suspend fun updateUseListViewForGroceries(useListViewForGroceries: Boolean) {
        userPreferencesDataSource.updateUseListViewForGroceries(useListViewForGroceries)
    }

    override suspend fun updateGroupByCategoryInListMode(groupByCategoryInListMode: Boolean) {
        userPreferencesDataSource.updateGroupByCategoryInListMode(groupByCategoryInListMode)
    }

    override suspend fun updateUseThreeLineTodoEntries(useThreeLineTodoEntries: Boolean) {
        userPreferencesDataSource.updateUseThreeLineTodoEntries(useThreeLineTodoEntries)
    }

    override suspend fun updateWidgetBackgroundOpacityPercent(opacityPercent: Int) {
        userPreferencesDataSource.updateWidgetBackgroundOpacityPercent(opacityPercent)
    }

    override suspend fun updateSelectedTheme(selectedTheme: ShoppingGeniusColorScheme) {
        userPreferencesDataSource.updateSelectedTheme(selectedTheme)
    }

    override suspend fun updateSelectedLanguageTag(selectedLanguageTag: String?) {
        userPreferencesDataSource.updateSelectedLanguageTag(selectedLanguageTag)
    }

    override suspend fun updateAutoDeleteCompletedAfterHours(hours: Int) {
        userPreferencesDataSource.updateAutoDeleteCompletedAfterHours(hours)
    }

    override suspend fun getGroceryListIdToOpenOnStartup(): String? {
        val userPreferences = userPreferencesFlow.first()
        val lastOpenedListId =
            if (userPreferences.openLastViewedList) userPreferences.lastOpenedListId else null
        val resultingListId = lastOpenedListId ?: userPreferences.defaultListId
        return resultingListId?.let { listId ->
            // to ensure that the list with this id exists
            groceryListRepository.getGroceryListById(listId).first()?.id
        }
    }
}
