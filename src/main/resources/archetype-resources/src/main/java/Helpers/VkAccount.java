package ${package}.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class VkAccount {
    public String access_token;
    public long user_id;
    public long expires_in;

    public void save(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("access_token", access_token);
        editor.putLong("user_id", user_id);
        editor.putLong("fetched_at", System.currentTimeMillis() / 1000L);
        editor.putLong("expires_in", expires_in);
        editor.commit();
    }

    public void restore(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        access_token = prefs.getString("access_token", null);
        user_id = prefs.getLong("user_id", 0);
        expires_in = prefs.getLong("expires_in", -1);
        long fetched_at = prefs.getLong("fetched_at", -1);

        if (access_token != null) {
            if (expires_in > 0 && fetched_at > 0) {
                long now = System.currentTimeMillis() / 1000L;
                if (fetched_at + expires_in <= now) {
                    access_token = null;
                }
            }
            else {
                access_token = null;
            }
        }
    }
}