package com.anton.nearby;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Fragment za prikaz odabranih lokacija na karti
 * 
 * @author anton
 *
 */
public class MapFragment extends Fragment {

	private GoogleMap map;
	private SupportMapFragment mapFragment;
	private ListView lv;
	private boolean hideStar;
	private static View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Ovdje sam napravio zaobilazak problema koji se dešavao kod drugog
		// prikazivanja karte zbog "duplicate id..."

		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			view = inflater.inflate(R.layout.map_fragment, container, false);
		} catch (Exception e) {
			Tools.logW(e.getMessage());
		}
		return view;

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle b = getArguments();
		mapFragment = ((SupportMapFragment) getActivity()
				.getSupportFragmentManager().findFragmentById(R.id.mapFragment));
		map = mapFragment.getMap();

		lv = (ListView) view.findViewById(R.id.nearbyList);

		initialOrientationConfig();
		map.setMyLocationEnabled(true);

		if (b != null) {

			boolean markMyPosition = b.getBoolean("markMyPosition");
			hideStar = b.getBoolean("hideStar");

			double[] coordinates = b.getDoubleArray("position");
			if (coordinates != null) {
				LatLng point = new LatLng(coordinates[0], coordinates[1]);
				moveMapToPoint(point, 17, markMyPosition);
			}

			String places = b.getString("places");
			if (places != null) {
				try {
					JSONArray placesArray = new JSONArray(places);
					setMarkers(placesArray);
					showNearbyList(placesArray);

				} catch (JSONException e) {
				}
			}

		}

	}

	/**
	 * Prikaz popisa svih dohvaćenih okolnih objekata
	 * 
	 * @param placesArray
	 */
	private void showNearbyList(JSONArray placesArray) {
		NearbyPlacesAdapter adapter = new NearbyPlacesAdapter(getActivity(),
				getActivity().getLayoutInflater(), placesArray);
		lv.setAdapter(adapter);
		if (hideStar) {
			adapter.hideStar();
		}
		lv.setOnItemClickListener(itemClickListener);
	}

	/**
	 * Postavljanje markera okolnih objekata na kartu
	 * 
	 * @param placesArray
	 */
	private void setMarkers(JSONArray placesArray) {

		map.clear();
		for (int i = 0; i < placesArray.length(); i++) {
			JSONObject o;
			try {
				o = placesArray.getJSONObject(i);
			} catch (JSONException e) {
				o = new JSONObject();
			}
			String geoLocation = o.optString("geoLocation", "0,0");
			double lat = Double.parseDouble(geoLocation.split(",")[0]);
			double lon = Double.parseDouble(geoLocation.split(",")[1]);
			String name = o.optString("name", "");
			String address = o.optString("address", "");
			MarkerOptions mo = new MarkerOptions();
			LatLng latLng = new LatLng(lat, lon);
			mo.position(latLng);
			mo.title(name + " : " + address);
			map.addMarker(mo);

		}

	}

	/**
	 * Postavljanje izgleda Fragmenta (landscape-lista pokraj karte, portrait-
	 * lista ispod karte)
	 */
	private void initialOrientationConfig() {
		LinearLayout mapLayout = (LinearLayout) view
				.findViewById(R.id.mapLayout);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			landscapeOrientation();
			mapLayout.setOrientation(LinearLayout.HORIZONTAL);
		} else {
			portraitOrientation();
			mapLayout.setOrientation(LinearLayout.VERTICAL);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		LinearLayout mapLayout = (LinearLayout) view
				.findViewById(R.id.mapLayout);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			landscapeOrientation();
			mapLayout.setOrientation(LinearLayout.HORIZONTAL);
		} else {
			portraitOrientation();
			mapLayout.setOrientation(LinearLayout.VERTICAL);

		}

		super.onConfigurationChanged(newConfig);
	}

	private void landscapeOrientation() {
		LinearLayout.LayoutParams lp = (LayoutParams) mapFragment.getView()
				.getLayoutParams();
		lp.height = LayoutParams.MATCH_PARENT;
		lp.width = 0;

		lp = (LayoutParams) lv.getLayoutParams();
		lp.height = LayoutParams.MATCH_PARENT;
		lp.width = 0;

	}

	private void portraitOrientation() {
		LinearLayout.LayoutParams lp = (LayoutParams) mapFragment.getView()
				.getLayoutParams();
		lp.height = 0;
		lp.width = LayoutParams.MATCH_PARENT;

		lp = (LayoutParams) lv.getLayoutParams();
		lp.height = 0;
		lp.width = LayoutParams.MATCH_PARENT;
	}

	/**
	 * Pomak na karti do određenog mjesta (Trenutna lokacija ili odabrana
	 * lokacija)
	 * 
	 * @param point
	 * @param zoom
	 * @param mark
	 */
	private void moveMapToPoint(LatLng point, float zoom, boolean mark) {

		MarkerOptions mo = new MarkerOptions();
		mo.position(point);
		mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.posudjena));
		if (mark) {
			map.addMarker(mo);
		}
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, zoom));

	}

	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int index,
				long arg3) {
			JSONObject o = (JSONObject) lv.getAdapter().getItem(index);
			String position = o.optString("geoLocation", "0,0");
			LatLng point = new LatLng(
					Double.parseDouble(position.split(",")[0]),
					Double.parseDouble(position.split(",")[1]));
			moveMapToPoint(point, map.getCameraPosition().zoom, false);

		}
	};

}
