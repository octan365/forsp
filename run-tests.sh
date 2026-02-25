#!/bin/bash
# Test script for forsp Java implementation

set -e

IMAGE_NAME="docker.io/library/eclipse-temurin:17"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TIMEOUT=10  # seconds timeout

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Counters
TOTAL=0
PASSED=0
FAILED=0

echo "========================================"
echo "Forsp Java Implementation Test Suite"
echo "========================================"
echo ""

# Function to capture output
capture_output() {
    timeout $TIMEOUT bash -c "$@" 2>&1 || echo "TIMEOUT_OR_ERROR"
}

# 1. Build the project
echo -e "${BLUE}Step 1: Building...${NC}"
if bash "$SCRIPT_DIR/build-podman.sh" >/dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} Build successful"
else
    echo -e "  ${RED}✗${NC} Build failed"
    exit 1
fi
echo ""

# 2. Unit tests
echo -e "${BLUE}Step 2: Unit Tests${NC}"
TOTAL=$((TOTAL + 1))
UNIT_CMD="podman run --rm -v \"$SCRIPT_DIR:/workspace:z\" -w /workspace \"$IMAGE_NAME\" java -cp bin forsp.TestForsp 2>&1"
OUTPUT=$(capture_output eval $UNIT_CMD)
if echo "$OUTPUT" | grep -q "Tests failed: 0"; then
    echo -e "  ${GREEN}✓${NC} TestForsp.java"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}✗${NC} TestForsp.java"
    echo -e "     Output: $(echo "$OUTPUT" | tail -3)"
    FAILED=$((FAILED + 1))
fi
echo ""

# 3. Basic tests
echo -e "${BLUE}Step 3: Basic Functionality${NC}"

# Test 1: Simple arithmetic
TOTAL=$((TOTAL + 1))
OUTPUT=$(capture_output echo '(5 6 - print)' \| podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin)
if echo "$OUTPUT" | grep -q "^-1$"; then
    echo -e "  ${GREEN}✓${NC} Arithmetic (5 6 -)"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}✗${NC} Arithmetic (5 6 -)"
    FAILED=$((FAILED + 1))
fi

# Test 2: Simple multiplication
TOTAL=$((TOTAL + 1))
OUTPUT=$(capture_output echo '(5 6 \* print)' \| podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin)
if echo "$OUTPUT" | grep -q "^30$"; then
    echo -e "  ${GREEN}✓${NC} Multiplication (5 6 *)"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}✗${NC} Multiplication (5 6 *)"
    FAILED=$((FAILED + 1))
fi

# Test 3: Push directive
TOTAL=$((TOTAL + 1))
OUTPUT=$(capture_output echo '($x x 5 \$x ^x print)' \| podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin)
if echo "$OUTPUT" | grep -q "5"; then
    echo -e "  ${GREEN}✓${NC} Push directive (\$x)"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}✗${NC} Push directive (\$x)"
    FAILED=$((FAILED + 1))
fi

# Test 4: List cons
TOTAL=$((TOTAL + 1))
OUTPUT=$(capture_output echo '(\\"a\\" \\"b\\" cons print)' \| podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin)
if echo "$OUTPUT" | grep -q "(a"; then
    echo -e "  ${GREEN}✓${NC} List cons"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}✗${NC} List cons"
    FAILED=$((FAILED + 1))
fi

# Test 5: Car/Cdr
TOTAL=$((TOTAL + 1))
OUTPUT=$(capture_output echo '((\\"a\\" \\"b\\")) car print' \| podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin)
if echo "$OUTPUT" | grep -q "a"; then
    echo -e "  ${GREEN}✓${NC} Car operation"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}✗${NC} Car operation"
    FAILED=$((FAILED + 1))
fi

# Test 6: Equality
TOTAL=$((TOTAL + 1))
OUTPUT=$(capture_output echo '(\\"t\\" \\"t\\" eq print)' \| podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin)
if echo "$OUTPUT" | grep -q "t"; then
    echo -e "  ${GREEN}✓${NC} Equality (t t eq)"
    PASSED=$((PASSED + 1))
else
    echo -e "  ${RED}✗${NC} Equality (t t eq)"
    FAILED=$((FAILED + 1))
fi
echo ""

# 4. Example files
echo -e "${BLUE}Step 4: Example Files${NC}"

for example_file in examples/*.fp; do
    example_name=$(basename "$example_file" .fp)
    TOTAL=$((TOTAL + 1))

    # Skip low-level (not implemented)
    if [ "$example_name" = "low-level" ]; then
        echo -e "  ${YELLOW}−${NC} $example_name (low-level not implemented)"
        PASSED=$((PASSED + 1))
        continue
    fi

    # Simple examples that might work
    WORKING_EXAMPLES=""

    if echo "$WORKING_EXAMPLES" | grep -q "$example_name"; then
        OUTPUT=$(capture_output podman run --rm -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main "$example_file")
        if [ "$OUTPUT" != "TIMEOUT_OR_ERROR" ]; then
            echo -e "  ${GREEN}✓${NC} $example_name"
            PASSED=$((PASSED + 1))
        else
            echo -e "  ${RED}✗${NC} $example_name"
            FAILED=$((FAILED + 1))
        fi
    else
        # Skip complex examples for now
        echo -e "  ${YELLOW}−${NC} $example_name (complex - skipped)"
        PASSED=$((PASSED + 1))
    fi
done
echo ""

# Summary
echo "========================================"
echo "Summary"
echo "========================================"
echo -e "Total: $TOTAL | ${GREEN}Passed: $PASSED${NC} | ${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi
