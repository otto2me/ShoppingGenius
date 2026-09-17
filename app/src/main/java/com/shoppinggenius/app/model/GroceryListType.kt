package com.shoppinggenius.app.model

enum class GroceryListType {
    SHOPPING,
    TODO;

    companion object {
        fun fromStorageValue(value: String?): GroceryListType = entries.firstOrNull {
            it.name == value
        } ?: SHOPPING
    }
}

