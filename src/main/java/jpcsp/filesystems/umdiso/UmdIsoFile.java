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
package jpcsp.filesystems.umdiso;

import jpcsp.filesystems.SeekableInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * The type Umd iso file.
 *
 * @author gigaherz : community developer for psp and other consoles.
 */
public class UmdIsoFile extends SeekableInputStream {
    /**
     * The constant sectorLength.
     */
    public static final int sectorLength = 2048;
    private final int startSectorNumber;
    private final long maxOffset;
    /**
     * The Internal reader.
     */
    UmdIsoReader internalReader;
    private int currentSectorNumber;
    private long currentOffset;
    private String name;
    private byte[] currentSector;
    private int sectorOffset;

    /**
     * Instantiates a new Umd iso file.
     *
     * @param reader        the reader
     * @param startSector   the start sector
     * @param lengthInBytes the length in bytes
     * @throws IOException the io exception
     */
    public UmdIsoFile(UmdIsoReader reader, int startSector, long lengthInBytes) throws IOException {
        startSectorNumber = startSector;
        currentSectorNumber = startSectorNumber;
        currentOffset = 0;

        int endSectorNumber = startSectorNumber + (int) ((lengthInBytes + sectorLength - 1) / sectorLength);
        if (endSectorNumber >= reader.numSectors) {
            endSectorNumber = reader.numSectors - 1;
            lengthInBytes = (long) (endSectorNumber - startSector + 1) * sectorLength;
        }

        if (lengthInBytes == 0) {
            currentSector = null;
        } else {
            currentSector = reader.readSector(startSector);
        }
        maxOffset = lengthInBytes;
        sectorOffset = 0;
        internalReader = reader;
    }

    private int Ubyte(byte b) {
        return (b) & 255;
    }

    @Override
    public int read() throws IOException {
        if (currentOffset == maxOffset)
            return -1; //throw new java.io.EOFException();

        checkSectorAvailable();
        currentOffset++;

        int debuggingVariable = Ubyte(currentSector[sectorOffset++]); // make unsigned

        assert (debuggingVariable >= 0);

        return debuggingVariable;
    }

    @Override
    public void reset() throws IOException {
        seek(0);
    }

    @Override
    public long skip(long n) throws IOException {
        long oldOffset = currentOffset;

        if (n < 0)
            return n;

        seek(currentOffset + n);

        return currentOffset - oldOffset;
    }

    @Override
    public long length() {
        return maxOffset;
    }

    @Override
    public void seek(long offset) throws IOException {

        if (offset < 0)
            throw new IOException("Seek offset " + offset + " out of bounds.");

        int oldSectorNumber = currentSectorNumber;
        int newSectorNumber = startSectorNumber + (int) (offset / sectorLength);
        if (oldSectorNumber != newSectorNumber) {
            currentSector = internalReader.readSector(newSectorNumber);
        }
        currentOffset = offset;
        currentSectorNumber = newSectorNumber;
        sectorOffset = (int) (currentOffset % sectorLength);
    }

    @Override
    public byte readByte() throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        return (byte) read();
    }

    @Override
    public short readShort() throws IOException {
        return (short) (readUnsignedByte() | ((readByte()) << 8));
    }

    @Override
    public int readInt() throws IOException {
        return (readUnsignedByte() | ((readUnsignedByte()) << 8) | ((readUnsignedByte()) << 16) | ((readByte()) << 24));
    }

    @Override
    public int readUnsignedByte() throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        return read();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return (readShort()) & 0xFFFF;
    }

    @Override
    public long readLong() throws IOException {
        return ((readInt()) & 0xFFFFFFFFL) | (((long) readInt()) << 32);
    }

    @Override
    public float readFloat() throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        return Double.longBitsToDouble(readLong());
    }

    @Override
    public boolean readBoolean() throws IOException {
        return (readUnsignedByte() != 0);
    }

    @Override
    public char readChar() throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char) ((ch1 << 8) + (ch2));
    }

    @Override
    public @NotNull String readUTF() throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        return DataInputStream.readUTF(this);
    }

    @Override
    public String readLine() throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        StringBuilder s = new StringBuilder();
        char c;
        do {
            c = readChar();

            if ((c != '\r')) {
                break;
            }
            s.append(c);
        } while (true);

        return s.toString();
    }

    @Override
    public void readFully(byte @NotNull [] b, int off, int len) throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        read(b, off, len);
    }

    @Override
    public void readFully(byte @NotNull [] b) throws IOException {
        if (currentOffset >= maxOffset)
            throw new EOFException();

        read(b);
    }

    @Override
    public int skipBytes(int bytes) throws IOException {
        return (int) skip(bytes);
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        if (name == null) {
            name = internalReader.getFileName(startSectorNumber);
        }

        return name;
    }

    private int readInternal(byte[] b, int off, int len) {
        if (len > 0) {
            if (len > (maxOffset - currentOffset)) {
                len = (int) (maxOffset - currentOffset);
            }
            System.arraycopy(currentSector, sectorOffset, b, off, len);
            sectorOffset += len;
            currentOffset += len;
        }

        return len;
    }

    private void checkSectorAvailable() throws IOException {
        if (sectorOffset == sectorLength && currentOffset < maxOffset) {
            currentSectorNumber++;
            currentSector = internalReader.readSector(currentSectorNumber);
            sectorOffset = 0;
        }
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || len > (b.length - off)) {
            throw new IndexOutOfBoundsException();
        }

        if (len > (maxOffset - currentOffset)) {
            len = (int) (maxOffset - currentOffset);
        }

        int totalLength = 0;

        int firstSector = readInternal(b, off, Math.min(len, sectorLength - sectorOffset));
        off += firstSector;
        len -= firstSector;
        totalLength += firstSector;

        // Read whole sectors
        if (len >= sectorLength) {
            int numberSectors = len / sectorLength;
            internalReader.readSectors(currentSectorNumber + 1, numberSectors, b, off);
            currentSectorNumber += numberSectors;
            sectorOffset = sectorLength;
            int n = numberSectors * sectorLength;
            currentOffset += n;
            checkSectorAvailable();
            off += n;
            len -= n;
            totalLength += n;
        }

        if (len > 0) {
            checkSectorAvailable();
            int lastSector = readInternal(b, off, len);
            totalLength += lastSector;
        }

        return totalLength;
    }
}
