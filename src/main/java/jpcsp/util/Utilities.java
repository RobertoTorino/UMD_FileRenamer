/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.util;

import jpcsp.filesystems.SeekableDataInput;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utilities {
    public static final Charset charset = StandardCharsets.UTF_8;

    public static String formatString(String type, String oldstring) {
        int counter = 0;
        if (type.equals("byte")) {
            counter = 2;
        }
        if (type.equals("short")) {
            counter = 4;
        }
        if (type.equals("long")) {
            counter = 8;
        }
        int len = oldstring.length();
        StringBuilder sb = new StringBuilder();
        while (len++ < counter) {
            sb.append('0');
        }
        oldstring = sb.append(oldstring).toString();
        return oldstring;

    }

    public static String integerToBin(int value) {
        return Long.toBinaryString(0x0000000100000000L | ((value) & 0x00000000FFFFFFFFL)).substring(1);
    }

    public static String integerToHex(int value) {
        return Integer.toHexString(0x100 | value).substring(1).toUpperCase();
    }

    public static String integerToHexShort(int value) {
        return Integer.toHexString(0x10000 | value).substring(1).toUpperCase();
    }

    public static long readUWord(SeekableDataInput f) throws IOException {
        long l = (f.readUnsignedByte() | (f.readUnsignedByte() << 8) | (f.readUnsignedByte() << 16) | ((long) f.readUnsignedByte() << 24));
        return (l & 0xFFFFFFFFL);
    }

    public static int readUByte(SeekableDataInput f) throws IOException {
        return f.readUnsignedByte();
    }

    public static int readUHalf(SeekableDataInput f) throws IOException {
        return f.readUnsignedByte() | (f.readUnsignedByte() << 8);
    }

    public static int readWord(SeekableDataInput f) throws IOException {
        //readByte() isn't more correct? (already exists one readUWord() method to unsigned values)
        return (f.readUnsignedByte() | (f.readUnsignedByte() << 8) | (f.readUnsignedByte() << 16) | (f.readUnsignedByte() << 24));
    }

    public static void skipUnknown(ByteBuffer buf, int length) throws IOException {
        buf.position(buf.position() + length);
    }

    public static String readStringZ(ByteBuffer buf) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte b;
        while (buf.position() < buf.limit()) {
            b = (byte) readUByte(buf);
            if (b == 0)
                break;
            sb.append((char) b);
        }
        return sb.toString();
    }

    public static String readStringNZ(ByteBuffer buf, int n) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte b;
        for (; n > 0; n--) {
            b = (byte) readUByte(buf);
            if (b != 0)
                sb.append((char) b);
        }
        return sb.toString();
    }

    /**
     * Read a string from memory.
     * The string ends when the maximal length is reached or a '\0' byte is found.
     * The memory bytes are interpreted as UTF-8 bytes to form the string.
     */

    ByteBuffer mem = ByteBuffer.allocate(1024);
    int address = 0;
    int n = 10;

    public static void writeStringZ(ByteBuffer buf, String s) {
        buf.put(s.getBytes());
        buf.put((byte) 0);
    }

    public static short getUnsignedByte(ByteBuffer bb) throws IOException {
        return ((short) (bb.get() & 0xff));
    }

    public static void putUnsignedByte(ByteBuffer bb, int value) {
        bb.put((byte) (value & 0xFF));
    }

    public static short readUByte(ByteBuffer buf) throws IOException {
        return getUnsignedByte(buf);
    }

    public static int readUHalf(ByteBuffer buf) throws IOException {
        return getUnsignedByte(buf) | (getUnsignedByte(buf) << 8);
    }

    public static long readUWord(ByteBuffer buf) throws IOException {
        long l = (getUnsignedByte(buf) | (getUnsignedByte(buf) << 8) | (getUnsignedByte(buf) << 16) | ((long) getUnsignedByte(buf) << 24));
        return (l & 0xFFFFFFFFL);
    }

    public static int readWord(ByteBuffer buf) throws IOException {
        return (getUnsignedByte(buf) | (getUnsignedByte(buf) << 8) | (getUnsignedByte(buf) << 16) | (getUnsignedByte(buf) << 24));
    }

    public static void writeWord(ByteBuffer buf, long value) {
        putUnsignedByte(buf, (int) (value));
        putUnsignedByte(buf, (int) (value >> 8));
        putUnsignedByte(buf, (int) (value >> 16));
        putUnsignedByte(buf, (int) (value >> 24));
    }

    public static void writeHalf(ByteBuffer buf, int value) {
        putUnsignedByte(buf, value);
        putUnsignedByte(buf, value >> 8);
    }

    public static void writeByte(ByteBuffer buf, int value) {
        putUnsignedByte(buf, value);
    }

    public static int parseAddress(String value) throws NumberFormatException {
        int address = 0;
        if (value == null) {
            return address;
        }

        value = value.trim();

        if (value.startsWith("0x")) {
            value = value.substring(2);
        }

        if (Integer.SIZE == 32 && value.length() == 8 && value.charAt(0) >= '8') {
            address = (int) Long.parseLong(value, 16);
        } else {
            address = Integer.parseInt(value, 16);
        }

        return address;
    }


    /**
     * Parse the string as a number and returns its value.
     * If the string starts with "0x", the number is parsed
     * in base 16, otherwise base 10.
     *
     * @param s the string to be parsed
     * @return the numeric value represented by the string.
     */
    public static long parseLong(String s) {
        long value = 0;

        if (s == null) {
            return value;
        }

        if (s.startsWith("0x")) {
            value = Long.parseLong(s.substring(2), 16);
        } else {
            value = Long.parseLong(s);
        }
        return value;
    }

    /**
     * Parse the string as a number and returns its value.
     * The number is always parsed in base 16.
     * The string can start as an option with "0x".
     *
     * @param s the string to be parsed in base 16
     * @return the numeric value represented by the string.
     */
    public static long parseHexLong(String s) {
        long value = 0;

        if (s == null) {
            return value;
        }

        if (s.startsWith("0x")) {
            s = s.substring(2);
        }
        value = Long.parseLong(s, 16);
        return value;
    }

    public static int makePow2(int n) {
        --n;
        n = (n >> 1) | n;
        n = (n >> 2) | n;
        n = (n >> 4) | n;
        n = (n >> 8) | n;
        n = (n >> 16) | n;
        return ++n;
    }

    public static void bytePositionBuffer(Buffer buffer, int bytePosition) {
        buffer.position(bytePosition / bufferElementSize(buffer));
    }

    public static int bufferElementSize(Buffer buffer) {
        if (buffer instanceof IntBuffer) {
            return 4;
        }

        return 1;
    }

    public static String stripNL(String s) {
        if (s != null && s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }

        return s;
    }

    /**
     * Reads input-stream i into a String with the UTF-8 charset
     * until the input-stream is finished (don't use with infinite streams).
     *
     * @param inputStream to read into a string
     * @param close       if true, close the input-stream
     * @return a string
     * @throws java.io.IOException            if thrown on reading the stream
     * @throws java.lang.NullPointerException if the given input-stream is null
     */
    public static String toString(InputStream inputStream, boolean close) throws IOException {
        if (inputStream == null) {
            throw new NullPointerException("null inputStream");
        }
        String string;
        StringBuilder outputBuilder = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while (null != (string = reader.readLine())) {
                outputBuilder.append(string).append('\n');
            }
        } finally {
            if (close) {
                close(inputStream);
            }
        }
        return outputBuilder.toString();
    }

    /**
     * Close closeable. Use this in a final clause.
     */
    public static void close(Closeable... closeables) {
        for (Closeable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ex) {
                    Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "Couldn't close Closeable", ex);
                }
            }
        }
    }

    public static long makeValue64(int low32, int high32) {
        return (((long) high32) << 32) | ((low32) & 0xFFFFFFFFL);
    }
}
