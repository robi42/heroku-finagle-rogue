#!/bin/sh

echo "Killing production process."

kill -9 $(cat PROD_PID)

rm PROD_PID
