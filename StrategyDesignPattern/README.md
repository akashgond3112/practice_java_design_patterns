# Payment Processing Strategy Pattern

## Overview
This project demonstrates the Strategy Design Pattern using a real-world payment processing example. The Strategy Pattern enables selecting different payment processing algorithms at runtime, making the system flexible and extensible.

## Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── java/
│   │           └── design/
│   │               └── patterns/
│   │                   ├── PaymentStrategy.java
│   │                   ├── CreditCardStrategy.java
│   │                   ├── PayPalStrategy.java
│   │                   ├── CryptoCurrencyStrategy.java
│   │                   ├── PaymentProcessor.java
│   │                   └── Main.java
│   └── resources/
│       └── logback.xml
└── pom.xml
```

## Implementation Details
- `PaymentStrategy`: Interface defining the payment processing contract
- `CreditCardStrategy`: Implementation for credit card payments
- `PayPalStrategy`: Implementation for PayPal payments
- `CryptoCurrencyStrategy`: Implementation for cryptocurrency payments
- `PaymentProcessor`: Context class that uses the payment strategies
- `Main`: Demonstrates usage of different payment strategies

## Features
- Dynamic strategy switching at runtime
- Support for multiple payment methods
- Lambda expression support
- Logging using SLF4J and Lombok
- Easy to add new payment strategies

## Requirements
- Java 11 or higher
- Maven
- Lombok plugin in your IDE

## Running the Application
1. Clone the repository
2. Run `mvn clean install`
3. Execute the `Main` class

## Adding New Payment Strategies
To add a new payment strategy:
1. Create a new class implementing `PaymentStrategy`
2. Implement the `processPayment` method
3. Use the new strategy with `PaymentProcessor`

Example:
```java
public class NewPaymentStrategy implements PaymentStrategy {
    @Override
    public void processPayment(double amount) {
        log.info("Processing payment using new method: ${}", amount);
    }
}
```

## Benefits
- Encapsulation of payment algorithms
- Runtime flexibility
- Easy maintenance and testing
- Clean separation of concerns
- Scalable architecture

## Contributing
Feel free to submit issues and enhancement requests!