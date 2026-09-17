package com.shoppinggenius.app.database

import androidx.room.TypeConverter
import com.shoppinggenius.app.model.GroceryListType

class GroceryListTypeConverters {
    @TypeConverter
    fun fromGroceryListType(value: GroceryListType): String = value.name

    @TypeConverter
    fun toGroceryListType(value: String?): GroceryListType = GroceryListType.fromStorageValue(value)
}

