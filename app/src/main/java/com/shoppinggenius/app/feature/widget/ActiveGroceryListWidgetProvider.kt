package com.shoppinggenius.app.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.shoppinggenius.app.MainActivity
import com.shoppinggenius.app.R
import com.shoppinggenius.app.database.grocery.WidgetGroceryItem
import dagger.hilt.android.EntryPointAccessors
import java.io.File
import kotlinx.coroutines.runBlocking

class ActiveGroceryListWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildRemoteViews(context, appWidgetId)
            )
            notifyWidgetDataChanged(appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            refreshAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.shoppinggenius.app.widget.REFRESH"

        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ActiveGroceryListWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return

            ids.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    buildRemoteViews(context, appWidgetId)
                )
                notifyWidgetDataChanged(appWidgetManager, appWidgetId)
            }
        }

        private fun buildRemoteViews(context: Context, appWidgetId: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_active_grocery_list)

            val launchIntent = Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val launchPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                launchIntent,
                pendingIntentFlags
            )
            views.setOnClickPendingIntent(R.id.widget_header_container, launchPendingIntent)
            views.setPendingIntentTemplate(R.id.widget_grocery_list, launchPendingIntent)

            val widgetData = runBlocking {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                resolveActiveWidgetListData(
                    userPreferencesDataSource = entryPoint.userPreferencesDataSource(),
                    groceryListDao = entryPoint.groceryListDao(),
                    groceryDao = entryPoint.groceryDao()
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                views.setRemoteAdapter(
                    R.id.widget_grocery_list,
                    buildRemoteCollectionItems(context, widgetData.items)
                )
            } else {
                setLegacyRemoteAdapter(context, views, appWidgetId)
            }

            val alpha = (widgetData.backgroundOpacityPercent.coerceIn(0, 100) * 255) / 100
            views.setInt(R.id.widget_root, "setBackgroundColor", Color.argb(alpha, 0x06, 0x33, 0x5C))

            views.setTextViewText(
                R.id.widget_title,
                widgetData.listName ?: context.getString(R.string.widget_active_list_fallback_title)
            )
            views.setEmptyView(R.id.widget_grocery_list, R.id.widget_empty_text)

            return views
        }

        @Suppress("DEPRECATION")
        private fun setLegacyRemoteAdapter(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int
        ) {
            val serviceIntent = Intent(context, ActiveGroceryListRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_grocery_list, serviceIntent)
        }

        @Suppress("DEPRECATION")
        private fun notifyWidgetDataChanged(
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_grocery_list)
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        private fun buildRemoteCollectionItems(
            context: Context,
            items: List<WidgetGroceryItem>
        ): RemoteViews.RemoteCollectionItems {
            return RemoteViews.RemoteCollectionItems.Builder()
                .setHasStableIds(true)
                .setViewTypeCount(1)
                .apply {
                    items.forEachIndexed { index, item ->
                        addItem(
                            "${item.productId}:$index".hashCode().toLong(),
                            buildItemRemoteViews(context, item)
                        )
                    }
                }
                .build()
        }

        private fun buildItemRemoteViews(
            context: Context,
            item: WidgetGroceryItem
        ): RemoteViews {
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
                    Intent()
                )
            }
        }
    }
}
