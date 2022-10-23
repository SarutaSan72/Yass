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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSheetInfo extends JPanel {
    private YassSheet sheet;
    private int track;
    private int minHeight;
    private int maxHeight;
    private int minBeat;
    private int maxBeat;
    private int rangeBeat;
    private int rangeHeight;

    double posMs = 0;
    private boolean inited = false;

    private static final int notesBar = 50;
    private static final int msgBar = 14;
    private static final int txtBar = 32;
    private static final int sideBar = 10;
    //private static final int selectBar = 20;

    private YassSheetListener sheetListener;

    public static Image err_page_icon = null, err_major_ico = null, err_file_icon = null, err_tags_icon = null, err_text_icon = null;
    public static Image no_err_page_icon = null, no_err_major_ico = null, no_err_file_icon = null, no_err_tags_icon = null, no_err_text_icon = null;

    Stroke medLineStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);

    private int hiliteCue = NONE;
    private static final int NONE = 0;
    private static final int ACTIVATE_TRACK = 1;
    private static final int SHOW_ERRORS = 2;
    //private static final int SHOW_SELECT = 3;
    private boolean isSelected = false;

    private boolean hasErr = false;

    public YassSheetInfo(YassSheet s, int track) {
        super(true);
        setFocusable(false);
        this.sheet = s;
        this.track = track;
        sheet.addYassSheetListener(sheetListener = new YassSheetListener() {
            @Override
            public void posChanged(YassSheet source, double posMs) {
                setPosMs(posMs);
            }
            @Override
            public void rangeChanged(YassSheet source, int minH, int maxH, int minB, int maxB) {
                setHeightRange(minH, maxH, minB, maxB);
            }
            @Override
            public void propsChanged(YassSheet source) {
                setBackground(sheet.darkMode ? sheet.hiGray2DarkMode : sheet.hiGray2);
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (sheet.isPlaying() || sheet.isTemporaryStop())
                    sheet.stopPlaying();
                if (! isActiveTrack())
                    activateTrack();
                //if (hiliteCue == SHOW_SELECT) {
                //    isSelected = !isSelected;
                //    repaint();
                //}
                if (hiliteCue == SHOW_ERRORS)
                    showErrors();
                else {
                    SwingUtilities.invokeLater(() -> {
                        boolean exact = e.isAltDown() || e.isControlDown() || e.getButton() == MouseEvent.BUTTON2|| e.getButton() == MouseEvent.BUTTON3;
                        moveTo(e.getX(), exact);
                        if (exact)
                            moveTo(e.getX(), exact);
                    });
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
                if (hiliteCue != NONE) {
                    hiliteCue = NONE;
                    repaint();
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!(sheet.isPlaying() || sheet.isTemporaryStop()))
                    moveTo(e.getX(), true);
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getX() > 150 && e.getX() < 350 && e.getY() < txtBar && hasErr) {
                    if (hiliteCue != SHOW_ERRORS) {
                        hiliteCue = SHOW_ERRORS;
                        repaint();
                    }
                }
                /*else if (e.getX() > getWidth() - sideBar -selectBar && e.getY() < txtBar) {
                    if (hiliteCue != SHOW_SELECT) {
                        hiliteCue = SHOW_SELECT;
                        repaint();
                    }
                }*/
                else {
                    if (! isActiveTrack()) {
                        if (hiliteCue != ACTIVATE_TRACK) {
                            hiliteCue = ACTIVATE_TRACK;
                            repaint();
                        }
                    }
                    else {
                        if (hiliteCue != NONE) {
                            hiliteCue = NONE;
                            repaint();
                        }
                    }
                }
            }
        });
        addMouseWheelListener(e -> sheet.dispatchEvent(e));

        try {
            err_page_icon = new ImageIcon(getClass().getResource("/yass/resources/img/MinorPageError.gif")).getImage();
            err_major_ico = new ImageIcon(getClass().getResource("/yass/resources/img/MajorError2.gif")).getImage();
            err_file_icon = new ImageIcon(getClass().getResource("/yass/resources/img/FileError.gif")).getImage();
            err_tags_icon = new ImageIcon(getClass().getResource("/yass/resources/img/TagError.gif")).getImage();
            err_text_icon = new ImageIcon(getClass().getResource("/yass/resources/img/TextError.gif")).getImage();

            no_err_page_icon = new ImageIcon(getClass().getResource("/yass/resources/img/MinorPageNoError.gif")).getImage();
            no_err_major_ico = new ImageIcon(getClass().getResource("/yass/resources/img/MajorNoError2.gif")).getImage();
            no_err_file_icon = new ImageIcon(getClass().getResource("/yass/resources/img/FileNoError.gif")).getImage();
            no_err_tags_icon = new ImageIcon(getClass().getResource("/yass/resources/img/TagNoError.gif")).getImage();
            no_err_text_icon = new ImageIcon(getClass().getResource("/yass/resources/img/TextNoError.gif")).getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeListener() {
        sheet.removeYassSheetListener(sheetListener);
    }

    public int getTrack() {
        return track;
    }

    private void activateTrack() {
        YassTable table = sheet.getActiveTable();
        if (table == null)
            return;
        if (track != table.getActions().getActiveTrack())
            table.getActions().activateTrack(track);
        sheet.getActiveTable().getActions().checkData(table, false, true);
    }
    private void showErrors() {
        sheet.getActiveTable().getActions().showErrors.actionPerformed(null);
    }
    private boolean isActiveTrack() {
        YassTable table = sheet.getActiveTable();
        return table != null && track == table.getActions().getActiveTrack();
    }

    private void moveTo(int x, boolean exact) {
        YassTable table = sheet.getActiveTable();
        if (table == null) return;

        if (sheet.isPlaying() || sheet.isTemporaryStop())
            return;

        // calculate ms in clicked track
        double minGapBeat = sheet.getMinGapInBeats();
        double gapBeat = sheet.getGapInBeats(track) - minGapBeat;
        // click position in beats
        int w = getWidth()-1;
        if (x < sideBar) x = sideBar;
        if (x > w-sideBar) x = w-sideBar;
        x -= sideBar;
        w -= 2 * sideBar;
        int beat = (int)((minBeat + x*rangeBeat) / (double) w - gapBeat);
        double ms = sheet.getTable(track).beatToMs(beat);
        int newpos = sheet.toTimeline(ms);

        double minMs = sheet.getMinVisibleMs();
        double maxMs = sheet.getMaxVisibleMs();
        boolean visible = minMs < ms && ms < maxMs;

        // select note at ms in active track
        int activeBeat = sheet.toBeat(ms);

        int i= table.getIndexOfNoteBeforeBeat(activeBeat);
        if (i < 0)
            i = table.getPage(1);
        if (i>=0) {
            if (i+2 < table.getRowCount() - 1) {
                YassRow r = table.getRowAt(i+1); // clicked directly after pagebreak -> select note after beat
                if (r.isPageBreak() && activeBeat > r.getBeatInt()) i+=2;
            }
            table.setRowSelectionInterval(i, i);
            table.updatePlayerPosition();
            if (! visible) table.zoomPage();

            if (exact) {
                int vx = sheet.getViewPosition().x;
                int tx = Math.max(0, newpos - 200);
                sheet.setPlayerPosition(newpos);
                sheet.slideRight(tx - vx);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, notesBar + msgBar + txtBar);
    }

    private void setHeightRange(int minH, int maxH, int minB, int maxB) {
        minHeight = minH;
        maxHeight = maxH;
        minBeat = minB;
        maxBeat = maxB;
        rangeBeat = maxBeat-minBeat;
        rangeHeight = maxHeight-minHeight;
        inited = true;
        repaint(0);
    }

    private void setPosMs(double ms) {
        posMs = ms;
        repaint(0);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintInfoArea((Graphics2D)g);
    }

    public void paintInfoArea(Graphics2D g2) {
        if (sheet.isPlaying()) return;

        YassTable table = sheet.getTable(track);
        if (table == null) return;
        boolean isActive = track == table.getActions().getActiveTrack();
        int currentDuetTrack = table.getDuetTrack();

        double bpm = table.getBPM();

        Color[] colorSet = sheet.getColors();

        int x = 0, y = txtBar, rx, ry, rw;
        final int versionWidth = sheet.getTableCount() > 1 ? 100 : 0;
        final int hBar = msgBar + txtBar;
        final int w = getWidth() - 2* sideBar - 1;
        final int h = getHeight();


        // sidebar
        if (currentDuetTrack >= 0) {
            g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
            g2.fillRect(0, 0, sideBar, h);
            g2.fillRect(sideBar+w+1, 0, sideBar, h);
        }
        else {
            g2.setColor(table.getTableColor());
            g2.fillRect(0, 0, sideBar, h);
            g2.fillRect(sideBar+w+1, 0, sideBar, h);
        }
        if (isActive) {
            g2.setColor(sheet.darkMode ? sheet.whiteDarkMode : sheet.white);
            g2.fillRect(3, 3, sideBar-6, h-6);
        }

        // background
        x += sideBar;
        if (isActive) {
            g2.setColor(sheet.darkMode ? sheet.whiteDarkMode : sheet.white);
            g2.fillRect(x, y + h - hBar - notesBar, w, notesBar);
            g2.fillRect(x, 0, w, txtBar);
        }
        else {
            g2.setColor(sheet.darkMode ? sheet.hiGray2DarkMode : sheet.hiGray2);
            g2.fillRect(x, 0, w, txtBar);
        }

        //  notes background
        g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
        g2.fillRect(x, y + h - hBar, w, hBar);
        g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
        g2.drawRect(x, y, w, h);
        g2.drawRect(x, y + h - hBar, w, hBar);

        if (rangeBeat <= 0)
            return;

        // shared notes
        double minGapBeat = sheet.getMinGapInBeats();
        double gapBeat = sheet.getGapInBeats(track) - minGapBeat;
        if (currentDuetTrack >= 0) {
            int track2 = track - 1;
            YassTable table2 = sheet.getTable(track - 1);
            if (table2 == null || table2.getDuetTrack() < 0) {
                track2 = track + 1;
                table2 = sheet.getTable(track2);
            }
            if (table2 != null && table2.getDuetTrack() >= 0) {
                g2.setColor(table.getTableColor());
                boolean newPage = true;
                int n = table.getRowCount();
                int i1 = 0;
                while (i1 < n) {
                    YassRow row = table.getRowAt(i1);
                    if (row.isPageBreak()) {
                        newPage = true;
                    } else if (row.isNote() && newPage) {
                        newPage = false;
                        int[] ij1 = table.enlargeToPages(i1, i1);
                        YassRow in1 = table.getRowAt(ij1[0]);
                        int x1 = sheet.beatToTimeline(in1.getBeatInt());
                        int i2 = sheet.nextNote(track2, x1);
                        int[] ij2 = table2.enlargeToPages(i2, i2);
                        if (ij2 != null && ij1[1] - ij1[0] == ij2[1] - ij2[0]) {
                            int len = ij1[1] - ij1[0] + 1;
                            int k = 0;
                            while (k < len) {
                                YassRow r1 = table.getRowAt(ij1[0] + k);
                                YassRow r2 = table2.getRowAt(ij2[0] + k);
                                if (!r1.equals(r2))
                                    break;
                                k++;
                            }
                            boolean same = k == len;
                            if (!same) {
                                YassRow s1 = table.getRowAt(ij1[0]);
                                YassRow e1 = table.getRowAt(ij1[1]);
                                int sb = s1.getBeatInt();
                                int eb = e1.getBeatInt() + e1.getLengthInt();
                                s1 = table.getRowAt(ij1[0] - 1);
                                if (s1.isPageBreak()) sb = s1.getSecondBeatInt();
                                e1 = table.getRowAt(ij1[1] + 1);
                                if (e1.isPageBreak()) eb = e1.getBeatInt();
                                int rx1 = (int) (w * (gapBeat + sb - minBeat) / (double) rangeBeat);
                                int rx2 = (int) (w * (gapBeat + eb - minBeat) / (double) rangeBeat);
                                g2.drawRect(x + rx1 + 1, y + 1, rx2 - rx1 - 2, h - hBar - 2);
                            }
                        }
                    }
                    ++i1;
                }
            }
        }

        // selection
        double minMs = sheet.getMinVisibleMs(track);
        double maxMs = sheet.getMaxVisibleMs(track); //TODO DUET
        rx = (int) (w * (gapBeat + sheet.toBeat(track, minMs) - minBeat) / (double) rangeBeat); //TODO DUET
        if (isActive) {
            int rx2 = (int) (w * (gapBeat + sheet.toBeat(track, maxMs) - minBeat) / (double) rangeBeat); //TODO DUET
            g2.setColor(sheet.darkMode ? sheet.blueDragDarkMode : sheet.blueDrag);
            g2.fillRect(x + rx, y, rx2 - rx, h - hBar);
            g2.setColor(sheet.darkMode ? sheet.blackDarkMode : sheet.black);
            g2.drawRect(x + rx, y+1, rx2 - rx, h - hBar-2);
        }

        // notes
        Graphics2D g3 = (Graphics2D) g2.create();
        g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g3.setStroke(medLineStroke);

        YassRow rPrev = null;
        int rxPrev = 0, ryPrev = 0, rwPrev = 0;
        boolean newpage = false;
        int rxnewpage = 0;
        int page = 1;
        for (YassRow r: table.getModelData()) {
            if (r.isComment())
                continue;
            if (r.isPageBreak()) {
                g3.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
                rx = (int) (w * (gapBeat + r.getBeatInt() - minBeat) / (double) rangeBeat);
                rw = 1;
                g3.fillRect(x + rx, y, rw, h);
                rPrev = null;
                newpage = true;
                String s = "" + page;
                int sw = g3.getFontMetrics().stringWidth(s);
                int sx = rxnewpage + rxPrev + rwPrev;
                if (sw < (rxPrev + rwPrev - rxnewpage) || page%5==0) {
                    g3.setColor(sheet.darkMode ? sheet.hiGray2DarkMode : sheet.hiGray2);
                    g3.drawString(s, x + (sx - sw) / 2, y + h - hBar + 12);
                }
                continue;
            }
            if (!r.isNote()) {
                rPrev = null;
                String s = "" + page;
                int sw = g3.getFontMetrics().stringWidth(s);
                int sx = rxnewpage + rxPrev + rwPrev;
                if (sw < (rxPrev + rwPrev - rxnewpage) || page%5==0) {
                    g3.setColor(sheet.darkMode ? sheet.hiGray2DarkMode : sheet.hiGray2);
                    g3.drawString(s, x + (sx - sw) / 2, y + h - hBar + 12);
                }
                continue;
            }
            rx = (int) (1+(w-1) * (gapBeat + r.getBeatInt() - minBeat) / (double) rangeBeat);
            ry = (int) ((h - hBar-4) * (r.getHeightInt() - minHeight) / (double) rangeHeight + 3);
            rw = (int) (w * r.getLengthInt() / rangeBeat + .5);
            Color hiliteFill = null;
            if (r.isGolden()) {
                hiliteFill = colorSet[YassSheet.COLOR_GOLDEN];
            } else if (r.isFreeStyle()) {
                hiliteFill = colorSet[YassSheet.COLOR_FREESTYLE];
            } else if (r.isRap()) {
                hiliteFill = colorSet[YassSheet.COLOR_RAP];
            } else if (r.isRapGolden()) {
                hiliteFill = colorSet[YassSheet.COLOR_RAPGOLDEN];
            } else if (r.hasMessage()) {
                hiliteFill = colorSet[YassSheet.COLOR_ERROR];
            }
            if (hiliteFill != null) {
                g3.setColor(hiliteFill);
                g3.fillRect(x + rx, y + h - hBar + 1, rw, hBar - 1);
            }
            g3.setColor(sheet.darkMode ?  sheet.dkGrayDarkMode : sheet.dkGray);
            g3.drawLine(x + rx, y + h - hBar - ry, x + rx + rw, y + h - hBar - ry);
            if (rPrev != null && rPrev.isNote()) {
                int gap = r.getBeatInt() - (rPrev.getBeatInt() + rPrev.getLengthInt());
                double gapMs = gap * 60 / (4 * bpm) * 1000;
                if (gapMs < 500) {
                    g3.drawLine(x + rxPrev + rwPrev, y + h - hBar - ryPrev, x + rx, y + h - hBar - ry);
                }
            }
            if (newpage) {
                page++;
                newpage = false;
                rxnewpage = rx;
            }

            rxPrev = rx;
            ryPrev = ry;
            rwPrev = rw;
            rPrev = r;
        }
        g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        g3.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        g3.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        g3.dispose();
        g2.setStroke(sheet.stdStroke);

        // cursor
        int curBeat = sheet.toBeat(track, posMs);
        rx = (int) (w * (gapBeat + curBeat - minBeat) / (double) rangeBeat); //TODO DUET
        g2.setColor(sheet.playerColor);
        g2.fillRect(x + rx - 1, y - 2, 2, h + 5);

        int maskWidth = 0;

        // singer mask
        x = x + 4;
        y = txtBar - 3;
        if (currentDuetTrack > 0) {
            int pc = table.getDuetTrackCount();
            int bitCount = YassUtils.getBitCount(pc);
            maskWidth = bitCount * 15;
            Vector<Integer> bitMask = YassUtils.getBitMask(currentDuetTrack);
            for (int i: bitMask) {
                int j = i;
                if (j >= 2) j = (int)Math.pow(2, j) -1;
                g2.setColor(table.getTableColor());
                g2.fillRect(x + i * 15, y-msgBar+2, 15, msgBar-2);
            }
            g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
            for (int i = 0; i < bitCount; i++) {
                g2.drawRect(x + i * 15, y-msgBar+2, 15, msgBar-2);
            }
            g2.setColor(sheet.darkMode ? sheet.whiteDarkMode : sheet.white);
            for (int i: bitMask) {
                g2.drawString("" + (i + 1), x + 5 + i * 15, y);
            }
        }
        else {
            int bitCount = 2;
            maskWidth = bitCount * 15;
            g2.setColor(table.getTableColor());
            g2.fillRect(x, y-msgBar+2, maskWidth, msgBar-2);
            g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
            g2.drawRect(x, y-msgBar+2, maskWidth, msgBar-2);
        }

        // version
        x = x + maskWidth + 6;
        String v = table.getVersion();
        y = txtBar - 3;
        if (v != null) {
            int vw = g2.getFontMetrics().stringWidth(v);
            if (vw > versionWidth)
                v = v.substring(0,10)+"...";
            g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
            g2.drawString(v, x, y);
        }

        // error selection
        x = x + versionWidth + 6;
        int errorWidth = 190;
        int goldenPoints = table.getGoldenPoints();
        int idealGoldenPoints = table.getIdealGoldenPoints();
        int goldenVariance = table.getGoldenVariance();
        String goldenDiff = table.getGoldenDiff();
        boolean goldenErr = Math.abs(goldenPoints - idealGoldenPoints) > goldenVariance;
        hasErr = table.hasUnhandledError() || table.hasMinorPageBreakMessages() || table.hasPageBreakMessages() || table.hasSpacingMessages() || goldenErr;
        if (hasErr) {
            if (hiliteCue == SHOW_ERRORS) {
                g2.setColor(sheet.darkMode ? sheet.blueDragDarkMode : sheet.blueDrag);
                g2.fillRect(x, 3, errorWidth, txtBar - 4);
                g2.setColor(colorSet[YassSheet.COLOR_ERROR]);
                g2.setStroke(sheet.thickStroke);
                g2.drawRect(x, 3, errorWidth, txtBar - 4);
                g2.setStroke(sheet.stdStroke);
            }
        }
        // errors
        x = x + 10;
        y = txtBar - 3 - 13;
        int we = 16;
        int he = 16;
        if (table.hasUnhandledError()) {
            if (err_major_ico != null)
                g2.drawImage(err_major_ico, x + (we+8)*0, y, we, he, null);
        }
        else {
            if (no_err_major_ico != null)
                g2.drawImage(no_err_major_ico, x + (we+8)*0, y, we, he, null);
        }
        if (table.hasMinorPageBreakMessages() || table.hasPageBreakMessages()) {
            if (err_page_icon != null)
                g2.drawImage(err_page_icon, x + (we+8)*1, y, we, he, null);
        }
        else {
            if (no_err_page_icon != null)
                g2.drawImage(no_err_page_icon, x + (we+8)*1, y, we, he, null);
        }
        if (table.hasSpacingMessages()) {
            if (err_text_icon != null)
                g2.drawImage(err_text_icon, x + (we+8)*2, y, we, he, null);
        }
        else {
            if (no_err_text_icon != null)
                g2.drawImage(no_err_text_icon, x + (we+8)*2, y, we, he, null);
        }

        // golden
        y = txtBar - 3;
        if (idealGoldenPoints > 0) {
            int wg = 60;
            int hg = 8;
            int xg = x + 74;
            int y2 = y - 9;

            double varPercentage = goldenVariance / (double) idealGoldenPoints;
            int xVar = (int) (wg * varPercentage);
            double goldenPercentage = goldenPoints / (double) idealGoldenPoints;
            if (goldenPercentage > 2) goldenPercentage = 2;
            int xGold = (int) (wg / 2 * goldenPercentage);

            boolean perfect = goldenDiff.equals("0");
            if (! perfect) {
                String goldenStringMinor = "\u2605" + goldenDiff;
                g2.setColor(goldenErr ? colorSet[YassSheet.COLOR_ERROR] : (sheet.darkMode ? sheet.hiGrayDarkMode : sheet.hiGray));
                g2.drawString(goldenStringMinor, xg + wg + 4, y);
            }
            else {
                String goldenStringMinor = "\u2605";
                g2.setColor(sheet.darkMode ? sheet.hiGrayDarkMode : sheet.hiGray);
                g2.drawString(goldenStringMinor, xg + wg + 4, y);
            }

            if (! perfect) {
                g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
                g2.drawRect(xg, y2, wg, hg);
                if (goldenErr) {g2.setColor(goldenErr ? colorSet[YassSheet.COLOR_ERROR] : (sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray));
                g2.fillRect(xg + 1, y2 + 1, wg - 1, hg - 1);}
                g2.setColor(colorSet[YassSheet.COLOR_GOLDEN]);
                g2.fillRect(xg + wg / 2 - xVar / 2, y2 + 1, xVar, hg - 1);
                g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
                g2.drawRect(xg + wg / 2, y2, 1, hg);
                g2.setColor(sheet.darkMode ? sheet.blackDarkMode : sheet.black);
                g2.drawRect(xg + xGold, y2 - 1, 1, hg + 2);
            }
            else {
                g2.setColor(sheet.darkMode ? sheet.hiGrayDarkMode : sheet.hiGray);
                g2.drawRect(xg, y2, wg, hg);
                g2.drawRect(xg + xGold, y2, 1, hg );
            }
        }

        // artist/title/year
        x = getWidth()-sideBar; //-selectBar;
        String t = table.getTitle();
        String a = table.getArtist();
        String year = table.getYear();
        String g = table.getGenre();
        String fn = table.getFilename();
        int sec = (int) (sheet.getDuration() / 1000.0 + 0.5);
        int min = sec / 60;
        sec = sec - min * 60;
        String dString = (sec < 10) ? min + ":0" + sec : min + ":" + sec;
        if (year == null) year = "";
        if (g == null) g = "";
        if (g.length() > 10) g = g.substring(0, 9) + "...";

        String s1 = a;
        if (s1.length() > 0 && t.length() > 0) s1 += " - ";
        s1 += t;

        String s2 = year;
        if (s2.length() > 0 && g.length() > 0) s2 += " · ";
        s2 += g;
        String bpmString;
        if (bpm == (long) bpm) bpmString = String.format("%d", (int) bpm);
        else bpmString = String.format("%s", bpm);
        s2 += " · " + bpmString + " bpm";
        s2 += " · " + dString;

        String s = fn;
        if (! table.isSaved())
            s = "\uD83D\uDDAB" + s;

        int sw = g2.getFontMetrics().stringWidth(s);
        int sw1 = g2.getFontMetrics().stringWidth(s1);
        int sw2 = g2.getFontMetrics().stringWidth(s2);
        y = txtBar - 3;
        if (sw < w-maskWidth-versionWidth-errorWidth-20) {
            if (! table.isSaved())
                g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
            else
                g2.setColor(sheet.darkMode ? sheet.hiGrayDarkMode : sheet.hiGray);
            g2.drawString(s, x - sw - 4, y);
        }
        if (sw2 < w-sw1) {
            g2.setColor(sheet.darkMode ? sheet.hiGrayDarkMode : sheet.hiGray);
            g2.drawString(s2, x - sw2 - 4, y-msgBar);
        }
        if (sw1 < w) {
            g2.setColor(sheet.darkMode ? sheet.hiGrayDarkMode : sheet.hiGray);
            g2.drawString(s1, sideBar + 4, y-msgBar);
        }

        // select
        /*x = getWidth()-sideBar-selectBar;
        if (hiliteCue == SHOW_SELECT) {
            g2.setColor(sheet.blue);
            g2.fillRect(x, 2, selectBar-2, txtBar-4);
        }
        if (isSelected) {
            g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
            g2.fillRect(x+3, 2+3, selectBar-2-5, txtBar-4-5);
        }
        g2.setColor(sheet.darkMode ? sheet.dkGrayDarkMode : sheet.dkGray);
        g2.drawRect(x, 2, selectBar-2, txtBar-4);*/
    }
}
