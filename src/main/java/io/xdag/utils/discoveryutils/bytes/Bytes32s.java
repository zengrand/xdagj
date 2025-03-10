/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2030 The XdagJ Developers
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
package io.xdag.utils.discoveryutils.bytes;

public abstract class Bytes32s {

    private Bytes32s() {}

    public static void and(final Bytes32 v1, final Bytes32 v2, final MutableBytes32 result) {
        for (int i = 0; i < Bytes32.SIZE; i++) {
            result.set(i, (byte) (v1.get(i) & v2.get(i)));
        }
    }

    public static Bytes32 and(final Bytes32 v1, final Bytes32 v2) {
        final MutableBytes32 mb32 = MutableBytes32.create();
        and(v1, v2, mb32);
        return mb32;
    }

    public static void or(final Bytes32 v1, final Bytes32 v2, final MutableBytes32 result) {
        for (int i = 0; i < Bytes32.SIZE; i++) {
            result.set(i, (byte) (v1.get(i) | v2.get(i)));
        }
    }

    public static Bytes32 or(final Bytes32 v1, final Bytes32 v2) {
        final MutableBytes32 mb32 = MutableBytes32.create();
        or(v1, v2, mb32);
        return mb32;
    }

    public static void xor(final Bytes32 v1, final Bytes32 v2, final MutableBytes32 result) {
        for (int i = 0; i < Bytes32.SIZE; i++) {
            result.set(i, (byte) (v1.get(i) ^ v2.get(i)));
        }
    }

    public static Bytes32 xor(final Bytes32 v1, final Bytes32 v2) {
        final MutableBytes32 mb32 = MutableBytes32.create();
        xor(v1, v2, mb32);
        return mb32;
    }

    public static void not(final Bytes32 v, final MutableBytes32 result) {
        for (int i = 0; i < Bytes32.SIZE; i++) {
            result.set(i, (byte) (~v.get(i)));
        }
    }

    public static Bytes32 not(final Bytes32 v) {
        final MutableBytes32 mb32 = MutableBytes32.create();
        not(v, mb32);
        return mb32;
    }

    public static String unprefixedHexString(final Bytes32 v) {
        return v.toString().substring(2);
    }

    public static Bytes32 shiftRight(final Bytes32 v, final int shiftBitCount) {
        // Code taken from the Apache 2 licensed library
        // https://github.com/patrickfav/bytes-java/blob/master/src/main/java/at/favre/lib/bytes/Util.java
        final byte[] byteArray = v.extractArray();
        final int shiftMod = shiftBitCount % 8;
        final byte carryMask = (byte) (0xFF << (8 - shiftMod));
        final int offsetBytes = (shiftBitCount / 8);

        int sourceIndex;
        for (int i = byteArray.length - 1; i >= 0; i--) {
            sourceIndex = i - offsetBytes;
            if (sourceIndex < 0) {
                byteArray[i] = 0;
            } else {
                final byte src = byteArray[sourceIndex];
                byte dst = (byte) ((0xff & src) >>> shiftMod);
                if (sourceIndex - 1 >= 0) {
                    dst = (byte) (dst | ((byteArray[sourceIndex - 1] << (8 - shiftMod)) & carryMask));
                }
                byteArray[i] = dst;
            }
        }
        return Bytes32.wrap(byteArray);
    }

    public static Bytes32 shiftLeft(final Bytes32 v, final int shiftBitCount) {
        // Code taken from the Apache 2 licensed library
        // https://github.com/patrickfav/bytes-java/blob/master/src/main/java/at/favre/lib/bytes/Util.java
        final byte[] byteArray = v.extractArray();
        final int shiftMod = shiftBitCount % 8;
        final byte carryMask = (byte) ((1 << shiftMod) - 1);
        final int offsetBytes = (shiftBitCount / 8);

        int sourceIndex;
        for (int i = 0; i < byteArray.length; i++) {
            sourceIndex = i + offsetBytes;
            if (sourceIndex >= byteArray.length) {
                byteArray[i] = 0;
            } else {
                final byte src = byteArray[sourceIndex];
                byte dst = (byte) (src << shiftMod);
                if (sourceIndex + 1 < byteArray.length) {
                    dst = (byte) (dst | ((byteArray[sourceIndex + 1] >>> (8 - shiftMod)) & carryMask));
                }
                byteArray[i] = dst;
            }
        }
        return Bytes32.wrap(byteArray);
    }
}
