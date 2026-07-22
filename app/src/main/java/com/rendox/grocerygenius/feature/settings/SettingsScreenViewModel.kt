package com.rendox.grocerygenius.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.grocerygenius.backup.BackupRestoreManager
import com.rendox.grocerygenius.data.Synchronizer
import com.rendox.grocerygenius.data.category.CategoryRepository
import com.rendox.grocerygenius.data.grocerylist.GroceryListRepository
import com.rendox.grocerygenius.data.icons.IconRepository
import com.rendox.grocerygenius.data.product.ProductRepository
import com.rendox.grocerygenius.data.userpreferences.UserPreferencesRepository
import com.rendox.grocerygenius.datastore.ChangeListVersionsDataSource
import com.rendox.grocerygenius.feature.iconpicker.DuckDuckGoImageSearchService
import com.rendox.grocerygenius.locale.AppLocaleManager
import com.rendox.grocerygenius.model.ChangeListVersions
import com.rendox.grocerygenius.ui.helpers.UiEvent
import com.rendox.grocerygenius.ui.theme.dynamicColorIsSupported
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val changeListVersionsDataSource: ChangeListVersionsDataSource,
    private val appLocaleManager: AppLocaleManager,
    groceryListRepository: GroceryListRepository,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val iconRepository: IconRepository,
    private val duckDuckGoImageSearchService: DuckDuckGoImageSearchService,
    private val backupRestoreManager: BackupRestoreManager
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(SettingsScreenState())
    val uiStateFlow = _uiStateFlow.asStateFlow()

    private val _showDynamicColorNotSupportedMessage = MutableStateFlow<UiEvent<Unit>?>(null)
    val showDynamicColorNotSupportedMessage = _showDynamicColorNotSupportedMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _uiStateFlow.update {
                SettingsScreenState(
                    groceryLists = groceryListRepository.getAllGroceryLists().first(),
                    categories = categoryRepository.getAllCategories()
                        .map { categories -> categories.sortedBy { it.sortingPriority } }
                        .first()
                )
            }
            userPreferencesRepository.userPreferencesFlow.collectLatest { userPreferences ->
                val useSystemAccentColor =
                    if (dynamicColorIsSupported) userPreferences.useSystemAccentColor else false
                _uiStateFlow.update { uiState ->
                    uiState.copy(
                        userPreferences = userPreferences.copy(useSystemAccentColor = useSystemAccentColor),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsScreenIntent) = viewModelScope.launch {
        when (intent) {
            is SettingsScreenIntent.ChangeDarkThemeConfig ->
                userPreferencesRepository.updateDarkThemeConfig(intent.config)

            is SettingsScreenIntent.OnChangeDefaultList ->
                userPreferencesRepository.updateDefaultListId(intent.listId)

            is SettingsScreenIntent.ChangeUseSystemAccentColor -> {
                if (dynamicColorIsSupported) {
                    userPreferencesRepository.updateUseSystemAccentColor(intent.use)
                } else {
                    _showDynamicColorNotSupportedMessage.update {
                        object : UiEvent<Unit> {
                            override val data = Unit
                            override fun onConsumed() {
                                _showDynamicColorNotSupportedMessage.update { null }
                            }
                        }
                    }
                }
            }

            is SettingsScreenIntent.ChangeLanguage -> {
                _uiStateFlow.update { it.copy(isLoading = true) }
                appLocaleManager.applyLanguageTag(intent.languageTag)
                userPreferencesRepository.updateSelectedLanguageTag(intent.languageTag)
                changeListVersionsDataSource.updateChangeListVersion { ChangeListVersions() }
                syncLocalizedData()
                _uiStateFlow.update { it.copy(isLoading = false) }
            }

            is SettingsScreenIntent.ChangeColorScheme ->
                userPreferencesRepository.updateSelectedTheme(intent.scheme)

            is SettingsScreenIntent.OnTestDuckDuckGoImageSearchConnection -> {
                _uiStateFlow.update {
                    it.copy(
                        duckDuckGoImageSearchTestInProgress = true,
                        duckDuckGoImageSearchTestSucceeded = null
                    )
                }
                val testSucceeded = duckDuckGoImageSearchService.testConnection()
                _uiStateFlow.update {
                    it.copy(
                        duckDuckGoImageSearchTestInProgress = false,
                        duckDuckGoImageSearchTestSucceeded = testSucceeded
                    )
                }
            }

            is SettingsScreenIntent.ChangeOpenLastViewedListConfig -> {
                userPreferencesRepository.updateOpenLastViewedList(intent.openLastViewedList)
                if (!intent.openLastViewedList) {
                    userPreferencesRepository.updateDefaultListId(null)
                }
            }

            is SettingsScreenIntent.ChangeUseListViewForGroceries ->
                userPreferencesRepository.updateUseListViewForGroceries(intent.useListViewForGroceries)

            is SettingsScreenIntent.ChangeWidgetBackgroundOpacityPercent ->
                userPreferencesRepository.updateWidgetBackgroundOpacityPercent(intent.opacityPercent)

            is SettingsScreenIntent.ChangeAutoDeleteCompletedAfterHours ->
                userPreferencesRepository.updateAutoDeleteCompletedAfterHours(intent.hours)

            is SettingsScreenIntent.OnUpdateCategories -> {
                _uiStateFlow.update { it.copy(categories = intent.categories) }
                categoryRepository.updateCategories(
                    categories = intent.categories.mapIndexed { index, category ->
                        category.copy(sortingPriority = index.toLong())
                    }
                )
            }

            is SettingsScreenIntent.OnResetCategoriesOrder -> {
                val newCategories = _uiStateFlow.value.categories
                    .map { it.copy(sortingPriority = it.defaultSortingPriority) }
                    .sortedBy { it.sortingPriority }
                _uiStateFlow.update { it.copy(categories = newCategories) }
                categoryRepository.updateCategories(newCategories)
            }

            is SettingsScreenIntent.OnExportData -> {
                _uiStateFlow.update { it.copy(exportInProgress = true) }
                val result = backupRestoreManager.exportBackup(intent.uri)
                _uiStateFlow.update { state ->
                    state.copy(
                        exportInProgress = false,
                        backupMessageEvent = result.fold(
                            onSuccess = { makeEvent("export_success") },
                            onFailure = { makeEvent("export_error:${it.message}") }
                        )
                    )
                }
            }

            is SettingsScreenIntent.OnImportData -> {
                _uiStateFlow.update { it.copy(importInProgress = true) }
                val result = backupRestoreManager.importBackup(intent.uri)
                _uiStateFlow.update { state ->
                    state.copy(
                        importInProgress = false,
                        backupMessageEvent = result.fold(
                            onSuccess = { makeEvent("import_success") },
                            onFailure = { makeEvent("import_error:${it.message}") }
                        )
                    )
                }
            }
        }
    }

    private fun makeEvent(key: String): UiEvent<String> = object : UiEvent<String> {
        override val data = key
        override fun onConsumed() {
            _uiStateFlow.update { it.copy(backupMessageEvent = null) }
        }
    }

    private suspend fun syncLocalizedData() {
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