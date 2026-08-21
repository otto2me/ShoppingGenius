package com.rendox.shoppinggenius.data.model

import com.rendox.shoppinggenius.database.groceryicon.IconEntity
import com.rendox.shoppinggenius.model.IconReference

fun IconReference.asEntity() = IconEntity(
    uniqueFileName = uniqueFileName,
    filePath = filePath
)
