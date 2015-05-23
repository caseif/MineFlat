/*
 * Voxem
 * Copyright (c) 2014-2015, Maxim Roncacé <caseif@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.voxem.util;

import net.caseif.voxem.vector.Vector4f;

public class MiscUtil {

    public static Vector4f hexToRGBA(String hex) {
        if (hex.startsWith("#"))
            hex = hex.replace("#", "");
        int r = 0;
        int g = 0;
        int b = 0;
        int a = 0;
        if (hex.length() == 3 || hex.length() == 4) {
            r = Integer.parseInt(hex.substring(0, 1) + hex.substring(0, 1), 16);
            g = Integer.parseInt(hex.substring(1, 2) + hex.substring(1, 2), 16);
            b = Integer.parseInt(hex.substring(2, 3) + hex.substring(2, 3), 16);
            if (hex.length() == 4)
                a = Integer.parseInt(hex.substring(3, 4) + hex.substring(3, 4));
            else
                a = 255;
            return new Vector4f(r / 255f, g / 255f, b / 255f, a / 255f);
        } else if (hex.length() == 6 || hex.length() == 8) {
            r = Integer.parseInt(hex.substring(0, 1) + hex.substring(1, 2), 16);
            g = Integer.parseInt(hex.substring(2, 3) + hex.substring(3, 4), 16);
            b = Integer.parseInt(hex.substring(4, 5) + hex.substring(5, 6), 16);
            if (hex.length() == 8)
                a = Integer.parseInt(hex.substring(6, 7) + hex.substring(7, 8));
            else
                a = 255;
            return new Vector4f(r / 255f, g / 255f, b / 255f, a / 255f);
        } else
            throw new IllegalArgumentException("Invalid hex color!");
    }

}
