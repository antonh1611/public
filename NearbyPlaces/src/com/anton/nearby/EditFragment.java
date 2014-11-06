package com.anton.nearby;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EditFragment extends Fragment implements OnClickListener {
	private static final int CAMERA_REQUEST = 3232;
	private Cache cache;
	private Button takePhoto;
	private Button done;
	private Button delete;
	private ImageView image;
	private EditText editName;
	private EditText editAddress;
	private Integer id;
	private File filePath;
	private TextView post;
	private String imagePath;
	private String name;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.edit_fragment, container, false);
	}

	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		cache = Tools.getCache(getActivity());
		Bundle b = this.getArguments();
		if (b != null) {
			setParams(b, view);
		}
		takePhoto = (Button) view.findViewById(R.id.editButtonTakePicture);
		takePhoto.setOnClickListener(this);

		done = (Button) view.findViewById(R.id.editButtonDone);
		done.setOnClickListener(this);
		delete = (Button) view.findViewById(R.id.editButtonDelete);
		delete.setOnClickListener(this);

	}

	/**
	 * Metoda za postavljanje početnih parametara i pronalaženje View-ova
	 * 
	 * @param b
	 * @param view
	 */
	private void setParams(Bundle b, View view) {
		JSONObject json = null;
		try {
			json = new JSONObject(b.getString("json"));
		} catch (JSONException e) {

		}

		if (json != null) {
			name = json.optString("name");
			String address = json.optString("address");
			imagePath = json.optString("image");
			id = json.optInt("_id");
			editName = (EditText) view.findViewById(R.id.editName);
			editAddress = (EditText) view.findViewById(R.id.editAddress);
			image = (ImageView) view.findViewById(R.id.editImage);

			editName.setText(name);
			editAddress.setText(address);
			image.setImageBitmap(cache.getBitmapFromFile(imagePath));
			image.setAdjustViewBounds(true);
			post = (TextView) view.findViewById(R.id.editTextPost);
			checkPostText();

		}
	}

	/**
	 * Provjera i prikaz texta za postanje slike ukoliko je postavljena custom
	 * slika
	 */
	private void checkPostText() {
		if (!Tools.isUrl(imagePath)) {
			post.setVisibility(View.VISIBLE);
			post.setOnClickListener(EditFragment.this);
		}
	}

	/**
	 * Metoda za startanje Camera intenta za dohvaćanje slike
	 */
	private void startCameraForCapture() {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		String random = ((Long) System.currentTimeMillis()).toString();
		filePath = new File(cache.EXTERNAL_PATH, random + ".bmp");
		filePath.getParentFile().mkdirs();

		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePath));
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}

	/**
	 * Ovdje ćemo dohvaćenu sliku spremiti u Cache i prikazati je u ImageView
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST
				&& resultCode == getActivity().RESULT_OK) {

			imagePath = filePath.getAbsolutePath();
			cache.setImageFromCache(filePath.getAbsolutePath(), image);
			ContentValues cv = new ContentValues();
			cv.put("image", filePath.getAbsolutePath());
			updateData(cv);

			refreshFavAdapter();
			checkPostText();

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Metoda za update table u bazi podataka
	 * 
	 * @param cv
	 */
	private void updateData(ContentValues cv) {

		DBUtils.updateTable(getActivity(), cv, "_id='" + id.toString() + "'");

	}

	@Override
	public void onClick(View v) {

		if (v == post) {
			Intent i = new Intent(getActivity(), UploadActivity.class);
			i.putExtra("filePath", imagePath);
			i.putExtra("fileName", name);
			startActivity(i);
		}
		if (v == takePhoto) {
			startCameraForCapture();
		}
		if (v == delete) {
			deleteConfirmation(id);
		}

		if (v == done) {
			String name = editName.getText().toString();
			String address = editAddress.getText().toString();
			ContentValues cv = new ContentValues();
			cv.put("name", name);
			cv.put("address", address);
			updateData(cv);
			Tools.fragmentBack(getActivity());
			refreshFavAdapter();
		}

	}

	private void refreshFavAdapter() {
		try {
			ObjectHolder.getFavoritesAdapter().getCursor().requery();
			ObjectHolder.getFavoritesAdapter().notifyDataSetChanged();
		} catch (Exception e) {

		}
	}

	/**
	 * OK, trebam potvrdu za brisanje, da spriječim slučajni pritisak buttona
	 * 
	 * @param id
	 */
	private void deleteConfirmation(final int id) {
		AlertDialog ad = Tools.getAlertDialog(getActivity(), "Delete",
				"Are you sure you wan't to delete this record?");
		ad.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteFromTable(id);

					}
				});
		ad.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
		ad.show();
	}

	/**
	 * Metoda koja stvarno briše red iz table
	 * 
	 * @param id
	 */
	private void deleteFromTable(int id) {
		String sqlQuery = "DELETE FROM " + DBUtils.TABLE_NAME + " WHERE _id='"
				+ id + "'";
		DBUtils.execQuery(getActivity(), sqlQuery);
		refreshFavAdapter();
		Tools.fragmentBack(getActivity());
	}

}
