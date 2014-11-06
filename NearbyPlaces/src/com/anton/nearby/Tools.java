/*
 * Klasa sa statičnim helper metodama
 */
package com.anton.nearby;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Helper klasa sa statičnim metodama koje koristim češće
 * 
 * @author anton
 *
 */
public class Tools {
	private static Cache cache;
	private static final String SHARED_PREFERENCES = "Nearby";

	private Tools() {
		// Spriječavam nepotrebno instanciranje klase
	}

	/**
	 * Ispis loga u logcatu
	 * 
	 * @param s
	 */
	public static void logW(String s) {
		Log.w("NearbyPlaces", s);
	}

	/**
	 * Stvaranje CurrentPostion objekta iz Stringa Lat, Lng
	 * 
	 * @param geoLoacation
	 * @return
	 */
	public static CurrentPosition currentPositionFomString(String geoLoacation) {

		double latitude = Double.parseDouble(geoLoacation.split(",")[0]);
		double longitude = Double.parseDouble(geoLoacation.split(",")[1]);
		return new CurrentPosition(latitude, longitude);
	}

	/**
	 * Metoda za prikaz drugih fragmenta
	 * 
	 * @param f
	 */
	public static void showFragment(FragmentActivity activity, Fragment f) {
		FragmentTransaction ft = activity.getSupportFragmentManager()
				.beginTransaction();
		ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
				R.anim.enter_from_left, R.anim.exit_to_right);

		ft.add(R.id.frameContainer, f);

		ft.show(f);
		ft.addToBackStack("before");

		ft.commit();
	}

	/**
	 * Skrivanje fragmenta i vračanje na početni MainFragment
	 * 
	 * @param activity
	 */
	public static void fragmentBack(FragmentActivity activity) {
		FragmentManager fm = activity.getSupportFragmentManager();
		fm.popBackStack();
	}

	/**
	 * Metoda za stvaranje Cache za spremanje slika
	 * 
	 * @param context
	 * @return
	 */
	public static Cache getCache(Context context) {
		if (cache == null) {
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			cache = new Cache(context, metrics.density);
		}
		return cache;
	}

	/**
	 * Provjera dali je String URL (Ukoliko nije pretpostavljam da je slika
	 * pohranjena na disku)
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isUrl(String path) {
		try {
			URL url = new URL(path);
			return true;
		} catch (MalformedURLException e) {

		}
		return false;
	}

	/**
	 * Stvaranje osnovnog progressDialoga da moram manje kodirati
	 * 
	 * @param context
	 * @param title
	 * @param message
	 * @return
	 */
	public static ProgressDialog getProgressDialog(Context context,
			String title, String message) {
		ProgressDialog pd = new ProgressDialog(context);
		pd.setTitle(title);
		pd.setMessage(message);

		return pd;
	}

	/**
	 * Stvaranje osnovnog AlertDialoga da moram manje kodirati
	 * 
	 * @param context
	 * @param title
	 * @param message
	 * @return
	 */
	public static AlertDialog getAlertDialog(Context context, String title,
			String message) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		return builder.create();
	}

	public static void putStringPrefs(Context context, String name, String value) {
		SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = sp.edit();
		edit.putString(name, value);
		edit.commit();

	}

	public static String getStringPrefs(Context context, String name,
			String defaultResponse) {
		SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return sp.getString(name, defaultResponse);

	}
}
