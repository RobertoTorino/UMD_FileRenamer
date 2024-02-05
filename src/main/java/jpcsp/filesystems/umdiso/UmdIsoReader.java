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

import jpcsp.filesystems.umdiso.iso9660.Iso9660Directory;
import jpcsp.filesystems.umdiso.iso9660.Iso9660File;
import jpcsp.filesystems.umdiso.iso9660.Iso9660Handler;
import jpcsp.util.Utilities;
import org.bolet.jgz.Inflater;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * The type Umd iso reader.
 *
 * @author gigaherz : community developer for psp and other consoles.
 * @version $Id: $Id
 */
public class UmdIsoReader {

    private final HashMap<String, Iso9660File> fileCache = new HashMap<>();
    private final HashMap<String, Iso9660Directory> dirCache = new HashMap<>();
    /**
     * The File reader.
     */
    RandomAccessFile fileReader;
    /**
     * The Format.
     */
    FileFormat format;
    /**
     * The Num sectors.
     */
    int numSectors; //
    /**
     * The Sector offsets.
     */
    long[] sectorOffsets; // for CSO/DAX
    /**
     * The Offset shift.
     */
    int offsetShift; // for CSO
    /**
     * The File name.
     */
    String fileName;

    /**
     * Instantiates a new Umd iso reader.
     *
     * @param umdFilename the umd filename
     * @throws java.io.IOException the io exception
     */
    public UmdIsoReader(String umdFilename) throws IOException {
        fileName = umdFilename;
        fileReader = new RandomAccessFile(umdFilename, "r");

        /*
         * u32 'CISO' u32 0? u32 image size in bytes (why? it could have been
         * sectors and make things simpler!) u32 sector size? (00000800 = 2048 =
         * sector size) u32 ? (1) u32[] sector offsets (as many as image size /
         * sector size, I guess)
         */

        format = FileFormat.Uncompressed;
        numSectors = (int) (fileReader.length() / 2048);

        byte[] id = new byte[24];

        fileReader.seek(0);
        fileReader.read(id);

        if ((((char) id[0]) == 'C') && (((char) id[1]) == 'I')
                && (((char) id[2]) == 'S') && (((char) id[3]) == 'O')) {
            format = FileFormat.CompressedCSO;

            int lenInbytes = BytesToInt(id, 8);
            int sectorSize = BytesToInt(id, 16);

            // int version = Ubyte(id[20]);
            offsetShift = Ubyte(id[21]);

            numSectors = lenInbytes / sectorSize;

            sectorOffsets = new long[numSectors + 1];

            byte[] offsetData = new byte[(numSectors + 1) * 4];

            fileReader.readFully(offsetData);

            for (int i = 0; i <= numSectors; i++) {
                sectorOffsets[i] = (BytesToInt(offsetData, i * 4)) & 0xFFFFFFFFL;
                if (i > 0) {
                    if ((sectorOffsets[i] & 0x7FFFFFFF) < (sectorOffsets[i - 1] & 0x7FFFFFFF)) {
                        throw new IOException("Invalid offset [" + i + "]: "
                                + sectorOffsets[i] + "<" + sectorOffsets[i - 1]);
                    }
                }
            }
        }

        // when we reach here, we assume it's either a .ISO or a .CSO,
        // but we still need to make sure of that

        id = new byte[6];

        UmdIsoFile f = null;
        try {
            f = new UmdIsoFile(this, 16, 2048);
            f.read(id);
        } catch (ArrayIndexOutOfBoundsException e) {
            // UmdIsoFile constructor calls readSector and will fail if given a
            // file less than 2048 bytes in size
            format = FileFormat.Unknown;
            throw new IOException("Unsupported file format or corrupt file.");
        } finally {
            Utilities.close(f);
        }

        if ((((char) id[1]) == 'C') && (((char) id[2]) == 'D')
                && (((char) id[3]) == '0') && (((char) id[4]) == '0')
                && (((char) id[5]) == '1')) {
            if (format == FileFormat.Uncompressed) {
                numSectors = (int) (fileReader.length() / 2048);
            }

            return;
        }

        format = FileFormat.Unknown;
        throw new IOException("Unsupported file format or corrupt file.");
    }

    private int Ubyte(byte b) {
        return (b) & 255;
    }

    private int BytesToInt(byte[] bytes, int offset)
            throws ArrayIndexOutOfBoundsException {
        return Ubyte(bytes[offset]) | (Ubyte(bytes[offset + 1]) << 8)
                | (Ubyte(bytes[offset + 2]) << 16) | (bytes[offset + 3] << 24);
    }

