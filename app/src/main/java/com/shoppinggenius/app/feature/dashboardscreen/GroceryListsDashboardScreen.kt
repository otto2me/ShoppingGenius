package com.shoppinggenius.app.feature.dashboardscreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shoppinggenius.app.R
import com.shoppinggenius.app.databinding.ListRecyclerviewBinding
import com.shoppinggenius.app.feature.dashboardscreen.recyclerview.DashboardRecyclerViewAdapter
import com.shoppinggenius.app.model.GroceryList
import com.shoppinggenius.app.model.GroceryListType
import com.shoppinggenius.app.ui.helpers.ObserveUiEvent
import com.shoppinggenius.app.ui.theme.ShoppingGeniusTheme
import com.shoppinggenius.app.ui.theme.TopAppBarActionsHorizontalPadding
@Composable
fun GroceryListsDashboardRoute(
    viewModel: GroceryListsDashboardViewModel = hiltViewModel(),
    navigateToGroceryListScreen: (String) -> Unit,
    navigateToSettingsScreen: () -> Unit
) {
    val screenState by viewModel.groceryListsFlow.collectAsStateWithLifecycle()
    val navigateToGroceryListEvent by viewModel.navigateToGroceryListEvent.collectAsStateWithLifecycle()
    ObserveUiEvent(navigateToGroceryListEvent) { groceryListId ->
        navigateToGroceryListScreen(groceryListId)
    }
    GroceryListsDashboardScreen(
        groceryLists = screenState,
        onIntent = viewModel::onIntent,
        navigateToSettingsScreen = navigateToSettingsScreen,
        navigateToGroceryListScreen = navigateToGroceryListScreen
    )
}
@Composable
fun GroceryListsDashboardScreen(
    groceryLists: List<GroceryList>,
    onIntent: (GroceryListsDashboardUiIntent) -> Unit = {},
    navigateToSettingsScreen: () -> Unit = {},
    navigateToGroceryListScreen: (String) -> Unit = {}
) {
    var scrollState by rememberSaveable { mutableIntStateOf(0) }
    var createListDialogIsShown by rememberSaveable { mutableStateOf(false) }
    if (createListDialogIsShown) {
        AlertDialog(
            onDismissRequest = { createListDialogIsShown = false },
            title = {
                Text(text = stringResource(R.string.create_grocery_list_dialog_title))
            },
            text = {
                Column {
                    Text(text = stringResource(R.string.create_grocery_list_dialog_description))
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            createListDialogIsShown = false
                            onIntent(
                                GroceryListsDashboardUiIntent.OnCreateGroceryList(
                                    GroceryListType.SHOPPING
                                )
                            )
                        }
                    ) {
                        Text(text = stringResource(R.string.grocery_list_type_shopping))
                    }
                    TextButton(
                        onClick = {
                            createListDialogIsShown = false
                            onIntent(
                                GroceryListsDashboardUiIntent.OnCreateGroceryList(
                                    GroceryListType.TODO
                                )
                            )
                        }
                    ) {
                        Text(text = stringResource(R.string.grocery_list_type_todo))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { createListDialogIsShown = false }) {
                    Text(text = stringResource(R.string.close))
                }
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = TopAppBarActionsHorizontalPadding)
            ) {
                IconButton(
                    onClick = navigateToSettingsScreen
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
        }
        AndroidViewBinding(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            factory = { inflater, parent, attachToParent ->
                val binding = ListRecyclerviewBinding.inflate(inflater, parent, attachToParent)
                binding.listRecyclerview.addOnScrollListener(
                    object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(
                            recyclerView: RecyclerView,
                            dx: Int,
                            dy: Int
                        ) {
                            super.onScrolled(recyclerView, dx, dy)
                            scrollState += dy
                        }
                    }
                )
                binding.listRecyclerview.adapter = DashboardRecyclerViewAdapter(
                    recyclerView = binding.listRecyclerview,
                    groceryLists = groceryLists,
                    updateLists = { newValue ->
                        onIntent(GroceryListsDashboardUiIntent.OnUpdateGroceryLists(newValue))
                    },
                    onItemClicked = navigateToGroceryListScreen,
                    onAdderItemClicked = {
                        createListDialogIsShown = true
                    }
                )
                binding
            },
            update = {
                val adapter = listRecyclerview.adapter as DashboardRecyclerViewAdapter
                val isInitialUpdate = adapter.groceryLists.isEmpty()
                adapter.updateGroceryLists(groceryLists)
                // this code is required to keep scroll position when updating the list
                // otherwise recyclerView will automatically follow the AdderItem and scroll
                // to bottom once the list is fetched
                if (isInitialUpdate) {
                    val layoutManager = listRecyclerview.layoutManager as LinearLayoutManager
                    layoutManager.scrollToPositionWithOffset(0, -scrollState)
                }
            }
        )
    }
}
@Preview
@Composable
private fun GroceryListsDashboardPreview() {
    ShoppingGeniusTheme {
        GroceryListsDashboardScreen(
            groceryLists = sampleDashboard
        )
    }
}
val sampleDashboard = List(20) {
    GroceryList(
        id = it.toString(),
        name = "List $it",
        numOfGroceries = it
    )
}
