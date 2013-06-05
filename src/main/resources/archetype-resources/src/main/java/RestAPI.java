package ${package};

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import ${package}.Helpers.AuthHelper;
import com.loopj.android.http.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;

import java.util.Map;


public class RestAPI {
    public final static String NON_FIELD_ERRORS = "non_field_errors";

    private static class AdwzMobileAsyncHttpClient extends AsyncHttpClient {
        private boolean isContextHeadersSet = false;

        public AdwzMobileAsyncHttpClient() {
            super();

            setUserAgent("Android/${artifactId}");
            addHeader("Push-ID", "");
            addHeader("Device-OS", "Android");
            addHeader("Device-OS-Version", Build.VERSION.RELEASE);
            addHeader("Device-Model", String.format("%s %s", Build.MANUFACTURER, Build.MODEL));
            addHeader("Application", "${artifactId}");
        }

        public void setContextDependentHeaders(Context context) {
            if (!isContextHeadersSet) {
                addHeader("Device-ID", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
                try {
                    addHeader("Application-Version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                isContextHeadersSet = true;
            }
        }
    }

    private static String getBaseUrl() {
        if (BuildConfig.DEBUG) {
            return "${base-url-local}";
        }
        return "${base-url-release}";
    }

    private static AdwzMobileAsyncHttpClient client = new AdwzMobileAsyncHttpClient();

    static {
        client.setTimeout(30000);
    }

    public static void setAuth(String token) {
        if (token != null) {
            client.addHeader("Authorization", String.format("Token %s", token));
        }
        else {
            client.addHeader("Authorization", null);
        }
    }

    public static void setGCMToken(String token) {
        client.addHeader("Push-ID", token);
    }

    public static void getUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, Context context) {
        client.setContextDependentHeaders(context);
        client.get(url, params, responseHandler);
    }

    private static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, Context context) {
        client.setContextDependentHeaders(context);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, Context context) {
        client.setContextDependentHeaders(context);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, Context context) {
        client.setContextDependentHeaders(context);
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void delete(String url, AsyncHttpResponseHandler responseHandler, Context context) {
        client.setContextDependentHeaders(context);
        client.delete(getAbsoluteUrl(url), responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return getBaseUrl() + relativeUrl;
    }

    //==============================================================================
    // Device
    //==============================================================================

    /**
     * Register device. All required parameters are passed automatically via headers.
     * @param responseHandler
     * @param context
     */
    public static void deviceRegistration(AsyncHttpResponseHandler responseHandler, Context context) {
        System.out.println("deviceRegistration");
        RequestParams params = new RequestParams();

        post("/api/device-registration/", params, responseHandler, context);
    }

    public static void pushIdRegistration(String pushId, AsyncHttpResponseHandler responseHandler, Context context) {
        System.out.println("pushIdRegistration");
        RequestParams params = new RequestParams();
        params.put("push_id", pushId);

        put("/api/device-register-push-token/", params, responseHandler, context);
    }

    //==============================================================================
    // Authentication
    //==============================================================================

    public static void login(String username, String password, AsyncHttpResponseHandler responseHandler, Context context) {
        System.out.println("login");
        RequestParams params = new RequestParams();
        params.put("mobile_phone", username);
        params.put("password", password);

        post("/api/persons/login/", params, responseHandler, context);
    }

    public static void logout(AsyncHttpResponseHandler responseHandler, Context context) {
        System.out.println("logout");
        delete("/api/persons/logout/", responseHandler, context);
    }

    public static void register(Map<String, String> parameters, AsyncHttpResponseHandler responseHandler, Context context) {
        System.out.println("register");
        RequestParams params = new RequestParams();
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }

        post("/api/persons/", params, responseHandler, context);
    }

    public static void loadProfile(AsyncHttpResponseHandler responseHandler, Context context) {
        System.out.println("loadProfile");
        RequestParams params = new RequestParams();

        get("/api/persons/me/", params, responseHandler, context);
    }

    //==============================================================================
    // Utils
    //==============================================================================

    public static String convertNonFieldErrorsToString(JSONArray errors) {
        if (errors == null) {
            return "";
        }

        StringBuilder s = new StringBuilder();
        for (int i = 0, size = errors.length(); i < size; i++) {
            if (i > 0) {
                s.append("\n");
            }
            s.append(errors.optString(i));
        }
        return s.toString();
    }
}
