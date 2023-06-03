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

import org.apache.commons.lang3.StringUtils;
import org.mozilla.universalchardet.Constants;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class YassFileUtils {

    private UniversalDetector detector = new UniversalDetector();

    public synchronized YassFile loadTextFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return null;
        }
        String encoding = detectEncoding(file);
        Path path = Paths.get(file.getAbsolutePath());
        try {
            BufferedReader reader = Files.newBufferedReader(path, Charset.forName(encoding));
            List<String> contents = reader.lines().collect(Collectors.toList());
            return new YassFile(encoding, contents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a list of strings as UTF-8 encoded text file
     * @param contentLines List of strings
     * @param path         Path of file to write
     */
    public void writeFile(List<String> contentLines, Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (String line : contentLines) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException ioex) {
            System.out.println("Failed to write contents to " + path.toString());
            throw new RuntimeException(ioex);
        }
    }

    public String detectEncoding(File file) {
        String enc = null;
        if (detector == null) detector = new UniversalDetector(null);

        BufferedInputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[512];
            int nRead;
            while ((nRead = input.read(buffer)) > 0 && !detector.isDone()) {
                detector.handleData(buffer, 0, nRead);
            }
            detector.dataEnd();
            enc = detector.getDetectedCharset();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        detector.reset(); // reuse
        return StringUtils.defaultString(enc, Constants.CHARSET_UTF_8);
    }

}
