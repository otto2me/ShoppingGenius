package com.shoppinggenius.app.data.model

import com.shoppinggenius.app.database.groceryicon.IconEntity
import com.shoppinggenius.app.model.IconReference

fun IconReference.asEntity() = IconEntity(
    uniqueFileName = uniqueFileName,
    filePath = filePath
)
