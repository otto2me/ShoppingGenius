package com.rendox.grocerygenius.feature.listen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.grocerygenius.R
import com.rendox.grocerygenius.ui.theme.GroceryGeniusTheme
import com.rendox.grocerygenius.ui.theme.TopAppBarSmallHeight

@Composable
fun ListenRoute(
    viewModel: ListenViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    ListenScreen(
        uiState = uiState,
        navigateBack = navigateBack
    )
}

@Composable
fun ListenScreen(
    uiState: ListenUiState,
    navigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Top bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(TopAppBarSmallHeight),
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
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .align(Alignment.CenterStart),
                    onClick = navigateBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        }

        // List content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(uiState.products) { product ->
                ListenProductItem(product = product)
            }
            items(uiState.categories) { category ->
                ListenCategoryItem(category = category)
            }
        }
    }
}

@Composable
private fun ListenProductItem(
    product: com.rendox.grocerygenius.model.Product
) {
    // TODO: Implement product item with edit capability
    Text(
        text = product.name,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ListenCategoryItem(
    category: com.rendox.grocerygenius.model.Category
) {
    // TODO: Implement category item with edit capability
    Text(
        text = category.name,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Preview
@Composable
private fun PreviewListenScreen() {
    GroceryGeniusTheme {
        ListenScreen(
            uiState = ListenUiState()
        )
    }
}

