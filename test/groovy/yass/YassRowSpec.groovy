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

package yass

import spock.lang.Specification

class YassRowSpec extends Specification {
    def 'YassRow is defined by String'() {
        when: 'Constructor is called'
        YassRow yassRow = new YassRow(initString)

        then:
        yassRow.type == val1
        yassRow.getBeat() == val2
        yassRow.length == val3
        yassRow.height == val4
        yassRow.text == val5

        where:
        initString          || val1 | val2  | val3 | val4 | val5
        ''                  || ''   | ''    | ''   | ''   | ''
        '-\t123'            || '-'  | '123' | ''   | ''   | ''
        '*\t123\t1\t2\tbla' || '*'  | '123' | '1'  | '2'  | 'bla'
    }
}
