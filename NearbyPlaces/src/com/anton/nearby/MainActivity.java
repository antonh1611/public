package com.anton.nearby;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Main activity koji će prikazivati tražene fragmente po potrbi i eventualno
 * handlati settings button (koji nisam implementirao ali sam ga napravio)
 * 
 * @author anton
 */
public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Fragment main = new MainFragment();
		setInitialFragment(main);
		configureSettingsButton();
		
		if(!isNetworkAvailable()){
			toastMessage(getString(R.string.please_enable_the_internet_connection_));
		}

	}

	/**
	 * Metoda za postavljanje početnog fragmenta
	 * 
	 * @param f
	 */
	public void setInitialFragment(Fragment f) {

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.frameContainer, f);
		ft.commit();

	}

	/**
	 * Dodavanje onclickListenera na settings button za otvaranje opcija
	 */
	private void configureSettingsButton() {
		// Jedini je button u ovom activity-ju pa ću samo dodati novi
		// onclickListener
		ImageView settings = (ImageView) findViewById(R.id.settingsImage);
		settings.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				toastMessage("Settings are pressed!");

			}
		});

	}
	/**
	 * Detekcija internet konekcije
	 * @return
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Metoda za prikaz toas poruke (wrapana u runOnUThread da nemoram brinuti o
	 * pozivanju iz drugih threadova)
	 * 
	 * @param s
	 */
	private void toastMessage(final String s) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();

			}
		});
	}
	
	

}
