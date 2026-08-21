package com.rendox.shoppinggenius.feature.listen

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.feature.editgrocery.EditGroceryBottomSheet
import com.rendox.shoppinggenius.feature.editgrocery.EditGroceryUiIntent
import com.rendox.shoppinggenius.feature.editgrocery.EditGroceryViewModel
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.model.IconReference
import com.rendox.shoppinggenius.model.Product
import com.rendox.shoppinggenius.ui.components.GroceryIcon
import com.rendox.shoppinggenius.ui.theme.ShoppingGeniusTheme
import com.rendox.shoppinggenius.ui.theme.TopAppBarSmallHeight
import java.io.File

@Composable
fun ListenRoute(
    viewModel: ListenViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToProductIconPicker: (productId: String) -> Unit = {},
    navigateToIconPickerForCategory: (categoryId: String) -> Unit = {}
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    ListenScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        navigateBack = navigateBack,
        navigateToProductIconPicker = navigateToProductIconPicker,
        navigateToIconPickerForCategory = navigateToIconPickerForCategory
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenScreen(
    uiState: ListenUiState,
    onIntent: (ListenUiIntent) -> Unit = {},
    navigateBack: () -> Unit = {},
    navigateToProductIconPicker: (productId: String) -> Unit = {},
    navigateToIconPickerForCategory: (categoryId: String) -> Unit = {}
) {
    var editProductIdState by remember { mutableStateOf<String?>(null) }
    var editCategoryDialogState by remember { mutableStateOf<EditDialogState?>(null) }
    var createCategoryDialogIsVisible by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filesDir = LocalContext.current.filesDir
    val normalizedQuery = searchQuery.trim()
    val filteredProducts = uiState.products.filter {
        normalizedQuery.isEmpty() || it.name.contains(normalizedQuery, ignoreCase = true)
    }
    val filteredCategories = uiState.categories.filter {
        normalizedQuery.isEmpty() || it.name.contains(normalizedQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth().height(TopAppBarSmallHeight),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.list_editor),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    modifier = Modifier.padding(start = 4.dp).align(Alignment.CenterStart),
                    onClick = navigateBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            value = searchQuery,
            onValueChange = { searchQuery = it },
            singleLine = true,
            label = { Text(text = stringResource(R.string.listen_search_field_placeholder)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.add_grocery_search_field_trailing_icon_description)
                        )
                    }
                }
            }
        )

        TextButton(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            onClick = { createCategoryDialogIsVisible = true }
        ) {
            Text(text = stringResource(R.string.category_create_title))
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredProducts) { product ->
                ListenProductItem(
                    product = product,
                    filesDir = filesDir,
                    onClick = {
                        editProductIdState = product.id
                    },
                    onLongClick = {
                        editProductIdState = product.id
                    }
                )
            }
            items(filteredCategories) { category ->
                ListenCategoryItem(
                    category = category,
                    filesDir = filesDir,
                    onClick = {
                        editCategoryDialogState = EditDialogState(
                            id = category.id,
                            name = category.name,
                            iconRef = category.icon
                        )
                    },
                    onLongClick = {
                        editCategoryDialogState = EditDialogState(
                            id = category.id,
                            name = category.name,
                            iconRef = category.icon
                        )
                    }
                )
            }
        }
    }

    val editProductId = editProductIdState
    if (editProductId != null) {
        val editGroceryViewModel: EditGroceryViewModel = hiltViewModel()
        val editGroceryUiState by editGroceryViewModel.uiStateFlow.collectAsStateWithLifecycle()

        LaunchedEffect(editProductId) {
            editGroceryViewModel.onIntent(
                EditGroceryUiIntent.OnEditProduct(productId = editProductId)
            )
        }

        EditGroceryBottomSheet(
            modifier = Modifier.fillMaxSize(),
            screenState = editGroceryUiState,
            editGroceryDescription = editGroceryViewModel.editGroceryDescription,
            hideBottomSheetOnCompletion = {
                editProductIdState = null
            },
            onIntent = editGroceryViewModel::onIntent,
            navigateToIconPicker = { productId ->
                editProductIdState = null
                navigateToProductIconPicker(productId)
            }
        )
    }

    editCategoryDialogState?.let { state ->
        EditCategoryDialog(
            title = state.name,
            initialName = state.name,
            iconRef = state.iconRef,
            filesDir = filesDir,
            onDismiss = { editCategoryDialogState = null },
            onSaveName = { newName ->
                onIntent(ListenUiIntent.OnEditCategoryName(categoryId = state.id, newName = newName))
                editCategoryDialogState = null
            },
            onEditIcon = {
                editCategoryDialogState = null
                navigateToIconPickerForCategory(state.id)
            }
        )
    }

    if (createCategoryDialogIsVisible) {
        AlertDialog(
            onDismissRequest = {
                createCategoryDialogIsVisible = false
                newCategoryName = ""
            },
            title = { Text(text = stringResource(R.string.category_create_title)) },
            text = {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.category_create_field_label)) }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newCategoryName.trim()
                        if (trimmed.isNotEmpty()) {
                            onIntent(ListenUiIntent.OnCreateCategory(trimmed))
                            createCategoryDialogIsVisible = false
                            newCategoryName = ""
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.category_create_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        createCategoryDialogIsVisible = false
                        newCategoryName = ""
                    }
                ) {
                    Text(text = stringResource(R.string.close))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListenProductItem(
    product: Product,
    filesDir: File,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        headlineContent = { Text(text = product.name) },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            ) {
                GroceryIcon(
                    modifier = Modifier.padding(4.dp),
                    groceryName = product.name,
                    iconFile = product.icon?.let { File(filesDir, it.filePath) }
                )
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListenCategoryItem(
    category: Category,
    filesDir: File,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        headlineContent = { Text(text = category.name) },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            ) {
                GroceryIcon(
                    modifier = Modifier.padding(4.dp),
                    groceryName = category.name,
                    iconFile = category.icon?.let { File(filesDir, it.filePath) }
                )
            }
        }
    )
}

@Composable
private fun EditCategoryDialog(
    title: String,
    initialName: String,
    iconRef: IconReference?,
    filesDir: File,
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit,
    onEditIcon: () -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Surface(
                    modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.medium,
                    onClick = onEditIcon
                ) {
                    GroceryIcon(
                        modifier = Modifier.padding(8.dp),
                        groceryName = name,
                        iconFile = iconRef?.let { File(filesDir, it.filePath) }
                    )
                }
                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 8.dp),
                    onClick = onEditIcon
                ) {
                    Text(text = stringResource(R.string.edit_grocery_change_icon_button_title))
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.edit)) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmedName = name.trim()
                if (trimmedName.isNotEmpty()) onSaveName(trimmedName)
            }) {
                Text(text = stringResource(R.string.done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.close)) }
        }
    )
}

private data class EditDialogState(
    val id: String,
    val name: String,
    val iconRef: IconReference?
)

@Preview
@Composable
private fun PreviewListenScreen() {
    ShoppingGeniusTheme { ListenScreen(uiState = ListenUiState()) }
}
