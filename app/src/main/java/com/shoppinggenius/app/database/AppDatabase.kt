package com.shoppinggenius.app.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shoppinggenius.app.database.category.CategoryDao
import com.shoppinggenius.app.database.category.CategoryEntity
import com.shoppinggenius.app.database.grocery.GroceryDao
import com.shoppinggenius.app.database.grocery.GroceryEntity
import com.shoppinggenius.app.database.groceryicon.IconDao
import com.shoppinggenius.app.database.groceryicon.IconEntity
import com.shoppinggenius.app.database.grocerylist.GroceryListDao
import com.shoppinggenius.app.database.grocerylist.GroceryListEntity
import com.shoppinggenius.app.database.product.ProductDao
import com.shoppinggenius.app.database.product.ProductEntity

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
