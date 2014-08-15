package com.cicc.geoLoc;

public class WLANAdapter {

	private String ssid, macAddress;
	private int signalStrength, snr, channel;
	
	public WLANAdapter(String ssid, String macAddress, int signalStrength, int snr, int channel) {
		this.ssid = ssid;
		this.macAddress = macAddress;
		this.signalStrength = signalStrength;
		this.snr = snr;
		this.channel = channel;
	}
	
	public String getSSID() {
		return ssid;
	}
	
	public String getMacAddress() {
		return macAddress;
	}
	
	public int getSignalStrength() {
		return signalStrength;
	}
	
	public int getSnr() {
		return snr;
	}
	
	public int getChannel() {
		return channel;
	}
	
}
