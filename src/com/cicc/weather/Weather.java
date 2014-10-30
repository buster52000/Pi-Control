package com.cicc.weather;

import java.util.ArrayList;

import org.apache.commons.lang3.text.WordUtils;
import org.bbelovic.weather.WeatherModel;
import org.bbelovic.weather.YahooWeatherReader;

import com.cicc.geoLoc.GeoLocation;
import com.cicc.gpio.LCDController;
import com.cicc.speech.Speak;
import com.cicc.voiceCont.Main;
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
		WeatherModel forecast = model.getForecast().get(0);
		String low = forecast.getTemperature().replaceAll(" - \\d+", "");
		String high = forecast.getTemperature().replaceAll("\\d+ - ", "");
		String wForecast = "The weather forecast is, " + forecast.getCondition() + " with a high of " + high + " degrees, and a low of " + low + " degrees";
		System.out.println(cWeather1 + " " + cWeather2);
		System.out.println(wForecast);
		ArrayList<String> cWeather = new ArrayList<String>();
		cWeather.add(cWeather1);
		cWeather.add(cWeather2);
		LCDController lcd = Main.lcd;
		if (lcd != null) {
			lcd.aquireLock();
			lcd.setMode(LCDController.LCD_MODE_WRITE);
			lcd.clear();
			String conditionStr = model.getCondition().toLowerCase().replaceAll("and", "&").replaceAll("thunder", "t").replaceAll("showers", "shwrs");
			conditionStr = WordUtils.capitalizeFully(conditionStr);
			lcd.setCursorPosition(0, 0);
			lcd.write(conditionStr);
			String tempString = "Temp: " + model.getTemperature();
			lcd.setCursorPosition(1, 0);
			lcd.write(tempString);
		}
		Speak.say(cWeather);
		if (lcd != null) {
			lcd.clear();
			String conditionStr = forecast.getCondition().toLowerCase().replaceAll("and", "&").replaceAll("thunder", "t").replaceAll("showers", "shwrs");
			conditionStr = WordUtils.capitalizeFully(conditionStr);
			lcd.setCursorPosition(0, 0);
			lcd.write(conditionStr);
			String tempString = "Temp H:" + high + " L:" + low;
			lcd.setCursorPosition(1, 0);
			lcd.write(tempString);
			lcd.releaseLock();
		}
		Speak.say(wForecast);
		if (lcd != null) {
			lcd.setMode(LCDController.LCD_MODE_MAIN);
		}
	}

	public static void main(String args[]) {
		Weather weather = new Weather();
		weather.start();
	}

}
