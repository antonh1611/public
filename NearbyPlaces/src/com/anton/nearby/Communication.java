/*
 * Klasa za fetch podataka sa interneta
 */
package com.anton.nearby;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;

public class Communication {
	private static final String USER_AGENT = "Custom useragent";
	private static byte[] buffer = new byte[8 * 1024];

	public Communication() {
	}

	/**
	 * Metoda za fetch podataka sa interneta vraća null u slučaju greške
	 * 
	 * @param urlAddress
	 * @return
	 */
	public String getResponseString(String urlAddress) {
		// Postavljam default na error, koji će se ispraviti ako je čitanje
		// odgovora OK

		String response = null;
		try {
			URL url = new URL(urlAddress);
			InputStream is = url.openStream();
			response = readStream(is);

		} catch (MalformedURLException e) {
			Tools.logW(e.getMessage());
		} catch (IOException e) {
			Tools.logW(e.getMessage());
		}

		return response;
	}

	/**
	 * metoda za parsanje odgovora sa interneta
	 * 
	 * @param is
	 *            - InputStream
	 * @throws IOException
	 */
	private String readStream(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String input;
		while ((input = reader.readLine()) != null) {
			sb.append(input);
		}
		reader.close();
		return sb.toString();
	}

	/**
	 * metoda za download slika sa interneta
	 */
	public Bitmap downloadBitmap(String urlAddress) {
		Bitmap b = null;
		URL url;
		try {
			url = new URL(urlAddress);
			b = BitmapFactory.decodeStream(url.openStream());
		} catch (Exception e) {
			Tools.logW(e.getMessage());
		}

		return b;
	}

	/**
	 * Slanje uriRequesta (POST ili GET)
	 * 
	 * @param request
	 * @return
	 */
	public String sendRequest(HttpUriRequest request) {
		AndroidHttpClient klijent = AndroidHttpClient.newInstance(USER_AGENT);
		String odgovor;

		try {
			HttpResponse response = klijent.execute(request);

			byte[] byt = getData(response.getEntity().getContent());
			if (byt != null) {
				odgovor = new String(byt);
			} else {
				odgovor = "Greška dohvaćanja podataka";
			}
		} catch (IOException e) {
			return "Greška: " + e.getMessage();
		} finally {
			klijent.close();
		}
		return odgovor;
	}
	
	

	/**
	 * Dohvaćanje podataka sa određene adrese (univerzalno)
	 */
	private byte[] getData(InputStream is) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while (is.read(buffer) != -1) {
				baos.write(buffer);
			}
		} catch (IOException e) {

			return null;
		}

		return baos.toByteArray();
	}
	


}
