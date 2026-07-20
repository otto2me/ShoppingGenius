package com.rendox.grocerygenius

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.grocerygenius.data.Synchronizer
import com.rendox.grocerygenius.data.category.CategoryRepository
import com.rendox.grocerygenius.data.checkFirstTimeSync
import com.rendox.grocerygenius.data.icons.IconRepository
import com.rendox.grocerygenius.data.product.ProductRepository
import com.rendox.grocerygenius.data.userpreferences.UserPreferencesRepository
import com.rendox.grocerygenius.datastore.ChangeListVersionsDataSource
import com.rendox.grocerygenius.feature.dashboardscreen.GROCERY_LISTS_DASHBOARD_ROUTE
import com.rendox.grocerygenius.feature.grocerylist.GROCERY_LIST_CATEGORY_NESTED_NAV_ROUTE_WITH_ARGS
import com.rendox.grocerygenius.model.ChangeListVersions
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