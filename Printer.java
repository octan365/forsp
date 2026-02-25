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
 * Converts ForspObj instances to S-expression string format.
 */
public class Printer {
    private final State state;

    public Printer(State state) {
        this.state = state;
    }

    public void printRecurse(StringBuilder sb, ForspObj obj) {
        if (obj == state.nil) {
            sb.append("()");
            return;
        }

        switch (obj.getTag()) {
            case ATOM:
                sb.append(obj.getAtom());
                break;
            case NUM:
                sb.append(obj.getNum());
                break;
            case PAIR:
                sb.append("(");
                printRecurse(sb, obj.car());
                printListTail(sb, obj.cdr());
                break;
            case CLOS:
                sb.append("CLOSURE<");
                printRecurse(sb, obj.getClos().body);
                sb.append(", ").append(obj.getClos().env).append(">");
                break;
            case PRIM:
                sb.append("PRIM<").append(obj.getPrim()).append(">");
                break;
            case NIL:
                sb.append("()");
                break;
        }
    }

    public void printListTail(StringBuilder sb, ForspObj obj) {
        if (obj == state.nil) {
            sb.append(")");
            return;
        }
        if (obj.isPair()) {
            sb.append(" ");
            printRecurse(sb, obj.car());
            printListTail(sb, obj.cdr());
        } else {
            sb.append(" . ");
            printRecurse(sb, obj);
            sb.append(")");
        }
    }

    public String print(ForspObj obj) {
        StringBuilder sb = new StringBuilder();
        printRecurse(sb, obj);
        return sb.toString();
    }

    public void println(ForspObj obj) {
        System.out.println(print(obj));
    }
}
