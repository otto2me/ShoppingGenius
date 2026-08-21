package com.rendox.shoppinggenius.feature.widget

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.rendox.shoppinggenius.R
import com.rendox.shoppinggenius.database.grocery.WidgetGroceryItem
import dagger.hilt.android.EntryPointAccessors
import java.io.File
import kotlinx.coroutines.runBlocking

class ActiveGroceryListRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<WidgetGroceryItem> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        items = runBlocking {
            resolveActiveWidgetListData(
                userPreferencesDataSource = entryPoint.userPreferencesDataSource(),
                groceryListDao = entryPoint.groceryListDao(),
                groceryDao = entryPoint.groceryDao()
            ).items
        }
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position !in items.indices) {
            return RemoteViews(context.packageName, R.layout.widget_active_grocery_item)
        }

        val item = items[position]
        return RemoteViews(context.packageName, R.layout.widget_active_grocery_item).apply {
            setTextViewText(R.id.widget_item_name, item.name)
            val description = item.description?.takeIf { it.isNotBlank() }
            setTextViewText(R.id.widget_item_description, description ?: "")
            setViewVisibility(
                R.id.widget_item_description,
                if (description != null) View.VISIBLE else View.GONE
            )

            val iconBitmap = item.iconFilePath
                ?.let { File(context.filesDir, it) }
                ?.takeIf { it.exists() }
                ?.let { BitmapFactory.decodeFile(it.absolutePath) }

            if (iconBitmap != null) {
                setImageViewBitmap(R.id.widget_item_icon, iconBitmap)
            } else {
                setImageViewResource(R.id.widget_item_icon, R.drawable.baseline_folder_24)
            }

            setFloat(R.id.widget_item_root, "setAlpha", if (item.purchased) 0.6f else 1f)
            setOnClickFillInIntent(
                R.id.widget_item_root,
                Intent(Intent.ACTION_VIEW).setData(
                    android.net.Uri.parse("ShoppingGenius://widget/item/${item.productId}")
                )
            )
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return items.getOrNull(position)?.productId?.hashCode()?.toLong() ?: position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}




