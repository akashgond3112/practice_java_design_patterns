# Observer Pattern in Java

This project demonstrates the implementation of the Observer Pattern in Java using a weather monitoring system as an example.

## Overview

The Observer Pattern is a behavioral design pattern where an object (known as the subject) maintains a list of dependents (observers) and notifies them automatically of any state changes, usually by calling one of their methods.

### Components

- **Subject**: Interface for objects that need to notify observers
- **Observer**: Interface for objects that need to be notified
- **ConcreteSubject**: Implementation of Subject (WeatherStation)
- **ConcreteObserver**: Implementation of Observer (Various display elements)

## Project Structure

```
src/main/java/com/design/patterns
├── Subject.java                 # Subject interface
├── Observer.java                # Observer interface
├── DisplayElement.java          # Interface for display components
├── WeatherStation.java          # Concrete Subject implementation
├── CurrentConditionsDisplay.java # Observer implementation
├── StatisticsDisplay.java        # Observer implementation
├── ForecastDisplay.java          # Observer implementation
└── WeatherStationDemo.java       # Demo application
```

## Use Case: Weather Monitoring

In this implementation, we model a weather monitoring system:

1. **WeatherStation** (Subject): Collects and stores temperature, humidity, and pressure data
2. **Display Components** (Observers):
   - **CurrentConditionsDisplay**: Shows current temperature and humidity
   - **StatisticsDisplay**: Shows min/max/average temperature
   - **ForecastDisplay**: Shows forecast based on barometric pressure

## How It Works

1. Display objects register as observers to the WeatherStation
2. When weather data changes, WeatherStation notifies all registered observers
3. Each observer receives the updated data and refreshes its display accordingly
4. Observers can be added or removed dynamically at runtime

## Key Benefits

- **Loose Coupling**: Subjects and observers are loosely coupled as subjects know nothing about their observers except that they implement the Observer interface
- **Broadcast Communication**: Changes can be broadcast to multiple objects in one go
- **Dynamic Relationships**: Observers can be added or removed at any time

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven

### Build and Run

1. Clone the repository
2. Build the project using Maven:
   ```
   mvn clean install
   ```
3. Run the demo application:
   ```
   mvn exec:java -Dexec.mainClass="com.example.observer.WeatherStationDemo"
   ```

## Testing

The project includes JUnit tests that verify:
- Initial state of the WeatherStation
- Proper notification of observers
- Calculation of statistics from multiple readings
- Adding and removing observers dynamically

Run the tests with:
```
mvn test
```

## Real-World Applications

The Observer Pattern is widely used in software development, particularly in:

- GUI Components (e.g., button click events)
- Event handling systems
- Model-View-Controller (MVC) architectures
- Publish-Subscribe messaging systems
- Stock market monitoring applications
- Social media notification systems

## Design Considerations

### When to Use

- When changes to one object require changing others, and you don't know how many objects need to change
- When an object needs to notify other objects without making assumptions about who those objects are
- When an abstraction has two aspects, one dependent on the other

### When Not to Use

- When the change notification is too complex or specific
- When observers need to know about each other
- For small applications where simple method calls are sufficient

## License

This project is licensed under the MIT License - see the LICENSE file for details.