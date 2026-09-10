package com.shoppinggenius.app.network.data.sources.icon

import com.shoppinggenius.app.model.IconReference
import com.shoppinggenius.app.network.model.NetworkChangeList

interface IconNetworkDataSource {
    suspend fun downloadIcons(): List<IconReference>
    suspend fun downloadIconsByIds(ids: List<String>): List<IconReference>
    suspend fun getIconChangeList(after: Int): List<NetworkChangeList>
}
