/*
 * Interface koji treba implementirati kako bi se, bez obzira na source podataka, vratio isti JSONArray za prikaz
 */
package com.anton.nearby;

import org.json.JSONArray;

public interface Places {

	/**
	 * Vraća JSONArray sa fetchanim podacima u obliku [{"name" : "NAME",
	 * "address" : "ADDRESS", "geoLocation" :
	 * "*.******,*.******","image":"ImageUrl"}]
	 * 
	 * @return
	 */
	public JSONArray getNearbyPlaces(String parameters);

}
