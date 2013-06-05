package ${package}.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import ${package}.Helpers.AuthHelper;
import ${package}.Helpers.GCMRegistrationService;
import ${package}.Helpers.TabsAdapter;
import ${package}.Helpers.UIHelper;
import ${package}.R;
import ${package}.RestAPI;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.viewpagerindicator.TabPageIndicator;
import net.hockeyapp.android.CrashManager;
import org.json.JSONObject;


public class TabBarActivity extends SherlockFragmentActivity {
    public static String ACTIVE_TAB = "activeTab";
    private TabsAdapter mTabsAdapter;
    private ViewPager mViewPager;
    private ProgressDialog logoutProgressDialog;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public void onCreate(Bundle savedInstanceState) {
        GCMRegistrationService.enablePushNotifications(getApplicationContext());

        setTheme(R.style.Theme_${artifactId});
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.tab_host);

        UIHelper.setupActionBar(this);

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(3);

        mTabsAdapter = new TabsAdapter(this, mViewPager);

        mTabsAdapter.addTab(MainTabFragment.class, null, getResources().getString(R.string.menu_main_tab).toUpperCase());
        mTabsAdapter.addTab(BadgesActivity.class, null, getResources().getString(R.string.menu_badges).toUpperCase());
        mTabsAdapter.addTab(RatingTabFragment.class, null, getResources().getString(R.string.menu_statistics).toUpperCase());

        TabPageIndicator titleIndicator = (TabPageIndicator)findViewById(R.id.titles);
        titleIndicator.setViewPager(mViewPager);

        logoutProgressDialog = UIHelper.networkDialogFactory(this);

        if (savedInstanceState != null) {
            mViewPager.setCurrentItem(savedInstanceState.getInt(ACTIVE_TAB));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(ACTIVE_TAB, mViewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logoutProgressDialog.isShowing()) {
            logoutProgressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (logoutProgressDialog.isShowing()) {
            logoutProgressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CrashManager.register(this, getResources().getString(R.string.hockey_app_crash_id));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AuthHelper.ACTION_401_UNAUTHORIZED);

        registerReceiver(mUnauthorizedBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mUnauthorizedBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = UIHelper.onOptionsItemSelected(item, TabBarActivity.this);

        if (!handled) {
            switch (item.getItemId()) {
                case R.id.menu_logout: {
                    logout();
                    handled = true;
                }
                default:
                    handled = super.onOptionsItemSelected(item);
            }
        }

        return handled;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    //==============================================================================
    // Methods
    //==============================================================================

    private void logout() {
        logoutProgressDialog.show();

        RestAPI.logout(new JsonHttpResponseHandler() {
            @Override
            public void onFailure(Throwable throwable, String s) {
                super.onFailure(throwable, s);
                logoutProgressDialog.hide();
                UIHelper.handleFailure(TabBarActivity.this, getResources().getString(R.string.msg_logout_error), throwable);
            }

            @Override
            public void onFailure(Throwable throwable, JSONObject jsonObject) {
                super.onFailure(throwable, jsonObject);
                logoutProgressDialog.hide();
                UIHelper.handleFailure(TabBarActivity.this, getResources().getString(R.string.msg_logout_error), throwable);
            }

            @Override
            public void onSuccess(JSONObject jsonObject) {
                AuthHelper.logout(getApplicationContext());
                Intent intent = new Intent(TabBarActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, TabBarActivity.this);
    }

    private BroadcastReceiver mUnauthorizedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AuthHelper.logout(getApplicationContext());
            Intent intent_login = new Intent(TabBarActivity.this, LoginActivity.class);
            startActivity(intent_login);
            finish();
            UIHelper.showNetworkErrorToast(getApplicationContext(), getResources().getString(R.string.msg_401_error), null);
        }
    };
}