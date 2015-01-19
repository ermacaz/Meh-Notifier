package com.ermacaz.mehfukubukuro;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;

/**
 * Author:: ermacaz (maito:mattahamada@gmail.com)
 * Created on:: 1/19/15
 */

//TODO set this class to get meh info to be called from alarm or widget
public class MehApi {

    private Context mContext;
    private String API_KEY = "6k9OGNDA6zqynKI9QIIe4rfkobKZeCBw";

    private SharedPreferences mPreferences;
    private Bitmap mBitmap;
    private Bitmap mLargeBitmap;
    private int mColor;
    private RemoteViews mWidgetView;

    public MehApi(Context context) {
        mContext = context;
    }

    private void getImages(String photoUrl) {
        try {
            mBitmap = BitmapFactory.decodeStream(
                    (InputStream) new URL(photoUrl).getContent());
            mLargeBitmap = Bitmap.createScaledBitmap(mBitmap, 450, 450, false);
            mBitmap = Bitmap.createScaledBitmap(mBitmap, 150, 150, false);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void launchNotification() {
        GetMehNotification getMehNotification = new GetMehNotification(mContext);
        getMehNotification.execute("https://api.meh.com/1/current.json?apikey=" + API_KEY);
    }

    public void updateWidget(RemoteViews view) {
        mWidgetView = view;
        GetMehWidget getMehWidget= new GetMehWidget(mContext);
        getMehWidget.execute("https://api.meh.com/1/current.json?apikey=" + API_KEY);
    }

    private class GetMehWidget extends UrlJsonAsyncTask {
        public GetMehWidget(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.has("success")) {
                    Toast.makeText(context, "Could not contact meh.com Server may be busy", Toast.LENGTH_LONG).show();
                } else {
                    JSONObject deal = json.getJSONObject("deal");
                    String title = deal.getString("title");
                    String description = deal.getString("features");
                    JSONArray items = deal.getJSONArray("items");
                    String photoUrl = items.getJSONObject(0).getString("photo");
                    String price = items.getJSONObject(0).getString("price");
                    getImages(photoUrl);

                    mWidgetView.setTextViewText(R.id.widgetTextView, description);
                    mWidgetView.setTextViewText(R.id.widgetTitleTextView, title + " ($" + price + ")");
                    mWidgetView.setImageViewBitmap(R.id.notificationBigPictureView, mLargeBitmap);
                    mWidgetView.setTextColor(R.id.notificationTitleTextView, mColor);
                    mWidgetView.setTextColor(R.id.notificationTextView, mColor);


                }
            } catch (Exception e) {
                Log.e("WIDGET", e.getMessage());
            }
        }
    }
    private class GetMehNotification extends UrlJsonAsyncTask {
        public GetMehNotification(Context context) { super(context); }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.has("success")) {
                    Toast.makeText(context, "Could not contact meh.com Server may be busy", Toast.LENGTH_LONG).show();
                }
                else {
                    JSONObject deal = json.getJSONObject("deal");
                    String title = deal.getString("title");
                    String description = deal.getString("features");
                    JSONArray items = deal.getJSONArray("items");
                    String photoUrl = items.getJSONObject(0).getString("photo");
                    String price = items.getJSONObject(0).getString("price");

//                RemoteViews smallView = new RemoteViews(context.getPackageName(),
//                R.layout.notification);
//                smallView.setTextViewText(R.id.textView,title);

                    getImages(photoUrl);
                    NotificationCompat.Builder mBuilder = configureNotification(title, description);
                    RemoteViews expandedView = buildExpandedView(title, description, price);

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://meh.com"));
                    PendingIntent pIntent = PendingIntent.getActivity(context,
                            0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pIntent);

                    Notification notification = mBuilder.build();
                    notification.bigContentView = expandedView;
                    //notification.contentView = smallView;

                    //dismiss notificaiton on click
                    notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

                    //enable vibrate
                    boolean enableVibrate = mPreferences.getBoolean("vibrateEnabled", true);
                    if (enableVibrate) {
                        notification.defaults |= Notification.DEFAULT_VIBRATE;
                    }

                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(0, notification);
                }
            }
            catch(Exception e) {
                Toast.makeText(context, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                //Log.e("MEH", "error: " + e.getMessage());
            }

        }

        private NotificationCompat.Builder configureNotification(String title, String description) {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context)
                            //.setDefaults(Notification.DEFAULT_ALL)
                            .setTicker(title)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setLargeIcon(mBitmap)
                            .setContentTitle(title)
                            .setContentText(description);

            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            mColor = Color.parseColor(mPreferences.getString("textColor", "#ffffff"));
//            builder.setColor(mColor);


            boolean enableSound = mPreferences.getBoolean("soundEnabled", false);
            if (enableSound) {
                String ringtoneStr = mPreferences.getString("ringtone", "DEFAULT_SOUND");
                builder.setSound(Uri.parse(ringtoneStr));
            }
            return builder;
        }

        private RemoteViews buildExpandedView(String title, String description, String price) {

            RemoteViews expandedView = new RemoteViews(context.getPackageName(),
                    R.layout.notification_expanded);
            expandedView.setTextViewText(R.id.notificationTextView, description);
            expandedView.setTextViewText(R.id.notificationTitleTextView, title + " ($" + price + ")");
            expandedView.setImageViewBitmap(R.id.notificationBigPictureView, mLargeBitmap);


            expandedView.setTextColor(R.id.notificationTitleTextView, mColor);
            expandedView.setTextColor(R.id.notificationTextView, mColor);
            return expandedView;
        }



    }
}
