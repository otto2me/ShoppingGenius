package com.shoppinggenius.app.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE CategoryEntity ADD COLUMN iconFileName TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ProductEntity ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE GroceryListEntity ADD COLUMN type TEXT NOT NULL DEFAULT 'SHOPPING'")
        db.execSQL("ALTER TABLE ProductEntity ADD COLUMN showInCatalog INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE ProductEntity ADD COLUMN ownerGroceryListId TEXT")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_ProductEntity_ownerGroceryListId` ON `ProductEntity` (`ownerGroceryListId`)"
        )
    }
}

