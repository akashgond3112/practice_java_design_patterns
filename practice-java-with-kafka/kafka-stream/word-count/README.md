# Kafka Word Count Demo

This directory contains a simple Kafka Streams application that performs word counting on messages from a Kafka topic.

## Setup

Ensure you have Kafka running locally on port 9092. If not, you can use the Docker Compose file in the parent directory:

```bash
cd ../../../..
docker-compose up -d
```

## Components

1. **WordCountApp**: The main Kafka Streams application that processes words
2. **WordCountProducer**: A simple producer to send messages to the input topic
3. **WordCountConsumer**: A consumer that displays the word count results
4. **CreateWordCountTopics**: A utility to create the required Kafka topics

## Running the Application

### 1. Create the Kafka Topics

First, ensure the required topics exist:

```bash
mvn compile exec:java -Dexec.mainClass="com.java.design.patterns.CreateWordCountTopics"
```

### 2. Start the Word Count Application

```bash
mvn compile exec:java -Dexec.mainClass="com.java.design.patterns.WordCountApp"
```

Keep this running in one terminal.

### 3. Produce Messages

In another terminal, run:

```bash
mvn compile exec:java -Dexec.mainClass="com.java.design.patterns.WordCountProducer"
```

Type text messages and press Enter to send them to the Kafka input topic. The WordCountApp will process these messages and output the word counts to the output topic.

Type `exit` to quit the producer.

### 4. Consume Messages with the Java Consumer

In another terminal, run:

```bash
mvn compile exec:java -Dexec.mainClass="com.java.design.patterns.WordCountConsumer"
```

This will display the word counts in a nicely formatted table.

### 5. Consume from Output Topic (Optional)

Alternatively, you can use Kafka's console consumer to see the word count results:

```bash
docker exec -it kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic word-count-output --from-beginning --property print.key=true --property key.separator=" : "
```

## Building and Running the Fat JAR

To build a fat JAR with all dependencies:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory. You can run it directly:

```bash
java -jar target/word-count-1.0-SNAPSHOT-jar-with-dependencies.jar
```
