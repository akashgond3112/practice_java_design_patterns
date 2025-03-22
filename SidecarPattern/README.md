# Sidecar Pattern Demo

This project demonstrates the Sidecar Pattern using a simple Java application. The sidecar pattern is a single-node pattern made up of two containers: the application container and the sidecar container. The sidecar container enhances and extends the functionality of the main application container without being part of it.

## Architecture

![Sidecar Pattern Architecture](docs/Sidecar%20Pattern%20Architecture%20Diagram%202025-03-22%2018.43.22.excalidraw.png)

The architecture consists of:

1. **Main Application** - The core business logic container that focuses on its primary responsibility.
2. **Sidecar** - A helper container that provides supporting features like logging, monitoring, and configuration.
3. **Shared Resources** - Communication happens via shared resources (filesystem, localhost network, etc.)

## Key Benefits

- **Separation of Concerns**: The main application focuses on business logic while the sidecar handles cross-cutting concerns.
- **Independent Development**: Teams can develop and deploy the main application and sidecars independently.
- **Language/Framework Agnostic**: Sidecars can be written in different languages or frameworks than the main application.
- **Reusability**: Sidecars can be reused across multiple applications.

## Project Structure

```
sidecar-pattern-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── sidecar/
│   │   │               ├── BusinessService.java
│   │   │               ├── LoggingSidecar.java
│   │   │               ├── MainApplication.java
│   │   │               └── Sidecar.java
│   │   └── resources/
│   │       └── logback.xml
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── sidecar/
│                       ├── BusinessServiceTest.java
│                       └── LoggingSidecarTest.java
├── pom.xml
├── docs/
│   └── Sidecar Pattern Architecture Diagram 2025-03-22 18.43.22.excalidraw.png
└── README.md
```

## Implementation Details

In this demo:

1. **MainApplication**: The core application that performs business logic.
2. **LoggingSidecar**: A sidecar that monitors logs and collects metrics.
3. **BusinessService**: Simulates processing business requests.
4. **Sidecar Interface**: Defines the contract for all sidecar implementations.

## Running the Application

**Prerequisites:**
- Java 17 or later
- Maven 3.6 or later

**Build the project:**
```bash
mvn clean package
```

**Run the application:**
```bash
java -jar target/sidecar-pattern-demo-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Real-World Applications

In production environments, the sidecar pattern is commonly used for:

1. **Logging and Monitoring**: Collecting and forwarding logs and metrics.
2. **Security**: Adding authentication, authorization, and encryption.
3. **Configuration**: Managing and refreshing application configuration.
4. **Service Mesh**: Implementing service discovery, load balancing, and circuit breaking.
5. **Legacy Application Enhancement**: Adding modern capabilities to legacy applications.

## Containerization

In a containerized environment, you would typically run both the main application and sidecar in the same pod (Kubernetes) or as services in the same service mesh. This demo simulates this pattern using threads within a single JVM process.

## Further Reading

- [Design Patterns for Microservices](https://microservices.io/patterns/index.html)
- [Sidecar Pattern - Microsoft Cloud Design Patterns](https://docs.microsoft.com/en-us/azure/architecture/patterns/sidecar)
- [Istio - Example of Sidecar Implementation](https://istio.io/latest/docs/concepts/what-is-istio/)

## License

This project is licensed under the MIT License - see the LICENSE file for details.