    /**
     * Read sequential sectors into a byte array
     *
     * @param sectorNumber  -                      the first sector to be read
     * @param numberSectors -                      the number of sectors to be read
     * @param buffer        -                      the byte array where to write the sectors
     * @param offset        -                      offset into the byte array where to start writing
     * @throws java.io.IOException if an I/O error occurs while reading the sector
     */
    public void readSectors(int sectorNumber, int numberSectors, byte[] buffer,
                            int offset) throws IOException {
        if ((sectorNumber < 0) || ((sectorNumber + numberSectors) > numSectors)) {
            throw new ArrayIndexOutOfBoundsException("Sectors Start="
                    + sectorNumber + ",Length=" + numberSectors
                    + " out of bounds.");
        }

        if (format == FileFormat.Uncompressed) {
            // Read an uncompressed ISO file in one call
            fileReader.seek(2048L * sectorNumber);
            fileReader.read(buffer, offset, numberSectors * 2048);
        } else {
            // Read sector per sector for the other formats
            for (int i = 0; i < numberSectors; i++) {
                readSector(sectorNumber + i, buffer, offset + i * 2048);
            }
        }

    }

    /**
     * Read one sector into a byte array
     *
     * @param sectorNumber -                     the sector number to be read
     * @param buffer       -                     the byte array where to write
     * @param offset       -                     offset into the byte array where to start writing
     * @throws java.io.IOException if an I/O error occurs while reading the sector
     */
    public void readSector(int sectorNumber, byte[] buffer, int offset)
            throws IOException {
        if ((sectorNumber < 0) || (sectorNumber >= numSectors))
            throw new ArrayIndexOutOfBoundsException("Sector number "
                    + sectorNumber + " out of bounds.");

        if (format == FileFormat.Uncompressed) {
            fileReader.seek(2048L * sectorNumber);
            fileReader.read(buffer, offset, 2048);
            return;
        }

        if (format == FileFormat.CompressedCSO) {
            long sectorOffset = sectorOffsets[sectorNumber];
            long sectorEnd = sectorOffsets[sectorNumber + 1];

            if ((sectorOffset & 0x80000000L) != 0) {
                long realOffset = (sectorOffset & 0x7fffffff) << offsetShift;

                fileReader.seek(realOffset);
                fileReader.read(buffer, offset, 2048);
                return;
            }

            sectorEnd = (sectorEnd & 0x7fffffff) << offsetShift;
            sectorOffset = (sectorOffset & 0x7fffffff) << offsetShift;

            int compressedLength = (int) (sectorEnd - sectorOffset);
            if (compressedLength < 0) {
                for (int i = 0; i < 2048; i++) {
                    buffer[offset + i] = 0;
                }
                return;
            }

            byte[] compressedData = new byte[compressedLength];

            fileReader.seek(sectorOffset);
            fileReader.read(compressedData);

            try {
                Inflater inf = new Inflater();

                ByteArrayInputStream b = new ByteArrayInputStream(
                        compressedData);
                inf.reset(b);
                inf.readAll(buffer, offset, 2048);
            } catch (IOException e) {
                throw new IOException(
                        "Exception while uncompressed sector from " + fileName);
            }

            return;
        }

        throw new IOException("Unsupported file format or corrupt file.");
    }

    /**
     * Read one sector
     *
     * @param sectorNumber -                     the sector number to be read
     * @return a new byte array of size sectorLength containing the sector
     * @throws java.io.IOException if an I/O error occurs while reading the sector
     */
    public byte[] readSector(int sectorNumber) throws IOException {
        byte[] buffer = new byte[2048];
        readSector(sectorNumber, buffer, 0);

        return buffer;
    }

    private Iso9660File getFileEntry(String filePath) throws IOException {
        Iso9660File info;

        info = fileCache.get(filePath);
        if (info != null) {
            return info;
        }

        int parentDirectoryIndex = filePath.lastIndexOf('/');
        if (parentDirectoryIndex >= 0) {
            String parentDirectory = filePath
                    .substring(0, parentDirectoryIndex);
            Iso9660Directory dir = dirCache.get(parentDirectory);
            if (dir != null) {
                int index = dir.getFileIndex(filePath
                        .substring(parentDirectoryIndex + 1));
                info = dir.getEntryByIndex(index);
                if (info != null) {
                    fileCache.put(filePath, info);
                    return info;
                }
            }
        }

        Iso9660Directory dir = new Iso9660Handler(this);

        String[] path = filePath.split("/");

        // walk through path
        for (int i = 0; i < path.length; ) {
            if (path[i].compareTo(".") == 0) {
                if (i == (path.length - 1)) {
                    break;
                }
                // skip the path "."
                i++;
            } else if (path[i].compareTo("..") == 0) {
                i = Math.max(0, i - 1);
            } else {
                int index = dir.getFileIndex(path[i]);

                info = dir.getEntryByIndex(index);

                if ((info.getProperties() & 2) == 2) // if it's a directory
                {
                    dir = new Iso9660Directory(this, info.getLBA(), info
                            .getSize());
                    StringBuilder dirPath = new StringBuilder(path[0]);
                    for (int j = 1; j <= i; j++) {
                        dirPath.append("/").append(path[j]);
                    }
                    dirCache.put(dirPath.toString(), dir);
                }
                i++;
            }
        }

        if (info != null) {
            fileCache.put(filePath, info);
        }

        return info;
    }

