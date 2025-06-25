#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED=        6)
            echo -e "${GREEN}Building fat JAR...${NC}"
            mvn clean package
            echo -e "${GREEN}JAR created at target/word-count-1.0-SNAPSHOT-jar-with-dependencies.jar${NC}"
            ;;
        7)
            echo -e "${GREEN}Running from fat JAR...${NC}"
            echo -e "${YELLOW}(Press Ctrl+C to stop)${NC}"
            java -jar target/word-count-1.0-SNAPSHOT-jar-with-dependencies.jar
            ;;
        0)'
NC='\033[0m' # No Color

echo -e "${YELLOW}Word Count Kafka Demo Helper Script${NC}"
echo

# Check if Kafka is running
echo -e "${YELLOW}Checking if Kafka is running...${NC}"
kafka_running=$(netstat -tuln | grep 9092 || echo "")

if [ -z "$kafka_running" ]; then
    echo -e "${RED}Kafka doesn't seem to be running on port 9092${NC}"
    echo -e "Would you like to start Kafka using docker-compose? (y/n)"
    read -r start_kafka
    if [[ $start_kafka == "y" || $start_kafka == "Y" ]]; then
        echo -e "${YELLOW}Starting Kafka with docker-compose...${NC}"
        cd ../../../../ || exit
        docker-compose up -d
        echo -e "${GREEN}Waiting 10 seconds for Kafka to start...${NC}"
        sleep 10
        cd - || exit
    else
        echo -e "${YELLOW}Please start Kafka manually and try again${NC}"
        exit 1
    fi
fi

# Menu
function show_menu {
    echo
    echo -e "${YELLOW}Select an option:${NC}"
    echo "1) Create Kafka topics"
    echo "2) Run Word Count Application"
    echo "3) Run Word Count Producer"
    echo "4) Run Word Count Consumer (Java)"
    echo "5) View output topic (requires Docker)"
    echo "6) Build fat JAR"
    echo "7) Run from fat JAR"
    echo "0) Exit"
    echo
}

while true; do
    show_menu
    read -r choice

    case $choice in
        1)
            echo -e "${GREEN}Creating Kafka topics...${NC}"
            mvn compile exec:java -Dexec.mainClass="com.java.design.patterns.CreateWordCountTopics"
            ;;
        2)
            echo -e "${GREEN}Starting Word Count Application...${NC}"
            echo -e "${YELLOW}(Press Ctrl+C to stop)${NC}"
            mvn compile exec:java -Dexec.mainClass="com.java.design.patterns.WordCountApp"
            ;;
        3)
            echo -e "${GREEN}Starting Word Count Producer...${NC}"
            echo -e "${YELLOW}(Type 'exit' to quit)${NC}"
            mvn compile exec:java -Dexec.mainClass="com.java.design.patterns.WordCountProducer"
            ;;
        4)
            echo -e "${GREEN}Starting Word Count Consumer...${NC}"
            echo -e "${YELLOW}(Press Ctrl+C to stop)${NC}"
            mvn compile exec:java -Dexec.mainClass="com.java.design.patterns.WordCountConsumer"
            ;;
        5)
            echo -e "${GREEN}Viewing output from word-count-output topic...${NC}"
            echo -e "${YELLOW}(Press Ctrl+C to stop)${NC}"
            docker exec -it kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic word-count-output --from-beginning --property print.key=true --property key.separator=" : "
            ;;
        6)
            echo -e "${GREEN}Building fat JAR...${NC}"
            mvn clean package
            echo -e "${GREEN}JAR created at target/word-count-1.0-SNAPSHOT-jar-with-dependencies.jar${NC}"
            ;;
        6)
            echo -e "${GREEN}Running from fat JAR...${NC}"
            echo -e "${YELLOW}(Press Ctrl+C to stop)${NC}"
            java -jar target/word-count-1.0-SNAPSHOT-jar-with-dependencies.jar
            ;;
        0)
            echo -e "${GREEN}Exiting...${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid option${NC}"
            ;;
    esac
done
