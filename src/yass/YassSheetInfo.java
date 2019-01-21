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

import yass.renderer.YassNote;
import yass.renderer.YassPlayerNote;
import yass.renderer.YassSession;
import yass.renderer.YassTrack;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSheetInfo extends JPanel {
    private YassSheet sheet;
    private int minHeight;
    private int maxHeight;
    private int minBeat;
    private int maxBeat;
    private int rangeBeat;
    private int rangeHeight;

    double posMs = 0;
    private boolean inited = false;

    private static final int notesBar = 50;
    private static final int msgBar = 10;
    private static final int txtBar = 30;

    public YassSheetInfo(YassSheet s) {
        super(true);
        setFocusable(false);
        this.sheet = s;
        sheet.addYassSheetListener(new YassSheetListener() {
            @Override
            public void posChanged(YassSheet source, double posMs) {
                setPosMs(posMs);
            }
            @Override
            public void rangeChanged(YassSheet source, int minH, int maxH, int minB, int maxB) {
                setHeightRange(minH, maxH, minB, maxB);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (sheet.isPlaying() || sheet.isTemporaryStop()) {
                    sheet.stopPlaying();
                }

                moveTo(e.getX());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (! (sheet.isPlaying() || sheet.isTemporaryStop()))
                    moveTo(e.getX());
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                sheet.dispatchEvent(e);
            }
        });
    }

    private void moveTo(int x)
    {
        YassTable table = sheet.getActiveTable();
        if (table == null) return;

        if (sheet.isPlaying() || sheet.isTemporaryStop())
            return;

        // click position in beats
        int w = getWidth();
        if (x < 0) x = 0;
        if (x > w) x = w;
        int beat = (int)((minBeat + x*rangeBeat) / (double) w);

        double minMs = sheet.getMinVisibleMs();
        double maxMs = sheet.getMaxVisibleMs();
        double ms = table.beatToMs(beat);
        boolean visible = minMs < ms && ms < maxMs;

        int i= table.getIndexOfNoteBeforeBeat(beat);
        if (i>=0) {
            if (i+2 < table.getRowCount() - 1) {
                YassRow r = table.getRowAt(i+1); // clicked directly after pagebreak -> select note after beat
                if (r.isPageBreak() && beat > r.getBeatInt()) i+=2;
            }
            table.setRowSelectionInterval(i, i);
            table.updatePlayerPosition();
            if (! visible) table.zoomPage();
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
        paintInfoArea((Graphics2D)g);
    }

    public void paintInfoArea(Graphics2D g2) {
        if (sheet.isPlaying()) return;

        YassTable table = sheet.getActiveTable();
        if (table == null) return;

        double bpm = table.getBPM();

        Color[] colorSet = sheet.getColors();

        int x = 0, y = txtBar, rx, ry, rw;
        int hBar = msgBar + txtBar;
        int w = getWidth();
        int h = getHeight();

        // background
        g2.setColor(sheet.disabledColor);
        g2.fillRect(0, 0, w, h);

        //  notes background
        g2.setColor(sheet.dkGray);
        g2.fillRect(x, y + h - hBar, w, hBar);
        g2.setColor(sheet.dkGray);
        g2.drawRect(x, y, w, h);
        g2.drawRect(x, y + h - hBar, w, hBar);

        // selection
        double minMs = sheet.getMinVisibleMs();
        double maxMs = sheet.getMaxVisibleMs();
        rx = (int) (w * (sheet.toBeat(minMs) - minBeat) / (double) rangeBeat);
        int rx2 = (int) (w * (sheet.toBeat(maxMs) - minBeat) / (double) rangeBeat);
        g2.setColor(Color.white);
        g2.fillRect(x + rx, y, rx2 - rx, h - hBar);
        g2.setColor(Color.black);
        g2.drawRect(x + rx, y, rx2 - rx, h - hBar);

        // notes
        YassRow r, rPrev = null;
        int rxPrev = 0, ryPrev = 0, rwPrev = 0;
        for (Enumeration<?> e = table.getRows(); e.hasMoreElements(); ) {
            r = (YassRow) e.nextElement();
            if (r.isPageBreak()) {
                g2.setColor(sheet.dkGray);
                rx = (int) (w * (r.getBeatInt() - minBeat) / (double) rangeBeat);
                rw = 1;
                g2.fillRect(x + rx, y, rw, h);
                rPrev = null;
                continue;
            }
            if (!r.isNote()) {
                rPrev = null;
                continue;
            }


            rx = (int) (w * (r.getBeatInt() - minBeat) / (double) rangeBeat);
            ry = (int) ((h - hBar) * (r.getHeightInt() - minHeight) / (double) rangeHeight);
            rw = (int) (w * r.getLengthInt() / rangeBeat + .5);

            Color hiliteFill = null;
            if (r.isGolden()) {
                hiliteFill = colorSet[3];
            } else if (r.isFreeStyle()) {
                hiliteFill = colorSet[4];
            } else if (r.hasMessage()) {
                hiliteFill = colorSet[5];
            }
            if (hiliteFill != null) {
                g2.setColor(hiliteFill);
                g2.fillRect(x + rx - 1, y + h - hBar + 1, rw + 2, hBar - 1);
            }
            g2.setColor(sheet.dkGray);
            g2.setStroke(sheet.medStroke);
            g2.drawLine(x + rx, y + h - hBar - ry, x + rx + rw, y + h - hBar - ry);
            if (rPrev != null && rPrev.isNote()) {
                int gap = r.getBeatInt() - (rPrev.getBeatInt() + rPrev.getLengthInt());
                double gapMs = gap * 60 / (4 * bpm) * 1000;
                if (gapMs < 500) {
                    g2.drawLine(x + rxPrev + rwPrev, y + h - hBar - ryPrev, x + rx, y + h - hBar - ry);
                }
            }
            g2.setStroke(sheet.stdStroke);
            rxPrev = rx;
            ryPrev = ry;
            rwPrev = rw;
            rPrev = r;
        }

        // cursor
        g2.setColor(sheet.playerColor);
        rx = (int) (w * (sheet.toBeat(posMs) - minBeat) / (double) rangeBeat);
        g2.fillRect(x + rx - 1, y - 2, 2, h + 5);

        // golden
        int goldenPoints = table.getGoldenPoints();
        if (goldenPoints > 0) {
            int idealGoldenPoints = table.getIdealGoldenPoints();
            int goldenVariance = table.getGoldenVariance();
            int idealGoldenBeats = table.getIdealGoldenBeats();
            int durationGolden = table.getDurationGolden();
            String goldenDiff = table.getGoldenDiff();
            boolean err = Math.abs(goldenPoints - idealGoldenPoints) > goldenVariance;
            g2.setColor(err ? colorSet[5] : sheet.dkGray);

            String goldenString = MessageFormat.format(
                    I18.get("correct_golden_info"), "" + idealGoldenPoints,
                    "" + goldenPoints, "" + idealGoldenBeats, "" + durationGolden, goldenDiff);

            x = 10;
            y = 8;
            w = 80;
            h = 12;

            double varPercentage = goldenVariance / (double) idealGoldenPoints;
            int xVar = (int) (w * varPercentage);

            double goldenPercentage = goldenPoints / (double) idealGoldenPoints;
            if (goldenPercentage > 2) goldenPercentage = 2;
            int xGold = (int) (w / 2 * goldenPercentage);

            g2.drawString(goldenString, x + w + 10, y + h);

            g2.setColor(sheet.dkGray);
            g2.drawRect(x, y, w, h);
            g2.setColor(err ? colorSet[5] : colorSet[0]);
            g2.fillRect(x + 1, y + 1, w - 1, h - 1);
            g2.setColor(colorSet[3]);
            g2.fillRect(x + w / 2 - xVar / 2, y + 1, xVar, h - 1);
            g2.setColor(sheet.dkGray);
            g2.drawRect(x + w / 2, y, 1, h);
            g2.setColor(Color.black);
            g2.drawRect(x + xGold, y - 1, 1, h + 2);
        }

        // artist/title/year
        String t = table.getTitle();
        String a = table.getArtist();
        String year = table.getYear();
        String g = table.getGenre();
        int sec = (int)(sheet.getDuration() / 1000.0 + 0.5);
        int min = sec / 60;
        sec = sec - min * 60;
        String dString = (sec < 10) ? min + ":0" + sec : min + ":" + sec;
        if (a == null) a = "";
        if (t == null) t = "";
        if (dString == null) dString = "";
        if (year == null) year = "";
        if (g == null) g = "";
        if (g.length() > 10) g = g.substring(0,9)+"...";
        String s = a;
        if (s.length() > 0 && t.length() > 0) s+= " - ";
        s += t;

        String s2 = year;
        if (s2.length() > 0 && g.length() > 0) s2+= " · ";
        s2 += g;

        String bpmString = "";
        if(bpm == (long) bpm) bpmString = String.format("%d",(int)bpm);
        else bpmString = String.format("%s", bpm);
        s2 += " · " + bpmString + " bpm";

        s2 += " · " + dString;

        w = getWidth();
        int sw1 = g2.getFontMetrics().stringWidth(s);
        int sw2 = g2.getFontMetrics().stringWidth(s2);
        int sw = Math.max(sw1, sw2);
        x = w - Math.max(sw1,sw2) - 10;
        y = txtBar-3;
        if (x > 300) {
            g2.setColor(sheet.dkGray);
            g2.drawString(s, w-sw-10, y - 15);
            g2.drawString(s2, w-sw2-10, y);
        }
    }
}
