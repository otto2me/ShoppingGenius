package com.rendox.shoppinggenius.network.data.sources.icon

import com.rendox.shoppinggenius.model.IconReference
import com.rendox.shoppinggenius.network.model.NetworkChangeList

interface IconNetworkDataSource {
    suspend fun downloadIcons(): List<IconReference>
    suspend fun downloadIconsByIds(ids: List<String>): List<IconReference>
    suspend fun getIconChangeList(after: Int): List<NetworkChangeList>
}
