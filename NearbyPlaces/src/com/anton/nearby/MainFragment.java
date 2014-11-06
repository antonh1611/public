package com.anton.nearby;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Početni fragment koji prikazuje Favorite listu
 * 
 * @author anton
 *
 */
public class MainFragment extends Fragment {

	private Context context;
	private ListView listView;
	private Integer radius = 100;
	private Cursor c;
	private Button getNearbyButton;
	private boolean hasGPSHardware;
	private boolean GPSOn;
	private LocationManager lm;
	private Speedometer ll;
	private MapFragment mapFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.main_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		listView = (ListView) view.findViewById(R.id.lista);
		listView.setOnItemClickListener(itemClickListener);
		context = getActivity();
		setListAdapter();
		checkGPSHardware();
		getNearbyButton = (Button) view.findViewById(R.id.getNearbyButton);
		getNearbyButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getNearbyButton.setEnabled(false);
				checkGPSHardware();
				if (hasGPSHardware) {
					getGpsCoordinates();
				} else {
					fetchByCityName();
				}

			}
		});

	}


	/**
	 * Ovdje ću dobiti podatke od GPS ili ponuditi ručni odabir mjesta
	 */

	/**
	 * Dohvaćanje nearby places
	 */
	private void fetchPlaces(final CurrentPosition cp,
			final boolean markMyPosition, final boolean hideStar) {
		String positionString = cp.getLatitude().toString() + ","
				+ cp.getLongitude().toString();
		final String params = "location=" + positionString + "&radius="
				+ radius.toString();
		final Places places = new GooglePlaces();

		new Thread(new Runnable() {

			@Override
			public void run() {

				final JSONArray placesArray = places.getNearbyPlaces(params);
				if (placesArray != null) {

					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							showPlaces(placesArray, cp, markMyPosition,
									hideStar);
							getNearbyButton.setEnabled(true);
						}
					});

				} else {
					Tools.logW("placesArray equals null");
					toastMessage("Error fetching data!");
				}

			}
		}).start();

	}

	/**
	 * Prikupljanje podataka potrebnih za prikaz mjesta na karti
	 * 
	 * @param places
	 * @param cp
	 * @param markMyPosition
	 * @param hideStar
	 */
	private void showPlaces(JSONArray places, CurrentPosition cp,
			boolean markMyPosition, boolean hideStar) {
		if (mapFragment == null) {
			mapFragment = new MapFragment();
		}
		Bundle b = new Bundle();
		b.putString("places", places.toString());
		double[] position = new double[2];
		position[0] = cp.getLatitude();
		position[1] = cp.getLongitude();
		b.putDoubleArray("position", position);
		b.putBoolean("hideStar", hideStar);
		b.putBoolean("markMyPosition", markMyPosition);
		mapFragment.setArguments(b);
		Tools.showFragment(getActivity(), mapFragment);
	}

	/**
	 * Prikaz toast poruke umotan u runOnUiThread da ne moram brinuti ukoliko ga
	 * pozivam iz drugih threadova
	 * 
	 * @param s
	 */
	private void toastMessage(final String s) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(context, s, Toast.LENGTH_LONG).show();

			}
		});
	}

	/**
	 * Postavljanje adaptera u listu i u ObjectHolder da ga mogu refreshati
	 * nakon edita ili dodavanja favorita (inače bi se to moglo
	 * broadcastReceiverom riješiti ali ne volim ga koristiti ako nije baš
	 * nužno)
	 */
	private void setListAdapter() {
		c = DBUtils.getCursor(getActivity(), "SELECT * FROM "+DBUtils.TABLE_NAME);
		// Znam, trebalo bi implementirati CursorLoader ali nažalost ne stignem
		// a i relativno je malo podataka iz favorite liste pa neće biti
		// štucanja na UI
		getActivity().startManagingCursor(c);
		FavoritesAdapter adapter = new FavoritesAdapter(getActivity(), c, false);
		ObjectHolder.setFavoritesAdapter(adapter);
		listView.setAdapter(adapter);
	}

	@Override
	public void onStop() {
		// Gasim GPS zahtjev ako je aktivan i još nije pokupio signal
		if (GPSOn) {
			lm.removeUpdates(ll);
		}
		// Isto kao i gore :)
		getActivity().stopManagingCursor(c);
		super.onStop();
	}

	/**
	 * Provjera dali GPS hardware postoji
	 */
	private void checkGPSHardware() {
		LocationManager locationManager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);
		hasGPSHardware = (locationManager
				.getProvider(LocationManager.GPS_PROVIDER) != null);
		// Otkomentirati ukoliko se želi testirati ručna pretraga
		// hasGPSHardware = false;

	}

	/**
	 * Prikupljanje podataka o trenutnoj lokaciji sa GPS koordinatama
	 */
	private void getGpsCoordinates() {
		LocationManager manager = (LocationManager) getActivity()
				.getSystemService(Context.LOCATION_SERVICE);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Location Manager");
			builder.setMessage("Enable GPS?");
			builder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent i = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(i);
							continueFetch();
						}
					});
			builder.setNegativeButton("Search by address",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							fetchByCityName();
						}
					});
			builder.create().show();
		} else {
			continueFetch();
		}

	}

	/**
	 * Metoda kojom možemo pronaći okolne objekte iako uređaj nema GPS chip
	 */
	private void fetchByCityName() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final EditText editText = new EditText(getActivity());
		builder.setTitle("Enter city name (\"Address,City,State\"):");
		builder.setView(editText);
		builder.setPositiveButton("Search",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new Runnable() {

							@Override
							public void run() {
								String cityName = editText.getText().toString()
										.replace(" ", "_");
								if (cityName.equals("")) {
									getNearbyButton.setEnabled(true);
									return;
								}
								String googleApiGeolocationAddress = "http://maps.googleapis.com/maps/api/geocode/json?";
								Communication com = new Communication();
								String jsonString = com
										.getResponseString(googleApiGeolocationAddress
												+ "address="
												+ cityName
												+ "&sensor=false");

								// ovdje ću pokupiti prvi rezultat da nemoram
								// sada raditi prikaz za odabir rezultata u
								// slučaju više ponuđenih rezultata
								try {
									if (new JSONObject(jsonString).getString(
											"status").equals("OK")) {

										JSONArray array = new JSONObject(
												jsonString)
												.getJSONArray("results");

										// Vadim podatke latitudu i longitudu
										// direktno iz JSON objekta
										JSONObject o = array.getJSONObject(0)
												.getJSONObject("geometry")
												.getJSONObject("location");

										Double latitude = o.getDouble("lat");
										Double longitude = o.getDouble("lng");

										final CurrentPosition cp = new CurrentPosition(
												latitude, longitude);

										getActivity().runOnUiThread(
												new Runnable() {

													@Override
													public void run() {
														fetchPlaces(cp, true,
																false);
													}
												});
									}
								} catch (Exception e) {
									getNearbyButton.setEnabled(false);
								}
							}
						}).start();

					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		builder.create().show();
	}

	/**
	 * Nastavak povlačenja podataka iz GPS-a
	 */
	private void continueFetch() {
		if (!GPSOn) {
			lm = (LocationManager) getActivity().getSystemService(
					Context.LOCATION_SERVICE);
			ll = new Speedometer();
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
			GPSOn = true;
		}

	}

	/**
	 * Klasa za grab trenutne lokacije
	 * 
	 * @author anton
	 *
	 */
	public class Speedometer implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			CurrentPosition cp = new CurrentPosition(location.getLatitude(),
					location.getLongitude());
			lm.removeUpdates(ll);
			GPSOn = false;
			fetchPlaces(cp, false, false);

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

	}

	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int row,
				long arg3) {
			JSONObject json = ((FavoritesAdapter) listView.getAdapter())
					.getJsonObject(row);
			JSONArray places = new JSONArray();
			places.put(json);
			showPlaces(places, Tools.currentPositionFomString(json
					.optString("geoLocation")), false, true);

		}
	};

}
