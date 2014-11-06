package com.anton.nearby;

/**
 * Klasa za pohranu adaptera (trebam je za refresh favorite liste)
 * 
 * @author anton
 *
 */
public class ObjectHolder {
	private static FavoritesAdapter favoritesAdapter;

	public static FavoritesAdapter getFavoritesAdapter() {
		return favoritesAdapter;
	}

	public static void setFavoritesAdapter(FavoritesAdapter favoritesAdapter) {
		ObjectHolder.favoritesAdapter = favoritesAdapter;
	}

}
