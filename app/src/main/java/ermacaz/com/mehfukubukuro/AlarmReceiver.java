package ermacaz.com.mehfukubukuro;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import com.savagelook.android.UrlJsonAsyncTask;

/**
 * Author:: ermacaz (maito:mattahamada@gmail.com)
 * Created on:: 1/7/15
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String MEH_API = "6k9OGNDA6zqynKI9QIIe4rfkobKZeCBw";

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

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.abc_ic_menu_paste_mtrl_am_alpha)
                        .setContentTitle(title)
                        .setContentText(description);
               // Intent resultIntent = new Intent(context, SettingsActivity.class);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.meh.com"));
                PendingIntent pIntent = PendingIntent.getActivity(context,
                        0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pIntent);

//                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                stackBuilder.addParentStack(SettingsActivity.class);
//                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());


            }
            catch(Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MEH", e.getMessage());
            }

        }

    }
}
