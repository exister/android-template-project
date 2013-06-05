package ${package}.ListAdapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import ${package}.R;
import ${package}.RestAPI;
import com.commonsware.cwac.endless.EndlessAdapter;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EndlessListAdapter extends EndlessAdapter {
    private final static String TAG = EndlessListAdapter.class.getSimpleName();
    private View pendingView = null;
    private int total = 0;
    private Object nextUrl = JSONObject.NULL;
    private Context context;
    private JSONArray newData = null;
    private final Object lock = new Object();

    public EndlessListAdapter(Context context, ListAdapter list) {
        super(list);
        setRunInBackground(false);
        this.context = context;
    }

    public EndlessListAdapter(Context context, ListAdapter list, int total, Object nextUrl, boolean keepOnAppending) {
        super(list, keepOnAppending);
        setRunInBackground(false);
        this.total = total;
        this.nextUrl = nextUrl;
        this.context = context;
    }

    @Override
    protected View getPendingView(ViewGroup parent) {
        if (pendingView == null) {
            pendingView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_row, null);
        }
        return pendingView;
    }

    @Override
    protected boolean cacheInBackground() {
        Object url;
        synchronized (this.lock) {
            url = nextUrl;
        }

        if (url != JSONObject.NULL) {
            Log.d(TAG, "loading next true");
            RestAPI.getUrl((String)url, new RequestParams(), new JsonHttpResponseHandler(){
                @Override
                protected void handleFailureMessage(Throwable throwable, String s) {
                    super.handleFailureMessage(throwable, s);
                }

                @Override
                public void onFailure(Throwable throwable, JSONObject jsonObject) {
                    super.onFailure(throwable, jsonObject);
                }

                @Override
                public void onSuccess(JSONObject jsonObject) {
                    super.onSuccess(jsonObject);
                    synchronized (EndlessListAdapter.this.lock) {
                        EndlessListAdapter.this.total = jsonObject.optInt("count", 0);
                        EndlessListAdapter.this.nextUrl = jsonObject.opt("next");
                        newData = jsonObject.optJSONArray("results");
                        appendCachedData();
                        if (EndlessListAdapter.this.nextUrl == JSONObject.NULL) {
                            stopAppending();
                        }
                        onDataReady();
                    }
                }
            }, context);
            return true;
        }

        Log.d(TAG, "loading next false");
        return false;
    }

    @Override
    protected void appendCachedData() {
        if (getWrappedAdapter().getCount() < total && newData != null) {
            @SuppressWarnings("unchecked")
            ArrayAdapter<JSONObject> wrappedAdapter = (ArrayAdapter<JSONObject>)getWrappedAdapter();

            for (int i = 0; i < newData.length(); i++) {
                try {
                    wrappedAdapter.add((JSONObject)newData.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
