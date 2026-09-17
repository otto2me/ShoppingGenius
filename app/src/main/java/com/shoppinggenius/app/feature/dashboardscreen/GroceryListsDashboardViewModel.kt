package com.shoppinggenius.app.feature.dashboardscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppinggenius.app.data.grocerylist.GroceryListRepository
import com.shoppinggenius.app.model.GroceryList
import com.shoppinggenius.app.model.GroceryListType
import com.shoppinggenius.app.ui.helpers.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GroceryListsDashboardViewModel @Inject constructor(
    private val groceryListRepository: GroceryListRepository
) : ViewModel() {

    val groceryListsFlow = groceryListRepository.getAllGroceryLists()
        .map { groceryLists ->
            groceryLists
                .sortedBy { it.sortingPriority }
                // sorting priority has served it's purpose, now we can reduce the number
                // of recompositions by setting it to 0
                .map { it.copy(sortingPriority = 0) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _navigateToGroceryListEvent = MutableStateFlow<UiEvent<String>?>(null)
    val navigateToGroceryListEvent = _navigateToGroceryListEvent.asStateFlow()

    fun onIntent(intent: GroceryListsDashboardUiIntent) = when (intent) {
        is GroceryListsDashboardUiIntent.OnAdderItemClick -> Unit
        is GroceryListsDashboardUiIntent.OnCreateGroceryList -> createNewGroceryList(intent.type)
        is GroceryListsDashboardUiIntent.OnUpdateGroceryLists -> updateGroceryLists(intent.groceryLists)
    }

    private fun createNewGroceryList(type: GroceryListType) {
        viewModelScope.launch {
            val groceryListId = UUID.randomUUID().toString()
            groceryListRepository.insertGroceryList(
                GroceryList(
                    id = groceryListId,
                    name = "",
                    type = type
                )
            )
            _navigateToGroceryListEvent.update {
                object : UiEvent<String> {
                    override val data = groceryListId
                    override fun onConsumed() {
                        _navigateToGroceryListEvent.update { null }
                    }
                }
            }
        }
    }

    private fun updateGroceryLists(dashboardItems: List<GroceryList>) {
        viewModelScope.launch {
            val groceryLists = dashboardItems.mapIndexed { index, dashboardItem ->
                dashboardItem.copy(sortingPriority = index.toLong())
            }
            groceryListRepository.updateGroceryLists(groceryLists)
        }
    }
}
