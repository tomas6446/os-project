#!/bin/bash

echo "Compiling project..."
mvn compile

if [ $? -eq 0 ]; then
    echo "Compilation successful. Running the application..."
    mvn exec:java
else
    echo "Compilation failed."
fi
