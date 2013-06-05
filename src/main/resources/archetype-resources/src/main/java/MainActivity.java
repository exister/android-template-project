package ${package};

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import ${package}.Activities.CodeConfirmationActivity;
import ${package}.Activities.LoginActivity;
import ${package}.Activities.TabBarActivity;
import ${package}.Activities.WelcomeActivity;
import ${package}.Helpers.AuthHelper;
import ${package}.Helpers.UIHelper;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import org.json.JSONObject;


public class MainActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_${artifactId});

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.splash_screen);

        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AuthHelper.getToken(getApplicationContext());
                AuthHelper.getDeviceRegistered(getApplicationContext());
                AuthHelper.getPhoneConfirmed(getApplicationContext());
                RestAPI.setAuth(AuthHelper.getToken());

                if (!AuthHelper.isLoggedIn(getApplicationContext())) {
                    if (!AuthHelper.getWelcomeScreenShown(getApplicationContext())) {
                        AuthHelper.setWelcomeScreenShown(true, getApplicationContext());
                        showWelcomeView();
                    }
                    else {
                        showLoginView();
                    }
                }
                else {
                    loadProfile();
                }
            }
        };
        splashTread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AuthHelper.ACTION_401_UNAUTHORIZED);

        registerReceiver(mUnauthorizedBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mUnauthorizedBroadcastReceiver);
    }

    private void showMainView() {
        Intent intent = new Intent(MainActivity.this, TabBarActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoginView() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showWelcomeView() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showPhoneConfirmationView() {
        Intent intent = new Intent(MainActivity.this, CodeConfirmationActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadProfile() {
        RestAPI.loadProfile(new JsonHttpResponseHandler(){
            @Override
            public void onFailure(final Throwable throwable, JSONObject jsonObject) {
                super.onFailure(throwable, jsonObject);

                onProfileLoadError(throwable);
            }

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);

                AuthHelper.setPhoneConfirmed(jsonObject.optBoolean("mobile_phone_confirmed", false), getApplicationContext());

                if (AuthHelper.getPhoneConfirmed()) {
                    showMainView();
                }
                else {
                    showPhoneConfirmationView();
                }
            }

            @Override
            public void onFailure(final Throwable throwable, String s) {
                super.onFailure(throwable, s);

                onProfileLoadError(throwable);
            }
        }, MainActivity.this);
    }

    private void onProfileLoadError(Throwable throwable) {
        if (AuthHelper.checkFor401(throwable)) {
            AuthHelper.handle401(MainActivity.this);
        }
        else {
            if (AuthHelper.getPhoneConfirmed()) {
                showMainView();
            }
            else {
                showPhoneConfirmationView();
            }
        }
    }

    private BroadcastReceiver mUnauthorizedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AuthHelper.logout(getApplicationContext());
            Intent intent_login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent_login);
            finish();
            UIHelper.showNetworkErrorToast(getApplicationContext(), getResources().getString(R.string.msg_401_error), null);
        }
    };
}