package com.cicc.weather;

import java.util.ArrayList;

import org.bbelovic.weather.WeatherModel;
import org.bbelovic.weather.YahooWeatherReader;

import com.cicc.geoLoc.GeoLocation;
import com.cicc.texttospeech.Speak;
import com.smartechz.geoloc.GeoPlanetExplorer;

public class Weather {

	private String woeid;
	private String location;

	public Weather() {
		location = GeoLocation.getLoc();
		this.woeid = GeoPlanetExplorer.getWOEID(location);
	}

	public Weather(String location) {
		this.location = location;
		this.woeid = GeoPlanetExplorer.getWOEID(location);
	}

	public void start() {
		YahooWeatherReader weather = new YahooWeatherReader(woeid, "F");
		weather.process();
		WeatherModel model = weather.getWeatherModel();
		String cWeather1 = null, cWeather2 = null;
		cWeather1 = "The Current weather conditions for " + location + " are,";
		cWeather2 = model.getCondition() + ", with a temperature of " + model.getTemperature() + " degrees.";
		WeatherModel forcast = model.getForecast().get(0);
		String low = forcast.getTemperature().replaceAll(" - \\d+", "");
		String high = forcast.getTemperature().replaceAll("\\d+ - ", "");
		String wForcast = "The weather forcast is, " + forcast.getCondition() + " with a high of " + high + " degrees, and a low of " + low + " degrees";
		System.out.println(cWeather1 + " " + cWeather2);
		System.out.println(wForcast);
		ArrayList<String> cWeather = new ArrayList<String>();
		cWeather.add(cWeather1);
		cWeather.add(cWeather2);
		Speak.say(cWeather);
		Speak.say(wForcast);
	}

	public static void main(String args[]) {
		Weather weather = new Weather();
		weather.start();
	}

}
