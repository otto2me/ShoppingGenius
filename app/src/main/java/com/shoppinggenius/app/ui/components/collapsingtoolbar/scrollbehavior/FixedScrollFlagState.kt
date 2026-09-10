package com.shoppinggenius.app.ui.components.collapsingtoolbar.scrollbehavior

abstract class FixedScrollFlagState(heightRange: IntRange) : ScrollFlagState(heightRange) {

    final override val offset: Float = 0f
}
