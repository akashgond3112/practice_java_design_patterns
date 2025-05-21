package com.java.design.patterns;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CurrentConditionsDisplay implements Observer, DisplayElement {
	// Used for testing
	@Getter
	private float temperature;
	@Getter
	private float humidity;
	@Getter
	private final Subject weatherData;

	public CurrentConditionsDisplay(Subject weatherData) {
		this.weatherData = weatherData;
		weatherData.registerObserver(this);
	}

	@Override
	public void update(float temperature, float humidity, float pressure) {
		this.temperature = temperature;
		this.humidity = humidity;
		display();
	}

	@Override
	public void display() {
		log.info(String.format("Current conditions: %.1f°C and %.1f%% humidity%n", temperature, humidity));
	}

}
