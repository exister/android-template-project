package ${package};

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import ${package}.Activities.TabBarActivity;
import ${package}.Helpers.AuthHelper;
import ${package}.Helpers.GCMRegistrationService;
import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends GCMBaseIntentService {
    @Override
    protected String[] getSenderIds(Context context) {
        String[] ids = new String[1];
        ids[0] = context.getResources().getString(R.string.gcm_sender_id);
        return ids;
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        generateNotification(context, intent.getStringExtra("alert"));
    }

    @Override
    protected void onError(Context context, String s) {

    }

    @Override
    protected void onRegistered(Context context, String s) {
        GCMRegistrationService.registerPushNotifications(context, s);
    }

    @Override
    protected void onUnregistered(Context context, String s) {
        GCMRegistrationService.registerPushNotifications(context, null);
    }

    protected static void generateNotification(Context context, String message) {
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        String title = context.getString(R.string.app_name);

        Intent notificationIntent = new Intent(context, TabBarActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setTicker(message)
                .setWhen(when)
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(intent);

        notificationManager.notify(0, notificationBuilder.getNotification());
    }
}
