#!/bin/bash

echo "Starting system monitor..."
./src/test/java/com/ecse429/monitor_system.sh &
MONITOR_PID=$!

echo "Running performance tests..."
mvn test -Dtest=TodoPerformanceTest

echo "Stopping system monitor..."
kill $MONITOR_PID

echo ""
echo "Generating charts..."
python3 generate_charts.py

echo ""
echo "Done! Check the following files:"
echo "  - test_results.csv (test performance data)"
echo "  - performance_log.csv (system monitoring data)"
echo "  - performance_analysis.png (main charts)"
echo "  - operation_comparison.png (operation breakdown)"
