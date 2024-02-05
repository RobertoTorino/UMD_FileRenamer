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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * The type Iso 9660 file.
 *
 * @author gigaherz : community developer for psp and other consoles.
 * @version $Id: $Id
 */
public class Iso9660File {

    private final int fileLBA;
    private final int fileSize;
    private final int fileProperties;
    // padding: byte[3]
    private String fileName; //[128+1];

    /**
     * Instantiates a new Iso 9660 file.
     *
     * @param data the data
     * @throws java.io.IOException the io exception
     */
    public Iso9660File(byte[] data) throws IOException {

        fileLBA = Ubyte(data[1]) | (Ubyte(data[2]) << 8) | (Ubyte(data[3]) << 16) | (data[4] << 24);
        fileSize = Ubyte(data[9]) | (Ubyte(data[10]) << 8) | (Ubyte(data[11]) << 16) | (data[12] << 24);
        int year = Ubyte(data[17]);
        int month = Ubyte(data[18]);
        int day = Ubyte(data[19]);
        int hour = Ubyte(data[20]);
        int minute = Ubyte(data[21]);
        int second = Ubyte(data[22]);
        String timeZoneName = getString(data);
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);

        Calendar timestampCalendar = Calendar.getInstance(timeZone);
        timestampCalendar.set(1900 + year, month - 1, day, hour, minute, second);
        timestampCalendar.getTime();

        fileProperties = data[24];

        if ((fileLBA < 0) || (fileSize < 0)) {
            throw new IOException("WTF?! Size or lba < 0?!");
        }

        int fileNameLength = data[31];

        fileName = "";
        for (int i = 0; i < fileNameLength; i++) {
            char c = (char) (data[32 + i]);
            if (c == 0) c = '.';

            fileName += c;
        }
    }

    @NotNull
    private static String getString(byte[] data) {
        int gmtOffset = data[23];

        int gmtOffsetHours = gmtOffset / 4;
        int gmtOffsetMinutes = (gmtOffset % 4) * 15;

        String timeZoneName = "GMT";
        if (gmtOffset >= 0) {
            timeZoneName += "+";
        }
        timeZoneName += gmtOffsetHours;
        if (gmtOffsetMinutes > 0) {
            timeZoneName += gmtOffsetMinutes;
        }
        return timeZoneName;
    }

    private int Ubyte(byte b) {
        return (b) & 255;
    }

    /**
     * Gets lba.
     *
     * @return the lba
     */
    public int getLBA() {
        return fileLBA;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public int getSize() {
        return fileSize;
    }

    /**
     * Gets properties.
     *
     * @return the properties
     */
    public int getProperties() {
        return fileProperties;
    }

    /**
     * Gets file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return fileName;
    }
}
