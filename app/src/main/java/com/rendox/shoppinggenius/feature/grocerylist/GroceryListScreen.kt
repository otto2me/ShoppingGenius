package com.rendox.shoppinggenius.feature.grocerylist

import androidx.activity.compose.BackHandler
import android.content.Intent
import android.content.ClipData
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.feature.addgrocery.AddGroceryBottomSheetContent
import com.rendox.shoppinggenius.feature.addgrocery.AddGroceryBottomSheetState
import com.rendox.shoppinggenius.feature.addgrocery.AddGroceryUiIntent
import com.rendox.shoppinggenius.feature.addgrocery.AddGroceryUiState
import com.rendox.shoppinggenius.feature.addgrocery.AddGroceryViewModel
import com.rendox.shoppinggenius.feature.addgrocery.rememberAddGroceryBottomSheetState
import com.rendox.shoppinggenius.feature.editgrocery.EditGroceryBottomSheet
import com.rendox.shoppinggenius.feature.editgrocery.EditGroceryUiIntent
import com.rendox.shoppinggenius.feature.editgrocery.EditGroceryViewModel
import com.rendox.shoppinggenius.feature.share.GroceryListShareManager
import com.rendox.shoppinggenius.feature.share.SHARE_FILE_MIME_TYPE
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.Grocery
import com.rendox.shoppinggenius.model.ShoppingGeniusColorScheme
import com.rendox.shoppinggenius.ui.components.BottomSheetDragHandle
import com.rendox.shoppinggenius.ui.components.DeleteConfirmationDialog
import com.rendox.shoppinggenius.ui.components.GroceryIcon
import com.rendox.shoppinggenius.ui.components.Scrim
import com.rendox.shoppinggenius.ui.components.grocerylist.GroceryGridItem
import com.rendox.shoppinggenius.ui.components.grocerylist.GroceryGroup
import com.rendox.shoppinggenius.ui.components.grocerylist.GroupedLazyGroceryGrid
import com.rendox.shoppinggenius.ui.components.grocerylist.LazyGroceryGrid
import com.rendox.shoppinggenius.ui.components.grocerylist.groceryListItemColors
import com.rendox.shoppinggenius.ui.helpers.ObserveUiEvent
import com.rendox.shoppinggenius.ui.helpers.UiEvent
import com.rendox.shoppinggenius.ui.theme.ShoppingGeniusTheme
import com.rendox.shoppinggenius.ui.theme.GroceryItemRounding
import com.rendox.shoppinggenius.ui.theme.TopAppBarSmallHeight
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListRoute(
    groceryListViewModel: GroceryListViewModel = hiltViewModel(),
    addGroceryViewModel: AddGroceryViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToCategoryScreen: () -> Unit,
    navigateToIconPicker: (String, String) -> Unit
) {
    val groceryGroups by groceryListViewModel.groceryGroupsFlow.collectAsStateWithLifecycle()
    val closeGroceryListScreenEvent by groceryListViewModel.closeGroceryListScreenEvent.collectAsStateWithLifecycle()
    val addGroceryUiState by addGroceryViewModel.uiStateFlow.collectAsStateWithLifecycle()
    val openedGroceryListId = groceryListViewModel.openedGroceryListId
    val groceryListEditModeIsEnabled by groceryListViewModel.groceryListEditModeIsEnabledFlow
        .collectAsStateWithLifecycle()
    val categories by groceryListViewModel.categoriesFlow.collectAsStateWithLifecycle()
    val navigateToCategoryScreenEvent by groceryListViewModel.navigateToCategoryScreenEvent
        .collectAsStateWithLifecycle()
    val groceryListPurchaseState by groceryListViewModel.groceryListPurchaseStateFlow.collectAsStateWithLifecycle()
    val scrollUpEvent by groceryListViewModel.scrollUpEventFlow.collectAsStateWithLifecycle()
    val useListViewForGroceries by groceryListViewModel.useListViewForGroceriesFlow.collectAsStateWithLifecycle()
    val favoriteGroceries by groceryListViewModel.favoriteGroceriesFlow.collectAsStateWithLifecycle()
    val shareListTextEvent by groceryListViewModel.shareListTextEventFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingShareContent by remember {
        mutableStateOf<GroceryListShareManager.ShareContent?>(null)
    }

    ObserveUiEvent(closeGroceryListScreenEvent) {
        navigateBack()
    }
    val navigateToCategoryScreenSafely = dropUnlessResumed {
        navigateToCategoryScreen()
    }
    ObserveUiEvent(navigateToCategoryScreenEvent) {
        navigateToCategoryScreenSafely()
    }
    ObserveUiEvent(shareListTextEvent) { shareText ->
        pendingShareContent = shareText
    }

    pendingShareContent?.let { shareContent ->
        AlertDialog(
            onDismissRequest = { pendingShareContent = null },
            title = { Text(text = stringResource(R.string.share_grocery_list_title)) },
            text = { Text(text = stringResource(R.string.share_grocery_list_mode_description)) },
            confirmButton = {
                TextButton(onClick = {
                    val shareDir = File(context.cacheDir, "shared_lists").apply { mkdirs() }
                    val shareFile = File(shareDir, shareContent.fileName)
                    shareFile.writeText(shareContent.fileContent, Charsets.UTF_8)
                    val shareFileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        shareFile
                    )
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = SHARE_FILE_MIME_TYPE
                        putExtra(Intent.EXTRA_TEXT, shareContent.text)
                        putExtra(Intent.EXTRA_SUBJECT, shareContent.fileName)
                        putExtra(Intent.EXTRA_STREAM, shareFileUri)
                        clipData = ClipData.newUri(
                            context.contentResolver,
                            shareContent.fileName,
                            shareFileUri
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            sendIntent,
                            context.getString(R.string.share_grocery_list_with_file)
                        )
                    )
                    pendingShareContent = null
                }) {
                    Text(text = stringResource(R.string.share_grocery_list_with_file))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val textIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareContent.text)
                            putExtra(Intent.EXTRA_SUBJECT, shareContent.fileName)
                        }
                        context.startActivity(
                            Intent.createChooser(
                                textIntent,
                                context.getString(R.string.share_grocery_list_text_only)
                            )
                        )
                        pendingShareContent = null
                    }) {
                        Text(text = stringResource(R.string.share_grocery_list_text_only))
                    }
                    TextButton(onClick = { pendingShareContent = null }) {
                        Text(text = stringResource(R.string.close))
                    }
                }
            }
        )
    }

    val addBottomSheetState = rememberStandardBottomSheetState()
    val scaffoldState = rememberBottomSheetScaffoldState(addBottomSheetState)
    val addGroceryBottomSheetState = rememberAddGroceryBottomSheetState(addBottomSheetState)

    LaunchedEffect(addGroceryBottomSheetState.sheetIsCollapsing) {
        if (addGroceryBottomSheetState.sheetIsCollapsing) {
            addGroceryViewModel.onIntent(AddGroceryUiIntent.OnAddGroceryBottomSheetCollapsing)
        }
    }
    LaunchedEffect(addGroceryBottomSheetState.sheetIsFullyExpanded, openedGroceryListId) {
        if (addGroceryBottomSheetState.sheetIsFullyExpanded) {
            addGroceryViewModel.onIntent(
                AddGroceryUiIntent.OnAddGroceryBottomSheetExpanded(openedGroceryListId)
            )
        }
    }

    val editBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editGroceryScreenIsVisible by rememberSaveable { mutableStateOf(false) }
    val editGroceryIdState = rememberSaveable { mutableStateOf<String?>(null) }

    GroceryListScreen(
        modifier = Modifier.fillMaxSize(),
        groceryGroups = groceryGroups,
        searchQuery = addGroceryViewModel.searchQuery,
        onGroceryListUiIntent = groceryListViewModel::onIntent,
        scaffoldState = scaffoldState,
        addGroceryBottomSheetState = addGroceryBottomSheetState,
        showEditGroceryBottomSheet = {
            editGroceryIdState.value = it
            editGroceryScreenIsVisible = true
        },
        scrollUpEvent = scrollUpEvent,
        scrimIsShown = addGroceryBottomSheetState.sheetIsExpanding ||
            editBottomSheetState.targetValue == SheetValue.Expanded,
        toolbarIsHidden = addGroceryBottomSheetState.sheetIsExpanding ||
            editBottomSheetState.targetValue == SheetValue.Expanded,
        groceryListName = groceryListViewModel.openedGroceryListName,
        navigateBack = navigateBack,
        onAddGroceryUiIntent = addGroceryViewModel::onIntent,
        addGroceryUiState = addGroceryUiState,
        groceryListEditModeIsEnabled = groceryListEditModeIsEnabled,
        categories = categories,
        navigateToCategoryScreen = { categoryId ->
            groceryListViewModel.onIntent(
                GroceryListsUiIntent.OnNavigateToCategoryScreen(categoryId)
            )
        },
        groceryListPurchaseState = groceryListPurchaseState,
        useListViewForGroceries = useListViewForGroceries
        ,
        favoriteGroceries = favoriteGroceries,
        onFavoriteGroceryClick = groceryListViewModel::onCategoryScreenGroceryClick
    )

    val editGroceryId = editGroceryIdState.value
    if (editGroceryScreenIsVisible && editGroceryId != null) {
        val editGroceryViewModel: EditGroceryViewModel = hiltViewModel()
        val editGroceryScreenState by editGroceryViewModel.uiStateFlow.collectAsStateWithLifecycle()

        LaunchedEffect(editGroceryIdState, openedGroceryListId) {
            editGroceryViewModel.onIntent(
                EditGroceryUiIntent.OnEditOtherGrocery(
                    productId = editGroceryId,
                    groceryListId = openedGroceryListId
                )
            )
        }

        EditGroceryBottomSheet(
            modifier = Modifier.fillMaxSize(),
            screenState = editGroceryScreenState,
            editGroceryDescription = editGroceryViewModel.editGroceryDescription,
            editBottomSheetState = editBottomSheetState,
            hideBottomSheetOnCompletion = {
                editGroceryScreenIsVisible = false
            },
            onIntent = editGroceryViewModel::onIntent,
            navigateToIconPicker = { groceryId ->
                navigateToIconPicker(groceryId, openedGroceryListId)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GroceryListScreen(
    modifier: Modifier = Modifier,
    groceryGroups: List<GroceryGroup>?,
    searchQuery: String,
    addGroceryUiState: AddGroceryUiState,
    groceryListName: TextFieldValue? = TextFieldValue(""),
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    addGroceryBottomSheetState: AddGroceryBottomSheetState = rememberAddGroceryBottomSheetState(),
    groceryListEditModeIsEnabled: Boolean = false,
    scrimIsShown: Boolean = false,
    toolbarIsHidden: Boolean = false,
    categories: List<Category>? = emptyList(),
    groceryListPurchaseState: GroceryListPurchaseState,
    useListViewForGroceries: Boolean = false,
    favoriteGroceries: List<Grocery> = emptyList(),
    scrollUpEvent: UiEvent<Unit>? = null,
    navigateBack: () -> Unit = {},
    onGroceryListUiIntent: (GroceryListsUiIntent) -> Unit = {},
    onAddGroceryUiIntent: (AddGroceryUiIntent) -> Unit = {},
    showEditGroceryBottomSheet: (String) -> Unit = {},
    navigateToCategoryScreen: (String?) -> Unit = {},
    onFavoriteGroceryClick: (Grocery) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(GroceryListTab.ITEMS) }

    val imeIsVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeIsVisible) {
        if (!imeIsVisible) onGroceryListUiIntent(GroceryListsUiIntent.OnKeyboardHidden)
    }

    var deleteGroceryListDialogIsShown by remember { mutableStateOf(false) }
    if (deleteGroceryListDialogIsShown) {
        DeleteConfirmationDialog(
            bodyText = stringResource(id = R.string.delete_grocery_list_dialog_text),
            onConfirm = {
                deleteGroceryListDialogIsShown = false
                onGroceryListUiIntent(GroceryListsUiIntent.OnDeleteGroceryList)
            },
            onDismissRequest = { deleteGroceryListDialogIsShown = false }
        )
    }

    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    BottomSheetScaffold(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
            .consumeWindowInsets(WindowInsets.navigationBars),
        sheetContent = {
            BackHandler(
                enabled = addGroceryBottomSheetState.handleBackButtonPress,
                onBack = { addGroceryBottomSheetState.onSheetDismissed() }
            )
            AddGroceryBottomSheetContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(LocalConfiguration.current.screenHeightDp * 0.75F.dp)
                    .navigationBarsPadding(),
                searchQuery = searchQuery,
                contentType = addGroceryUiState.bottomSheetContentType,
                clearSearchQueryButtonIsShown = addGroceryUiState.clearSearchQueryButtonIsShown,
                customProducts = addGroceryUiState.customProducts,
                grocerySearchResults = addGroceryUiState.grocerySearchResults,
                previousGrocery = addGroceryUiState.previouslyAddedGrocery,
                showExtendedContent = addGroceryBottomSheetState.showExtendedContent,
                useExpandedPlaceholderText = addGroceryBottomSheetState.useExpandedPlaceholderText,
                changeSearchFieldFocusEvent = addGroceryBottomSheetState.changeSearchFieldFocusEvent,
                showCancelButtonInsteadOfFab = addGroceryBottomSheetState.showCancelButtonInsteadOfFab,
                onKeyboardDone = {
                    addGroceryBottomSheetState.onKeyboardDone()
                    onAddGroceryUiIntent(AddGroceryUiIntent.OnSearchFieldKeyboardDone)
                },
                onCancelButtonClicked = addGroceryBottomSheetState::onCancelButtonClicked,
                onFabClicked = addGroceryBottomSheetState::onFabClicked,
                onSearchFieldFocusChanged = addGroceryBottomSheetState::onSearchFieldFocusChanged,
                onSearchQueryChanged = {
                    onAddGroceryUiIntent(AddGroceryUiIntent.OnUpdateSearchQuery(it))
                },
                onClearSearchQuery = {
                    onAddGroceryUiIntent(AddGroceryUiIntent.OnClearSearchQuery)
                },
                onGrocerySearchResultClick = {
                    onAddGroceryUiIntent(AddGroceryUiIntent.OnGrocerySearchResultClick(it))
                },
                onCustomProductClick = {
                    onAddGroceryUiIntent(AddGroceryUiIntent.OnCustomProductClick(it))
                },
                onEditGroceryClicked = {
                    addGroceryUiState.previouslyAddedGrocery?.let {
                        showEditGroceryBottomSheet(it.productId)
                    }
                }
            )
        },
        sheetDragHandle = { BottomSheetDragHandle() },
        sheetPeekHeight = 100.dp + navigationBarHeight,
        scaffoldState = scaffoldState
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GroceryListTopBar(
                listName = groceryListName,
                toolbarIsHidden = toolbarIsHidden,
                onUpdateGroceryListName = {
                    onGroceryListUiIntent(GroceryListsUiIntent.UpdateGroceryListName(it))
                },
                editModeIsEnabled = groceryListEditModeIsEnabled,
                onNavigationIconClicked = navigateBack,
                onDeleteGroceryList = {
                    deleteGroceryListDialogIsShown = true
                },
                onEditGroceryListToggle = {
                    onGroceryListUiIntent(GroceryListsUiIntent.OnEditGroceryListToggle(it))
                },
                onShareGroceryList = {
                    onGroceryListUiIntent(GroceryListsUiIntent.OnShareGroceryList)
                }
            )

            GroceryListTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            if (groceryGroups != null && categories != null) {
                key(selectedTab) {
                    when (selectedTab) {
                        GroceryListTab.ITEMS -> {
                            if (useListViewForGroceries) {
                                GroceryListItemsList(
                                    modifier = Modifier.fillMaxSize(),
                                    groceryGroups = groceryGroups,
                                    groceryListPurchaseState = groceryListPurchaseState,
                                    scrollUpEvent = scrollUpEvent,
                                    onGroceryClick = {
                                        onGroceryListUiIntent(GroceryListsUiIntent.OnGroceryItemClick(it))
                                    },
                                    onGroceryLongClick = {
                                        showEditGroceryBottomSheet(it.productId)
                                    }
                                )
                            } else {
                                GroceryGrid(
                                    modifier = Modifier.fillMaxSize(),
                                    groceryGroups = groceryGroups,
                                    onGroceryClick = {
                                        onGroceryListUiIntent(
                                            GroceryListsUiIntent.OnGroceryItemClick(it)
                                        )
                                    },
                                    onGroceryLongClick = {
                                        showEditGroceryBottomSheet(it.productId)
                                    },
                                    groceryListPurchaseState = groceryListPurchaseState,
                                    scrollUpEvent = scrollUpEvent
                                )
                            }
                        }

                        GroceryListTab.CATEGORIES -> {
                            CategoriesList(
                                modifier = Modifier.fillMaxSize(),
                                categories = categories,
                                onCategoryItemClick = navigateToCategoryScreen
                            )
                        }

                        GroceryListTab.FAVORITES -> {
                            FavoriteGroceriesGrid(
                                modifier = Modifier.fillMaxSize(),
                                groceries = favoriteGroceries,
                                    onGroceryClick = onFavoriteGroceryClick,
                                    onGroceryLongClick = { grocery ->
                                        showEditGroceryBottomSheet(grocery.productId)
                                    }
                            )
                        }
                    }
                }
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        if (scrimIsShown) {
            Scrim(
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { addGroceryBottomSheetState.onSheetDismissed() }
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroceryListTopBar(
    modifier: Modifier = Modifier,
    listName: TextFieldValue?,
    editModeIsEnabled: Boolean,
    toolbarIsHidden: Boolean,
    onUpdateGroceryListName: (TextFieldValue) -> Unit,
    onNavigationIconClicked: () -> Unit = {},
    onDeleteGroceryList: () -> Unit = {},
    onEditGroceryListToggle: (Boolean) -> Unit = {},
    onShareGroceryList: () -> Unit = {}
) {
    Column(modifier = modifier) {
        val toolbarEnterTransition = remember {
            expandVertically(animationSpec = tween(durationMillis = 150, easing = LinearEasing))
        }
        val toolbarExitTransition = remember {
            shrinkVertically(animationSpec = tween(durationMillis = 150, easing = LinearEasing))
        }

        AnimatedVisibility(
            visible = toolbarIsHidden,
            enter = toolbarEnterTransition,
            exit = toolbarExitTransition
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        }

        val listNameFieldFocusRequester = remember(editModeIsEnabled) {
            if (editModeIsEnabled) FocusRequester() else null
        }
        LaunchedEffect(editModeIsEnabled) {
            listNameFieldFocusRequester?.requestFocus()
        }

        AnimatedVisibility(
            visible = !toolbarIsHidden,
            enter = toolbarEnterTransition,
            exit = toolbarExitTransition
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Box {
                        if (listName != null) {
                            val textColor = MaterialTheme.colorScheme.onSurface
                            val titleStyle = MaterialTheme.typography.titleLarge.copy(
                                textMotion = TextMotion.Animated,
                                color = textColor
                            )
                            if (editModeIsEnabled) {
                                BasicTextField(
                                    modifier = Modifier
                                        .testTag("GroceryListNameField")
                                        .focusRequester(listNameFieldFocusRequester!!),
                                    value = listName,
                                    onValueChange = onUpdateGroceryListName,
                                    singleLine = true,
                                    textStyle = titleStyle,
                                    cursorBrush = SolidColor(textColor)
                                )
                            } else {
                                Text(
                                    text = listName.text,
                                    style = titleStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (listName.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.grocery_list_name_text_field_placeholder),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShareGroceryList) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_grocery_list_title)
                        )
                    }
                    IconButton(onClick = onDeleteGroceryList) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                    IconButton(onClick = { onEditGroceryListToggle(!editModeIsEnabled) }) {
                        if (editModeIsEnabled) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = stringResource(R.string.done)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit)
                            )
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroceryGrid(
    modifier: Modifier = Modifier,
    groceryGroups: List<GroceryGroup>,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    groceryListPurchaseState: GroceryListPurchaseState,
    scrollUpEvent: UiEvent<Unit>? = null,
    onGroceryClick: (Grocery) -> Unit,
    onGroceryLongClick: (Grocery) -> Unit
) {
    val context = LocalContext.current

    ObserveUiEvent(scrollUpEvent) {
        lazyGridState.animateScrollToItem(0)
    }

    GroupedLazyGroceryGrid(
        modifier = modifier,
        groceryGroups = groceryGroups,
        groceryItem = { grocery ->
            GroceryGridItem(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = { onGroceryClick(grocery) },
                        onLongClick = { onGroceryLongClick(grocery) }
                    ),
                groceryName = grocery.name,
                groceryDescription = grocery.description,
                color = if (grocery.purchased) {
                    MaterialTheme.colorScheme.groceryListItemColors.purchasedBackgroundColor
                } else {
                    MaterialTheme.colorScheme.groceryListItemColors.defaultBackgroundColor
                },
                iconFile = remember(grocery.icon?.filePath) {
                    grocery.icon?.filePath?.let { filePath ->
                        File(context.filesDir, filePath)
                    }
                }
            )
        },
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        lazyGridState = lazyGridState,
        groceryListPurchaseState = groceryListPurchaseState
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroceryListItemsList(
    modifier: Modifier = Modifier,
    groceryGroups: List<GroceryGroup>,
    groceryListPurchaseState: GroceryListPurchaseState,
    scrollUpEvent: UiEvent<Unit>? = null,
    onGroceryClick: (Grocery) -> Unit,
    onGroceryLongClick: (Grocery) -> Unit
) {
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    ObserveUiEvent(scrollUpEvent) {
        lazyListState.animateScrollToItem(0)
    }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Workaround: verhindert unerwartetes Verhalten beim ersten klickbaren Listen-Element.
        item(key = "DummyListHeader", contentType = "Dummy") {
            Spacer(modifier = Modifier.height(0.dp))
        }

        when (groceryListPurchaseState) {
            GroceryListPurchaseState.SHOPPING_DONE,
            GroceryListPurchaseState.LIST_IS_EMPTY -> item(key = "EmptyListState", contentType = "EmptyListState") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.foundation.Image(
                        modifier = Modifier.padding(top = 8.dp),
                        painter = androidx.compose.ui.res.painterResource(R.drawable.empty_grocery_list_illustration),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(
                            id = when (groceryListPurchaseState) {
                                GroceryListPurchaseState.SHOPPING_DONE -> R.string.shopping_done_title
                                GroceryListPurchaseState.LIST_IS_EMPTY -> R.string.empty_grocery_list_title
                                else -> throw IllegalStateException()
                            }
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            else -> {}
        }

        groceryGroups.forEachIndexed { groupIndex, group ->
            if (group.titleId != null || !group.title.isNullOrBlank()) {
                item(
                    key = "ListTitle$groupIndex",
                    contentType = "Title"
                ) {
                    Text(
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        text = group.title ?: stringResource(id = group.titleId!!),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            items(
                items = group.groceries,
                key = { it.productId },
                contentType = { "ListGrocery" }
            ) { grocery ->
                GroceryListRowItem(
                    grocery = grocery,
                    iconFile = remember(grocery.icon?.filePath) {
                        grocery.icon?.filePath?.let { filePath ->
                            File(context.filesDir, filePath)
                        }
                    },
                    onClick = { onGroceryClick(grocery) },
                    onCheckedChange = { isChecked ->
                        if (isChecked != grocery.purchased) onGroceryClick(grocery)
                    },
                    onLongClick = { onGroceryLongClick(grocery) }
                )
            }
        }

    }
}

@Composable
private fun GroceryListTabs(
    selectedTab: GroceryListTab,
    onTabSelected: (GroceryListTab) -> Unit
) {
    val tabs = listOf(
        GroceryListTab.ITEMS to stringResource(R.string.grocery_list_tab_items),
        GroceryListTab.CATEGORIES to stringResource(R.string.grocery_list_tab_categories),
        GroceryListTab.FAVORITES to stringResource(R.string.grocery_list_tab_favorites)
    )
    TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab }) {
        tabs.forEach { (tab, title) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(text = title) }
            )
        }
    }
}

@Composable
private fun CategoriesList(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    onCategoryItemClick: (String?) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            count = categories.size,
            key = { index -> categories[index].id },
            contentType = { "Category" }
        ) { index ->
            CategoryItem(
                modifier = Modifier
                    .clip(
                        shape = RoundedCornerShape(
                            topStart = if (index == 0) GroceryItemRounding else 0.dp,
                            topEnd = if (index == 0) GroceryItemRounding else 0.dp
                        )
                    )
                    .clickable { onCategoryItemClick(categories[index].id) },
                name = categories[index].name
            )
        }
        item(key = "CustomCategory", contentType = "Category") {
            CategoryItem(
                modifier = Modifier
                    .clip(
                        shape = RoundedCornerShape(
                            topStart = if (categories.isEmpty()) GroceryItemRounding else 0.dp,
                            topEnd = if (categories.isEmpty()) GroceryItemRounding else 0.dp,
                            bottomStart = GroceryItemRounding,
                            bottomEnd = GroceryItemRounding
                        )
                    )
                    .clickable { onCategoryItemClick(null) },
                name = stringResource(R.string.custom_category_title)
            )
        }
    }
}

private enum class GroceryListTab {
    ITEMS,
    CATEGORIES,
    FAVORITES
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteGroceriesGrid(
    modifier: Modifier = Modifier,
    groceries: List<Grocery>,
    onGroceryClick: (Grocery) -> Unit,
    onGroceryLongClick: (Grocery) -> Unit = {}
) {
    val context = LocalContext.current
    LazyGroceryGrid(
        modifier = modifier,
        groceries = groceries,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        groceryItem = { grocery ->
            GroceryGridItem(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = { onGroceryClick(grocery) },
                        onLongClick = { onGroceryLongClick(grocery) }
                    ),
                groceryName = grocery.name,
                groceryDescription = grocery.description,
                color = if (grocery.purchased) {
                    MaterialTheme.colorScheme.groceryListItemColors.purchasedBackgroundColor
                } else {
                    MaterialTheme.colorScheme.groceryListItemColors.defaultBackgroundColor
                },
                iconFile = remember(grocery.icon?.filePath) {
                    grocery.icon?.filePath?.let { filePath ->
                        File(context.filesDir, filePath)
                    }
                }
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroceryListRowItem(
    grocery: Grocery,
    iconFile: File?,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        color = if (grocery.purchased) {
            MaterialTheme.colorScheme.groceryListItemColors.purchasedBackgroundColor
        } else {
            MaterialTheme.colorScheme.groceryListItemColors.defaultBackgroundColor
        },
        shape = RoundedCornerShape(GroceryItemRounding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 42.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            ) {
                GroceryIcon(
                    modifier = Modifier.padding(3.dp),
                    groceryName = grocery.name,
                    iconFile = iconFile
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grocery.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                grocery.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Checkbox(
                checked = grocery.purchased,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

val sampleGroceryGroups = listOf(
    GroceryGroup(
        titleId = null,
        groceries = List(5) {
            Grocery(
                name = "Not purchased grocery $it",
                purchased = false,
                productId = "not_purchased_$it"
            )
        }
    ),
    GroceryGroup(
        titleId = R.string.purchased_groceries_group_title,
        groceries = List(10) {
            Grocery(
                name = "Purchased grocery $it",
                purchased = true,
                productId = "purchased_$it"
            )
        }
    )
)

@Composable
private fun CategoryItem(
    modifier: Modifier = Modifier,
    name: String
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.secondaryContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(16.dp)
                .weight(1F),
            text = name
        )
        Icon(
            modifier = Modifier.padding(end = 16.dp),
            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = stringResource(R.string.forward)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GroceryListScreenPreview() {
    ShoppingGeniusTheme(requestedColorScheme = ShoppingGeniusColorScheme.YellowColorScheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            GroceryListScreen(
                groceryGroups = sampleGroceryGroups,
                searchQuery = "",
                addGroceryUiState = AddGroceryUiState(),
                groceryListPurchaseState = GroceryListPurchaseState.LIST_IS_FULL
            )
        }
    }
}
