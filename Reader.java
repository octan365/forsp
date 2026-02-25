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

import java.util.HashMap;
import java.util.Map;

/**
 * Parser for forsp S-expressions.
 */
public class Reader {
    private final State state;
    private final Map<String, ForspObj> atomCache;

    public Reader(State state) {
        this.state = state;
        this.atomCache = new HashMap<>();
    }

    // Atom interning
    public ForspObj intern(String atom) {
        return atomCache.computeIfAbsent(atom, ForspObj::makeAtom);
    }

    // Reader methods
    public void skipWhitespaceAndComments() {
        char c = state.peek();
        if (c == 0) return;

        if (isWhitespace(c)) {
            state.advance();
            skipWhitespaceAndComments();
            return;
        }

        if (c == ';') {
            state.advance();
            while (true) {
                c = state.peek();
                if (c == 0) return;
                state.advance();
                if (c == '\n') break;
            }
            skipWhitespaceAndComments();
            return;
        }
    }

    public static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    public static boolean isDirective(char c) {
        return c == '\'' || c == '^' || c == '$';
    }

    public static boolean isPunctuation(char c) {
        return c == 0 || isWhitespace(c) || isDirective(c) || c == '(' || c == ')' || c == ';';
    }

    public ForspObj readScalar() {
        int start = state.inputPos;
        while (!isPunctuation(state.peek())) {
            state.advance();
        }

        String str = state.inputStr.substring(start, state.inputPos);

        try {
            long num = Long.parseLong(str);
            return ForspObj.makeNum(num);
        } catch (NumberFormatException e) {
            return intern(str);
        }
    }

    public ForspObj readList() {
        // Skip whitespace and check for ) only when read_stack is empty
        if (state.readStack == null) {
            skipWhitespaceAndComments();
            char c = state.peek();
            if (c == ')') {
                state.advance();
                return state.nil;
            }
        }
        // Use read() which will consume from read_stack if available,
        // or read from input otherwise
        ForspObj first = read();
        ForspObj second = readList();
        return ForspObj.makePair(first, second);
    }

    public void setReadStack(ForspObj stack) {
        state.readStack = stack;
    }

    public ForspObj read() {
        if (state.readStack != null) {
            ForspObj result = state.readStack.car();
            setReadStack(state.readStack.cdr());
            return result;
        }

        skipWhitespaceAndComments();

        char c = state.peek();
        if (c == 0) {
            throw new RuntimeException("End of input: could not read()");
        }

        if (c == '\'') {
            state.advance();
            return state.atomQuote;
        }

        if (c == '^') {
            state.advance();
            ForspObj s = null;
            s = ForspObj.makePair(state.atomPush, s);
            s = ForspObj.makePair(readScalar(), s);
            s = ForspObj.makePair(state.atomQuote, s);
            setReadStack(s);
            return read();
        }

        if (c == '$') {
            state.advance();
            ForspObj s = null;
            s = ForspObj.makePair(state.atomPop, s);
            s = ForspObj.makePair(readScalar(), s);
            s = ForspObj.makePair(state.atomQuote, s);
            setReadStack(s);
            return read();
        }

        if (c == '(') {
            state.advance();
            return readList();
        } else {
            return readScalar();
        }
    }
}
