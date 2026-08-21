package com.rendox.shoppinggenius.feature.widget

import android.content.Intent
import android.widget.RemoteViewsService

class ActiveGroceryListRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ActiveGroceryListRemoteViewsFactory(applicationContext)
    }
}


