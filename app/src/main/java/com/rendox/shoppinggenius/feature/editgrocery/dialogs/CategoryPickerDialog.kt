package com.rendox.shoppinggenius.feature.editgrocery.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.model.Category
import com.rendox.shoppinggenius.ui.theme.ShoppingGeniusTheme

@Composable
fun CategoryPickerDialog(
    modifier: Modifier = Modifier,
    selectedCategoryId: String?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    onCustomCategorySelected: () -> Unit,
    onCreateCategory: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    PickerDialog(
        modifier = modifier,
        title = stringResource(R.string.select_category_dialog_title),
        onDismissRequest = onDismissRequest
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1F),
                value = newCategoryName,
                onValueChange = { newCategoryName = it },
                singleLine = true,
                label = { Text(stringResource(R.string.category_create_field_label)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                modifier = Modifier.widthIn(min = 92.dp),
                onClick = {
                    val trimmedName = newCategoryName.trim()
                    if (trimmedName.isNotEmpty()) {
                        onCreateCategory(trimmedName)
                        newCategoryName = ""
                    }
                }
            ) {
                Text(text = stringResource(R.string.category_create_button))
            }
        }
        LazyColumn(modifier = Modifier.weight(1F)) {
            item(key = categories.size) {
                CategoryOption(
                    isSelected = selectedCategoryId == null,
                    categoryName = stringResource(R.string.custom_category_title),
                    onClick = onCustomCategorySelected
                )
            }
            items(
                items = categories,
                key = { it.id }
            ) { category ->
                CategoryOption(
                    onClick = { onCategorySelected(category) },
                    isSelected = category.id == selectedCategoryId,
                    categoryName = category.name
                )
            }
        }
    }
}

@Composable
private fun CategoryOption(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    categoryName: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            modifier = Modifier.padding(start = 16.dp),
            selected = isSelected,
            onClick = onClick
        )
        Text(text = categoryName)
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun ChooseCategoryDialogPreview() {
    val categories = remember {
        listOf(
            Category("1", "Fruit"),
            Category("2", "Vegetable"),
            Category("3", "Meat")
        )
    }
    ShoppingGeniusTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CategoryPickerDialog(
                modifier = Modifier
                    .width(200.dp)
                    .height(400.dp),
                selectedCategoryId = "1",
                categories = categories,
                onCategorySelected = {},
                onDismissRequest = {},
                onCustomCategorySelected = {},
                onCreateCategory = {}
            )
        }
    }
}
