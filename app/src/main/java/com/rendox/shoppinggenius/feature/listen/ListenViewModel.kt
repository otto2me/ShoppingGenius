package com.rendox.shoppinggenius.feature.listen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rendox.shoppinggenius.data.category.CategoryRepository
import com.rendox.shoppinggenius.data.product.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ListenViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(ListenUiState())
    val uiStateFlow: StateFlow<ListenUiState> = _uiStateFlow.asStateFlow()

    init {
        loadData()
    }


    private fun loadData() {
        viewModelScope.launch {
            _uiStateFlow.update { it.copy(isLoading = true) }
            try {
                productRepository.getAllProducts().collect { products ->
                    _uiStateFlow.update { state ->
                        state.copy(products = products, isLoading = false)
                    }
                }
            } catch (_: Exception) {
                _uiStateFlow.update { it.copy(isLoading = false) }
            }
        }

        viewModelScope.launch {
            try {
                categoryRepository.getAllCategories().collect { categories ->
                    _uiStateFlow.update { state ->
                        state.copy(categories = categories)
                    }
                }
            } catch (_: Exception) {
                // Handle error silently
            }
        }
    }

    fun onIntent(intent: ListenUiIntent) {
        when (intent) {
            is ListenUiIntent.OnEditProductName -> {
                viewModelScope.launch {
                    productRepository.updateProductName(
                        intent.productId,
                        intent.newName
                    )
                }
            }
            is ListenUiIntent.OnEditCategoryName -> {
                viewModelScope.launch {
                    categoryRepository.updateCategoryName(
                        intent.categoryId,
                        intent.newName
                    )
                }
            }
            is ListenUiIntent.OnEditProductIcon -> {
                _uiStateFlow.update { it.copy(editingProductId = intent.productId) }
            }
            is ListenUiIntent.OnEditProductCategory -> {
                viewModelScope.launch {
                    productRepository.updateProductCategory(
                        intent.productId,
                        intent.categoryId
                    )
                }
            }
            is ListenUiIntent.OnToggleProductFavorite -> {
                viewModelScope.launch {
                    productRepository.updateProductFavorite(
                        productId = intent.productId,
                        isFavorite = intent.isFavorite
                    )
                }
            }
            is ListenUiIntent.OnDeleteProduct -> {
                viewModelScope.launch {
                    productRepository.deleteProductById(intent.productId)
                }
            }
            is ListenUiIntent.OnCreateCategory -> {
                val name = intent.name.trim()
                if (name.isEmpty()) return
                viewModelScope.launch {
                    categoryRepository.createCategory(name)
                }
            }
            is ListenUiIntent.OnCancelEdit -> {
                _uiStateFlow.update {
                    it.copy(
                        editingProductId = null,
                        editingCategoryId = null,
                        editingProductName = "",
                        editingCategoryName = ""
                    )
                }
            }
        }
    }
}
