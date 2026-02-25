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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main entry point for the forsp interpreter.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: forsp.Main <path>");
            System.exit(1);
        }

        String inputPath = args[0];
        String input;

        try {
            input = new String(Files.readAllBytes(Paths.get(inputPath)));
        } catch (IOException e) {
            System.err.println("Failed to read file: " + inputPath);
            System.exit(1);
            return;
        }

        ForspInterpreter interpreter = new ForspInterpreter();
        interpreter.run(input);
    }
}

/**
 * Main interpreter class that coordinates all components.
 */
class ForspInterpreter {
    private final State state;
    private final Reader reader;
    private final Printer printer;
    private final Environment env;
    private final Evaluator evaluator;

    public ForspInterpreter() {
        this.state = new State();
        this.reader = new Reader(state);
        this.printer = new Printer(state);
        this.env = new Environment(state);
        this.evaluator = new Evaluator(state, reader, printer, env);
    }

    public void run(String input) {
        setup(input);

        ForspObj obj = reader.read();
        evaluator.compute(obj, new ForspObj[]{state.env});
    }

    private void setup(String input) {
        state.resetInput(input);
        state.readStack = null;
        state.nil = ForspObj.makeNil();

        // Set up interned atoms
        state.internedAtoms = state.nil;
        state.atomTrue = reader.intern("t");
        state.atomQuote = reader.intern("quote");
        state.atomPush = reader.intern("push");
        state.atomPop = reader.intern("pop");

        state.stack = state.nil;

        // Set up all primitives
        ForspObj env = state.nil;
        env = registerPrimitives(env);

        state.env = env;
    }

    private ForspObj definePrim(ForspObj env, String name, ForspObj.PrimitiveFunction func) {
        return this.env.define(env, reader.intern(name), ForspObj.makePrim(func));
    }

    private ForspObj registerPrimitives(ForspObj env) {
        // Core primitives
        env = definePrim(env, "push", e -> {
            e.push(e.getEnv().find(e.getCurrentEnv(), e.pop()));
        });
        env = definePrim(env, "pop", e -> {
            ForspObj k = e.pop();
            ForspObj v = e.pop();
            e.setCurrentEnv(e.getEnv().define(e.getCurrentEnv(), k, v));
        });
        env = definePrim(env, "eq", e -> {
            ForspObj b = e.pop();
            ForspObj a = e.pop();
            e.push(ForspObj.equal(a, b) ? e.getState().atomTrue : e.getState().nil);
        });
        env = definePrim(env, "cons", e -> {
            ForspObj b = e.pop();
            ForspObj a = e.pop();
            e.push(ForspObj.makePair(a, b));
        });
        env = definePrim(env, "car", e -> e.push(e.pop().car()));
        env = definePrim(env, "cdr", e -> e.push(e.pop().cdr()));
        env = definePrim(env, "cswap", e -> {
            if (e.pop() == e.getState().atomTrue) {
                ForspObj b = e.pop();
                ForspObj a = e.pop();
                e.push(a);
                e.push(b);
            }
        });
        env = definePrim(env, "tag", e -> e.push(ForspObj.makeNum(e.pop().getTag().ordinal())));
        env = definePrim(env, "read", e -> e.push(e.getReader().read()));
        env = definePrim(env, "print", e -> e.getPrinter().println(e.pop()));

        // Extra primitives
        env = definePrim(env, "stack", e -> e.push(e.getState().stack));
        env = definePrim(env, "env", e -> e.push(e.getState().env));
        env = definePrim(env, "-", e -> {
            ForspObj b = e.pop();
            ForspObj a = e.pop();
            e.push(ForspObj.makeNum(a.toLong() - b.toLong()));
        });
        env = definePrim(env, "*", e -> {
            ForspObj b = e.pop();
            ForspObj a = e.pop();
            e.push(ForspObj.makeNum(a.toLong() * b.toLong()));
        });
        env = definePrim(env, "nand", e -> {
            ForspObj b = e.pop();
            ForspObj a = e.pop();
            e.push(ForspObj.makeNum(~(a.toLong() & b.toLong())));
        });
        env = definePrim(env, "<<", e -> {
            ForspObj b = e.pop();
            ForspObj a = e.pop();
            e.push(ForspObj.makeNum(a.toLong() << b.toLong()));
        });
        env = definePrim(env, ">>", e -> {
            ForspObj b = e.pop();
            ForspObj a = e.pop();
            e.push(ForspObj.makeNum(a.toLong() >> b.toLong()));
        });

        return env;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public State getState() {
        return state;
    }

    public Reader getReader() {
        return reader;
    }

    public Printer getPrinter() {
        return printer;
    }
}
