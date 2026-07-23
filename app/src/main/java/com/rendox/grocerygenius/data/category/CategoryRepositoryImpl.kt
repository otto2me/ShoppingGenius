package com.rendox.grocerygenius.data.category

import com.rendox.grocerygenius.data.Synchronizer
import com.rendox.grocerygenius.data.changeListSync
import com.rendox.grocerygenius.data.model.asEntity
import com.rendox.grocerygenius.data.model.asExternalModel
import com.rendox.grocerygenius.database.category.CategoryEntity
import com.rendox.grocerygenius.database.category.CategoryDao
import com.rendox.grocerygenius.model.Category
import com.rendox.grocerygenius.network.data.sources.category.CategoryNetworkDataSource
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val categoryNetworkDataSource: CategoryNetworkDataSource
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories().map { categories ->
        categories.map { categoryEntity ->
            categoryEntity.asExternalModel()
        }
    }

    override fun getCategoryById(id: String): Flow<Category?> = categoryDao.getCategoryById(id).map { categoryEntity ->
        categoryEntity?.asExternalModel()
    }

    override suspend fun createCategory(name: String): Category {
        val sortingPriority = categoryDao.getNextSortingPriority()
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            sortingPriority = sortingPriority,
            defaultSortingPriority = sortingPriority,
            icon = null
        )
        categoryDao.insertCategories(
            listOf(
                CategoryEntity(
                    id = category.id,
                    name = category.name,
                    defaultSortingPriority = category.defaultSortingPriority,
                    sortingPriority = category.sortingPriority,
                    iconFileName = null
                )
            )
        )
        return category
    }

    override suspend fun updateCategories(categories: List<Category>) {
        categoryDao.updateCategories(categories.map { it.asEntity() })
    }

    override suspend fun updateCategoryName(
        categoryId: String,
        name: String
    ) {
        categoryDao.updateCategoryName(categoryId = categoryId, name = name)
    }

    override suspend fun updateCategoryIcon(
        categoryId: String,
        iconId: String?
    ) {
        categoryDao.updateCategoryIcon(categoryId = categoryId, iconId = iconId)
    }

    override suspend fun syncWith(synchronizer: Synchronizer) = synchronizer.changeListSync(
        prepopulateWithInitialData = {
            val categories = categoryNetworkDataSource.getAllCategories()
            categoryDao.upsertCategories(categories.map { it.asEntity() })
        },
        versionReader = { it.categoryVersion },
        changeListFetcher = { currentVersion ->
            categoryNetworkDataSource.getCategoryChangeList(after = currentVersion)
        },
        versionUpdater = { latestVersion ->
            copy(categoryVersion = latestVersion)
        },
        modelDeleter = { categoryIds ->
            categoryDao.deleteCategories(categoryIds)
        },
        modelUpdater = { changedIds ->
            val networkCategories =
                categoryNetworkDataSource.getCategoriesByIds(ids = changedIds)
            val localCategoriesById = categoryDao.getCategoriesByIds(changedIds).associateBy { it.id }
            categoryDao.upsertCategories(
                networkCategories.map { networkCategory ->
                    networkCategory.asEntity(
                        iconFileName = localCategoriesById[networkCategory.id]?.iconFileName
                    )
                }
            )
        }
    )
}