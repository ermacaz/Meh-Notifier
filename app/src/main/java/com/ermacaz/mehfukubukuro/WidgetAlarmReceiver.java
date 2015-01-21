package com.ermacaz.mehfukubukuro;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

/**
 * MDme Android application
 * Author:: ermacaz (maito:mattahamada@gmail.com)
 * Created on:: 1/20/15
 * Copyright:: Copyright (c) 2014 MDme
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */
public class WidgetAlarmReceiver extends BroadcastReceiver {

    private SharedPreferences mPreferences;
    private Bitmap mBitmap;
    private Bitmap mLargeBitmap;
    private int mColor;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Alarm Running", Toast.LENGTH_LONG).show();
        mContext = context;
//        context.startService(new Intent(context, UpdateNotificationService.class));

        MehApi mehApi = new MehApi(mContext);
        ComponentName cn = new ComponentName(mContext, DefaultWidget.class);
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.notification_widget);
        AppWidgetManager appWidgetManager =  AppWidgetManager.getInstance(mContext);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(cn);
        mehApi.updateWidget(appWidgetIds, view);

    }
}