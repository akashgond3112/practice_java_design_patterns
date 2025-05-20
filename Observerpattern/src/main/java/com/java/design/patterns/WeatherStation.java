package com.java.design.patterns;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * The WeatherStation class represents the Subject in the Observer pattern. It maintains a list of observers and
 * notifies them of any state changes.
 */
public class WeatherStation implements Subject {
	private final List<Observer> observers;
	// Getters for the weather measurements
	@Getter
	private float temperature;
	@Getter
	private float humidity;
	@Getter
	private float pressure;

	public WeatherStation() {
		observers = new ArrayList<>();
	}

	@Override
	public void registerObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(Observer o) {
		int i = observers.indexOf(o);
		if (i >= 0) {
			observers.remove(i);
		}
	}

	@Override
	public void notifyObservers() {
		for (Observer observer : observers) {
			observer.update(temperature, humidity, pressure);
		}
	}

	/**
	 * Called when weather measurements have been updated
	 */
	public void measurementsChanged() {
		notifyObservers();
	}

	/**
	 * Sets the weather measurements and triggers notification
	 *
	 * @param temperature
	 * 		the current temperature
	 * @param humidity
	 * 		the current humidity
	 * @param pressure
	 * 		the current pressure
	 */
	public void setMeasurements(float temperature, float humidity, float pressure) {
		this.temperature = temperature;
		this.humidity = humidity;
		this.pressure = pressure;
		measurementsChanged();
	}

}
