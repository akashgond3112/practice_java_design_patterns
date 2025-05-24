package com.java.design.patterns;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ForecastDisplay implements Observer, DisplayElement {
	// Used for testing
	@Getter
	private float currentPressure = 29.92f;
	@Getter
	private float lastPressure;
	private final Subject weatherData;

	public ForecastDisplay(Subject weatherData) {
		this.weatherData = weatherData;
		weatherData.registerObserver(this);
	}

	@Override
	public void update(float temp, float humidity, float pressure) {
		lastPressure = currentPressure;
		currentPressure = pressure;
		display();
	}

	@Override
	public void display() {
		log.info("Forecast: {}", currentPressure > lastPressure ?
				"Improving weather on the way!" :
				currentPressure == lastPressure ? "More of the same" : "Watch out for cooler, rainy weather");
	}

}
