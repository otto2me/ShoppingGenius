package com.shoppinggenius.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppinggenius.app.data.Synchronizer
import com.shoppinggenius.app.data.category.CategoryRepository
import com.shoppinggenius.app.data.checkFirstTimeSync
import com.shoppinggenius.app.data.icons.IconRepository
import com.shoppinggenius.app.data.product.ProductRepository
import com.shoppinggenius.app.data.userpreferences.UserPreferencesRepository
import com.shoppinggenius.app.datastore.ChangeListVersionsDataSource
import com.shoppinggenius.app.feature.dashboardscreen.GROCERY_LISTS_DASHBOARD_ROUTE
import com.shoppinggenius.app.feature.grocerylist.GROCERY_LIST_CATEGORY_NESTED_NAV_ROUTE_WITH_ARGS
import com.shoppinggenius.app.model.ChangeListVersions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    private val changeListVersionsDataSource: ChangeListVersionsDataSource,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val iconRepository: IconRepository
) : ViewModel() {

    val uiStateFlow = MutableStateFlow<MainActivityUiState?>(null)

    init {
        viewModelScope.launch {
            val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow
            // using only the first value because the nav host start destination
            // should be the same throughout the whole app session (until the app is closed)
            val defaultListId = userPreferencesRepository.getGroceryListIdToOpenOnStartup()
            val changeListVersions = changeListVersionsDataSource.getChangeListVersions()
            val dataHasNotBeenPopulated = listOf(
                changeListVersions.iconVersion,
                changeListVersions.categoryVersion,
                changeListVersions.productVersion
            ).any { checkFirstTimeSync(localVersion = it) }

            if (dataHasNotBeenPopulated) {
                syncLocalData()
            }

            val startDestinationRoute = when {
                defaultListId != null -> GROCERY_LIST_CATEGORY_NESTED_NAV_ROUTE_WITH_ARGS
                else -> GROCERY_LISTS_DASHBOARD_ROUTE
            }

            uiStateFlow.update {
                MainActivityUiState(
                    startDestinationRoute = startDestinationRoute,
                    defaultListId = defaultListId
                )
            }
            userPreferencesFlow.collectLatest { userPreferences ->
                uiStateFlow.update { uiState ->
                    uiState?.copy(
                        darkThemeConfig = userPreferences.darkThemeConfig,
                        useSystemAccentColor = userPreferences.useSystemAccentColor,
                        selectedTheme = userPreferences.selectedTheme
                    )
                }
            }
        }
    }

    private suspend fun syncLocalData() {
        val synchronizer = object : Synchronizer {
            override suspend fun getChangeListVersions(): ChangeListVersions =
                changeListVersionsDataSource.getChangeListVersions()

            override suspend fun updateChangeListVersions(update: ChangeListVersions.() -> ChangeListVersions) {
                changeListVersionsDataSource.updateChangeListVersion(update)
            }
        }

        with(synchronizer) {
            iconRepository.sync()
            categoryRepository.sync()
            productRepository.sync()
        }
    }
}
