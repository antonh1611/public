package com.anton.nearby;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementacija Places interfacea za vraćanje JSONArray-a sa nearby objektima
 * 
 * @author anton
 *
 */
public class GooglePlaces implements Places {

	private final String PLACES_API_ADDRESS = "https://maps.googleapis.com/maps/api/place/search/json?";
	private final String API_KEY = "AIzaSyCj5x53fqn840BLDemSmkmbm12mxuyqP-4";

	@Override
	public JSONArray getNearbyPlaces(String parameters) {

		Communication com = new Communication();
		String jsonString = com.getResponseString(PLACES_API_ADDRESS
				+ parameters + "&key=" + API_KEY);

		// Query vraća JSONObject iz kojega moram izvući "results:" array
		try {
			JSONObject json = new JSONObject(jsonString);
			// Radim JSONArray koji ću vratiti sa potrebnim podacima
			JSONArray array = new JSONArray();
			JSONArray temp = json.getJSONArray("results");
			for (int i = 0; i < temp.length(); i++) {
				JSONObject o = temp.getJSONObject(i);
				JSONObject tempObject = new JSONObject();
				tempObject.put("name", o.optString("name", "No name"));
				// GooglePlaces vraća adresu kao "vicinity"
				tempObject.put("address", o.optString("vicinity", ""));
				Double lat = o.optJSONObject("geometry")
						.optJSONObject("location").optDouble("lat");
				Double lng = o.optJSONObject("geometry")
						.optJSONObject("location").optDouble("lng");
				tempObject.put("geoLocation",
						lat.toString() + "," + lng.toString());
				tempObject.put("image", o.optString("icon", ""));
				array.put(tempObject);
			}
			return array;

		} catch (JSONException e) {
			Tools.logW(e.getMessage());
		}
		// U slučaju greške vraćam null
		return null;
	}
}
