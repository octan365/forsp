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
 * Environment for variable lookup and binding.
 * Environment is a simple list of key-value (dotted) pairs.
 */
public class Environment {
    private final State state;

    public Environment(State state) {
        this.state = state;
    }

    public ForspObj find(ForspObj env, ForspObj key) {
        if (!key.isAtom()) {
            throw new RuntimeException("Expected 'key' to be an atom in env_find()");
        }

        ForspObj current = env;
        while (current != state.nil) {
            ForspObj kv = current.car();
            if (key == kv.car()) {
                return kv.cdr();
            }
            current = current.cdr();
        }

        throw new RuntimeException("Failed to find key='" + key.getAtom() + "' in environment");
    }

    public ForspObj define(ForspObj env, ForspObj key, ForspObj val) {
        return ForspObj.makePair(ForspObj.makePair(key, val), env);
    }

    public ForspObj definePrim(ForspObj env, String name, ForspObj.PrimitiveFunction func) {
        // This method is deprecated - use definePrim that takes a Reader
        return define(env, ForspObj.makeAtom(name), ForspObj.makePrim(func));
    }
}
