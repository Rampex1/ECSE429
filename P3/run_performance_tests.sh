#!/bin/bash
echo "Starting system monitor..."
./src/test/java/com/ecse429/monitor_system.sh &
MONITOR_PID=$!

echo "Running performance tests..."
mvn test -Dtest=TodoPerformanceTest

echo "Stopping system monitor..."
kill $MONITOR_PID

echo "Done! Check performance_log.csv for results"
