#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <interactive|multi_os>"
    exit 1
fi

mode="$1"

if [[ "$mode" != "interactive" && "$mode" != "multi_os" ]]; then
    echo "Invalid mode. Please choose 'interactive' or 'multi_os'."
    exit 1
fi

echo "Compiling project..."
mvn compile

if [ $? -eq 0 ]; then
    echo "Compilation successful. Running the application..."
    clear
    mvn exec:java -Dexec.mainClass="org.os.util.Main" -Dexec.args="$mode"
else
    echo "Compilation failed."
fi
