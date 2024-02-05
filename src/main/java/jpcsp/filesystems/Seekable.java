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
package jpcsp.filesystems;

import java.io.IOException;

/**
 * This is the Seekable class description.
 * It represents a class that provides seek operations.
 *
 * @author gigaherz : community developer for psp and other consoles.
 * @version $Id: $Id
 */
public interface Seekable {

    /**
     * Length long.
     *
     * @return the long
     * @throws java.io.IOException the io exception
     */
    long length() throws IOException;

    /**
     * Seek.
     *
     * @param position the position
     * @throws java.io.IOException the io exception
     */
    void seek(long position) throws IOException;

}
