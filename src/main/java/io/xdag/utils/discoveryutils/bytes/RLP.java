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

import io.vertx.core.buffer.Buffer;
import io.xdag.utils.discoveryutils.RLPInput;
import io.xdag.utils.discoveryutils.bytes.uint.BytesValueRLPInput;
import io.xdag.utils.discoveryutils.bytes.uint.RLPException;

import java.util.function.Consumer;

import static java.lang.String.format;
import static io.xdag.utils.discoveryutils.bytes.RLPDecodingHelpers.extractSize;
import static io.xdag.utils.discoveryutils.bytes.RLPEncodingHelpers.*;

public abstract class RLP {
    private RLP() {}

    /** The RLP encoding of a single empty value, also known as RLP null. */
    public static final BytesValue NULL = encodeOne(BytesValue.EMPTY);

    public static final BytesValue EMPTY_LIST;

    static {
        final BytesValueRLPOutput out = new BytesValueRLPOutput();
        out.startList();
        out.endList();
        EMPTY_LIST = out.encoded();
    }

    /**
     * Creates a new {@link RLPInput} suitable for decoding the provided RLP encoded value.
     *
     * <p>The created input is strict, in that exceptions will be thrown for any malformed input,
     * either by this method or by future reads from the returned input.
     *
     * @param encoded The RLP encoded data for which to create a {@link RLPInput}.
     * @return A newly created {@link RLPInput} to decode {@code encoded}.
     * @throws MalformedRLPInputException if {@code encoded} doesn't contain a single RLP encoded item
     *     (item that can be a list itself). Note that more deeply nested corruption/malformation of
     *     the input will not be detected by this method call, but will be later when the input is
     *     read.
     */
    public static RLPInput input(final BytesValue encoded) {
        return new BytesValueRLPInput(encoded, false);
    }

    /**
     * Creates a new {@link RLPInput} suitable for decoding an RLP value encoded in the provided
     * Vert.x {@link Buffer}.
     *
     * <p>The created input is strict, in that exceptions will be thrown for any malformed input,
     * either by this method or by future reads from the returned input.
     *
     * @param buffer A buffer containing the RLP encoded data to decode.
     * @param offset The offset in {@code encoded} at which the data to decode starts.
     * @return A newly created {@link RLPInput} to decode RLP data in {@code encoded} from {@code
     *     offset}.
     * @throws MalformedRLPInputException if {@code encoded} doesn't contain a properly encoded RLP
     *     item. Note that this only detect malformation on the main item at {@code offset}, but more
     *     deeply nested corruption/malformation of the input will not be detected by this method
     *     call, but only later when the input is read.
     */
    public static BytesValueRLPInput input(final Buffer buffer, final int offset) {
        return new BytesValueRLPInput(BytesValue.wrapBuffer(buffer, offset), false, false);
    }

    /**
     * Creates a {@link RLPOutput}, pass it to the provided consumer for writing, and then return the
     * RLP encoded result of that writing.
     *
     * <p>This method is a convenience method that is mostly meant for use with class that have a
     * method to write to an {@link RLPOutput}. For instance:
     *
     * <pre>{@code
     * class Foo {
     *   public void writeTo(RLPOutput out) {
     *     //... write some data to out ...
     *   }
     * }
     *
     * Foo f = ...;
     * // RLP encode f
     * BytesValue encoded = RLPs.encode(f::writeTo);
     * }</pre>
     *
     * @param writer A method that given an {@link RLPOutput}, writes some data to it.
     * @return The RLP encoding of the data written by {@code writer}.
     */
    public static BytesValue encode(final Consumer<RLPOutput> writer) {
        final BytesValueRLPOutput out = new BytesValueRLPOutput();
        writer.accept(out);
        return out.encoded();
    }

    /**
     * Encodes a single binary value into RLP.
     *
     * <p>This is equivalent (but possibly more efficient) to:
     *
     * <pre>
     * {
     *   &#64;code
     *   BytesValueRLPOutput out = new BytesValueRLPOutput();
     *   out.writeBytesValue(value);
     *   return out.encoded();
     * }
     * </pre>
     *
     * So note in particular that the value is encoded as is (and so not as a scalar in particular).
     *
     * @param value The value to encode.
     * @return The RLP encoding containing only {@code value}.
     */
    public static BytesValue encodeOne(final BytesValue value) {
        if (isSingleRLPByte(value)) return value;

        final MutableBytesValue res = MutableBytesValue.create(elementSize(value));
        writeElement(value, res, 0);
        return res;
    }

    /**
     * Decodes an RLP-encoded value assuming it contains a single non-list item.
     *
     * <p>This is equivalent (but possibly more efficient) to:
     *
     * <pre>{@code
     * return input(value).readBytesValue();
     * }</pre>
     *
     * So note in particular that the value is decoded as is (and so not as a scalar in particular).
     *
     * @param encodedValue The encoded RLP value.
     * @return The single value encoded in {@code encodedValue}.
     * @throws RLPException if {@code encodedValue} is not a valid RLP encoding or if it does not
     *     contains a single non-list item.
     */
    public static BytesValue decodeOne(final BytesValue encodedValue) {
        if (encodedValue.size() == 0) {
            throw new RLPException("Invalid empty input for RLP decoding");
        }

        final int prefix = encodedValue.get(0) & 0xFF;
        final RLPDecodingHelpers.Kind kind = RLPDecodingHelpers.Kind.of(prefix);
        if (kind.isList()) {
            throw new RLPException(format("Invalid input: value %s is an RLP list", encodedValue));
        }

        if (kind == RLPDecodingHelpers.Kind.BYTE_ELEMENT) {
            return encodedValue;
        }

        final int offset;
        final int size;
        if (kind == RLPDecodingHelpers.Kind.SHORT_ELEMENT) {
            offset = 1;
            size = prefix - 0x80;
        } else {
            final int sizeLength = prefix - 0xb7;
            if (1 + sizeLength > encodedValue.size()) {
                throw new RLPException(
                        format(
                                "Malformed RLP input: not enough bytes to read size of "
                                        + "long item in %s: expected %d bytes but only %d",
                                encodedValue, sizeLength + 1, encodedValue.size()));
            }
            offset = 1 + sizeLength;
            size = extractSize(encodedValue::get, 1, sizeLength);
        }
        if (offset + size != encodedValue.size()) {
            throw new RLPException(
                    format(
                            "Malformed RLP input: %s should be of size %d according to "
                                    + "prefix byte but of size %d",
                            encodedValue, offset + size, encodedValue.size()));
        }
        return encodedValue.slice(offset, size);
    }

    /**
     * Validates that the provided value is a valid RLP encoding.
     *
     * @param encodedValue The value to check.
     * @throws RLPException if {@code encodedValue} is not a valid RLP encoding.
     */
    public static void validate(final BytesValue encodedValue) {
        final RLPInput in = input(encodedValue);
        while (!in.isDone()) {
            if (in.nextIsList()) {
                in.enterList();
            } else if (in.isEndOfCurrentList()) {
                in.leaveList();
            } else {
                // Skip does as much validation as can be done in general, without allocating anything.
                in.skipNext();
            }
        }
    }

    /**
     * Given a {@link BytesValue} containing rlp-encoded data, determines the full length of the
     * encoded value (including the prefix) by inspecting the prefixed metadata.
     *
     * @param value the rlp-encoded byte string
     * @return the length of the encoded data, according to the prefixed metadata
     */
    public static int calculateSize(final BytesValue value) {
        return RLPDecodingHelpers.rlpElementMetadata(value::get, value.size(), 0).getEncodedSize();
    }
}
