package com.rendox.shoppinggenius.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.RemoteViews
import com.rendox.shoppinggenius.MainActivity
import com.rendox.shoppinggenius.R
import dagger.hilt.android.EntryPointAccessors
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
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_grocery_list)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            refreshAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.rendox.shoppinggenius.widget.REFRESH"

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
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_grocery_list)
            }
        }

        private fun buildRemoteViews(context: Context, appWidgetId: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_active_grocery_list)

            val serviceIntent = Intent(context, ActiveGroceryListRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_grocery_list, serviceIntent)

            val launchIntent = Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .setData(Uri.parse("ShoppingGenius://widget/$appWidgetId"))
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

            val alpha = (widgetData.backgroundOpacityPercent.coerceIn(0, 100) * 255) / 100
            views.setInt(R.id.widget_root, "setBackgroundColor", Color.argb(alpha, 0x06, 0x33, 0x5C))

            views.setTextViewText(
                R.id.widget_title,
                widgetData.listName ?: context.getString(R.string.widget_active_list_fallback_title)
            )
            views.setEmptyView(R.id.widget_grocery_list, R.id.widget_empty_text)

            return views
        }
    }
}

