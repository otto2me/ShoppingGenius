package com.rendox.shoppinggenius.ui.components.collapsingtoolbar

import androidx.compose.foundation.gestures.ScrollableState

interface CollapsingToolbarScaffoldScrollableState : ScrollableState {
    val firstVisibleItemIndex: Int
    val firstVisibleItemScrollOffset: Int
}
