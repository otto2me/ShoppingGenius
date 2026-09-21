package com.shoppinggenius.app.feature.editgrocery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.shoppinggenius.app.R
import com.shoppinggenius.app.feature.editgrocery.dialogs.CategoryPickerDialog
import com.shoppinggenius.app.ui.components.BottomSheetDragHandle
import com.shoppinggenius.app.ui.components.DeleteConfirmationDialog
import com.shoppinggenius.app.ui.theme.ShoppingGeniusTheme
import kotlinx.coroutines.launch

private enum class EditField {
    NAME,
    DESCRIPTION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroceryBottomSheet(
    modifier: Modifier = Modifier,
    editBottomSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    screenState: EditGroceryUiState,
    editGroceryName: TextFieldValue,
    editGroceryDescription: TextFieldValue,
    showChangeCategoryButton: Boolean = true,
    showFavoriteButton: Boolean = true,
    hideBottomSheetOnCompletion: () -> Unit,
    onIntent: (EditGroceryUiIntent) -> Unit,
    navigateToIconPicker: (String) -> Unit
) = Box(modifier = modifier) {
    var categoryPickerIsVisible by remember { mutableStateOf(false) }
    var deleteProductDialogIsVisible by remember { mutableStateOf(false) }
    var activeEditField by rememberSaveable(screenState.editGrocery?.productId) {
        mutableStateOf<EditField?>(null)
    }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val hideBottomSheet = {
        focusManager.clearFocus()
        coroutineScope
            .launch { editBottomSheetState.hide() }
            .invokeOnCompletion { hideBottomSheetOnCompletion() }
    }
    ModalBottomSheet(
        modifier = Modifier.padding(top = 30.dp),
        onDismissRequest = { hideBottomSheet() },
        sheetState = editBottomSheetState,
        scrimColor = Color.Transparent,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        val itemNameFocusRequester = remember { FocusRequester() }
        val itemDescriptionFocusRequester = remember { FocusRequester() }
        LaunchedEffect(activeEditField, screenState.nameCanBeModified) {
            when (activeEditField) {
                EditField.NAME -> {
                    if (screenState.nameCanBeModified) {
                        itemNameFocusRequester.requestFocus()
                    } else {
                        activeEditField = EditField.DESCRIPTION
                    }
                }

                EditField.DESCRIPTION -> itemDescriptionFocusRequester.requestFocus()

                null -> {
                    focusManager.clearFocus()
                }
            }
        }
        EditGroceryBottomSheetContent(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .imePadding(),
            groceryName = editGroceryName,
            groceryDescription = editGroceryDescription,
            groceryCategoryName = screenState.editGrocery?.category?.name,
            clearGroceryNameButtonIsShown = screenState.clearEditGroceryNameButtonIsShown,
            clearGroceryDescriptionButtonIsShown = screenState.clearEditGroceryDescriptionButtonIsShown,
            nameCanBeModified = screenState.nameCanBeModified,
            isEditing = activeEditField != null,
            showChangeCategoryButton = showChangeCategoryButton,
            showFavoriteButton = showFavoriteButton,
            onStartEditing = {
                activeEditField = if (screenState.nameCanBeModified) {
                    EditField.NAME
                } else {
                    EditField.DESCRIPTION
                }
            },
            onStartNameEditing = {
                if (screenState.nameCanBeModified) {
                    activeEditField = EditField.NAME
                }
            },
            onStartDescriptionEditing = {
                activeEditField = EditField.DESCRIPTION
            },
            onGroceryNameChanged = {
                onIntent(EditGroceryUiIntent.OnNameChanged(it))
            },
            onClearGroceryName = {
                onIntent(EditGroceryUiIntent.OnClearName)
            },
            onGroceryDescriptionChanged = {
                onIntent(EditGroceryUiIntent.OnDescriptionChanged(it))
            },
            onClearGroceryDescription = {
                onIntent(EditGroceryUiIntent.OnClearDescription)
            },
            onDoneButtonClick = {
                activeEditField = null
                hideBottomSheet()
            },
            onKeyboardDone = {
                activeEditField = null
                hideBottomSheet()
            },
            itemNameFocusRequester = itemNameFocusRequester,
            itemDescriptionFocusRequester = itemDescriptionFocusRequester,
            onChangeCategoryClick = {
                categoryPickerIsVisible = true
            },
            onChangeIconClick = {
                screenState.editGrocery?.productId?.let { productId ->
                    coroutineScope
                        .launch {
                            editBottomSheetState.hide()
                            navigateToIconPicker(productId)
                            hideBottomSheetOnCompletion()
                        }
                }
            },
            onToggleFavoriteClick = {
                onIntent(EditGroceryUiIntent.OnToggleFavorite)
            },
            isFavorite = screenState.editGrocery?.isFavorite == true,
            productCanBeModified = screenState.editGrocery?.productIsDefault == false,
            showRemoveFromListButton = screenState.showRemoveFromListButton,
            onRemoveGrocery = {
                onIntent(EditGroceryUiIntent.OnRemoveGroceryFromList)
                hideBottomSheet()
            },
            onDeleteProduct = {
                deleteProductDialogIsVisible = true
            }
        )
    }

    if (categoryPickerIsVisible) {
        CategoryPickerDialog(
            modifier = Modifier,
            selectedCategoryId = screenState.editGrocery?.category?.id,
            categories = screenState.groceryCategories,
            onCategorySelected = {
                onIntent(EditGroceryUiIntent.OnCategorySelected(it))
                categoryPickerIsVisible = false
            },
            onDismissRequest = { categoryPickerIsVisible = false },
            onCustomCategorySelected = {
                onIntent(EditGroceryUiIntent.OnCustomCategorySelected)
                categoryPickerIsVisible = false
            },
            onCreateCategory = { name ->
                onIntent(EditGroceryUiIntent.OnCreateCategory(name))
                categoryPickerIsVisible = false
            }
        )
    }

    if (deleteProductDialogIsVisible) {
        DeleteConfirmationDialog(
            onConfirm = {
                screenState.editGrocery?.productId?.let {
                    onIntent(EditGroceryUiIntent.OnDeleteProduct)
                }
                hideBottomSheet()
                deleteProductDialogIsVisible = false
            },
            onDismissRequest = { deleteProductDialogIsVisible = false },
            bodyText = stringResource(R.string.delete_product_dialog_text)
        )
    }
}

class ParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

@Preview
@Composable
private fun EditGroceryBottomSheetContentPreview(
    @PreviewParameter(ParameterProvider::class) productCanBeModified: Boolean
) {
    ShoppingGeniusTheme {
        Surface {
            EditGroceryBottomSheetContent(
                modifier = Modifier.padding(16.dp),
                groceryName = TextFieldValue("Tea"),
                groceryDescription = TextFieldValue("Green, 32 bags"),
                groceryCategoryName = "Drinks",
                clearGroceryNameButtonIsShown = true,
                clearGroceryDescriptionButtonIsShown = false,
                nameCanBeModified = true,
                onGroceryNameChanged = {},
                onClearGroceryName = {},
                onGroceryDescriptionChanged = {},
                onClearGroceryDescription = {},
                onKeyboardDone = {},
                onDoneButtonClick = {},
                isEditing = false,
                onStartEditing = {},
                onStartNameEditing = {},
                onStartDescriptionEditing = {},
                itemNameFocusRequester = remember { FocusRequester() },
                itemDescriptionFocusRequester = remember { FocusRequester() },
                onChangeCategoryClick = {},
                onChangeIconClick = {},
                onToggleFavoriteClick = {},
                isFavorite = false,
                productCanBeModified = productCanBeModified,
                onRemoveGrocery = {},
                onDeleteProduct = {}
            )
        }
    }
}
