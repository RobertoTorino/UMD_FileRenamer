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
package jpcsp.filesystems.umdiso.iso9660;

import jpcsp.filesystems.umdiso.UmdIsoFile;
import jpcsp.filesystems.umdiso.UmdIsoReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Iso 9660 directory.
 *
 * @author gigaherz : community developer for psp and other consoles.
 * @version $Id: $Id
 */
public class Iso9660Directory {

    private final List<Iso9660File> files;

    /**
     * Instantiates a new Iso 9660 directory.
     *
     * @param r               the r
     * @param directorySector the directory sector
     * @param directorySize   the directory size
     * @throws java.io.IOException the io exception
     */
    public Iso9660Directory(UmdIsoReader r, int directorySector, int directorySize) throws IOException {
        // parse directory sector
        UmdIsoFile dataStream = new UmdIsoFile(r, directorySector, directorySize);

        files = new ArrayList<>();

        byte[] b;

        while (directorySize >= 1) {
            int entryLength = dataStream.read();

            // This is assuming that the padding bytes are always filled with 0's.
            if (entryLength == 0) {
                directorySize--;
                continue;
            }

            directorySize -= entryLength;

            b = new byte[entryLength - 1];
            dataStream.read(b);

            Iso9660File file = new Iso9660File(b);
            files.add(file);
        }
    }

    /**
     * Gets entry by index.
     *
     * @param index the index
     * @return the entry by index
     * @throws java.lang.ArrayIndexOutOfBoundsException the array index out of bounds exception
     */
    public Iso9660File getEntryByIndex(int index) throws ArrayIndexOutOfBoundsException {
        return files.get(index);
    }

    /**
     * Gets file index.
     *
     * @param fileName the file name
     * @return the file index
     * @throws java.io.FileNotFoundException the file not found exception
     */
    public int getFileIndex(String fileName) throws FileNotFoundException {
        for (int i = 0; i < files.size(); i++) {
            String file = files.get(i).getFileName();
            if (file.equalsIgnoreCase(fileName)) {
                return i;
            }
        }

        throw new FileNotFoundException("File " + fileName + " not found in directory.");
    }

    /**
     * Get file list string [ ].
     *
     * @return the string [ ]
     * @throws java.io.FileNotFoundException the file not found exception
     */
    public String[] getFileList() throws FileNotFoundException {
        String[] list = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            list[i] = files.get(i).getFileName();
        }
        return list;
    }
}
