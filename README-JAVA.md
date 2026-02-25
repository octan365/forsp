# Forsp Java Implementation

This is a Java reimplementation of the forsp Lisp-like stack-based interpreter.

## Description

Forsp is a stack-based functional language with Lisp-like syntax. Key features:

- **Stack-based evaluation**: Values are pushed to and popped from a value stack
- **First-class functions**: Functions are values that can be passed around
- **Closures**: Functions can capture their environment
- **Quote/Push/Pop syntax**: Special operators for manipulating the stack

## Syntax

- `'x` - Quote atom `x`
- `^x` - Push atom `x` (from environment) to stack
- `$x` - Pop from stack and bind to `x` in environment
- `($args ...body) $name` - Define function
- `()` - Nil/empty list
- `(a b c)` - List/sequence

## Building

```bash
./build.sh
```

## Running

```bash
java -cp bin forsp.Main examples/demo.fp
java -cp bin forsp.Main examples/factorial.fp
java -cp bin forsp.Main tutorial.fp
```

## Testing

```bash
javac -d bin TestForsp.java
java -cp bin forsp.TestForsp
```

## Implementation Details

### Components

- **ForspObj.java**: Tagged union object system (NIL, ATOM, NUM, PAIR, CLOS, PRIM)
- **State.java**: Global interpreter state
- **Reader.java**: S-expression parser with quote/push/pop syntax
- **Printer.java**: S-expression printer
- **Environment.java**: Variable lookup and binding
- **Evaluator.java**: Expression evaluation and value stack
- **Main.java**: Main entry point and primitive registration

### Differences from C Implementation

1. **No low-level pointer primitives**: The C version includes primitives for
   direct memory access (ptr-read!, ptr-write!, etc.) which are not portable
   to Java due to its memory safety model.

2. **Object-oriented design**: Uses classes instead of structs and tagged unions.

3. **No manual memory management**: Java handles garbage collection.

4. **Exceptions instead of abort()**: Runtime errors throw RuntimeException.

## Examples

### Factorial

```
($self $n
  ^if (^n 0 eq) 1
    (^n 1 - self ^n *)
  endif
) rec $factorial
5 factorial print
```

### Y-Combinator

```
($f
  ($x (^x x) f)
  ($x (^x x) f)
  force
) $Y
```

## License

MIT License - Same as original forsp implementation
