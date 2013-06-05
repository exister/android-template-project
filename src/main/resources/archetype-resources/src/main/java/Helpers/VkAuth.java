package ${package}.Helpers;

import android.util.Log;
import com.perm.kate.api.Auth;
import com.perm.utils.Utils;

import java.net.URLEncoder;


public class VkAuth extends Auth {
    private static final String TAG = "${package}.Helpers.VkAuth";

    public static String getUrl(String api_id){
        return "http://oauth.vk.com/authorize?client_id="+api_id+"&display=touch&redirect_uri="+ URLEncoder.encode(redirect_url)+"&response_type=token";
    }

    public static String[] parseRedirectUrl(String url) throws Exception {
        //url is something like http://api.vkontakte.ru/blank.html#access_token=66e8f7a266af0dd477fcd3916366b17436e66af77ac352aeb270be99df7deeb&expires_in=0&user_id=7657164
        String access_token = Utils.extractPattern(url, "access_token=(.*?)&");
        Log.i(TAG, "access_token=" + access_token);
        String user_id = Utils.extractPattern(url, "user_id=(\\d*)");
        Log.i(TAG, "user_id=" + user_id);
        if(user_id == null || user_id.length()==0 || access_token == null || access_token.length() == 0)
            throw new Exception("Failed to parse redirect url "+url);
        String expires_in = Utils.extractPattern(url, "expires_in=(\\d*)");
        Log.i(TAG, "expires_in=" + expires_in);
        return new String[]{access_token, user_id, expires_in};
    }
}
