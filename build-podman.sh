#!/bin/bash
# Build script for forsp Java implementation using podman

set -e

# Container image with Java development kit (using fully qualified name)
IMAGE_NAME="docker.io/library/eclipse-temurin:17"

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Create bin directory if it doesn't exist
mkdir -p "$SCRIPT_DIR/bin"

echo "Building forsp Java implementation using podman..."

# Run javac in the podman container
podman run --rm \
  -v "$SCRIPT_DIR:/workspace:z" \
  -w /workspace \
  "$IMAGE_NAME" \
  javac -d bin -sourcepath . *.java

# Fix ownership of output directory (in case it was created by container)
sudo chown -R $(whoami):$(whoami) "$SCRIPT_DIR/bin" 2>/dev/null || true

echo "Build complete!"
echo ""
echo "To run the interpreter:"
echo "  podman run --rm -v '$SCRIPT_DIR:/workspace:z' -w /workspace '$IMAGE_NAME' java -cp bin forsp.Main <file.fp>"
echo ""
echo "To run a forsp example:"
echo "  podman run --rm -v '$SCRIPT_DIR:/workspace:z' -w /workspace '$IMAGE_NAME' java -cp bin forsp.Main examples/factorial.fp"
echo ""
echo "To run tests:"
echo "  podman run --rm -v '$SCRIPT_DIR:/workspace:z' -w /workspace '$IMAGE_NAME' java -cp bin forsp.TestForsp"
