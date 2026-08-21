package com.rendox.shoppinggenius.feature.settings

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rendox.shoppinggenius.BuildConfig
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.feature.settings.categories.recyclerview.CategoriesRecyclerViewAdapter
import com.rendox.shoppinggenius.model.AppLanguage
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.DarkThemeConfig
import com.rendox.shoppinggenius.model.ShoppingGeniusColorScheme
import com.rendox.shoppinggenius.model.GroceryList
import com.rendox.shoppinggenius.model.UserPreferences
import com.rendox.shoppinggenius.ui.components.BottomSheetDragHandle
import com.rendox.shoppinggenius.ui.components.CustomIconSetting
import com.rendox.shoppinggenius.ui.components.DropDownMenuToggleIcon
import com.rendox.shoppinggenius.ui.components.LazyDropdownMenu
import com.rendox.shoppinggenius.ui.components.TonalDataInput
import com.rendox.shoppinggenius.ui.helpers.ObserveUiEvent
import com.rendox.shoppinggenius.ui.helpers.UiEvent
import com.rendox.shoppinggenius.ui.theme.ShoppingGeniusTheme
import com.rendox.shoppinggenius.ui.theme.TopAppBarSmallHeight
import com.rendox.shoppinggenius.ui.theme.deriveColorScheme
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    viewModel: SettingsScreenViewModel = hiltViewModel(),
    navigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val showDynamicColorNotSupportedMessage by viewModel.showDynamicColorNotSupportedMessage
        .collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val exportSuccessMsg = stringResource(R.string.settings_backup_export_success)
    val exportErrorMsg = stringResource(R.string.settings_backup_export_error)
    val importSuccessMsg = stringResource(R.string.settings_backup_import_success)
    val importErrorMsg = stringResource(R.string.settings_backup_import_error)

    ObserveUiEvent(uiState.backupMessageEvent) { key ->
        val message = when {
            key.startsWith("export_success") -> exportSuccessMsg
            key.startsWith("export_error") -> exportErrorMsg
            key.startsWith("import_success") -> importSuccessMsg
            else -> importErrorMsg
        }
        snackbarHostState.showSnackbar(message)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { viewModel.onIntent(SettingsScreenIntent.OnExportData(it)) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onIntent(SettingsScreenIntent.OnImportData(it)) }
    }

    SettingsScreen(
        modifier = Modifier.fillMaxSize(),
        uiState = uiState,
        showDynamicColorNotSupportedMessage = showDynamicColorNotSupportedMessage,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onExportClick = { exportLauncher.launch("ShoppingGenius_backup.zip") },
        onImportClick = { importLauncher.launch(arrayOf("application/zip", "*/*")) },
        navigateBack = navigateBack
    )
}

