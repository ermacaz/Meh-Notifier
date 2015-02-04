package com.ermacaz.mehfukubukuro;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * MDme Android application
 * Author:: ermacaz (maito:mattahamada@gmail.com)
 * Created on:: 2/4/15
 * Copyright:: Copyright (c) 2014 MDme
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */
public class BootBroadCastReceiver extends BroadcastReceiver {
    private SharedPreferences mPreferences;

    @Override
    public void onReceive(Context pContext, Intent intent) {
        Intent alarmIntent = new Intent(pContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(pContext, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager)pContext.getSystemService(Context.ALARM_SERVICE);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(pContext);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        try {
            Date date = sdf.parse(mPreferences.getString("lookup_time", "10:00 PM"));
            cal.setTimeInMillis(date.getTime());
            if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            manager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
        catch (Exception e) {
            Toast.makeText(pContext, "Could not load time", Toast.LENGTH_SHORT).show();
        }


    }
}
