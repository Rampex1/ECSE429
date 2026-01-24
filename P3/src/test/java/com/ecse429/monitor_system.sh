#!/bin/bash

echo "Monitoring system resources (press Ctrl+C to stop)..."
echo "Timestamp,CPU%,MemoryFree(MB)" >performance_log.csv

while true; do
  timestamp=$(date +"%Y-%m-%d %H:%M:%S")
  cpu=$(ps -A -o %cpu | awk '{s+=$1} END {print s}')
  mem_info=$(vm_stat | grep "Pages free")
  mem_free=$(echo "$mem_info" | awk '{print $3}' | sed 's/\.//')
  mem_free_mb=$((mem_free * 4096 / 1024 / 1024))

  echo "$timestamp,$cpu,$mem_free_mb" >>performance_log.csv
  echo "$timestamp - CPU: $cpu% | Free Memory: $mem_free_mb MB"
  sleep 1
done