    /**
     * Gets file.
     *
     * @param filePath the file path
     * @return the file
     * @throws java.io.IOException the io exception
     */
    public UmdIsoFile getFile(String filePath) throws IOException {
        int fileStart;
        long fileLength;

        if (filePath != null && filePath.startsWith("sce_lbn")) {
            filePath = filePath.substring(7);
            int sep = filePath.indexOf("_size");
            fileStart = (int) Utilities
                    .parseHexLong(filePath.substring(0, sep));
            fileLength = Utilities.parseHexLong(filePath.substring(sep + 5));

            if (fileStart < 0 || fileStart >= numSectors) {
                throw new IOException("File '" + filePath
                        + "': Invalid Start Sector");
            }
        } else if (filePath != null && filePath.isEmpty()) {
            fileStart = 0;
            fileLength = numSectors * 2048L;
        } else {
            Iso9660File info = getFileEntry(filePath);
            if (info != null) {
                if ((info.getProperties() & 2) == 2) // if it's a directory
                {
                    info = null;
                }
            }

            if (info == null)
                throw new FileNotFoundException("File '" + filePath
                        + "' not found or not a file.");

            fileStart = info.getLBA();
            fileLength = info.getSize();
        }

        return new UmdIsoFile(this, fileStart, fileLength);
    }

    /**
     * List directory string [ ].
     *
     * @param filePath the file path
     * @return the string [ ]
     * @throws java.io.IOException the io exception
     */
    public String[] listDirectory(String filePath) throws IOException {
        Iso9660Directory dir = null;

        if (filePath.compareTo("") == 0) {
            dir = new Iso9660Handler(this);
        } else {
            Iso9660File info = getFileEntry(filePath);

            if (info != null) {
                if ((info.getProperties() & 2) == 2) // if it's a directory
                {
                    dir = new Iso9660Directory(this, info.getLBA(), info
                            .getSize());
                }
            }
        }

        if (dir == null)
            throw new FileNotFoundException("File '" + filePath
                    + "' not found or not a directory.");

        return dir.getFileList();
    }

    /**
     * Gets filename.
     *
     * @return the filename
     */
    public String getFilename() {
        return fileName;
    }

    private String getFileNameRecursive(int fileStartSector, String path,
                                        String[] files) throws IOException {
        for (String file : files) {
            String filePath = path + "/" + file;
            Iso9660File info = null;
            if (path.isEmpty()) {
                filePath = file;
            } else {
                info = getFileEntry(filePath);
                if (info != null) {
                    if (info.getLBA() == fileStartSector) {
                        return info.getFileName();
                    }
                }
            }

            if ((info == null || (info.getProperties() & 2) == 2)
                    && !file.equals(".") && !file.equals("\01")) {
                try {
                    String[] childFiles = listDirectory(filePath);
                    String fileName = getFileNameRecursive(fileStartSector,
                            filePath, childFiles);
                    if (fileName != null) {
                        return fileName;
                    }
                } catch (FileNotFoundException e) {
                    // Continue
                }
            }
        }

        return null;
    }

    /**
     * Gets file name.
     *
     * @param fileStartSector the file start sector
     * @return the file name
     */
    public String getFileName(int fileStartSector) {
        try {
            String[] files = listDirectory("");
            return getFileNameRecursive(fileStartSector, "", files);
        } catch (IOException e) {
            // Ignore Exception
        }

        return null;
    }

    /**
     * The enum File format.
     */
    enum FileFormat {
        /**
         * Uncompressed file format.
         */
        Uncompressed,
        /**
         * Compressed cso file format.
         */
        CompressedCSO,
        /**
         * Compressed dax file format.
         */
        CompressedDAX, // not implemented yet
        /**
         * Unknown file format.
         */
// ...
        Unknown
    }

}
