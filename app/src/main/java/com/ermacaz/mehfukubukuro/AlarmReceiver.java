package com.ermacaz.mehfukubukuro;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import com.savagelook.android.UrlJsonAsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Author:: ermacaz (maito:mattahamada@gmail.com)
 * Created on:: 1/7/15
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String MEH_API = "6k9OGNDA6zqynKI9QIIe4rfkobKZeCBw";

    private ImageView mNotifImageView;
    private SharedPreferences mPreferences;
    private Bitmap mBitmap;
    private Bitmap mLargeBitmap;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Alarm Running", Toast.LENGTH_LONG).show();
        loadMehFromApi(context);
    }

    private void loadMehFromApi(Context context) {
        GetMehTask getMehTask = new GetMehTask(context);
        getMehTask.setMessageLoading("Loading Meh deal...");
        getMehTask.execute("https://api.meh.com/1/current.json?apikey=" + MEH_API);
    }

    private class GetMehTask extends UrlJsonAsyncTask {
        public GetMehTask(Context context) { super(context);}

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONObject deal = json.getJSONObject("deal");
                String title = deal.getString("title");
                String description = deal.getString("features");
                JSONArray items = deal.getJSONArray("items");
                String photoUrl = items.getJSONObject(0).getString("photo");



                //new DownloadImageTask(expandedView, smallView).execute(photoUrl);


                try {
                    mBitmap = BitmapFactory.decodeStream(
                            (InputStream) new URL(photoUrl).getContent());
                    mLargeBitmap = Bitmap.createScaledBitmap(mBitmap, 450, 450, false);
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, 150, 150, false);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                        //.setDefaults(Notification.DEFAULT_ALL)
                        .setTicker(title)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setLargeIcon(mBitmap)
                        .setContentTitle(title)
                        .setContentText(description);
                       // .setStyle(pictureStyle);

                RemoteViews smallView = new RemoteViews(context.getPackageName(),
                        R.layout.notification);
                smallView.setTextViewText(R.id.textView,title);

                RemoteViews expandedView = new RemoteViews(context.getPackageName(),
                        R.layout.notification_expanded);
                expandedView.setTextViewText(R.id.notificationTextView, description);
                expandedView.setTextViewText(R.id.notificationTitleTextView, title);
                expandedView.setImageViewBitmap(R.id.notificationBigPictureView, mLargeBitmap);
                //expandedView.setTextViewText(R.id.notificationTextTitleView, title);

                mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean enableSound = mPreferences.getBoolean("soundEnabled", false);
                if (enableSound) {
                    String ringtoneStr = mPreferences.getString("ringtone", "DEFAULT_SOUND");
                    mBuilder.setSound(Uri.parse(ringtoneStr));
                }
                boolean enableVibrate = mPreferences.getBoolean("vibrateEnabled", true);
                if (enableVibrate) {
                    long[] pattern = {500,500,500};
                    mBuilder.setVibrate(pattern);
                }

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://meh.com"));
                PendingIntent pIntent = PendingIntent.getActivity(context,
                        0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pIntent);

                Notification notification = mBuilder.build();
                 notification.bigContentView = expandedView;
                 //notification.contentView = smallView;

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, notification);


            }
            catch(Exception e) {
                Toast.makeText(context, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                //Log.e("MEH", "error: " + e.getMessage());
            }

        }

    }

//    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//
//        private RemoteViews mExpandedView;
//        private RemoteViews mSmallView;
//
//        public DownloadImageTask(RemoteViews expandedView, RemoteViews smallView) {
//            this.mExpandedView = expandedView;
//            this.mSmallView = smallView;
//        }
//
//        protected Bitmap doInBackground(String... urls) {
//            String urldisplay = urls[0];
//            Bitmap bitmap = null;
//            Bitmap scaledBitmap = null;
//            try {
//                InputStream in = new java.net.URL(urldisplay).openStream();
//                bitmap = BitmapFactory.decodeStream(in);
//                if (bitmap != null) {
//                    scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
//                }
//            } catch (Exception e) {
//                Log.e("Error", "image download error");
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//            return scaledBitmap;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            //set image of your imageview
//            mSmallView.setImageViewBitmap(R.id.imageView, result);
//        }
//    }
}
