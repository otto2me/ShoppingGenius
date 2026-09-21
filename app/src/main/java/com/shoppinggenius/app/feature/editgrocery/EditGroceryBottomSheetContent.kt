package com.shoppinggenius.app.feature.editgrocery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.shoppinggenius.app.ui.components.SearchField
import com.shoppinggenius.app.ui.theme.CornerRoundingDefault
import com.shoppinggenius.app.ui.theme.extendedColors

@Composable
fun EditGroceryBottomSheetContent(
    modifier: Modifier = Modifier,
    groceryName: TextFieldValue,
    groceryDescription: TextFieldValue,
    groceryCategoryName: String?,
    clearGroceryNameButtonIsShown: Boolean,
    clearGroceryDescriptionButtonIsShown: Boolean,
    nameCanBeModified: Boolean,
    onGroceryNameChanged: (TextFieldValue) -> Unit,
    onClearGroceryName: () -> Unit,
    productCanBeModified: Boolean,
    onGroceryDescriptionChanged: (TextFieldValue) -> Unit,
    onClearGroceryDescription: () -> Unit,
    onDoneButtonClick: () -> Unit,
    onKeyboardDone: () -> Unit,
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
        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = onDoneButtonClick
            ) {
                Text(text = stringResource(R.string.done))
            }
        }

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

        SearchField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .focusRequester(itemDescriptionFocusRequester),
            searchQuery = groceryDescription,
            onSearchQueryChanged = onGroceryDescriptionChanged,
            placeholder = {
                Text(text = stringResource(R.string.edit_grocery_item_description_placeholder))
            },
            keyboardActions = KeyboardActions(
                onDone = { onKeyboardDone() }
            ),
            clearSearchInputButtonIsShown = clearGroceryDescriptionButtonIsShown,
            onClearSearchInputClicked = onClearGroceryDescription
        )

        GrocerySettings(
            modifier = Modifier.padding(top = 32.dp),
            groceryCategoryName = groceryCategoryName,
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

@Composable
private fun GrocerySettings(
    modifier: Modifier = Modifier,
    groceryCategoryName: String?,
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
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = "${stringResource(R.string.edit_grocery_change_category_button_title)}: " +
                (groceryCategoryName ?: stringResource(R.string.custom_category_title)),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
