package ${package}.Helpers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import ${package}.Activities.TabBarActivity;
import ${package}.R;
import ${package}.RestAPI;
import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONObject;


public class GCMRegistrationService extends Service {
    public static final String ACTION_GCM_START = "${package}.GCM_START";
    public static final String ACTION_GCM_STOP = "${package}.GCM_STOP";
    public static final String ACTION_GCM_REGISTERED = "${package}.GCM_REGISTERED";
    public static final String GCM_REGISTRATION_ID = "${package}.GCM_REGISTRATION_ID";
    public static final String GCM_SENDER_ID = "${package}.GCM_SENDER_ID";

    private final IBinder mBinder = new LocalBinder();
    private boolean mPushNotifications;
    private String mPushRegistrationId;
    private String mPushSenderId;

    private static Context mContext;
    private static final Object LOCK = new Object();

    public class LocalBinder extends Binder {
        GCMRegistrationService getService() {
            return GCMRegistrationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            GCMRegistrar.onDestroy(this);
        }
        catch (IllegalArgumentException e) {
            // ignore "unable to unregister receiver"
        }
        mPushSenderId = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_GCM_START.equals(action)) {
                mPushSenderId = intent.getStringExtra(GCM_SENDER_ID);
                setPushNotifications(true);
            }
            else if (ACTION_GCM_STOP.equals(action)) {
                setPushNotifications(false);
            }
            else if (ACTION_GCM_REGISTERED.equals(action)) {
                String regId = intent.getStringExtra(GCM_REGISTRATION_ID);
                AuthHelper.setGcmToken(regId);
                registerOnServer(regId);
            }
        }
        return START_STICKY;
    }

    public static void enablePushNotifications(Context context) {
        synchronized (LOCK) {
            mContext = context;
        }

        if (ConnectionDetector.isConnectedToInternet(context)) {
            Intent i = new Intent(context, GCMRegistrationService.class);
            i.setAction(GCMRegistrationService.ACTION_GCM_START);
            i.putExtra(GCM_SENDER_ID, context.getResources().getString(R.string.gcm_sender_id));
            context.startService(i);
        }
    }

    public static void disablePushNotifications(Context context) {
        synchronized (LOCK) {
            mContext = context;
        }
        Intent i = new Intent(context, GCMRegistrationService.class);
        i.setAction(GCMRegistrationService.ACTION_GCM_STOP);
        context.startService(i);
    }

    public static void registerPushNotifications(Context context, String registrationId) {
        synchronized (LOCK) {
            mContext = context;
        }

        Intent i = new Intent(context, GCMRegistrationService.class);
        i.setAction(ACTION_GCM_REGISTERED);
        i.putExtra(GCM_REGISTRATION_ID, registrationId);
        context.startService(i);
    }

    public void setPushNotifications(boolean enabled) {
        mPushNotifications = enabled;
        if (mPushNotifications) {
            if (mPushRegistrationId == null) {
                gcmRegister();
            }
        }
        else {
            gcmUnregister();
        }
    }

    private void gcmRegister() {
        if (mPushSenderId != null) {
            try {
                GCMRegistrar.checkDevice(this);
                mPushRegistrationId = GCMRegistrar.getRegistrationId(this);
                if (TextUtils.isEmpty(mPushRegistrationId))
                    GCMRegistrar.register(this, mPushSenderId);
                else {
                    AuthHelper.setGcmToken(mPushRegistrationId);
                    registerOnServer(mPushRegistrationId);
                }
            }
            catch (Exception e) {
                // nothing happens...
            }

        }
    }

    private void gcmUnregister() {
        if (GCMRegistrar.isRegistered(this)) {
            GCMRegistrar.unregister(this);
        }
        else {
            AuthHelper.setGcmToken(null);
        }
    }

    private void registerOnServer(String pushId) {
        synchronized (LOCK) {
            RestAPI.pushIdRegistration(pushId, new JsonHttpResponseHandler() {
                @Override
                protected void handleFailureMessage(Throwable throwable, String s) {
                    super.handleFailureMessage(throwable, s);
                    GCMRegistrar.setRegisteredOnServer(mContext, false);
                }

                @Override
                public void onFailure(Throwable throwable, JSONObject jsonObject) {
                    super.onFailure(throwable, jsonObject);
                    GCMRegistrar.setRegisteredOnServer(mContext, false);
                }

                @Override
                public void onSuccess(JSONObject jsonObject) {
                    super.onSuccess(jsonObject);
                    GCMRegistrar.setRegisteredOnServer(mContext, true);
                }
            }, mContext);
        }
    }
}
