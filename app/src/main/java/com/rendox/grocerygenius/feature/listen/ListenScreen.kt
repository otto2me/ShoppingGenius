package com.rendox.grocerygenius.feature.listen

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.rendox.grocerygenius.R
import com.rendox.grocerygenius.feature.editgrocery.dialogs.CategoryPickerDialog
import com.rendox.grocerygenius.model.Category
import com.rendox.grocerygenius.model.IconReference
import com.rendox.grocerygenius.model.Product
import com.rendox.grocerygenius.ui.components.GroceryIcon
import com.rendox.grocerygenius.ui.theme.GroceryGeniusTheme
import com.rendox.grocerygenius.ui.theme.TopAppBarSmallHeight
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

@Composable
fun ListenScreen(
    uiState: ListenUiState,
    onIntent: (ListenUiIntent) -> Unit = {},
    navigateBack: () -> Unit = {},
    navigateToProductIconPicker: (productId: String) -> Unit = {},
    navigateToIconPickerForCategory: (categoryId: String) -> Unit = {}
) {
    var editProductDialogState by remember { mutableStateOf<EditDialogState?>(null) }
    var editCategoryDialogState by remember { mutableStateOf<EditDialogState?>(null) }
    val filesDir = LocalContext.current.filesDir

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth().height(TopAppBarSmallHeight),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.listen),
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.products) { product ->
                ListenProductItem(product = product, filesDir = filesDir, onClick = {
                    editProductDialogState = EditDialogState(
                        id = product.id,
                        name = product.name,
                        iconRef = product.icon,
                        categoryId = product.category?.id
                    )
                })
            }
            items(uiState.categories) { category ->
                ListenCategoryItem(category = category, filesDir = filesDir, onClick = {
                    editCategoryDialogState = EditDialogState(
                        id = category.id,
                        name = category.name,
                        iconRef = category.icon,
                        categoryId = null
                    )
                })
            }
        }
    }

    editProductDialogState?.let { state ->
        EditDialog(
            title = state.name,
            initialName = state.name,
            iconRef = state.iconRef,
            selectedCategoryId = state.categoryId,
            allCategories = uiState.categories,
            filesDir = filesDir,
            onDismiss = { editProductDialogState = null },
            onSaveName = { newName ->
                onIntent(ListenUiIntent.OnEditProductName(productId = state.id, newName = newName))
                editProductDialogState = null
            },
            onEditIcon = {
                editProductDialogState = null
                navigateToProductIconPicker(state.id)
            },
            onCategorySelected = { categoryId ->
                onIntent(ListenUiIntent.OnEditProductCategory(productId = state.id, categoryId = categoryId))
            }
        )
    }

    editCategoryDialogState?.let { state ->
        EditDialog(
            title = state.name,
            initialName = state.name,
            iconRef = state.iconRef,
            selectedCategoryId = null,
            allCategories = emptyList(),
            filesDir = filesDir,
            onDismiss = { editCategoryDialogState = null },
            onSaveName = { newName ->
                onIntent(ListenUiIntent.OnEditCategoryName(categoryId = state.id, newName = newName))
                editCategoryDialogState = null
            },
            onEditIcon = {
                editCategoryDialogState = null
                navigateToIconPickerForCategory(state.id)
            },
            onCategorySelected = null
        )
    }
}

@Composable
private fun ListenProductItem(
    product: Product,
    filesDir: File,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
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

@Composable
private fun ListenCategoryItem(
    category: Category,
    filesDir: File,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
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
private fun EditDialog(
    title: String,
    initialName: String,
    iconRef: IconReference?,
    selectedCategoryId: String?,
    allCategories: List<Category>,
    filesDir: File,
    onDismiss: () -> Unit,
    onSaveName: (String) -> Unit,
    onEditIcon: () -> Unit,
    onCategorySelected: ((categoryId: String?) -> Unit)?
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            selectedCategoryId = selectedCategoryId,
            categories = allCategories,
            onCategorySelected = { category ->
                onCategorySelected?.invoke(category.id)
                showCategoryPicker = false
            },
            onCustomCategorySelected = {
                onCategorySelected?.invoke(null)
                showCategoryPicker = false
            },
            onDismissRequest = { showCategoryPicker = false }
        )
        return
    }

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
                if (onCategorySelected != null) {
                    TextButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                        onClick = { showCategoryPicker = true }
                    ) {
                        Text(text = stringResource(R.string.edit_grocery_change_category_button_title))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = name.trim()
                if (t.isNotEmpty()) onSaveName(t)
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
    val iconRef: IconReference?,
    val categoryId: String?
)

@Preview
@Composable
private fun PreviewListenScreen() {
    GroceryGeniusTheme { ListenScreen(uiState = ListenUiState()) }
}