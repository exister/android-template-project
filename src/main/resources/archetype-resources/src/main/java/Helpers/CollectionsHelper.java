package ${package}.Helpers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CollectionsHelper {
    public static List<JSONObject> convertJSONArrayToList(JSONArray values) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        if (values != null) {
            for (int i = 0; i < values.length(); i++) {
                JSONObject item = (JSONObject)values.optJSONObject(i);
                if (item != null) {
                    list.add(item);
                }
            }
        }
        return list;
    }
}