@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsScreenState,
    showDynamicColorNotSupportedMessage: UiEvent<Unit>?,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (SettingsScreenIntent) -> Unit,
    onExportClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    navigateBack: () -> Unit
) {
    var isThemeDropdownExpanded by remember { mutableStateOf(false) }
    var isLanguageDropdownExpanded by remember { mutableStateOf(false) }
    var isDefaultListDropdownExpanded by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    val dynamicColorNotSupportedMessage =
        stringResource(R.string.settings_dynamic_color_not_supported_message)
    ObserveUiEvent(showDynamicColorNotSupportedMessage) {
        snackbarHostState.showSnackbar(message = dynamicColorNotSupportedMessage)
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .systemBarsPadding()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TopAppBarSmallHeight),
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .align(Alignment.CenterStart),
                        onClick = navigateBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            }

            if (!uiState.isLoading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        SettingsTitle(
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                            title = stringResource(R.string.settings_theme)
                        )
                    }
                    item {
                        DarkThemeConfigSetting(
                            modifier = Modifier.padding(vertical = 16.dp),
                            darkThemeConfig = uiState.userPreferences.darkThemeConfig,
                            isThemeDropdownExpanded = isThemeDropdownExpanded,
                            onChangeDarkThemeConfig = {
                                onIntent(SettingsScreenIntent.ChangeDarkThemeConfig(it))
                            },
                            onThemeDropdownExpandedChanged = { isThemeDropdownExpanded = it }
                        )
                    }
                    item {
                        SystemAccentColorSetting(
                            useSystemAccentColor = uiState.userPreferences.useSystemAccentColor,
                            onUseSystemAccentColorChanged = {
                                onIntent(SettingsScreenIntent.ChangeUseSystemAccentColor(it))
                            }
                        )
                    }
                    item {
                        AnimatedVisibility(visible = !uiState.userPreferences.useSystemAccentColor) {
                            ColorSchemePicker(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 40.dp,
                                        end = 16.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    ),
                                useDarkTheme = when (uiState.userPreferences.darkThemeConfig) {
                                    DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                                    DarkThemeConfig.LIGHT -> false
                                    DarkThemeConfig.DARK -> true
                                },
                                selectedTheme = uiState.userPreferences.selectedTheme,
                                onSchemeSelected = {
                                    onIntent(SettingsScreenIntent.ChangeColorScheme(it))
                                }
                            )
                        }
                    }
                    item {
                        SettingsTitle(
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                            title = stringResource(R.string.settings_language)
                        )
                    }
                    item {
                        LanguageSetting(
                            modifier = Modifier.padding(vertical = 16.dp),
                            selectedLanguageTag = uiState.userPreferences.selectedLanguageTag,
                            isLanguageDropdownExpanded = isLanguageDropdownExpanded,
                            onChangeLanguage = { onIntent(SettingsScreenIntent.ChangeLanguage(it)) },
                            onLanguageDropdownExpandedChanged = {
                                isLanguageDropdownExpanded = it
                            }
                        )
                    }
                    item {
                        SettingsTitle(
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                            title = stringResource(R.string.settings_preferences)
                        )
                    }
                    item {
                        OpenLastViewedListSetting(
                            openLastViewedList = uiState.userPreferences.openLastViewedList,
                            onChangeOpenLastViewedListConfig = {
                                onIntent(SettingsScreenIntent.ChangeOpenLastViewedListConfig(it))
                            }
                        )
                    }
                    item {
                        GroceryListViewModeSetting(
                            useListViewForGroceries = uiState.userPreferences.useListViewForGroceries,
                            onUseListViewForGroceriesChanged = {
                                onIntent(SettingsScreenIntent.ChangeUseListViewForGroceries(it))
                            }
                        )
                    }
                    item {
                        WidgetBackgroundOpacitySetting(
                            opacityPercent = uiState.userPreferences.widgetBackgroundOpacityPercent,
                            onOpacityPercentChanged = {
                                onIntent(SettingsScreenIntent.ChangeWidgetBackgroundOpacityPercent(it))
                            }
                        )
                    }
                    item {
                        AutoDeleteCompletedSetting(
                            hours = uiState.userPreferences.autoDeleteCompletedAfterHours,
                            onHoursChanged = {
                                onIntent(SettingsScreenIntent.ChangeAutoDeleteCompletedAfterHours(it))
                            }
                        )
                    }
                    item {
                        AnimatedVisibility(visible = !uiState.userPreferences.openLastViewedList) {
                            DefaultListSetting(
                                groceryLists = uiState.groceryLists,
                                defaultListId = uiState.userPreferences.defaultListId,
                                isDefaultListDropdownExpanded = isDefaultListDropdownExpanded,
                                onChangeDefaultList = {
                                    onIntent(SettingsScreenIntent.OnChangeDefaultList(it))
                                },
                                onDefaultListDropdownExpandedChanged = {
                                    isDefaultListDropdownExpanded = it
                                }
                            )
                        }
                    }
                    item {
                        CategoriesOrderSetting(
                            categories = uiState.categories,
                            updateCategories = { categories ->
                                onIntent(SettingsScreenIntent.OnUpdateCategories(categories))
                            },
                            onResetCategoriesOrder = {
                                onIntent(SettingsScreenIntent.OnResetCategoriesOrder)
                            }
                        )
                    }
                    if (BuildConfig.DEBUG) {
                        item {
                            SettingsTitle(
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                                title = stringResource(R.string.settings_duckduckgo_image_search_title)
                            )
                        }
                        item {
                            DebugDuckDuckGoImageSearchSetting(
                                testInProgress = uiState.duckDuckGoImageSearchTestInProgress,
                                testSucceeded = uiState.duckDuckGoImageSearchTestSucceeded,
                                onTestConnection = {
                                    onIntent(SettingsScreenIntent.OnTestDuckDuckGoImageSearchConnection)
                                }
                            )
                        }
                    }
                    item {
                        SettingsTitle(
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                            title = stringResource(R.string.settings_backup_title)
                        )
                    }
                    item {
                        BackupExportSetting(
                            inProgress = uiState.exportInProgress,
                            onClick = onExportClick
                        )
                    }
                    item {
                        BackupImportSetting(
                            inProgress = uiState.importInProgress,
                            onClick = onImportClick
                        )
                    }
                    item {
                        SettingsTitle(
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                            title = stringResource(R.string.settings_credits)
                        )
                    }
                    item {
                        GitHubLink()
                    }
                    item {
                        FreepikAttribution()
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugDuckDuckGoImageSearchSetting(
    modifier: Modifier = Modifier,
    testInProgress: Boolean,
    testSucceeded: Boolean?,
    onTestConnection: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_duckduckgo_image_search_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            modifier = Modifier.padding(top = 8.dp),
            onClick = onTestConnection,
            enabled = !testInProgress
        ) {
            Text(
                text = if (testInProgress) {
                    stringResource(R.string.settings_duckduckgo_connection_testing)
                } else {
                    stringResource(R.string.settings_duckduckgo_connection_test_button)
                }
            )
        }
        if (testSucceeded != null) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = if (testSucceeded) {
                    stringResource(R.string.settings_duckduckgo_connection_test_success)
                } else {
                    stringResource(R.string.settings_duckduckgo_connection_test_failed)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (testSucceeded) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
fun SettingsTitle(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier,
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun DarkThemeConfigSetting(
    modifier: Modifier = Modifier,
    darkThemeConfig: DarkThemeConfig,
    isThemeDropdownExpanded: Boolean,
    onChangeDarkThemeConfig: (DarkThemeConfig) -> Unit,
    onThemeDropdownExpandedChanged: (Boolean) -> Unit
) {
    CustomIconSetting(
        modifier = modifier,
        title = stringResource(R.string.settings_theme_mode),
        icon = {
            Icon(
                painterResource(R.drawable.day_night),
                contentDescription = null
            )
        },
        trailingComponent = {
            val themeModes = DarkThemeConfig.entries.map { it.asLocalString() }
            val selectedOptionIndex = remember(darkThemeConfig) {
                DarkThemeConfig.entries.indexOf(darkThemeConfig)
            }
            TonalDataInput(
                onClick = { onThemeDropdownExpandedChanged(!isThemeDropdownExpanded) },
                indication = null,
                dropDownMenu = {
                    DropdownMenu(
                        expanded = isThemeDropdownExpanded,
                        onDismissRequest = { onThemeDropdownExpandedChanged(false) }
                    ) {
                        themeModes.forEachIndexed { index, themeMode ->
                            DropdownMenuItem(
                                onClick = {
                                    onChangeDarkThemeConfig(DarkThemeConfig.entries[index])
                                    onThemeDropdownExpandedChanged(false)
                                },
                                text = {
                                    Text(text = themeMode)
                                }
                            )
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.widthIn(min = 56.dp, max = 136.dp),
                        text = themeModes[selectedOptionIndex],
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    DropDownMenuToggleIcon(expanded = isThemeDropdownExpanded)
                }
            }
        }
    )
}

@Composable
fun LanguageSetting(
    modifier: Modifier = Modifier,
    selectedLanguageTag: String?,
    isLanguageDropdownExpanded: Boolean,
    onChangeLanguage: (String?) -> Unit,
    onLanguageDropdownExpandedChanged: (Boolean) -> Unit
) {
    val languageOptions = remember {
        listOf<String?>(null) + AppLanguage.supportedLanguageTags
    }
    val languageLabels = languageOptions.map { languageTag ->
        when (languageTag) {
            null -> stringResource(R.string.settings_language_system_default)
            else -> AppLanguage.displayName(languageTag)
        }
    }
    val selectedOptionIndex = remember(selectedLanguageTag, languageOptions) {
        languageOptions.indexOf(selectedLanguageTag).let { index -> if (index >= 0) index else 0 }
    }

    CustomIconSetting(
        modifier = modifier,
        title = stringResource(R.string.settings_language),
        icon = {
            Icon(
                painterResource(R.drawable.baseline_folder_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            TonalDataInput(
                onClick = { onLanguageDropdownExpandedChanged(!isLanguageDropdownExpanded) },
                indication = null,
                dropDownMenu = {
                    LazyDropdownMenu(
                        expanded = isLanguageDropdownExpanded,
                        onDismissRequest = { onLanguageDropdownExpandedChanged(false) },
                        options = languageLabels,
                        onOptionSelected = { index ->
                            onChangeLanguage(languageOptions[index])
                            onLanguageDropdownExpandedChanged(false)
                        }
                    )
                }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.widthIn(min = 56.dp, max = 136.dp),
                        text = languageLabels[selectedOptionIndex],
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    DropDownMenuToggleIcon(expanded = isLanguageDropdownExpanded)
                }
            }
        }
    )
}

@Composable
private fun SystemAccentColorSetting(
    modifier: Modifier = Modifier,
    useSystemAccentColor: Boolean,
    onUseSystemAccentColorChanged: (Boolean) -> Unit
) {
    CustomIconSetting(
        modifier = modifier.clickable {
            onUseSystemAccentColorChanged(!useSystemAccentColor)
        },
        title = stringResource(R.string.settings_use_system_accent_color),
        icon = {
            Icon(
                painterResource(R.drawable.baseline_palette_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            Switch(
                checked = useSystemAccentColor,
                onCheckedChange = onUseSystemAccentColorChanged
            )
        }
    )
}

@Composable
fun ColorSchemePicker(
    modifier: Modifier = Modifier,
    useDarkTheme: Boolean,
    selectedTheme: ShoppingGeniusColorScheme,
    onSchemeSelected: (ShoppingGeniusColorScheme) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = SpaceEvenly
    ) {
        for (scheme in ShoppingGeniusColorScheme.entries) {
            val colors = scheme.deriveColorScheme(useDarkTheme)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color = colors.primaryContainer)
                    .clickable { onSchemeSelected(scheme) },
                contentAlignment = Alignment.Center
            ) {
                if (scheme == selectedTheme) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenLastViewedListSetting(
    modifier: Modifier = Modifier,
    openLastViewedList: Boolean,
    onChangeOpenLastViewedListConfig: (Boolean) -> Unit
) {
    CustomIconSetting(
        modifier = modifier
            .padding(vertical = 6.dp)
            .clickable { onChangeOpenLastViewedListConfig(!openLastViewedList) },
        title = stringResource(R.string.settings_open_last_viewed_list),
        icon = {
            Icon(
                painterResource(id = R.drawable.baseline_history_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            Switch(
                checked = openLastViewedList,
                onCheckedChange = onChangeOpenLastViewedListConfig
            )
        }
    )
}

@Composable
private fun GroceryListViewModeSetting(
    modifier: Modifier = Modifier,
    useListViewForGroceries: Boolean,
    onUseListViewForGroceriesChanged: (Boolean) -> Unit
) {
    CustomIconSetting(
        modifier = modifier
            .padding(vertical = 6.dp)
            .clickable { onUseListViewForGroceriesChanged(!useListViewForGroceries) },
        title = stringResource(R.string.settings_grocery_list_list_mode_title),
        description = stringResource(R.string.settings_grocery_list_list_mode_description),
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_history_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            Switch(
                checked = useListViewForGroceries,
                onCheckedChange = onUseListViewForGroceriesChanged
            )
        }
    )
}

@Composable
private fun WidgetBackgroundOpacitySetting(
    modifier: Modifier = Modifier,
    opacityPercent: Int,
    onOpacityPercentChanged: (Int) -> Unit
) {
    var sliderValue by remember(opacityPercent) { mutableStateOf(opacityPercent.toFloat()) }

    CustomIconSetting(
        modifier = modifier.padding(vertical = 6.dp),
        title = stringResource(R.string.settings_widget_background_opacity_title),
        description = stringResource(
            R.string.settings_widget_background_opacity_description,
            opacityPercent
        ),
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_palette_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            Text(
                text = "$opacityPercent%",
                style = MaterialTheme.typography.labelMedium
            )
        }
    )

    Slider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 16.dp, bottom = 8.dp),
        value = sliderValue,
        onValueChange = { sliderValue = it },
        valueRange = 0f..100f,
        onValueChangeFinished = {
            onOpacityPercentChanged(sliderValue.toInt().coerceIn(0, 100))
        }
    )
}

@Composable
private fun AutoDeleteCompletedSetting(
    modifier: Modifier = Modifier,
    hours: Int,
    onHoursChanged: (Int) -> Unit
) {
    var sliderValue by remember(hours) { mutableStateOf(hours.toFloat()) }

    CustomIconSetting(
        modifier = modifier.padding(vertical = 6.dp),
        title = stringResource(R.string.settings_auto_delete_completed_title),
        description = stringResource(
            R.string.settings_auto_delete_completed_description,
            hours
        ),
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_history_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            Text(
                text = stringResource(R.string.settings_auto_delete_completed_hours, hours),
                style = MaterialTheme.typography.labelMedium
            )
        }
    )

    Slider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 16.dp, bottom = 8.dp),
        value = sliderValue,
        onValueChange = { sliderValue = it },
        valueRange = 1f..120f,
        steps = 118,
        onValueChangeFinished = {
            onHoursChanged(sliderValue.toInt().coerceIn(1, 120))
        }
    )
}

@Composable
fun DefaultListSetting(
    modifier: Modifier = Modifier,
    groceryLists: List<GroceryList>,
    defaultListId: String? = null,
    isDefaultListDropdownExpanded: Boolean,
    onChangeDefaultList: (String?) -> Unit,
    onDefaultListDropdownExpandedChanged: (Boolean) -> Unit
) {
    CustomIconSetting(
        modifier = modifier.padding(vertical = 6.dp),
        title = stringResource(R.string.settings_default_list),
        icon = {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null
            )
        },
        trailingComponent = {
            val unspecifiedListTitle = stringResource(R.string.settings_default_list_unspecified)
            val options = listOf(unspecifiedListTitle) + groceryLists.map { it.name }
            TonalDataInput(
                onClick = {
                    onDefaultListDropdownExpandedChanged(!isDefaultListDropdownExpanded)
                },
                indication = null,
                dropDownMenu = {
                    when {
                        options.isEmpty() -> {}
                        options.size <= 6 -> {
                            DropdownMenu(
                                expanded = isDefaultListDropdownExpanded,
                                onDismissRequest = { onDefaultListDropdownExpandedChanged(false) }
                            ) {
                                options.forEachIndexed { index, option ->
                                    DropdownMenuItem(
                                        onClick = {
                                            onChangeDefaultList(
                                                if (index == 0) null else groceryLists[index - 1].id
                                            )
                                            onDefaultListDropdownExpandedChanged(false)
                                        },
                                        text = {
                                            Text(text = option)
                                        }
                                    )
                                }
                            }
                        }

                        else -> {
                            LazyDropdownMenu(
                                expanded = isDefaultListDropdownExpanded,
                                onDismissRequest = { onDefaultListDropdownExpandedChanged(false) },
                                options = options,
                                onOptionSelected = { index ->
                                    val groceryListId =
                                        if (index == 0) null else groceryLists[index - 1].id
                                    groceryListId?.let(onChangeDefaultList)
                                    onDefaultListDropdownExpandedChanged(false)
                                }
                            )
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.widthIn(min = 56.dp, max = 136.dp),
                        text = remember(groceryLists, defaultListId) {
                            groceryLists.find {
                                it.id == defaultListId
                            }?.name ?: unspecifiedListTitle
                        },
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    DropDownMenuToggleIcon(expanded = isDefaultListDropdownExpanded)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriesOrderSetting(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    updateCategories: (List<Category>) -> Unit,
    onResetCategoriesOrder: () -> Unit
) {
    var bottomSheetIsVisible by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val hideBottomSheet = {
        coroutineScope
            .launch { bottomSheetState.hide() }
            .invokeOnCompletion { bottomSheetIsVisible = false }
    }

    CustomIconSetting(
        modifier = modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .clickable { bottomSheetIsVisible = true },
        title = stringResource(R.string.settings_reorder_categories_title),
        icon = {
            Icon(
                painterResource(id = R.drawable.baseline_swap_vert_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            TonalDataInput(onClick = onResetCategoriesOrder) {
                Text(
                    modifier = Modifier
                        .widthIn(min = 114.dp)
                        .padding(10.dp),
                    text = stringResource(R.string.reset),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    )

    if (bottomSheetIsVisible) {
        ModalBottomSheet(
            onDismissRequest = { hideBottomSheet() },
            sheetState = bottomSheetState,
            dragHandle = { BottomSheetDragHandle() },
            windowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    text = stringResource(R.string.settings_reorder_categories_description),
                    style = MaterialTheme.typography.labelLarge
                )
                AndroidView(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                    factory = { context ->
                        RecyclerView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            layoutManager = LinearLayoutManager(context)
                            this.adapter = CategoriesRecyclerViewAdapter(
                                recyclerView = this,
                                categories = categories,
                                updateLists = updateCategories
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BackupExportSetting(
    modifier: Modifier = Modifier,
    inProgress: Boolean,
    onClick: () -> Unit
) {
    CustomIconSetting(
        modifier = modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .clickable(enabled = !inProgress, onClick = onClick),
        title = stringResource(R.string.settings_backup_export_title),
        description = stringResource(R.string.settings_backup_export_description),
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_folder_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            if (inProgress) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    )
}

@Composable
private fun BackupImportSetting(
    modifier: Modifier = Modifier,
    inProgress: Boolean,
    onClick: () -> Unit
) {
    CustomIconSetting(
        modifier = modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .clickable(enabled = !inProgress, onClick = onClick),
        title = stringResource(R.string.settings_backup_import_title),
        description = stringResource(R.string.settings_backup_import_description),
        icon = {
            Icon(
                painter = painterResource(R.drawable.baseline_folder_24),
                contentDescription = null
            )
        },
        trailingComponent = {
            if (inProgress) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
    )
}

@Composable
private fun GitHubLink(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    CustomIconSetting(
        modifier = modifier
            .padding(top = 16.dp)
            .clickable { uriHandler.openUri("https://github.com/DanielRendox/ShoppingGenius") },
        title = stringResource(R.string.github_link_title),
        description = stringResource(R.string.github_link_description),
        icon = {
            Icon(
                painter = painterResource(R.drawable.github_mark),
                contentDescription = null
            )
        }
    )
}

@Composable
private fun EmailLink(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val title = stringResource(R.string.email_link_title)
    val developerEmail = "daniel.rendox@gmail.com"
    CustomIconSetting(
        modifier = modifier
            .padding(top = 16.dp)
            .clickable {
                val intent = Intent(
                    Intent.ACTION_SENDTO,
                    "mailto:$developerEmail".toUri()
                )
                context.startActivity(Intent.createChooser(intent, title))
            },
        title = title,
        description = stringResource(R.string.email_link_description),
        icon = {
            Icon(
                painterResource(R.drawable.mail),
                contentDescription = null
            )
        }
    )
}

private const val FREEPIK_ATTRIBUTION_LINK = "https://www.freepik.com/free-vector/" +
    "tiny-family-grocery-bag-with-healthy-food-parents-kids-fresh-vegetables-flat-illustration_12291304.htm"

@Composable
private fun FreepikAttribution(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    CustomIconSetting(
        modifier = modifier
            .padding(top = 16.dp)
            .clickable {
                uriHandler.openUri(FREEPIK_ATTRIBUTION_LINK)
            },
        title = stringResource(R.string.settings_image_by_freepik_title),
        description = stringResource(R.string.settings_image_by_freepik_description),
        icon = {
            Icon(
                painterResource(R.drawable.image_icon),
                contentDescription = null
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    ShoppingGeniusTheme {
        Surface {
            SettingsScreen(
                uiState = remember {
                    SettingsScreenState(
                        userPreferences = UserPreferences(
                            useSystemAccentColor = false,
                            openLastViewedList = false,
                            selectedTheme = ShoppingGeniusColorScheme.YellowColorScheme
                        ),
                        isLoading = false
                    )
                },
                onIntent = {},
                onExportClick = {},
                onImportClick = {},
                navigateBack = {},
                showDynamicColorNotSupportedMessage = null
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ColorSchemePickerPreview() {
    ShoppingGeniusTheme {
        Surface(modifier = Modifier.width(400.dp)) {
            ColorSchemePicker(
                modifier = Modifier.padding(16.dp),
                selectedTheme = ShoppingGeniusColorScheme.PurpleColorScheme,
                onSchemeSelected = {},
                useDarkTheme = isSystemInDarkTheme()
            )
        }
    }
}

@Composable
private fun DarkThemeConfig.asLocalString() = when (this) {
    DarkThemeConfig.FOLLOW_SYSTEM ->
        stringResource(R.string.settings_dark_theme_config_system_default)

    DarkThemeConfig.LIGHT ->
        stringResource(R.string.settings_dark_theme_config_light)

    DarkThemeConfig.DARK ->
        stringResource(R.string.settings_dark_theme_config_dark)
}
