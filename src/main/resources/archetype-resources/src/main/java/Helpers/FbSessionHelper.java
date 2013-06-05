package ${package}.Helpers;

import android.app.Activity;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;


public class FbSessionHelper extends UiLifecycleHelper {
    protected Session.StatusCallback p_callback;

    public FbSessionHelper(Activity activity, Session.StatusCallback callback) {
        super(activity, callback);
        p_callback = callback;
    }

    @Override
    public void onResume() {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (p_callback != null) {
                session.addCallback(p_callback);
            }
        }
    }
}
