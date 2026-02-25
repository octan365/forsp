#!/bin/bash
# Build script for forsp Java implementation

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files
javac -d bin -sourcepath . *.java

echo "Build complete! Run with: java -cp bin forsp.Main <file.fp>"
