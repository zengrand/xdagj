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
package io.xdag.config;

import com.google.common.primitives.UnsignedLong;
import io.xdag.utils.BasicUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class ConstantsTest {
    @Test
    public void testMainStartAmount() {
        UnsignedLong startAmount = UnsignedLong.fromLongBits(1L << 42);
        UnsignedLong apolloStartAmount = UnsignedLong.fromLongBits(1L << 39);
        assertEquals(Constants.MAIN_START_AMOUNT, startAmount.longValue());
        assertEquals(Constants.MAIN_APOLLO_AMOUNT, apolloStartAmount.longValue());
        assertEquals(Constants.MAIN_START_AMOUNT, startAmount.longValue());
        assertEquals(String.valueOf(BasicUtils.amount2xdag(Constants.MAIN_START_AMOUNT)), "1024.0");
        assertEquals(String.valueOf(BasicUtils.amount2xdag(Constants.MAIN_APOLLO_AMOUNT)), "128.0");
    }

}
