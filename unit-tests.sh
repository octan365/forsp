#!/bin/bash
# Unit tests for the forsp Java implementation

IMAGE_NAME="docker.io/library/eclipse-temurin:17"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FAILED=0
PASSED=0

echo "Running forsp Java unit tests..."
echo ""

cleanup() {
    rm -f test-*.fp 2>/dev/null || true
}
trap cleanup EXIT

# Test 1: Simple arithmetic
echo -n "Test 1: Simple arithmetic (5 6 -) ... "
if echo '(5 6 - print)' | podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin | grep -q -- "-1"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

# Test 2: Multiplication
echo -n "Test 2: Multiplication (5 6 *) ... "
echo "(5 6 * print)" > test-mult.fp
if podman run --rm -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main test-mult.fp | grep -q -- "30"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

# Test 3: List cons
echo -n "Test 3: List cons (a b cons) ... "
if echo '(a b cons print)' | podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin | grep -q -- "(a"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

# Test 4: Car operation
echo -n "Test 4: Car operation ... "
if echo '(a b cons car print)' | podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin | grep -q -- "a"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

# Test 5: Cdr operation
echo -n "Test 5: Cdr operation ... "
if echo '(a b cons cdr print)' | podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin | grep -q -- "b"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

# Test 6: Equality (atoms)
echo -n "Test 6: Equality (t t eq) ... "
if echo '(t t eq print)' | podman run --rm -i -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main /dev/stdin | grep -q -- "t"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

# Test 7: Number equality
echo -n "Test 7: Number equality (3 3 eq) ... "
echo "(3 3 eq print)" > test-num-eq.fp
if podman run --rm -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main test-num-eq.fp | grep -q -- "t"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

# Test 8: Inequality
echo -n "Test 8: Inequality (3 4 eq) ... "
echo "(4 5 eq print)" > test-neq.fp
if podman run --rm -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main test-neq.fp | grep -q -- "()"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

# Test 9: Complex expression
echo -n "Test 9: Complex expression (5 5 + print) ... "
echo "(5 5 + print)" > test-complex.fp
if podman run --rm -v "$SCRIPT_DIR:/workspace:z" -w /workspace "$IMAGE_NAME" java -cp bin forsp.Main test-complex.fp | grep -q -- "10"; then
    echo "PASS"
    PASSED=$((PASSED + 1))
else
    echo "FAIL"
    FAILED=$((FAILED + 1))
fi

echo ""
echo "========================================"
echo "Unit Tests: $PASSED passed, $FAILED failed"
echo "========================================"

if [ $FAILED -eq 0 ]; then
    echo -e "\033[0;32mAll unit tests passed!\033[0m"
    exit 0
else
    echo -e "\033[0;31mSome tests failed.\033[0m"
    echo ""
    echo "Note: Complex features ($ directive, recursion, etc.) are still in progress."
    exit 1
fi
