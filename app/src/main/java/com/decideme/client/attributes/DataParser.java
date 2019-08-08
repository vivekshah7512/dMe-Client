package com.decideme.client.attributes;

/**
 * Created by vivek_shah on 23/10/17.
 */

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList();
        try {
            JSONArray jRoutes = jObject.getJSONArray("routes");
            for (int i = 0; i < jRoutes.length(); i++) {
                JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList();
                for (int j = 0; j < jLegs.length(); j++) {
                    JSONArray jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        List<LatLng> list = decodePoly((String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points"));
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e2) {
        }
        return routes;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;
        int b = 0;
        while (index < len) {
            int index2;
            int shift = 0;
            int result = 0;
            while (true) {
                index2 = index + 1;
                b = encoded.charAt(index) - 63;
                result |= (b & 31) << shift;
                shift += 5;
                if (b < 32) {
                    break;
                }
                index = index2;
            }
            lat += (result & 1) != 0 ? (result >> 1) ^ -1 : result >> 1;
            shift = 0;
            result = 0;
            index = index2;
            while (true) {
                index2 = index + 1;
                b = encoded.charAt(index) - 63;
                result |= (b & 31) << shift;
                shift += 5;
                if (b < 32) {
                    break;
                }
                index = index2;
            }
            lng += (result & 1) != 0 ? (result >> 1) ^ -1 : result >> 1;
            poly.add(new LatLng(((double) lat) / 100000.0d, ((double) lng) / 100000.0d));
            index = index2;
        }
        return poly;
    }
}
