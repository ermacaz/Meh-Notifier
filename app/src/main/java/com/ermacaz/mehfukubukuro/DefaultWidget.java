package com.ermacaz.mehfukubukuro;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Author:: ermacaz (maito:mattahamada@gmail.com)
 * Created on:: 1/19/15
 */
public class DefaultWidget extends AppWidgetProvider {

    private Context mContext;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        mContext = context;
//        context.startService(new Intent(context, UpdateWidgetService.class));
        MehApi mehApi = new MehApi(mContext);
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.notification_widget);
        mehApi.updateWidget(view);
    }

//    public class UpdateWidgetService extends Service {
//
//        @Override
//        public void onStart(Intent intent, int startId) {
//            MehApi mehApi = new MehApi(mContext);
//            RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.notification_widget);
//            mehApi.updateWidget(view);
//        }
//
//        @Override
//        public IBinder onBind(Intent intent) {
//            return null;
//        }
//    }
}
