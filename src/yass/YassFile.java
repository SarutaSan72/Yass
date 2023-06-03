/*
 * Yass Reloaded - Karaoke Editor
 * Copyright (C) 2009-2023 Saruta
 * Copyright (C) 2023 DoubleDee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass;

import org.mozilla.universalchardet.Constants;

import java.util.List;

public class YassFile {
    private final String encoding;
    private final List<String> contentLines;

    public YassFile(String encoding, List<String> contentLines) {
        this.encoding = encoding;
        this.contentLines = contentLines;
    }

    public boolean isUtf8() {
        return Constants.CHARSET_UTF_8.equals(encoding);
    }
    public List<String> getContentLines() {
        return contentLines;
    }
}
