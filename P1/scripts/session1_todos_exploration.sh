#!/bin/bash

BASE_URL="http://localhost:4567"

echo "=============================="
echo " ECSE 429 Exploratory Session"
echo " Todo API - Session 1"
echo "=============================="

echo -e "\n--- GET all todos ---"
curl -i $BASE_URL/todos

echo -e "\n--- POST create todo (valid) ---"
curl -i -X POST $BASE_URL/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Study","description":"ECSE 429","doneStatus":false}'

echo -e "\n--- GET todo by id (example: id=1) ---"
curl -i $BASE_URL/todos/1

echo -e "\n--- POST update todo (partial update) ---"
curl -i -X POST $BASE_URL/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"doneStatus":true}'

echo -e "\n--- PUT update todo (missing mandatory field) ---"
curl -i -X PUT $BASE_URL/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"description":"Updated"}'

echo -e "\n--- DELETE todo ---"
curl -i -X DELETE $BASE_URL/todos/1

echo -e "\n--- GET deleted todo ---"
curl -i $BASE_URL/todos/1

echo -e "\n--- DELETE same todo again (undocumented behavior) ---"
curl -i -X DELETE $BASE_URL/todos/1

echo -e "\n--- POST missing required field (no title) ---"
curl -i -X POST $BASE_URL/todos \
  -H "Content-Type: application/json" \
  -d '{"description":"No title"}'

echo -e "\n--- POST empty title ---"
curl -i -X POST $BASE_URL/todos \
  -H "Content-Type: application/json" \
  -d '{"title":""}'

echo -e "\n--- POST extra undocumented field ---"
curl -i -X POST $BASE_URL/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","priority":10}'

echo -e "\n--- GET all todos as XML ---"
curl -i $BASE_URL/todos -H "Accept: application/xml"

echo -e "\n--- POST new todo using XML ---"
curl -i -X POST $BASE_URL/todos \
  -H "Content-Type: application/xml" \
  -d '<todo><title>XML Task</title></todo>'

echo -e "\n--- POST malformed JSON ---"
curl -i -X POST $BASE_URL/todos \
  -H "Content-Type: application/json" \
  -d '{"title":'

echo -e "\n--- SHUTDOWN server ---"
curl -i $BASE_URL/shutdown

echo -e "\n--- END OF SESSION SCRIPT ---"
