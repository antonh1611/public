package com.anton.nearby;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

/**
 * Servis za upload slika u pozadini
 * 
 * @author anton
 *
 */
public class UploadService extends IntentService {
	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/drive.file";
	private String filePath;
	private String user;
	private String fileName;
	private String token;

	public UploadService() {
		super("Upload service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle b = intent.getExtras();
		if (b != null) {
			filePath = b.getString("filePath");
			user = b.getString("user");
			fileName = b.getString("fileName");
			token = b.getString("token");
			new UploadTask(getApplicationContext(), user, filePath, SCOPE,
					token).execute();
			notification(getString(R.string.uploading_),
					getString(R.string.app_name));

		}

	}

	/**
	 * Notifikacija u notification baru
	 * 
	 * @param message
	 * @param title
	 */
	private void notification(String message, String title) {

		Context context = getApplicationContext();
		try {
			Intent intent = new Intent(context, UploadService.class);
			PendingIntent pIntent = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);

			Builder mNotificationBuilder = new NotificationCompat.Builder(
					context)

			.setContentTitle(title).setContentText(message)
					.setSmallIcon(R.drawable.posudjena)
					.setContentIntent(pIntent).setAutoCancel(true)
					.setTicker(message)
					.setLights(Color.parseColor("#009900"), 3000, 2000);

			Notification notification = mNotificationBuilder.build();

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			notificationManager.notify(0, notification);
		} catch (Exception e) {

			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(ns);
			Notification notification = new Notification(R.drawable.posudjena,
					title, System.currentTimeMillis());

			Intent notificationIntent = new Intent(context, UploadService.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(context, title, message,
					pendingIntent);
			notificationManager.notify(7878, notification);
			notificationManager.cancel(7878);
		}
	}

	/**
	 * Asinkroni task za upload slike
	 * 
	 * @author anton
	 *
	 */
	private class UploadTask extends AsyncTask<Void, String, String> {
		private Context context;
		private String filePath;
		private String token;

		public UploadTask(Context context, String username, String filePath,
				String scope, String token) {
			this.context = context;
			this.filePath = filePath;
			this.token = token;

		}

		@Override
		protected String doInBackground(Void... params) {
			String response = null;

			try {
				response = sendFile(token, filePath, fileName);
			} catch (Exception e) {
			}

			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				notification(context.getString(R.string.upload_done),
						context.getString(R.string.upload_finished));
			}
			super.onPostExecute(result);
		}

		/**
		 * Slanje podataka o slici i vraćanje odgovora u obliku stringa
		 * 
		 * @param token
		 * @param filePath
		 * @return
		 * @throws Exception
		 */
		protected String sendFile(String token, String filePath, String fileName)
				throws Exception {

			HttpPost post = new HttpPost(
					"https://www.googleapis.com/upload/drive/v2/files?uploadType=media");

			File f = new File(filePath);
			byte[] buffer = new byte[(int) f.length()];
			FileInputStream fis = new FileInputStream(f);
			fis.read(buffer);
			fis.close();

			post.setHeader("Authorization", "Bearer " + token);
			post.setHeader("Content-Type", "image/bmp");
			post.setEntity(new ByteArrayEntity(buffer));
			Communication com = new Communication();
			String response = com.sendRequest(post);
			// Moram ručno parsati jer ne vraća pravilan JSONObject
			JSONObject meta = new JSONObject(id(response));

			String id = meta.getString("id");

			HttpPatch patch = new HttpPatch(
					"https://www.googleapis.com/drive/v2/files/" + id);
			JSONObject json = new JSONObject();
			json.put("title", fileName);
			patch.setHeader("Authorization", "Bearer " + token);
			patch.setHeader("Content-Type", "application/json");
			patch.setEntity(new ByteArrayEntity(json.toString().getBytes()));
			response = com.sendRequest(patch);

			return response;

		}

		/**
		 * Parsanje odgovora jer response sa google drivea nije JSON
		 * 
		 * @param response
		 * @return
		 */
		private String id(String response) {
			String[] split = response.split("\n");
			String id = null;
			for (String s : split) {
				if (s.contains("id")) {
					if (s.endsWith(",")) {
						s = s.substring(0, s.length() - 1);
					}
					id = "{" + s + "}";
					break;
				}
			}

			return id;
		}

	}

}
