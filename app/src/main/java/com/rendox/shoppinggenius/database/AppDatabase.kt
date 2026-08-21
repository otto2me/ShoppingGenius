package com.rendox.shoppinggenius.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rendox.shoppinggenius.database.category.CategoryDao
import com.rendox.shoppinggenius.database.category.CategoryEntity
import com.rendox.shoppinggenius.database.grocery.GroceryDao
import com.rendox.shoppinggenius.database.grocery.GroceryEntity
import com.rendox.shoppinggenius.database.groceryicon.IconDao
import com.rendox.shoppinggenius.database.groceryicon.IconEntity
import com.rendox.shoppinggenius.database.grocerylist.GroceryListDao
import com.rendox.shoppinggenius.database.grocerylist.GroceryListEntity
import com.rendox.shoppinggenius.database.product.ProductDao
import com.rendox.shoppinggenius.database.product.ProductEntity

@Database(
    entities = [
        CategoryEntity::class,
        GroceryEntity::class,
        GroceryListEntity::class,
        ProductEntity::class,
        IconEntity::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun groceryDao(): GroceryDao
    abstract fun groceryListDao(): GroceryListDao
    abstract fun productDao(): ProductDao
    abstract fun iconDao(): IconDao
}
