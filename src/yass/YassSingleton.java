/*
 * Yass - Karaoke Editor
 * Copyright (C) 2009 Saruta
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

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSingleton {
    private static YassSingleton instance = new YassSingleton();


    /**
     * Constructor for the YassSingleton object
     */
    private YassSingleton() {
    }


    /**
     * Gets the instance attribute of the YassSingleton class
     *
     * @return The instance value
     */
    public static YassSingleton getInstance() {
        return instance;
    }
}

