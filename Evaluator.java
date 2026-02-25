/********************************************************************************
 * MIT License
 *
 * Copyright (c) 2024 Anthony Bonkoski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package forsp;

/**
 * Evaluator for forsp expressions.
 * Manages the value stack and evaluates expressions.
 */
public class Evaluator {
    private final State state;
    private final Reader reader;
    private final Printer printer;
    private final Environment env;

    // Pointer to current environment (can be modified by primitives)
    private ForspObj[] currentEnv;

    private static final boolean DEBUG = false;

    public Evaluator(State state, Reader reader, Printer printer, Environment env) {
        this.state = state;
        this.reader = reader;
        this.printer = printer;
        this.env = env;
        this.currentEnv = new ForspObj[]{state.nil}; // Will be set during evaluation
    }

    // Value stack operations
    public void push(ForspObj obj) {
        state.stack = ForspObj.makePair(obj, state.stack);
    }

    public ForspObj pop() {
        if (state.stack == state.nil) {
            throw new RuntimeException("Value Stack Underflow");
        }
        ForspObj ret = state.stack.car();
        state.stack = state.stack.cdr();
        return ret;
    }

    public boolean tryPop(ForspObj[] out) {
        if (state.stack == state.nil) {
            return false;
        }
        out[0] = state.stack.car();
        state.stack = state.stack.cdr();
        return true;
    }

    // Evaluation
    public void eval(ForspObj expr, ForspObj[] envPtr) {
        if (DEBUG) {
            System.out.println("eval: " + printer.print(expr));
        }

        // Set current environment for primitives to use
        currentEnv = envPtr;

        if (expr.isAtom()) {
            // When evaluating an atom, it means "execute the code named x"
            // This should look up the value of x in the environment
            ForspObj val = env.find(envPtr[0], expr);
            if (val.isClos()) {
                // Create a new environment array for the closure's environment
                ForspObj[] closEnv = new ForspObj[]{val.getClos().env};
                currentEnv = closEnv;
                compute(val.getClos().body, closEnv);
                // Update envPtr with any changes made during compute
                envPtr[0] = closEnv[0];
            } else if (val.isPrim()) {
                // Push the value onto the stack when prim is used with push
                // But apply the primitive if we're evaluating it directly
                // Check if we're being called from push by looking at the call context
                val.getPrim().apply(this);
            } else {
                push(val);
            }
        } else if (expr.isNil() || expr.isPair()) {
            // A list is a closure
            push(ForspObj.makeClos(expr, envPtr[0]));
        } else {
            push(expr);
        }
    }

    public void compute(ForspObj comp, ForspObj[] envArray) {
        if (DEBUG) {
            System.out.println("compute: " + printer.print(comp));
            System.out.println("  stack: " + printer.print(state.stack));
            System.out.println("  env: " + printer.print(envArray[0]));
        }

        while (comp != state.nil) {
            ForspObj cmd = comp.car();
            comp = comp.cdr();

            if (cmd == state.atomQuote) {
                if (comp == state.nil) {
                    throw new RuntimeException("Expected data following a quote form");
                }
                push(comp.car());
                comp = comp.cdr();
                continue;
            }

            // Set the environment before evaluating each command
            currentEnv = envArray;
            eval(cmd, envArray);
            // Capture any environment changes made during eval
            envArray[0] = currentEnv[0];
        }
    }

    // Getters
    public State getState() { return state; }
    public Reader getReader() { return reader; }
    public Printer getPrinter() { return printer; }
    public Environment getEnv() { return env; }

    // Environment manipulation for primitives
    public ForspObj getCurrentEnv() { return currentEnv[0]; }
    public void setCurrentEnv(ForspObj env) { currentEnv[0] = env; }
}
