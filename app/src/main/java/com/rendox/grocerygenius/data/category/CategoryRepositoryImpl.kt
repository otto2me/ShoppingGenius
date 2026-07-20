package com.rendox.grocerygenius.data.category

import com.rendox.grocerygenius.data.Synchronizer
import com.rendox.grocerygenius.data.changeListSync
import com.rendox.grocerygenius.data.model.asEntity
import com.rendox.grocerygenius.data.model.asExternalModel
import com.rendox.grocerygenius.database.category.CategoryDao
import com.rendox.grocerygenius.model.Category
import com.rendox.grocerygenius.network.data.sources.category.CategoryNetworkDataSource
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

    override suspend fun updateCategories(categories: List<Category>) {
        categoryDao.updateCategories(categories.map { it.asEntity() })
    }

    override suspend fun updateCategoryName(categoryId: String, name: String) {
        categoryDao.updateCategoryName(categoryId = categoryId, name = name)
    }

    override suspend fun updateCategoryIcon(categoryId: String, iconId: String?) {
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