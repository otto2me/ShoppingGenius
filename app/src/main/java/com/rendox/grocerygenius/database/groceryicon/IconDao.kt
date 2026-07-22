package com.rendox.grocerygenius.database.groceryicon

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.rendox.grocerygenius.database.category.CombinedCategory
import com.rendox.grocerygenius.model.IconReference
import kotlinx.coroutines.flow.Flow

@Dao
abstract class IconDao {
    @Insert
    abstract suspend fun insertGroceryIcons(groceryIconEntities: List<IconEntity>)

    @Upsert
    abstract suspend fun upsertGroceryIcons(groceryIconEntities: List<IconEntity>)

    @Upsert
    abstract suspend fun upsertGroceryIcon(groceryIconEntity: IconEntity)

    @Query(
        """
        SELECT 
        COALESCE(MIN(c.id), 'uncategorized') AS id,
        COALESCE(MIN(c.name), 'Uncategorized') AS name,
        COALESCE(MIN(c.sortingPriority), 9223372036854775807) AS sortingPriority,
        COALESCE(MIN(c.defaultSortingPriority), 9223372036854775807) AS defaultSortingPriority,
        null AS iconId,
        null AS iconFilePath,
        i.uniqueFileName,
        i.filePath,
        COALESCE(MIN(p.name), i.uniqueFileName) AS name
        FROM IconEntity i
        LEFT JOIN ProductEntity p ON i.uniqueFileName = p.iconFileName AND p.isDefault IS 1
        LEFT JOIN CategoryEntity c ON p.categoryId = c.id
        GROUP BY i.uniqueFileName
    """
    )
    abstract fun getIconsGroupedByCategory(): Flow<Map<CombinedCategory, List<IconReference>>>

    @Query(
        """
        SELECT 
        i.uniqueFileName,
        i.filePath,
        COALESCE(MIN(p.name), i.uniqueFileName) AS name
        FROM IconEntity i
        LEFT JOIN ProductEntity p ON i.uniqueFileName = p.iconFileName
        WHERE LOWER(COALESCE(p.name, i.uniqueFileName)) LIKE LOWER(:name)
        GROUP BY i.uniqueFileName, i.filePath
        """
    )
    abstract suspend fun getGroceryIconsByName(name: String): List<IconReference>

    @Query(
        """
            DELETE FROM IconEntity
            WHERE uniqueFileName in (:ids)
        """
    )
    abstract suspend fun deleteIcons(ids: List<String>)

    /** Returns only user-uploaded icons (filename starts with "custom_"). */
    @Query("SELECT * FROM IconEntity WHERE uniqueFileName LIKE 'custom_%'")
    abstract suspend fun getCustomIconEntities(): List<IconEntity>
}