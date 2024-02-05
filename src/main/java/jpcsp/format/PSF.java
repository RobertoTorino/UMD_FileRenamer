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
package jpcsp.format;

import jpcsp.util.Utilities;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import static jpcsp.util.Utilities.*;

/**
 * The type Psf.
 *
 * @author wolf003
 * @version $Id: $Id
 */
public class PSF {
    /**
     * The constant PSF_IDENT.
     */
    public final static int PSF_IDENT = 0x46535000;
    /**
     * The constant PSF_DATA_TYPE_BINARY.
     */
    public final static int PSF_DATA_TYPE_BINARY = 0;
    /**
     * The constant PSF_DATA_TYPE_STRING.
     */
    public final static int PSF_DATA_TYPE_STRING = 2;
    /**
     * The constant PSF_DATA_TYPE_INT32.
     */
    public final static int PSF_DATA_TYPE_INT32 = 4;
    private final LinkedList<PSFKeyValuePair> pairList;
    private int psfOffset;
    private int size;
    private boolean sizeDirty;
    private boolean tablesDirty;
    private int ident;
    private int version; // yapspd: 0x1100. actual: 0x0101.
    private int keyTableOffset;
    private int valueTableOffset;
    private int indexEntryCount;

    /**
     * Instantiates a new Psf.
     *
     * @param psfOffset the psf offset
     */
    public PSF(long psfOffset) {
        this.psfOffset = (int) psfOffset;
        size = 0;

        sizeDirty = true;
        tablesDirty = true;

        ident = PSF_IDENT;
        version = 0x0101;

        pairList = new LinkedList<>();

    }

    /**
     * Instantiates a new Psf.
     */
    public PSF() {
        this(0);
    }

    /**
     * f.position() is undefined after calling this
     *
     * @param f the f
     * @throws java.io.IOException the io exception
     */
    public void read(ByteBuffer f) throws IOException {
        psfOffset = f.position();

        ident = (int) readUWord(f);
        if (ident != PSF_IDENT) {
            System.out.println("Not a valid PSF file (ident=" + String.format("%08X", ident) + ")");
            return;
        }

        // header
        version = (int) readUWord(f); // 0x0101
        keyTableOffset = (int) readUWord(f);
        valueTableOffset = (int) readUWord(f);
        indexEntryCount = (int) readUWord(f);

        // index table
        for (int i = 0; i < indexEntryCount; i++) {
            PSFKeyValuePair pair = new PSFKeyValuePair();
            pair.read(f);
            pairList.add(pair);
        }

        // key/pairs
        for (PSFKeyValuePair pair : pairList) {
            f.position(psfOffset + keyTableOffset + pair.keyOffset);
            pair.key = readStringZ(f);

            f.position(psfOffset + valueTableOffset + pair.valueOffset);
            switch (pair.dataType) {
                case PSF_DATA_TYPE_BINARY:
                    byte[] data = new byte[pair.dataSize];
                    f.get(data);
                    pair.data = data;

                    //System.out.println(String.format("offset=%08X key='%s' binary packed [len=%d]",
                    //    keyTableOffset + pair.keyOffset, pair.key, pair.dataSize));
                    break;

                case PSF_DATA_TYPE_STRING:
                    // String may not be in english!
                    byte[] s = new byte[pair.dataSize];
                    f.get(s);
                    // Strip trailing null character
                    pair.data = new String(s, 0, s[s.length - 1] == '\0' ? s.length - 1 : s.length, Utilities.charset);

                    //System.out.println(String.format("offset=%08X key='%s' string '%s' [len=%d]",
                    //    keyTableOffset + pair.keyOffset, pair.key, pair.data, pair.dataSize));
                    break;

                case PSF_DATA_TYPE_INT32:
                    pair.data = (int) readUWord(f);

                    //System.out.println(String.format("offset=%08X key='%s' int32 %08X %d [len=%d]",
                    //    keyTableOffset + pair.keyOffset, pair.key, pair.data, pair.data, pair.dataSize));
                    break;

                default:
                    System.out.println(String.format("offset=%08X key='%s' unhandled data type %d [len=%d]",
                            keyTableOffset + pair.keyOffset, pair.key, pair.dataType, pair.dataSize));
                    break;
            }
        }

        sizeDirty = true;
        tablesDirty = false;
        calculateSize();
    }

