package com.java.design.patterns;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatisticsDisplay implements Observer, DisplayElement {
	// Used for testing
	@Getter
	private float maxTemp = 0.0f;
	@Getter
	private float minTemp = 200;
	@Getter
	private float tempSum = 0.0f;
	@Getter
	private int numReadings = 0;
	@Getter
	private final Subject weatherData;

	public StatisticsDisplay(Subject weatherData) {
		this.weatherData = weatherData;
		weatherData.registerObserver(this);
	}

	@Override
	public void update(float temp, float humidity, float pressure) {
		tempSum += temp;
		numReadings++;

		if (temp > maxTemp) {
			maxTemp = temp;
		}

		if (temp < minTemp) {
			minTemp = temp;
		}

		display();
	}

	@Override
	public void display() {
		log.info(String.format("Weather Statistics: Avg/Max/Min temperature = %.1f/%.1f/%.1f", (tempSum / numReadings),
				maxTemp, minTemp));
	}

	public float getAvgTemp() {
		return numReadings > 0 ? tempSum / numReadings : 0;
	}
}
