package com.rendox.grocerygenius.feature.iconpicker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rendox.grocerygenius.R
import com.rendox.grocerygenius.model.IconReference
import com.rendox.grocerygenius.ui.components.GroceryIcon
import com.rendox.grocerygenius.ui.components.SearchField
import com.rendox.grocerygenius.ui.components.scrollbar.DecorativeScrollbar
import com.rendox.grocerygenius.ui.components.scrollbar.scrollbarState
import com.rendox.grocerygenius.ui.theme.GroceryItemRounding
import com.rendox.grocerygenius.ui.theme.TopAppBarActionsHorizontalPadding
import com.rendox.grocerygenius.ui.theme.TopAppBarSmallHeight
import java.io.File

@Composable
fun IconPickerRoute(
    viewModel: IconPickerViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    Surface {
        IconPickerScreen(
            searchQuery = viewModel.searchQuery,
            uiState = uiState,
            onIntent = viewModel::onIntent,
            navigateBack = navigateBack
        )
    }
}

@Composable
fun IconPickerScreen(
    searchQuery: String,
    uiState: IconPickerUiState,
    onIntent: (IconPickerIntent) -> Unit,
    navigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val importSuccessMessage = stringResource(R.string.icon_picker_import_success)
    val importFailedMessage = stringResource(R.string.icon_picker_import_failed)

    LaunchedEffect(uiState.remoteImportEventId) {
        if (uiState.remoteImportEventId <= 0L) return@LaunchedEffect
        val succeeded = uiState.remoteImportSucceeded ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = if (succeeded) importSuccessMessage else importFailedMessage
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        val gridCells = GridCells.Adaptive(104.dp)
        val horizontalArrangement = Arrangement.spacedBy(8.dp)
        val lazyGridState = rememberLazyGridState()

        LaunchedEffect(uiState.searchResultsShown) {
            lazyGridState.scrollToItem(0, 0)
        }

        IconPickerTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            onLeadingIconClick = navigateBack,
            onTrailingIconClick = { onIntent(IconPickerIntent.OnRemoveIcon) }
        )
        uiState.product?.let { product ->
            val overviewIcon = uiState.previewIcon ?: product.icon
            IconPickerItem(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(104.dp)
                    .align(Alignment.CenterHorizontally),
                iconRef = overviewIcon,
                iconName = product.name,
                isSelected = true
            )
        }
        IconPickerSearchField(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            searchQuery = searchQuery,
            clearSearchQueryButtonIsShown = uiState.clearSearchQueryButtonIsShown,
            onSearchQueryChanged = {
                onIntent(IconPickerIntent.OnUpdateSearchQuery(it))
            },
            onClearSearchQuery = {
                onIntent(IconPickerIntent.OnClearSearchQuery)
            }
        )
        DuckDuckGoImageResults(
            modifier = Modifier.padding(horizontal = 16.dp),
            uiState = uiState,
            searchQuery = searchQuery,
            onImageClick = { image ->
                onIntent(
                    IconPickerIntent.OnPickRemoteImage(
                        imageUrl = image.thumbnailUrl,
                        fallbackImageUrl = image.imageUrl
                    )
                )
            }
        )
        IconGrid(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onIntent = onIntent,
            gridCells = gridCells,
            horizontalArrangement = horizontalArrangement,
            lazyGridState = lazyGridState
        )
    }

        SnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            hostState = snackbarHostState
        )
    }
}

