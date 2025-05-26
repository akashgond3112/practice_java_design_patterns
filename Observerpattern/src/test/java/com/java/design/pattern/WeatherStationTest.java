package com.java.design.pattern;

import com.java.design.patterns.CurrentConditionsDisplay;
import com.java.design.patterns.ForecastDisplay;
import com.java.design.patterns.StatisticsDisplay;
import com.java.design.patterns.WeatherStation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherStationTest {

	private WeatherStation weatherStation;
	private CurrentConditionsDisplay currentDisplay;
	private StatisticsDisplay statisticsDisplay;
	private ForecastDisplay forecastDisplay;

	@BeforeEach
	void setUp() {
		weatherStation = new WeatherStation();
		currentDisplay = new CurrentConditionsDisplay(weatherStation);
		statisticsDisplay = new StatisticsDisplay(weatherStation);
		forecastDisplay = new ForecastDisplay(weatherStation);
	}

	@Test
	void testInitialState() {
		assertEquals(0.0f, weatherStation.getTemperature());
		assertEquals(0.0f, weatherStation.getHumidity());
		assertEquals(0.0f, weatherStation.getPressure());
	}

	@Test
	void testCurrentConditionsDisplay() {
		weatherStation.setMeasurements(25.0f, 65.0f, 30.4f);

		assertEquals(25.0f, currentDisplay.getTemperature());
		assertEquals(65.0f, currentDisplay.getHumidity());
	}

	@Test
	void testStatisticsDisplay() {
		weatherStation.setMeasurements(25.0f, 65.0f, 30.4f);
		weatherStation.setMeasurements(26.0f, 70.0f, 29.8f);
		weatherStation.setMeasurements(24.5f, 69.0f, 29.2f);

		assertEquals(26.0f, statisticsDisplay.getMaxTemp());
		assertEquals(24.5f, statisticsDisplay.getMinTemp());
		assertEquals(25.1667f, statisticsDisplay.getAvgTemp(), 0.001f);
	}

	@Test
	void testForecastDisplay() {
		// Initial pressure is 29.92f
		weatherStation.setMeasurements(25.0f, 65.0f, 30.4f);
		assertEquals(30.4f, forecastDisplay.getCurrentPressure());
		assertEquals(29.92f, forecastDisplay.getLastPressure());

		// Pressure drops
		weatherStation.setMeasurements(25.0f, 65.0f, 29.2f);
		assertEquals(29.2f, forecastDisplay.getCurrentPressure());
		assertEquals(30.4f, forecastDisplay.getLastPressure());
	}

	@Test
	void testRemoveObserver() {
		weatherStation.setMeasurements(25.0f, 65.0f, 30.4f);
		assertEquals(25.0f, currentDisplay.getTemperature());

		weatherStation.removeObserver(currentDisplay);
		weatherStation.setMeasurements(26.0f, 70.0f, 29.8f);

		// Temperature should not be updated after removal
		assertEquals(25.0f, currentDisplay.getTemperature());
	}
}
