package com.rendox.grocerygenius.database.category

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Query(
        """
        SELECT
            category.id,
            category.name,
            category.sortingPriority,
            category.defaultSortingPriority,
            icon.uniqueFileName AS iconId,
            icon.filePath AS iconFilePath
        FROM CategoryEntity category
        LEFT JOIN IconEntity icon ON category.iconFileName = icon.uniqueFileName
        """
    )
    fun getAllCategories(): Flow<List<CombinedCategory>>

    @Query(
        """
        SELECT
            category.id,
            category.name,
            category.sortingPriority,
            category.defaultSortingPriority,
            icon.uniqueFileName AS iconId,
            icon.filePath AS iconFilePath
        FROM CategoryEntity category
        LEFT JOIN IconEntity icon ON category.iconFileName = icon.uniqueFileName
        WHERE category.id = :id
        """
    )
    fun getCategoryById(id: String): Flow<CombinedCategory?>

    @Query("SELECT * FROM CategoryEntity WHERE id in (:ids)")
    suspend fun getCategoriesByIds(ids: List<String>): List<CategoryEntity>

    @Update
    suspend fun updateCategories(categories: List<CategoryEntity>)

    @Query("UPDATE CategoryEntity SET name = :name WHERE id = :categoryId")
    suspend fun updateCategoryName(
        categoryId: String,
        name: String
    )

    @Query("UPDATE CategoryEntity SET iconFileName = :iconId WHERE id = :categoryId")
    suspend fun updateCategoryIcon(
        categoryId: String,
        iconId: String?
    )

    @Query(
        """
            DELETE FROM CategoryEntity
            WHERE id in (:ids)
        """
    )
    suspend fun deleteCategories(ids: List<String>)
}