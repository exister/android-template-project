package ${package}.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import ${package}.Activities.TabBarActivity;
import ${package}.RestAPI;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.perm.kate.api.User;
import org.apache.http.client.HttpResponseException;

public class AuthHelper {
    public final static String ACTION_401_UNAUTHORIZED = "${package}.UNAUTHORIZED";

    private static String USER_PREFERENCES = "USER";

    private static GraphUser facebookUser;
    private static User vkontakteUser;
    private static Integer balance;
    private static String gcmToken;
    private static String token;
    private static Boolean deviceRegistered;
    private static Boolean phoneConfirmed;
    private static Boolean welcomeScreenShown;
    private static String phoneNumber;

    public static void authenticate(Context context) {
        AuthHelper.setIsLoggedIn(true, context);
        RestAPI.setAuth(AuthHelper.getToken());
    }

    public static void logout(Context context) {
        AuthHelper.setIsLoggedIn(false, context);
        RestAPI.setAuth(null);
        AuthHelper.setToken(null, context);
        Session session = Session.getActiveSession();
        if (session != null) {
            session.closeAndClearTokenInformation();
        }
    }

    public static boolean checkFor401(Throwable throwable) {
        if (throwable instanceof HttpResponseException) {
            if (((HttpResponseException) throwable).getStatusCode() == 401) {
                return true;
            }
        }
        return false;
    }

    public static void handle401(Context context) {
        Intent intent = new Intent();
        intent.setAction(AuthHelper.ACTION_401_UNAUTHORIZED);
        context.sendBroadcast(intent);
    }

    public static void setIsLoggedIn(boolean isLoggedIn, Context context) {
        SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("logged_in", isLoggedIn);
        editor.commit();
        if (!isLoggedIn) {
            AuthHelper.setVkontakteUser(null);
            AuthHelper.setFacebookUser(null);
        }
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        return settings.getBoolean("logged_in", false);
    }

    public static GraphUser getFacebookUser() {
        return facebookUser;
    }

    public static void setFacebookUser(GraphUser facebookUser) {
        AuthHelper.facebookUser = facebookUser;
    }

    public static User getVkontakteUser() {
        return vkontakteUser;
    }

    public static void setVkontakteUser(User vkontakteUser) {
        AuthHelper.vkontakteUser = vkontakteUser;
    }

    public static String getGcmToken() {
        return gcmToken;
    }

    public static void setGcmToken(String gcmToken) {
        AuthHelper.gcmToken = gcmToken;
        RestAPI.setGCMToken(gcmToken);
    }

    /**
     * Load token from preferences and cache it.
     * @param context
     * @return token
     */
    public static String getToken(Context context) {
        if (token == null) {
            SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
            token = settings.getString("token", null);
        }
        return token;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token, Context context) {
        AuthHelper.token = token;
        SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", token);
        editor.commit();
    }

    /**
     * Load flag that device is registered
     * @param context
     * @return isRegistered
     */
    public static Boolean getDeviceRegistered(Context context) {
        if (deviceRegistered == null) {
            SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
            deviceRegistered = settings.getBoolean("deviceRegistered", false);
        }
        return deviceRegistered;
    }

    public static Boolean getDeviceRegistered() {
        return deviceRegistered;
    }

    public static void setDeviceRegistered(Boolean isRegistered, Context context) {
        deviceRegistered = isRegistered;
        SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("deviceRegistered", isRegistered);
        editor.commit();
    }

    public static Boolean getPhoneConfirmed(Context context) {
        if (phoneConfirmed == null) {
            SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
            phoneConfirmed = settings.getBoolean("phoneConfirmed", false);
        }
        return phoneConfirmed;
    }

    public static Boolean getPhoneConfirmed() {
        return phoneConfirmed;
    }

    public static void setPhoneConfirmed(Boolean phoneConfirmed, Context context) {
        AuthHelper.phoneConfirmed = phoneConfirmed;
        SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("phoneConfirmed", phoneConfirmed);
        editor.commit();
    }

    public static Boolean getWelcomeScreenShown(Context context) {
        if (welcomeScreenShown == null) {
            SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
            welcomeScreenShown = settings.getBoolean("welcomeScreenShown", false);
        }
        return welcomeScreenShown;
    }

    public static void setWelcomeScreenShown(Boolean welcomeScreenShown, Context context) {
        AuthHelper.welcomeScreenShown = welcomeScreenShown;
        SharedPreferences settings = context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("welcomeScreenShown", welcomeScreenShown);
        editor.commit();
    }
}
