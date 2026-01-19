#!/bin/bash

BASE=http://localhost:4567

echo
echo "---- Relationship GET works ----"
echo
curl $BASE/todos/1/categories
echo
curl $BASE/todos/1/task-of
echo

echo "---- Relationship POST rejects ID (undocumented) ----"
echo
curl -X POST $BASE/todos/1/categories \
  -H "Content-Type: application/json" \
  -d '{"id":"1"}'
echo
curl -X POST $BASE/todos/1/task-of \
  -H "Content-Type: application/json" \
  -d '{"id":"1"}'
echo

echo "---- Relationship DELETE works ----"
echo
curl -X DELETE $BASE/todos/1/categories/1
echo
curl -X DELETE $BASE/todos/1/task-of/1
echo

echo "---- Unsupported HTTP methods ----"
echo
curl -X PATCH $BASE/todos/1
echo
curl -X TRACE $BASE/todos
echo

echo "---- Content-Type confusion ----"
echo
curl -X POST $BASE/todos \
  -H "Content-Type: application/xml" \
  -d '{"title":"confused"}'
echo
curl -X POST $BASE/todos \
  -H "Content-Type: application/json" \
  -d '<todo><title>confused</title></todo>'
echo
curl -X POST $BASE/todos \
  -d '{"title":"nocontenttype"}'
echo

echo "---- Done ----"
echo
