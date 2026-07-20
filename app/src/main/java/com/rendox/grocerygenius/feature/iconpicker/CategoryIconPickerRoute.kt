package com.rendox.grocerygenius.feature.iconpicker

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CategoryIconPickerRoute(
    viewModel: CategoryIconPickerViewModel = hiltViewModel(),
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