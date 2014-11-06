package com.anton.nearby;

import java.io.IOException;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.Scopes;

/**
 * Activity za upload slike na Google Drive
 * 
 * @author anton
 *
 */
public class UploadActivity extends Activity {
	private static int REQUEST_CODE = 2341;
	public static final int REQUEST_AUTHORIZATION = 2344;
	private static final String SCOPE = "oauth2:" + Scopes.DRIVE_FILE;
	private String filePath;
	private String filename;
	private String token;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_activity);

		Bundle b = getIntent().getExtras();
		if (b != null) {
			filePath = b.getString("filePath");
			filename = b.getString("fileName");
			pickUserAccount();
		} else {
			// Gasim activity ako nema podataka za upload
			finish();
		}
	}

	/**
	 * Pokretanje uploada na Drive
	 */
	private void upload(String user) {
		Intent intent = new Intent(this, UploadService.class);
		intent.putExtra("filePath", filePath);
		intent.putExtra("user", user);
		intent.putExtra("fileName", filename);
		intent.putExtra("token", token);
		startService(intent);

	}

	private void pickUserAccount() {
		String[] accountTypes = new String[] { "com.google" };
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				accountTypes, false, null, null, null, null);
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			String user = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			new TokenTask(UploadActivity.this, user, filePath, SCOPE).execute();
			toastMessage("Uploading started in background!");
			finish();

		} else {
			toastMessage("Photo not uploaded!");
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
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
				Toast.makeText(UploadActivity.this, s, Toast.LENGTH_LONG)
						.show();

			}
		});
	}

	/**
	 * AsyncTask za prikupiti oauth token
	 * 
	 * @author anton
	 *
	 */
	private class TokenTask extends AsyncTask<Void, String, String> {
		private Context context;
		private String username;
		private String filePath;
		private String scope;

		public TokenTask(Context context, String username, String filePath,
				String scope) {
			this.context = context;
			this.username = username;
			this.filePath = filePath;
			this.scope = scope;

		}

		@Override
		protected String doInBackground(Void... params) {

			try {
				token = Tools.getStringPrefs(context, "token", null);
				token = fetchToken();
			} catch (Exception e) {
				Tools.logW(e.getMessage());
			}

			return token;
		}

		@Override
		protected void onPostExecute(String result) {
			upload(username);
			super.onPostExecute(result);
		}

		/**
		 * Dobivanje autorizacijskog tokena za post slike
		 * 
		 * @return
		 * @throws IOException
		 */
		protected String fetchToken() throws IOException {

			
			try {

				token = GoogleAuthUtil.getToken(context, username, scope);
			} catch (UserRecoverableAuthException e) {
				startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
			} catch (GoogleAuthException e) {
				Tools.logW(e.getMessage());
			}

			return token;

		}
	}

}
