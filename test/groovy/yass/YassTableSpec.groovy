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

import javax.swing.*
import javax.swing.table.TableModel

class YassTableSpec extends Specification {

    private final List<YassRow> LEADING_SPACE_END_TILDE_SONG = initSong1()
    private final List<YassRow> TRAILING_SPACE_END_TILDE_SONG = initSong2()
    private final List<YassRow> LEADING_SPACE_END_WORD_SONG = initSong3()
    private final List<YassRow> TRAILING_SPACE_END_WORD_SONG = initSong4()

    def 'isSongWithTrailingSpaces should check, if a song has trailing spaces'() {
        given:
        YassTableModel ytm = new YassTableModel()
        LEADING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }
        when:
        YassProperties props = Stub(YassProperties)
        YassTable yassTable = new YassTable(ytm, props)

        then:
        !yassTable.isSongWithTrailingSpaces()

        when:
        ytm = new YassTableModel()
        TRAILING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }
        and:
        yassTable = new YassTable(ytm, props)

        then:
        yassTable.isSongWithTrailingSpaces()
    }

    def 'rollRight applied to a song with leading spaces ending with ~. Legacy spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        LEADING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        ListSelectionModel selectionModel = Stub(ListSelectionModel) {
            getMinSelectionIndex() >> rowNum
        }
        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> false
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.selectionModel = selectionModel
        yassTable.model = Stub(TableModel) {
            getRowCount() >> LEADING_SPACE_END_TILDE_SONG.size()
        }

        when:
        yassTable.rollRight(splitCode as char, splitPos)

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum | splitCode | splitPos || expectation
        0      | '$'       | 0        || ['~', 'One', '~', ' two', '_', 'three', ' Four', ' five']
        0      | ' '       | 1        || ['O', ' ne', '~', ' two', '_', 'three', ' Four', ' five']
        0      | '-'       | 1        || ['O', 'ne', '~', ' two', '_', 'three', ' Four', ' five']
        1      | '$'       | 0        || ['One', '~', '~', ' two', '_', 'three', ' Four', ' five']
        2      | '$'       | 0        || ['One', '~', '~', ' two', '_', 'three', ' Four', ' five']
        2      | ' '       | 2        || ['One', '~', ' t', ' wo', '_', 'three', ' Four', ' five']
        3      | '$'       | 0        || ['One', '~', ' two', '~', '_', ' three', ' Four', ' five'] // why?
        5      | '$'       | 0        || ['One', '~', ' two', ' three', '_', '~', 'Four', ' five']
        5      | '-'       | 2        || ['One', '~', ' two', ' three', '_', 'Fo', 'ur', ' five']
        6      | '$'       | 0        || ['One', '~', ' two', ' three', '_', 'Four', '~', ' five']
        6      | ' '       | 3        || ['One', '~', ' two', ' three', '_', 'Four', ' fi', ' ve']
        6      | '-'       | 3        || ['One', '~', ' two', ' three', '_', 'Four', ' fi', 've']
        7      | '$'       | 0        || ['One', '~', ' two', ' three', '_', 'Four', ' five', '~']
    }

    def 'rollRight applied to a song with leading spaces ending with ~. New spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        TRAILING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        ListSelectionModel selectionModel = Stub(ListSelectionModel) {
            getMinSelectionIndex() >> rowNum
        }
        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> true
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.selectionModel = selectionModel
        yassTable.model = Stub(TableModel) {
            getRowCount() >> TRAILING_SPACE_END_TILDE_SONG.size()
        }

        when:
        yassTable.rollRight(splitCode as char, (slitPos + 1)) // We are substracting this in the code again

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum | splitCode | slitPos || expectation
        0      | '$'       | 0       || ['~', 'One', '~ ', 'two ', '_', 'three ', 'Four ', 'five ']
        0      | ' '       | 1       || ['O ', 'ne', '~ ', 'two ', '_', 'three ', 'Four ', 'five ']
        0      | ' '       | 2       || ['On ', 'e', '~ ', 'two ', '_', 'three ', 'Four ', 'five ']
        0      | '-'       | 1       || ['O', 'ne', '~ ', 'two ', '_', 'three ', 'Four ', 'five ']
        1      | '$'       | 0       || ['One', '~', '~ ', 'two ', '_', 'three ', 'Four ', 'five ']
        2      | '$'       | 0       || ['One', '~', '~ ', 'two ', '_', 'three ', 'Four ', 'five ']
        2      | ' '       | 2       || ['One', '~ ', 'tw ', 'o ', '_', 'three ', 'Four ', 'five ']
        3      | '$'       | 0       || ['One', '~ ', 'two', '~ ', '_', 'three ', 'Four ', 'five ']
        3      | ' '       | 3       || ['One', '~ ', 'two ', 'thr ', '_', 'ee ', 'Four ', 'five ']
        5      | '$'       | 0       || ['One', '~ ', 'two ', 'three ', '_', '~', 'Four ', 'five ']
        5      | '-'       | 2       || ['One', '~ ', 'two ', 'three ', '_', 'Fo', 'ur ', 'five ']
        6      | '$'       | 0       || ['One', '~ ', 'two ', 'three ', '_', 'Four', '~ ', 'five ']
        6      | ' '       | 2       || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'fi ', 've ']
        6      | '-'       | 2       || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'fi', 've ']
        7      | '$'       | 0       || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'five', '~ ']
    }

    def 'rollRight applied to a song with leading spaces ending with word. Legacy spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        LEADING_SPACE_END_WORD_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        ListSelectionModel selectionModel = Stub(ListSelectionModel) {
            getMinSelectionIndex() >> rowNum
        }
        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> false
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.selectionModel = selectionModel
        yassTable.model = Stub(TableModel) {
            getRowCount() >> LEADING_SPACE_END_WORD_SONG.size()
        }

        when:
        yassTable.rollRight(splitCode as char, slitPos)

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum | splitCode | slitPos || expectation
        0      | '$'       | 0       || ['~', 'One', '~', ' two', '_', 'three', ' Four', ' five six']
        0      | ' '       | 1       || ['O', ' ne', '~', ' two', '_', 'three', ' Four', ' five six']
        0      | '-'       | 1       || ['O', 'ne', '~', ' two', '_', 'three', ' Four', ' five six']
        1      | '$'       | 0       || ['One', '~', '~', ' two', '_', 'three', ' Four', ' five six']
        2      | '$'       | 0       || ['One', '~', '~', ' two', '_', 'three', ' Four', ' five six']
        2      | ' '       | 2       || ['One', '~', ' t', ' wo', '_', 'three', ' Four', ' five six']
        3      | '$'       | 0       || ['One', '~', ' two', '~', '_', ' three', ' Four', ' five six'] // why?
        5      | '$'       | 0       || ['One', '~', ' two', ' three', '_', '~', 'Four', ' five six']
        5      | '-'       | 2       || ['One', '~', ' two', ' three', '_', 'Fo', 'ur', ' five six']
        6      | '$'       | 0       || ['One', '~', ' two', ' three', '_', 'Four', '~', ' five']
        6      | ' '       | 3       || ['One', '~', ' two', ' three', '_', 'Four', ' fi', ' ve']
        6      | '-'       | 3       || ['One', '~', ' two', ' three', '_', 'Four', ' fi', 've']
        7      | '$'       | 0       || ['One', '~', ' two', ' three', '_', 'Four', ' five', ' six']
    }

    def 'rollRight applied to a song with leading spaces ending with word. New spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        TRAILING_SPACE_END_WORD_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        ListSelectionModel selectionModel = Stub(ListSelectionModel) {
            getMinSelectionIndex() >> rowNum
        }
        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> true
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.selectionModel = selectionModel
        yassTable.model = Stub(TableModel) {
            getRowCount() >> TRAILING_SPACE_END_WORD_SONG.size()
        }

        when:
        yassTable.rollRight(splitCode as char, slitPos)

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum | splitCode | slitPos || expectation
        0      | '$'       | 0       || ['~', 'One', '~ ', 'two ', '_', 'three ', 'Four ', 'five six ']
        0      | ' '       | 1       || ['O ', 'ne', '~ ', 'two ', '_', 'three ', 'Four ', 'five six ']
        0      | '-'       | 1       || ['O', 'ne', '~ ', 'two ', '_', 'three ', 'Four ', 'five six ']
        1      | '$'       | 0       || ['One', '~', '~ ', 'two ', '_', 'three ', 'Four ', 'five six ']
        2      | '$'       | 0       || ['One', '~', '~ ', 'two ', '_', 'three ', 'Four ', 'five six ']
        2      | ' '       | 2       || ['One', '~ ', 'tw ', 'o ', '_', 'three ', 'Four ', 'five six ']
        3      | '$'       | 0       || ['One', '~ ', 'two', '~ ', '_', 'three ', 'Four ', 'five six ']
        5      | '$'       | 0       || ['One', '~ ', 'two ', 'three ', '_', '~ ', 'Four ', 'five six ']
        5      | '-'       | 2       || ['One', '~ ', 'two ', 'three ', '_', 'Fo', 'ur ', 'five six ']
        6      | '$'       | 0       || ['One', '~ ', 'two ', 'three ', '_', 'Four', '~ ', 'five six ']
        6      | ' '       | 2       || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'fi ', 've six ']
        6      | '-'       | 2       || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'fi', 've six ']
        7      | '$'       | 0       || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'five', 'six ']
    }

    // ----------

    def 'rollLeft applied to a song with leading spaces ending with ~. Legacy spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        LEADING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        ListSelectionModel selectionModel = Stub(ListSelectionModel) {
            getMinSelectionIndex() >> rowNum
        }
        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> false
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.selectionModel = selectionModel
        yassTable.model = Stub(TableModel) {
            getRowCount() >> LEADING_SPACE_END_TILDE_SONG.size()
        }

        when:
        yassTable.rollLeft()

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum || expectation
        0      || ['One~', ' two', ' three', ' Four', '_', 'five', '~', '~']
        1      || ['One', ' two', ' three', ' Four', '_', 'five', '~', '~']
        2      || ['One', '~', ' two three', ' Four', '_', 'five', '~', '~']
        3      || ['One', '~', ' two', ' three Four', '_', 'five', '~', '~']
        5      || ['One', '~', ' two', ' three', '_', 'Four five', '~', '~']
        6      || ['One', '~', ' two', ' three', '_', 'Four', '~', '~']
        7      || ['One', '~', ' two', ' three', '_', 'Four', ' five', '~']
    }

    def 'rollLeft applied to a song with leading spaces ending with ~. New spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        TRAILING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        ListSelectionModel selectionModel = Stub(ListSelectionModel) {
            getMinSelectionIndex() >> rowNum
        }
        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> true
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.selectionModel = selectionModel
        yassTable.model = Stub(TableModel) {
            getRowCount() >> TRAILING_SPACE_END_TILDE_SONG.size()
        }

        when:
        yassTable.rollLeft()

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum || expectation
        0      || ['One ', 'two ', 'three ', 'Four ', '_', 'five', '~', '~ ']
        1      || ['One ', 'two ', 'three ', 'Four ', '_', 'five', '~', '~ ']
        2      || ['One', '~ ', 'two three ', 'Four ', '_', 'five', '~', '~ ']
        3      || ['One', '~ ', 'two ', 'three Four ', '_', 'five', '~', '~ ']
        5      || ['One', '~ ', 'two ', 'three ', '_', 'Four five', '~', '~ ']
        6      || ['One', '~ ', 'two ', 'three ', '_', 'Four ', '~', '~ ']
        7      || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'five', '~ ']
    }

    def 'rollLeft applied to a song with leading spaces ending with word. Legacy spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        LEADING_SPACE_END_WORD_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        ListSelectionModel selectionModel = Stub(ListSelectionModel) {
            getMinSelectionIndex() >> rowNum
        }
        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> false
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.selectionModel = selectionModel
        yassTable.model = Stub(TableModel) {
            getRowCount() >> LEADING_SPACE_END_WORD_SONG.size()
        }

        when:
        yassTable.rollLeft()

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum || expectation
        0      || ['One~', ' two', ' three', ' Four', '_', 'five', ' six', '~']
        1      || ['One', ' two', ' three', ' Four', '_', 'five', ' six', '~']
        2      || ['One', '~', ' two three', ' Four', '_', 'five', ' six', '~']
        3      || ['One', '~', ' two', ' three Four', '_', 'five', ' six', '~']
        5      || ['One', '~', ' two', ' three', '_', 'Four five', ' six', '~']
        6      || ['One', '~', ' two', ' three', '_', 'Four', ' six', '~']
        7      || ['One', '~', ' two', ' three', '_', 'Four', ' five', ' six']
    }

    def 'rollLeft applied to a song with leading spaces ending with word. New spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        TRAILING_SPACE_END_WORD_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        ListSelectionModel selectionModel = Stub(ListSelectionModel) {
            getMinSelectionIndex() >> rowNum
        }

        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> true
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.selectionModel = selectionModel
        yassTable.model = Stub(TableModel) {
            getRowCount() >> TRAILING_SPACE_END_WORD_SONG.size()
        }

        when:
        yassTable.rollLeft()

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum || expectation
        0      || ['One ', 'two ', 'three ', 'Four ', '_', 'five ', 'six', '~ ']
        1      || ['One ', 'two ', 'three ', 'Four ', '_', 'five ', 'six', '~ ']
        2      || ['One', '~ ', 'two three ', 'Four ', '_', 'five ', 'six', '~ ']
        3      || ['One', '~ ', 'two ', 'three Four ', '_', 'five ', 'six', '~ ']
        5      || ['One', '~ ', 'two ', 'three ', '_', 'Four five ', 'six', '~ ']
        6      || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'six', '~ ']
        7      || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'five', '~ ']
    }

    // ----------

    def 'insertPageBreakAt applied to a song with leading spaces ending with ~. Legacy spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        LEADING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> false
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.model = Stub(TableModel) {
            getRowCount() >> TRAILING_SPACE_END_WORD_SONG.size()
        }
        when:
        yassTable.insertPageBreakAt(rowNum)

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum || expectation
        0      || ['One', '_', '~', ' two', ' three', '_', 'Four', ' five', '~']
        1      || ['One', '~', '_', 'two', ' three', '_', 'Four', ' five', '~']
        2      || ['One', '~', ' two', '_', 'three', '_', 'Four', ' five', '~']
        3      || ['One', '~', ' two', ' three', '_', 'Four', ' five', '~']
        5      || ['One', '~', ' two', ' three', '_', 'Four', '_', 'five', '~']
        6      || ['One', '~', ' two', ' three', '_', 'Four', ' five', '_', '~']
    }

    def 'insertPageBreakAt applied to a song with trailing spaces ending with ~. New spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        TRAILING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> true
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.model = Stub(TableModel) {
            getRowCount() >> TRAILING_SPACE_END_TILDE_SONG.size()
        }

        when:
        yassTable.insertPageBreakAt(rowNum)

        then:
        verifyExpectation(yassTable, expectation)

        where:
        rowNum || expectation
        0      || ['One', '_', '~ ', 'two ', 'three ', '_', 'Four ', 'five', '~ ']
        1      || ['One', '~ ', '_', 'two ', 'three ', '_', 'Four ', 'five', '~ ']
        2      || ['One', '~ ', 'two ', '_', 'three ', '_', 'Four ', 'five', '~ ']
        3      || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'five', '~ ']
        5      || ['One', '~ ', 'two ', 'three ', '_', 'Four ', '_', 'five', '~ ']
        6      || ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'five', '_', '~ ']
    }

    def 'getText retrieves the text of a song with Legacy spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        LEADING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> false
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.model = Stub(TableModel) {
            getRowCount() >> LEADING_SPACE_END_TILDE_SONG.size()
        }

        when:
        String text = yassTable.getText()

        then:
        verifyExpectation(yassTable, ['One', '~', ' two', ' three', '_', 'Four', ' five', '~'])
        text == 'One-~ two three\nFour five-~'
    }

    def 'getText retrieves the text of a song with regular spacing'() {
        given:
        YassTableModel ytm = new YassTableModel()
        TRAILING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> true
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.model = Stub(TableModel) {
            getRowCount() >> TRAILING_SPACE_END_TILDE_SONG.size()
        }

        when:
        String text = yassTable.getText()

        then:
        verifyExpectation(yassTable, ['One', '~ ', 'two ', 'three ', '_', 'Four ', 'five', '~ ' ])
        text == 'One-~ two three\nFour five-~'
    }

    def 'insertRowsAt should insert rows'() {
        given:
        YassTableModel ytm = new YassTableModel()
        TRAILING_SPACE_END_TILDE_SONG.each { row ->
            ytm.addRow(row)
        }

        and:
        I18.setDefaultLanguage()

        and:
        YassProperties props = Stub(YassProperties) {
            isUncommonSpacingAfter() >> true
        }
        YassTable yassTable = new YassTable(ytm, props)
        yassTable.setBPM(240d)
        yassTable.gap = 1000
        yassTable.model = Stub(TableModel) {
            getRowCount() >> TRAILING_SPACE_END_TILDE_SONG.size()
        }

        when:
        yassTable.insertRowsAt(textToInsert, 7, false)

        then:
        verifyExpectation(yassTable, expectation)

        where:
        textToInsert                 || expectation
        ':\t0\t4\t20\tHello '        || ['One', '~ ', 'two ', 'three ', '_', 'Hello ', 'five', '~ ']
        ':\t0\t8\t20\tHello '        || ['One', '~ ', 'two ', 'three ', '_', 'Hello ', '~ ']
        ':\t0\t2\t20\ta \n' +
                ':\t3\t2\t20\tb \n' +
                ':\t6\t2\t20\tc \n' +
                ':\t9\t3\t20\td \n' +
                ':\t14\t2\t20\te \n' || ['One', '~ ', 'two ', 'three ', '_', 'a ', 'b ', 'c ', 'd ', 'e ']
    }

    private boolean verifyExpectation(YassTable yassTable, List<String> expectation) {
        int offset = 0
        YassRow yassRow
        do {
            yassRow = yassTable.getRowAt(++offset)
        } while(yassRow.isComment())
        expectation.eachWithIndex { String entry, int i ->
            yassRow = yassTable.getRowAt(i + offset)
            if (yassRow.isPageBreak()) {
                assert entry == '_'
            } else {
                String actual = yassRow.getText().replace(YassRow.SPACE, ' ' as char)
                assert actual == entry
            }
        }
    }

    private List<YassRow> initSong1() {
        List<YassRow> rows = [
                new YassRow(':', '0', '5', '10', 'One'),
                new YassRow(':', '6', '5', '10', '~'),
                new YassRow(':', '12', '5', '10', ' two'),
                new YassRow(':', '18', '5', '10', ' three'),
                new YassRow('-', '24', '', '', ''),
                new YassRow(':', '26', '5', '10', 'Four'),
                new YassRow(':', '32', '5', '10', ' five'),
                new YassRow(':', '38', '5', '10', '~'),
                new YassRow('E', '', '', '', '')
        ]
        rows.each { row ->
            row.setText(row.getText().replace(' ' as char, YassRow.SPACE))
        }
        rows
    }

    private List<YassRow> initSong2() {
        List<YassRow> rows = [
                new YassRow(':', '0', '5', '10', 'One'),
                new YassRow(':', '6', '5', '10', '~ '),
                new YassRow(':', '12', '5', '10', 'two '),
                new YassRow(':', '18', '5', '10', 'three '),
                new YassRow('-', '24', '', '', ''),
                new YassRow(':', '26', '5', '10', 'Four '),
                new YassRow(':', '32', '5', '10', 'five'),
                new YassRow(':', '38', '5', '10', '~ '),
                new YassRow('E', '', '', '', '')
        ]
        rows.each { row ->
            row.setText(row.getText().replace(' ' as char, YassRow.SPACE))
        }
        rows
    }

    private List<YassRow> initSong3() {
        List<YassRow> rows = [
                new YassRow(':', '0', '5', '10', 'One'),
                new YassRow(':', '6', '5', '10', '~'),
                new YassRow(':', '12', '5', '10', ' two'),
                new YassRow(':', '18', '5', '10', ' three'),
                new YassRow('-', '24', '', '', ''),
                new YassRow(':', '26', '5', '10', 'Four'),
                new YassRow(':', '32', '5', '10', ' five'),
                new YassRow(':', '38', '5', '10', ' six'),
                new YassRow('E', '', '', '', '')
        ]
        rows.each { row ->
            row.setText(row.getText().replace(' ' as char, YassRow.SPACE))
        }
        rows
    }

    private List<YassRow> initSong4() {
        List<YassRow> rows = [
                new YassRow(':', '0', '5', '10', 'One'),
                new YassRow(':', '6', '5', '10', '~ '),
                new YassRow(':', '12', '5', '10', 'two '),
                new YassRow(':', '18', '5', '10', 'three '),
                new YassRow('-', '24', '', '', ''),
                new YassRow(':', '26', '5', '10', 'Four '),
                new YassRow(':', '32', '5', '10', 'five '),
                new YassRow(':', '38', '5', '10', 'six '),
                new YassRow('E', '', '', '', '')
        ]
        rows.each { row ->
            row.setText(row.getText().replace(' ' as char, YassRow.SPACE))
        }
        rows
    }
}
