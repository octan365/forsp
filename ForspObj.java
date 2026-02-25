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
 * Base class for all forsp objects. Uses a tagged union pattern with
 * different types: NIL, ATOM, NUM, PAIR, CLOSURE, PRIMITIVE.
 */
public class ForspObj {
    public enum Tag {
        NIL, ATOM, NUM, PAIR, CLOS, PRIM
    }

    private final Tag tag;
    private String atom;
    private long num;
    private Pair pair;
    private Closure clos;
    private Primitive prim;

    private ForspObj(Tag tag) {
        this.tag = tag;
    }

    // Factory methods
    public static ForspObj makeNil() {
        return new ForspObj(Tag.NIL);
    }

    public static ForspObj makeAtom(String str) {
        ForspObj obj = new ForspObj(Tag.ATOM);
        obj.atom = str;
        return obj;
    }

    public static ForspObj makeNum(long num) {
        ForspObj obj = new ForspObj(Tag.NUM);
        obj.num = num;
        return obj;
    }

    public static ForspObj makePair(ForspObj car, ForspObj cdr) {
        ForspObj obj = new ForspObj(Tag.PAIR);
        obj.pair = new Pair(car, cdr);
        return obj;
    }

    public static ForspObj makeClos(ForspObj body, ForspObj env) {
        ForspObj obj = new ForspObj(Tag.CLOS);
        obj.clos = new Closure(body, env);
        return obj;
    }

    public static ForspObj makePrim(PrimitiveFunction func) {
        ForspObj obj = new ForspObj(Tag.PRIM);
        obj.prim = new Primitive(func);
        return obj;
    }

    // Type predicates
    public boolean isNil() { return tag == Tag.NIL; }
    public boolean isAtom() { return tag == Tag.ATOM; }
    public boolean isNum() { return tag == Tag.NUM; }
    public boolean isPair() { return tag == Tag.PAIR; }
    public boolean isClos() { return tag == Tag.CLOS; }
    public boolean isPrim() { return tag == Tag.PRIM; }

    // Accessors
    public Tag getTag() { return tag; }

    public String getAtom() {
        if (tag != Tag.ATOM) throw new RuntimeException("Expected ATOM");
        return atom;
    }

    public long getNum() {
        if (tag != Tag.NUM) throw new RuntimeException("Expected NUM");
        return num;
    }

    public Pair getPair() {
        if (tag != Tag.PAIR) throw new RuntimeException("Expected PAIR");
        return pair;
    }

    public Closure getClos() {
        if (tag != Tag.CLOS) throw new RuntimeException("Expected CLOS");
        return clos;
    }

    public Primitive getPrim() {
        if (tag != Tag.PRIM) throw new RuntimeException("Expected PRIM");
        return prim;
    }

    // Helper methods for working with pairs
    public ForspObj car() {
        if (tag != Tag.PAIR) throw new RuntimeException("Expected pair to apply car() function");
        return pair.car;
    }

    public ForspObj cdr() {
        if (tag != Tag.PAIR) throw new RuntimeException("Expected pair to apply cdr() function");
        return pair.cdr;
    }

    // Helper methods for numbers
    public long toLong() {
        return isNum() ? num : 0;
    }

    // Equality
    public static boolean equal(ForspObj a, ForspObj b) {
        return a == b || (a.isNum() && b.isNum() && a.num == b.num);
    }

    @Override
    public String toString() {
        return "ForspObj{" + tag + "}";
    }

    // Inner classes for complex types
    public static class Pair {
        public final ForspObj car;
        public final ForspObj cdr;

        public Pair(ForspObj car, ForspObj cdr) {
            this.car = car;
            this.cdr = cdr;
        }
    }

    public static class Closure {
        public final ForspObj body;
        public final ForspObj env;

        public Closure(ForspObj body, ForspObj env) {
            this.body = body;
            this.env = env;
        }
    }

    public static class Primitive {
        private final PrimitiveFunction func;

        public Primitive(PrimitiveFunction func) {
            this.func = func;
        }

        public void apply(Evaluator evaluator) {
            func.apply(evaluator);
        }
    }

    @FunctionalInterface
    public interface PrimitiveFunction {
        void apply(Evaluator evaluator);
    }
}
