#!/bin/bash

# Check if the correct number of arguments is provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <source_file> <arg1> <arg2>"
    exit 1
fi

# Compile the Java code
javac Main.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    # Run the compiled Java program with the provided arguments
    java Main "$1" "$2"
else
    echo "Compilation failed."
fi
