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
 * Global state for the forsp interpreter.
 */
public class State {
    public String inputStr;
    public int inputPos;
    public int inputLen;

    public ForspObj nil;
    public ForspObj readStack;

    public ForspObj internedAtoms;
    public ForspObj atomTrue;
    public ForspObj atomQuote;
    public ForspObj atomPush;
    public ForspObj atomPop;

    public ForspObj stack;
    public ForspObj env;

    public State() {
        this.readStack = null;
        this.inputStr = "";
        this.inputPos = 0;
        this.inputLen = 0;
    }

    public void resetInput(String input) {
        this.inputStr = input;
        this.inputLen = input.length();
        this.inputPos = 0;
    }

    public char peek() {
        if (inputPos >= inputLen) return 0;
        return inputStr.charAt(inputPos);
    }

    public void advance() {
        char c = peek();
        if (c == 0) {
            throw new RuntimeException("Cannot advance beyond end of input");
        }
        inputPos++;
    }
}
