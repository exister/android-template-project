package ${package}.Helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectionDetector extends BroadcastReceiver {
    private static final String TAG = ConnectionDetector.class.getSimpleName();
    private static final int ACTION_START = 1;
    private static final int ACTION_STOP = 2;

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                return info.isConnected();
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        int serviceAction = 0;

        // connectivity status has changed
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            final NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                Log.w(TAG, "network state changed!");
                switch (info.getState()) {
                    case CONNECTED:
                        serviceAction = ACTION_START;
                        break;
                    default:
                        serviceAction = ACTION_STOP;
                        break;
                }
            }
            else {
                serviceAction = ACTION_STOP;
            }
        }

        if (serviceAction == ACTION_START) {
            GCMRegistrationService.enablePushNotifications(context);
        }
        else if (serviceAction == ACTION_STOP) {
            GCMRegistrationService.disablePushNotifications(context);
        }
    }
}
