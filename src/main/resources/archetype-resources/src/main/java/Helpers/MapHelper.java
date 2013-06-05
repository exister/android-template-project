package ${package}.Helpers;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;


public class MapHelper {
    public static LatLng parseLocation(String locationString) {
        String[] latLng = locationString.split(",");

        if (latLng.length != 2) {
            return null;
        }

        double lat = Location.convert(latLng[0]);
        double lng = Location.convert(latLng[1]);

        return new LatLng(lat, lng);
    }
}
