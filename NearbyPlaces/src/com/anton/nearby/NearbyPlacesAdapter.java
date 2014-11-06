package com.anton.nearby;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter za prikaz liste sa okolnim objektima na MapFragmentu (inače volim
 * extendati BaseAdapter, jer nije kompliciran za namjestiti a ne stvara
 * probleme kao ArrayAdapter)
 * 
 * @author anton
 *
 */
public class NearbyPlacesAdapter extends BaseAdapter {
	private JSONArray jsonArray;
	private LayoutInflater li;
	private Cache cache;
	private Context context;
	private boolean hideStar;

	public NearbyPlacesAdapter(Context context, LayoutInflater li,
			JSONArray jsonArray) {
		this.context = context;
		this.jsonArray = jsonArray;
		this.li = li;
		cache = Tools.getCache(context);
	}

	@Override
	public int getCount() {
		return jsonArray.length();
	}

	@Override
	public Object getItem(int index) {

		return jsonArray.optJSONObject(index);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Sakriti ću zvijezdicu za dodavanje favorita ukoliko prikazujem objekt iz
	 * Favorita
	 */
	public void hideStar() {
		hideStar = true;
	}

	@Override
	public View getView(int index, View row, ViewGroup parent) {
		if (row == null) {
			row = li.inflate(R.layout.nearby_row, parent, false);
		}
		JSONObject o = jsonArray.optJSONObject(index);

		if (o != null) {
			TextView name = (TextView) row.findViewById(R.id.nearbyName);
			TextView address = (TextView) row.findViewById(R.id.nearbyAddress);
			ImageView image = (ImageView) row.findViewById(R.id.nearbyImage);

			name.setText(o.optString("name"));
			address.setText(o.optString("address"));
			cache.setImageFromCache(o.optString("image") + "-thumb", image);

			ImageView star = (ImageView) row.findViewById(R.id.nearbyStar);
			if (!hideStar) {
				if (o.optBoolean("favorite")) {
					star.setImageResource(android.R.drawable.star_big_on);
				} else {
					star.setImageResource(android.R.drawable.star_big_off);
				}
				star.setTag(index);
				star.setOnClickListener(onClickListener);
			} else {
				star.setVisibility(View.INVISIBLE);
			}

		}

		return row;
	}

	/**
	 * Metoda za dodavanje kliknutog objekta u Favorite (klik na zvijezdicu)
	 * 
	 * @param row
	 */
	private void addToFavorite(int row) {

		JSONObject json = jsonArray.optJSONObject(row);

		String name = json.optString("name");
		String address = json.optString("address");
		String image = json.optString("image");
		String geoLocation = json.optString("geoLocation");

		String dodaj = "INSERT INTO " + DBUtils.TABLE_NAME
				+ "('name','address','image','geoLocation') VALUES ('" + name
				+ "','" + address + "','" + image + "','" + geoLocation + "')";
		DBUtils.execQuery(context, dodaj);
		
		try {
			ObjectHolder.getFavoritesAdapter().getCursor().requery();
			ObjectHolder.getFavoritesAdapter().notifyDataSetChanged();
		} catch (Exception e) {

		}
		if (json != null) {
			try {
				json.put("favorite", true);
			} catch (JSONException e) {
			}
		}

		try {
			jsonArray.put(row, json);
		} catch (JSONException e) {
		}

		notifyDataSetChanged();

	}

	OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int row = (Integer) v.getTag();
			addToFavorite(row);

		}
	};
}
