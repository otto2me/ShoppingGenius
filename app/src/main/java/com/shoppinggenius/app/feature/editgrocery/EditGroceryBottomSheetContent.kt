package com.shoppinggenius.app.feature.editgrocery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shoppinggenius.app.R
import com.shoppinggenius.app.ui.theme.CornerRoundingDefault
import com.shoppinggenius.app.ui.theme.extendedColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditGroceryBottomSheetContent(
    modifier: Modifier = Modifier,
    groceryName: TextFieldValue,
    groceryDescription: TextFieldValue,
    groceryCategoryName: String?,
    clearGroceryNameButtonIsShown: Boolean,
    clearGroceryDescriptionButtonIsShown: Boolean,
    nameCanBeModified: Boolean,
    isEditing: Boolean,
    onGroceryNameChanged: (TextFieldValue) -> Unit,
    onClearGroceryName: () -> Unit,
    productCanBeModified: Boolean,
    onGroceryDescriptionChanged: (TextFieldValue) -> Unit,
    onClearGroceryDescription: () -> Unit,
    onDoneButtonClick: () -> Unit,
    onKeyboardDone: () -> Unit,
    showChangeCategoryButton: Boolean = true,
    showFavoriteButton: Boolean = true,
    onStartEditing: () -> Unit,
    onStartNameEditing: () -> Unit,
    onStartDescriptionEditing: () -> Unit,
    onChangeCategoryClick: () -> Unit,
    onChangeIconClick: () -> Unit,
    onToggleFavoriteClick: () -> Unit,
    isFavorite: Boolean,
    showRemoveFromListButton: Boolean = true,
    onRemoveGrocery: () -> Unit,
    onDeleteProduct: () -> Unit,
    itemNameFocusRequester: FocusRequester,
    itemDescriptionFocusRequester: FocusRequester
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onStartEditing) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.edit)
                )
            }
            TextButton(onClick = onDoneButtonClick) {
                Text(text = stringResource(R.string.done))
            }
        }

        if (isEditing) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .focusRequester(itemNameFocusRequester),
                value = groceryName,
                onValueChange = onGroceryNameChanged,
                label = {
                    Text(text = stringResource(R.string.edit_grocery_name_field_label))
                },
                textStyle = MaterialTheme.typography.titleMedium,
                readOnly = !nameCanBeModified,
                minLines = if (nameCanBeModified) 3 else 1,
                maxLines = Int.MAX_VALUE,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onKeyboardDone() }),
                trailingIcon = {
                    if (nameCanBeModified && clearGroceryNameButtonIsShown) {
                        IconButton(onClick = onClearGroceryName) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    R.string.add_grocery_search_field_trailing_icon_description
                                )
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors().copy(
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .focusRequester(itemDescriptionFocusRequester),
                value = groceryDescription,
                onValueChange = onGroceryDescriptionChanged,
                label = {
                    Text(text = stringResource(R.string.edit_grocery_item_description_placeholder))
                },
                minLines = 3,
                maxLines = Int.MAX_VALUE,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onKeyboardDone() }),
                trailingIcon = {
                    if (clearGroceryDescriptionButtonIsShown) {
                        IconButton(onClick = onClearGroceryDescription) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    R.string.add_grocery_search_field_trailing_icon_description
                                )
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors().copy(
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        } else {
            DetailTextDisplay(
                modifier = Modifier.padding(top = 4.dp),
                label = stringResource(R.string.edit_grocery_name_field_label),
                text = groceryName.text,
                textStyle = MaterialTheme.typography.titleMedium,
                onLongClick = onStartNameEditing,
                enabled = nameCanBeModified
            )
            DetailTextDisplay(
                modifier = Modifier.padding(top = 12.dp),
                label = stringResource(R.string.edit_grocery_item_description_placeholder),
                text = groceryDescription.text,
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholderText = stringResource(R.string.edit_grocery_item_description_placeholder),
                onLongClick = onStartDescriptionEditing,
                enabled = true
            )
        }

        GrocerySettings(
            modifier = Modifier.padding(top = 32.dp),
            groceryCategoryName = groceryCategoryName,
            showChangeCategoryButton = showChangeCategoryButton,
            showFavoriteButton = showFavoriteButton,
            onChangeCategoryClick = onChangeCategoryClick,
            onChangeIconClick = onChangeIconClick,
            onToggleFavoriteClick = onToggleFavoriteClick,
            isFavorite = isFavorite
        )

        if (showRemoveFromListButton) {
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = onRemoveGrocery,
                shape = CornerRoundingDefault,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.edit_grocery_remove_item_button_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (productCanBeModified) {
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDeleteProduct,
                shape = CornerRoundingDefault,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.extendedColors.redAccent
                )
            ) {
                Text(
                    text = stringResource(R.string.edit_grocery_delete_grocery_button_title),
                    color = MaterialTheme.extendedColors.onRedAccent
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailTextDisplay(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    placeholderText: String? = null,
    onLongClick: () -> Unit,
    enabled: Boolean
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = enabled,
                onClick = {},
                onLongClick = onLongClick
            ),
        shape = CornerRoundingDefault,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = text.ifBlank { placeholderText.orEmpty() },
                style = textStyle,
                color = if (text.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun GrocerySettings(
    modifier: Modifier = Modifier,
    groceryCategoryName: String?,
    showChangeCategoryButton: Boolean,
    showFavoriteButton: Boolean,
    onChangeCategoryClick: () -> Unit,
    onChangeIconClick: () -> Unit,
    onToggleFavoriteClick: () -> Unit,
    isFavorite: Boolean
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.titleMedium
        )
        if (showChangeCategoryButton) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "${stringResource(R.string.edit_grocery_change_category_button_title)}: " +
                    (groceryCategoryName ?: stringResource(R.string.custom_category_title)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showChangeCategoryButton) {
                SettingButton(
                    modifier = Modifier.weight(1F),
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.baseline_folder_24),
                            contentDescription = null
                        )
                    },
                    title = stringResource(R.string.edit_grocery_change_category_button_title),
                    onClick = onChangeCategoryClick
                )
            }
            SettingButton(
                modifier = Modifier.weight(1F),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null
                    )
                },
                title = stringResource(R.string.edit_grocery_change_icon_button_title),
                onClick = onChangeIconClick
            )
            if (showFavoriteButton) {
                SettingButton(
                    modifier = Modifier.weight(1F),
                    icon = {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = null
                        )
                    },
                    title = stringResource(R.string.edit_grocery_toggle_favorite_button_title),
                    onClick = onToggleFavoriteClick
                )
            }
        }
    }
}

@Composable
private fun SettingButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(CornerRoundingDefault)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = title,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
