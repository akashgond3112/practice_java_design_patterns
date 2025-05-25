package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeatherStationDemo {
	public static void main(String[] args) {
		// Create the WeatherStation (Subject)
		WeatherStation weatherStation = new WeatherStation();

		// Create and register the displays (Observers)
		CurrentConditionsDisplay currentDisplay = new CurrentConditionsDisplay(weatherStation);
		StatisticsDisplay statisticsDisplay = new StatisticsDisplay(weatherStation);
		ForecastDisplay forecastDisplay = new ForecastDisplay(weatherStation);

		log.info("Weather station initialized with three observers.");
		log.info("Simulating weather changes...\n");

		// Simulate weather changes
		weatherStation.setMeasurements(26.0f, 65.0f, 30.4f);
		log.info("");

		weatherStation.setMeasurements(28.0f, 70.0f, 29.2f);
		log.info("");

		weatherStation.setMeasurements(22.0f, 90.0f, 29.2f);
		log.info("");

		// Remove an observer
		log.info("Removing current conditions display...");
		weatherStation.removeObserver(currentDisplay);

		weatherStation.setMeasurements(24.0f, 80.0f, 30.0f);
	}
}
