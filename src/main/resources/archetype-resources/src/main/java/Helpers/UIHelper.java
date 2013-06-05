package ${package}.Helpers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import ${package}.R;
import ${package}.RestAPI;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class UIHelper {
    /**
     * Show server error if status code of throwable is gte 500.
     * Otherwise show a message.
     * @param context
     * @param message
     * @param throwable
     */
    public static void showNetworkErrorToast(Context context, String message, Throwable throwable) {
        String text = message;

        if (throwable != null) {
            if (throwable instanceof HttpHostConnectException) {
                text = context.getResources().getString(R.string.msg_network_error);
            }
            else if (throwable instanceof HttpResponseException) {
                if (((HttpResponseException) throwable).getStatusCode() >= 500) {
                    text = context.getResources().getString(R.string.msg_server_error);
                }
            }
        }

        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void handleFailure(Context context, String message, Throwable throwable) {
        if (AuthHelper.checkFor401(throwable)) {
            AuthHelper.handle401(context);
        }
        else {
            UIHelper.showNetworkErrorToast(context, message, throwable);
        }
    }

    /**
     * If throwable is a BadRequest than show non-field errors as a Toast and set error messages for each field in formFieldsMap.
     * Otherwise just show message in a Toast.
     * @param context
     * @param jsonObject
     * @param formFieldsMap
     * @param message
     * @param throwable
     */
    public static void handleFormErrors(Context context, JSONObject jsonObject, HashMap<String, View> formFieldsMap, String message, Throwable throwable) {
        if (throwable instanceof HttpResponseException) {
            if (((HttpResponseException) throwable).getStatusCode() == 400) {
                JSONArray nonFieldErrors = jsonObject.optJSONArray(RestAPI.NON_FIELD_ERRORS);
                if (nonFieldErrors != null) {
                    UIHelper.showNetworkErrorToast(context, RestAPI.convertNonFieldErrorsToString(nonFieldErrors), null);
                    return;
                }
                else {
                    Iterator keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (formFieldsMap.containsKey(key)) {
                            EditText field = (EditText) formFieldsMap.get(key);
                            field.setError(RestAPI.convertNonFieldErrorsToString(jsonObject.optJSONArray(key)));
                        }
                    }
                    return;
                }
            }
        }
        UIHelper.handleFailure(context, message, throwable);
    }

    public static ProgressDialog networkDialogFactory(Context context) {
        return networkDialogFactory(context, context.getResources().getString(R.string.msg_network_loading));
    }

    public static ProgressDialog networkDialogFactory(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static AlertDialog alertDialogFactory(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.lbl_alert_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return alertDialog;
    }

    public static boolean onOptionsItemSelected(MenuItem item, Context context){
        switch (item.getItemId()) {
            default:
                return false;
        }
    }

    public static void setupActionBar(SherlockActivity sherlockActivity) {
        final ActionBar actionBar = sherlockActivity.getSupportActionBar();
        View customNav = LayoutInflater.from(sherlockActivity).inflate(R.layout.ab_title_image, null);
        setupActionBar(actionBar, customNav, font);
    }

    public static void setupActionBar(SherlockFragmentActivity sherlockActivity) {
        final ActionBar actionBar = sherlockActivity.getSupportActionBar();
        View customNav = LayoutInflater.from(sherlockActivity).inflate(R.layout.ab_title_image, null);
        setupActionBar(actionBar, customNav, font);
    }

    public static void setupActionBar(SherlockListActivity sherlockActivity) {
        final ActionBar actionBar = sherlockActivity.getSupportActionBar();
        View customNav = LayoutInflater.from(sherlockActivity).inflate(R.layout.ab_title_image, null);
        setupActionBar(actionBar, customNav, font);
    }

    private static void setupActionBar(ActionBar actionBar, View titleView, Typeface font) {
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        //Attach to the action bar
        actionBar.setCustomView(titleView);
        actionBar.setDisplayShowCustomEnabled(true);
    }

    public static void configureUIL(Context context) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .showStubImage(android.R.color.transparent)
                .showImageForEmptyUri(android.R.color.transparent)
                .showImageOnFail(android.R.color.transparent)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .threadPoolSize(2)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
