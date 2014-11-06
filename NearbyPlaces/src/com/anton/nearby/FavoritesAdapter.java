package com.anton.nearby;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter za prikaz favorita iz baze podataka
 * 
 * @author anton
 *
 */
public class FavoritesAdapter extends CursorAdapter {
	private LayoutInflater li;
	private Cache cache;
	private FragmentActivity activity;

	public FavoritesAdapter(FragmentActivity activity, Cursor c,
			boolean autoRequery) {
		super(activity, c, autoRequery);
		this.activity = activity;
		this.li = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		cache = Tools.getCache(activity);
	}

	@Override
	public void bindView(View row, Context arg1, Cursor c) {
		ImageView image = (ImageView) row.findViewById(R.id.favoriteImage);
		TextView textName = (TextView) row.findViewById(R.id.favoriteName);
		TextView textAddress = (TextView) row
				.findViewById(R.id.favoriteAddress);

		textName.setText(c.getString(1));
		String address = c.getString(2);
		textAddress.setText(address);
		image.setOnClickListener(onClickListener);
		cache.setImageFromCache(c.getString(4) + "-thumb", image);
		Button b = (Button) row.findViewById(R.id.favoriteEditButton);
		b.setOnClickListener(onClickListener);
		b.setTag(c.getPosition());

	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
		return li.inflate(R.layout.favorite_row, parent, false);
	}

	/**
	 * Na pritisak thumb sličice, velika slika će se prikazati u
	 * dialogu(preview)
	 * 
	 * @param v
	 */
	private void showImageInDialog(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		ImageView image = new ImageView(activity);
		String address = ((String) v.getTag()).replace("-thumb", "");
		// Ovdje koristim za prikaz samo sliku sa diska, da je ne keširam u
		// lruCache, jer mi lruCache bitan samo za brzinu prilaza listView
		image.setImageBitmap(cache.getBitmapFromFile(address));
		builder.setView(image);
		builder.setNegativeButton("Close", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub

			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();

	}

	/**
	 * JSONObject sa svim potrebnim podacima za prikaz na karti ili edit
	 * 
	 * @param row
	 * @return
	 */
	public JSONObject getJsonObject(int row) {
		JSONObject json = new JSONObject();
		Cursor c = getCursor();
		c.moveToPosition(row);

		try {
			json.put("name", c.getString(1));
			json.put("address", c.getString(2));
			json.put("geoLocation", c.getString(3));
			json.put("image", c.getString(4));
			json.put("_id", c.getInt(0));
		} catch (JSONException e) {

		}
		return json;
	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v instanceof ImageView) {
				showImageInDialog(v);
			} else {
				int row = (Integer) v.getTag();
				showEditFragment(row);
			}

		}
	};

	/**
	 * Prikaz fragmenta za izmjenu podataka u bazi
	 * 
	 * @param row
	 */
	private void showEditFragment(Integer row) {
		Bundle b = new Bundle();
		b.putString("json", getJsonObject(row).toString());
		Fragment editfFragment = new EditFragment();
		editfFragment.setArguments(b);
		Tools.showFragment(activity, editfFragment);
	}

}
