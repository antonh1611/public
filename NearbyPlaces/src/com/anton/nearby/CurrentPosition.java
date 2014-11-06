package com.anton.nearby;

public class CurrentPosition {
	private Double latitude;
	private Double longitude;

	/**
	 * Klasa koja sprema podatke o trenutnoj poziciji koji se šalju na fetch sa
	 * servera
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public CurrentPosition(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

}