    /**
     * Write.
     *
     * @param f the f
     */
// assumes we want to write at the start of the buffer, and that the current buffer position is 0
    // doesn't handle psfOffset
    public void write(ByteBuffer f) {
        if (indexEntryCount != pairList.size())
            throw new RuntimeException("incremental size and actual size do not match! " + indexEntryCount + "/" + pairList.size());

        if (tablesDirty) {
            calculateTables();
        }

        // header
        writeWord(f, ident);
        writeWord(f, version);
        writeWord(f, keyTableOffset);
        writeWord(f, valueTableOffset);
        writeWord(f, indexEntryCount);

        // index table
        for (PSFKeyValuePair pair : pairList) {
            pair.write(f);
        }

        // key/value pairs

        for (PSFKeyValuePair pair : pairList) {
            f.position(keyTableOffset + pair.keyOffset);
            //System.err.println("PSF write key   fp=" + f.position() + " datalen=" + (pair.key.length() + 1) + " top=" + (f.position() + pair.key.length() + 1));
            writeStringZ(f, pair.key);

            f.position(valueTableOffset + pair.valueOffset);
            //System.err.println("PSF write value fp=" + f.position() + " datalen=" + (pair.dataSizePadded) + " top=" + (f.position() + pair.dataSizePadded));
            switch (pair.dataType) {
                case PSF_DATA_TYPE_BINARY:
                    f.put((byte[]) pair.data);
                    break;

                case PSF_DATA_TYPE_STRING:
                    String s = (String) pair.data;
                    f.put(s.getBytes(Utilities.charset));
                    writeByte(f, (byte) 0);
                    break;

                case PSF_DATA_TYPE_INT32:
                    writeWord(f, (Integer) pair.data);
                    break;

                default:
                    System.out.println("not writing unhandled data type " + pair.dataType);
                    break;
            }
        }
    }

    /**
     * Get object.
     *
     * @param key the key
     * @return the object
     */
    public Object get(String key) {
        for (PSFKeyValuePair pair : pairList) {
            if (pair.key.equals(key))
                return pair.data;
        }
        return null;
    }

    /**
     * Gets string.
     *
     * @param key the key
     * @return the string
     */
    public String getString(String key) {
        Object obj = get(key);
        if (obj != null)
            return (String) obj;
        return null;
    }

    /**
     * kxploit patcher tool adds "\nKXPloit Boot by PSP-DEV Team"
     *
     * @param key the key
     * @return the printable string
     */
    public String getPrintableString(String key) {
        String rawString = getString(key);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawString.length(); i++) {
            char c = rawString.charAt(i);
            if (c == '\0' || c == '\n')
                break;
            sb.append(rawString.charAt(i));
        }

        return sb.toString();
    }

    /**
     * Gets numeric.
     *
     * @param key the key
     * @return the numeric
     */
    public int getNumeric(String key) {
        Object obj = get(key);
        if (obj != null)
            return (Integer) obj;
        return 0;
    }

    /**
     * Put.
     *
     * @param key  the key
     * @param data the data
     */
    public void put(String key, byte[] data) {
        PSFKeyValuePair pair = new PSFKeyValuePair(key, PSF_DATA_TYPE_BINARY, data.length, data);
        pairList.add(pair);

        sizeDirty = true;
        tablesDirty = true;
        indexEntryCount++;
    }

    /**
     * Put.
     *
     * @param key    the key
     * @param data   the data
     * @param rawlen the rawlen
     */
    public void put(String key, String data, int rawlen) {
        byte[] b = (data.getBytes(Utilities.charset));

        //if (b.length != data.length())
        //    System.out.println("put string '" + data + "' size mismatch. UTF-8=" + b.length + " regular=" + (data.length() + 1));

        //PSFKeyValuePair pair = new PSFKeyValuePair(key, PSF_DATA_TYPE_STRING, data.length() + 1, rawlen, data);
        PSFKeyValuePair pair = new PSFKeyValuePair(key, PSF_DATA_TYPE_STRING, b.length + 1, rawlen, data);
        pairList.add(pair);

        sizeDirty = true;
        tablesDirty = true;
        indexEntryCount++;
    }

    /**
     * Put.
     *
     * @param key  the key
     * @param data the data
     */
    public void put(String key, String data) {
        byte[] b = (data.getBytes(Utilities.charset));
        //int rawlen = data.length() + 1;
        int rawlen = b.length + 1;

        put(key, data, (rawlen + 3) & ~3);
    }

    /**
     * Put.
     *
     * @param key  the key
     * @param data the data
     */
    public void put(String key, int data) {
        PSFKeyValuePair pair = new PSFKeyValuePair(key, PSF_DATA_TYPE_INT32, 4, data);
        pairList.add(pair);

        sizeDirty = true;
        tablesDirty = true;
        indexEntryCount++;
    }

    private void calculateTables() {
        tablesDirty = false;

        // position the key table after the index table and before the value table
        // 20 byte PSF header
        // 16 byte per index entry
        keyTableOffset = 5 * 4 + indexEntryCount * 0x10;


        // position the value table after the key table
        valueTableOffset = keyTableOffset;

        for (PSFKeyValuePair pair : pairList) {
            // keys are not aligned
            valueTableOffset += pair.key.length() + 1;
        }

        // 32-bit align for data start
        valueTableOffset = (valueTableOffset + 3) & ~3;


        // index table
        int keyRunningOffset = 0;
        int valueRunningOffset = 0;

        for (PSFKeyValuePair pair : pairList) {
            pair.keyOffset = keyRunningOffset;
            keyRunningOffset += pair.key.length() + 1;

            pair.valueOffset = valueRunningOffset;
            valueRunningOffset += pair.dataSizePadded;
        }
    }

    private void calculateSize() {
        sizeDirty = false;
        size = 0;

        if (tablesDirty) {
            calculateTables();
        }

        for (PSFKeyValuePair pair : pairList) {
            int keyHighBound = keyTableOffset + pair.keyOffset + pair.key.length() + 1;
            int valueHighBound = valueTableOffset + pair.valueOffset + pair.dataSizePadded;
            if (keyHighBound > size)
                size = keyHighBound;
            if (valueHighBound > size)
                size = valueHighBound;
        }
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        if (sizeDirty) {
            calculateSize();
        }

        return size;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (PSFKeyValuePair pair : pairList) {
            sb.append(pair.toString()).append("\n");
        }

        sb.append("probably homebrew? ").append(isLikelyHomebrew());

        return sb.toString();
    }

    /**
     * used by isLikelyHomebrew()
     */
    private boolean safeEquals(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    /**
     * Is likely homebrew boolean.
     *
     * @return the boolean
     */
    public boolean isLikelyHomebrew() {
        boolean homebrew = false;

        String disc_version = getString("DISC_VERSION");
        String disc_id = getString("DISC_ID");
        String category = getString("CATEGORY");
        Integer bootable = (Integer) get("BOOTABLE"); // don't use getNumeric, we also want to know if the entry exists or not
        Integer region = (Integer) get("REGION");
        String psp_system_ver = getString("PSP_SYSTEM_VER");
        Integer parental_level = (Integer) get("PARENTAL_LEVEL");

        Integer ref_one = Integer.valueOf(1);
        Integer ref_region = Integer.valueOf(32768);

        if (safeEquals(disc_version, "1.00") &&
                safeEquals(disc_id, "UCJS10041") && // loco roco demo, should not false positive since that demo has sys ver 3.40
                safeEquals(category, "MG") &&
                safeEquals(bootable, ref_one) &&
                safeEquals(region, ref_region) &&
                safeEquals(psp_system_ver, "1.00") &&
                safeEquals(parental_level, ref_one)) {

            if (indexEntryCount == 8) {
                homebrew = true;
            } else if (indexEntryCount == 9 &&
                    safeEquals(get("MEMSIZE"), ref_one)) {
                // lua player hm 8
                homebrew = true;
            }
        } else if (indexEntryCount == 4 &&
                safeEquals(category, "MG") &&
                safeEquals(bootable, ref_one) &&
                safeEquals(region, ref_region)) {
            homebrew = true;
        }

        return homebrew;
    }

    /**
     * The type Psf key value pair.
     */
    public static class PSFKeyValuePair {
        /**
         * The Key offset.
         */
// index table info
        public int keyOffset;
        /**
         * The Unknown 1.
         */
        public int unknown1;
        /**
         * The Data type.
         */
        public int dataType;
        /**
         * The Data size.
         */
        public int dataSize;
        /**
         * The Data size padded.
         */
        public int dataSizePadded;
        /**
         * The Value offset.
         */
        public int valueOffset;

        /**
         * The Key.
         */
// key table info
        public String key;

        /**
         * The Data.
         */
// data table info
        public Object data;

        /**
         * Instantiates a new Psf key value pair.
         */
        public PSFKeyValuePair() {
            this(null, 0, 0, null);
        }

        /**
         * Instantiates a new Psf key value pair.
         *
         * @param key      the key
         * @param dataType the data type
         * @param dataSize the data size
         * @param data     the data
         */
        public PSFKeyValuePair(String key, int dataType, int dataSize, Object data) {
            this(key, dataType, dataSize, (dataSize + 3) & ~3, data);
        }

        /**
         * Instantiates a new Psf key value pair.
         *
         * @param key            the key
         * @param dataType       the data type
         * @param dataSize       the data size
         * @param dataSizePadded the data size padded
         * @param data           the data
         */
        public PSFKeyValuePair(String key, int dataType, int dataSize, int dataSizePadded, Object data) {
            this.key = key;
            this.dataType = dataType;
            this.dataSize = dataSize;
            this.dataSizePadded = dataSizePadded;
            this.data = data;

            // yapspd: 4
            // probably alignment of the value data
            unknown1 = 4;
        }

        /**
         * only reads the index entry, since this class has doesn't know about the psf/key/value offsets
         *
         * @param f the f
         * @throws IOException the io exception
         */
        public void read(ByteBuffer f) throws IOException {
            // index table entry
            keyOffset = readUHalf(f);
            unknown1 = readUByte(f);
            dataType = readUByte(f);
            dataSize = (int) readUWord(f);
            dataSizePadded = (int) readUWord(f);
            valueOffset = (int) readUWord(f);
        }

        /**
         * only writes the index entry, since this class has doesn't know about the psf/key/value offsets
         *
         * @param f the f
         */
        public void write(ByteBuffer f) {
            // index table entry
            writeHalf(f, keyOffset);
            writeByte(f, unknown1);
            writeByte(f, dataType);
            writeWord(f, dataSize);
            writeWord(f, dataSizePadded);
            writeWord(f, valueOffset);
        }

        @Override
        public String toString() {

            return key + " = " + data;
        }
    }
}