@Composable
private fun IconPickerTopAppBar(
    modifier: Modifier = Modifier,
    onLeadingIconClick: () -> Unit,
    onTrailingIconClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TopAppBarSmallHeight)
    ) {
        IconButton(
            modifier = Modifier
                .padding(start = TopAppBarActionsHorizontalPadding)
                .align(Alignment.CenterStart),
            onClick = onLeadingIconClick
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close)
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.icon_picker_screen_title),
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(
            modifier = Modifier
                .padding(end = TopAppBarActionsHorizontalPadding)
                .align(Alignment.CenterEnd),
            onClick = onTrailingIconClick
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IconGrid(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState,
    uiState: IconPickerUiState,
    gridCells: GridCells,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    onIntent: (IconPickerIntent) -> Unit
) {
    var iconPendingDelete by remember { mutableStateOf<IconReference?>(null) }

    if (iconPendingDelete != null) {
        AlertDialog(
            onDismissRequest = { iconPendingDelete = null },
            title = { Text(stringResource(R.string.icon_picker_delete_dialog_title)) },
            text = { Text(stringResource(R.string.icon_picker_delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onIntent(IconPickerIntent.OnDeleteIcon(iconPendingDelete!!))
                    iconPendingDelete = null
                }) {
                    Text(
                        text = stringResource(R.string.icon_picker_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { iconPendingDelete = null }) {
                    Text(stringResource(R.string.icon_picker_delete_cancel))
                }
            }
        )
    }

    Box(modifier = modifier) {
        val itemsAvailable = remember(uiState.groupedIcons) {
            val numOfTitles = uiState.groupedIcons.size
            val numOfIcons = uiState.groupedIcons.values.sumOf { it.size }
            numOfTitles + numOfIcons
        }
        val scrollbarState = lazyGridState.scrollbarState(itemsAvailable)
        LazyVerticalGrid(
            modifier = Modifier.padding(horizontal = 16.dp),
            state = lazyGridState,
            columns = gridCells,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (uiState.searchResultsShown) {
                items(
                    items = uiState.searchResults,
                    key = { "GroceryIcon-${it.uniqueFileName}" },
                    contentType = { "GroceryIcon" }
                ) { iconRef ->
                    IconPickerItem(
                        modifier = Modifier.aspectRatio(1F),
                        iconRef = iconRef,
                        onIconClick = { icon ->
                            if (icon != null) onIntent(IconPickerIntent.OnPickIcon(icon))
                        },
                        onIconLongClick = { icon -> iconPendingDelete = icon },
                        isSelected = iconRef.uniqueFileName == uiState.product?.icon?.uniqueFileName
                    )
                }
            } else {
                for ((category, icons) in uiState.groupedIcons) {
                    item(
                        key = "Title-${category.id}",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "Title"
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .animateItemPlacement(),
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    items(
                        items = icons,
                        key = { "SearchResult-${it.uniqueFileName}" },
                        contentType = { "GroceryIcon" }
                    ) { iconRef ->
                        IconPickerItem(
                            modifier = Modifier.aspectRatio(1F),
                            iconRef = iconRef,
                            onIconClick = { icon ->
                                if (icon != null) onIntent(IconPickerIntent.OnPickIcon(icon))
                            },
                            onIconLongClick = { icon -> iconPendingDelete = icon },
                            isSelected = iconRef.uniqueFileName == uiState.product?.icon?.uniqueFileName
                        )
                    }
                }
            }
        }
        lazyGridState.DecorativeScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterEnd),
            state = scrollbarState,
            orientation = Orientation.Vertical
        )
    }
}

@Composable
private fun IconPickerSearchField(
    modifier: Modifier = Modifier,
    searchQuery: String,
    clearSearchQueryButtonIsShown: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearchQuery: () -> Unit
) {
    SearchField(
        modifier = modifier.fillMaxWidth(),
        searchQuery = searchQuery,
        onSearchQueryChanged = onSearchQueryChanged,
        placeholder = {
            Text(text = stringResource(R.string.icon_picker_search_field_placeholder))
        },
        clearSearchInputButtonIsShown = clearSearchQueryButtonIsShown,
        onClearSearchInputClicked = onClearSearchQuery,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        }
    )
}

@Composable
private fun DuckDuckGoImageResults(
    modifier: Modifier = Modifier,
    uiState: IconPickerUiState,
    searchQuery: String,
    onImageClick: (DuckDuckGoImageSearchResult) -> Unit
) {
    val queryIsNotBlank = searchQuery.trim().isNotEmpty()
    if (!queryIsNotBlank) return

    if (uiState.duckDuckGoImageResults.isEmpty()) return

    Text(
        modifier = modifier.padding(bottom = 8.dp),
        text = stringResource(R.string.icon_picker_duckduckgo_images_section_title),
        style = MaterialTheme.typography.titleSmall
    )
    LazyRow(
        modifier = modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = uiState.duckDuckGoImageResults,
            key = { it.imageUrl }
        ) { image ->
            val isImportingThisImage =
                uiState.remoteImportInProgress && uiState.importingImageUrl == image.thumbnailUrl
            Surface(
                modifier = Modifier.size(84.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(GroceryItemRounding),
                onClick = {
                    if (!uiState.remoteImportInProgress) onImageClick(image)
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = image.thumbnailUrl,
                        contentDescription = image.title,
                        contentScale = ContentScale.Crop
                    )
                    if (isImportingThisImage) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IconPickerItem(
    modifier: Modifier = Modifier,
    iconRef: IconReference?,
    iconName: String = iconRef?.name ?: "",
    isSelected: Boolean,
    onIconClick: (IconReference?) -> Unit = {},
    onIconLongClick: (IconReference) -> Unit = {}
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        shape = RoundedCornerShape(GroceryItemRounding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { onIconClick(iconRef) },
                    onLongClick = { if (iconRef != null) onIconLongClick(iconRef) }
                )
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(modifier = Modifier.weight(1F)) {
                GroceryIcon(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    groceryName = iconName,
                    iconFile = iconRef?.let { File(context.filesDir, it.filePath) }
                )
            }
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = iconName,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}