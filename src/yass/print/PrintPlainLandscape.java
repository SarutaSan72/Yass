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

package yass.print;

import com.lowagie.text.*;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.MultiColumnText;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import yass.I18;
import yass.YassSong;

import javax.swing.*;
import java.awt.*;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class PrintPlainLandscape implements PrintPlugin {
    String foottext = "", dformat = null;
    Rectangle rect;
    int ncol = 0;
    int mleft, mright, mtop, mbottom, mcol;
    boolean show[] = new boolean[9], showCovers;
    JTextField footnote, dateformat;
    JComboBox<?> co = null, ed = null;
    JComboBox<String> formatBox = null;


    /**
     * Gets the title attribute of the PrintPlainLandscape object
     *
     * @return The title value
     */
    public String getTitle() {
        return I18.get("print_land_title");
    }


    /**
     * Gets the description attribute of the PrintPlainLandscape object
     *
     * @return The description value
     */
    public String getDescription() {
        return I18.get("print_land_description");
    }


    /**
     * Gets the control attribute of the PrintPlainLandscape object
     *
     * @return The control value
     */
    public JPanel getControl() {
        JPanel styleOptions = new JPanel(new BorderLayout());
        styleOptions.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JPanel styleOptionsLeft = new JPanel(new GridLayout(0, 1));
        JPanel styleOptionsRight = new JPanel(new GridLayout(0, 1));
        styleOptionsLeft.add(new JLabel(I18.get("print_land_format")));
        styleOptionsRight.add(formatBox = new JComboBox<>(new String[]{I18.get("print_land_format_0"), I18.get("print_land_format_1"), I18.get("print_land_format_2")}));
        styleOptionsLeft.add(new JLabel(I18.get("print_land_footer")));
        styleOptionsRight.add(footnote = new JTextField(I18.get("print_land_footer_text")));
        styleOptionsLeft.add(new JLabel(I18.get("print_land_date")));
        styleOptionsRight.add(dateformat = new JTextField(I18.get("print_land_date_text")));
        styleOptionsLeft.add(new JLabel(I18.get("print_land_sort")));
        String str = "";
        if ((YassSong.ordering & YassSong.SORT_BY_TITLE) != 0) {
            str = str + I18.get("print_land_sort_0");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_ARTIST) != 0) {
            str = str + I18.get("print_land_sort_1");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_COMPLETE) != 0) {
            str = str + I18.get("print_land_sort_7");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_DUETSINGER) != 0) {
            str = str + I18.get("print_land_sort_8");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_GENRE) != 0) {
            str = str + I18.get("print_land_sort_4");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_EDITION) != 0) {
            str = str + I18.get("print_land_sort_3");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_LANGUAGE) != 0) {
            str = str + I18.get("print_land_sort_2");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_YEAR) != 0) {
            str = str + I18.get("print_land_sort_5");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_FOLDER) != 0) {
            str = str + I18.get("print_land_sort_6");
        }
        if ((YassSong.ordering & YassSong.SORT_INVERSE) != 0) {
            str = str + " " + I18.get("print_land_sort_9");
        }
        styleOptionsRight.add(new Label(str));
        styleOptionsLeft.add(new JLabel(I18.get("print_land_add")));
        JPanel addwhat = new JPanel();
        addwhat.add(co = new JComboBox<Object>(new String[]{I18.get("print_land_add_0"), I18.get("print_land_add_1"), I18.get("print_land_add_2")}));
        ed = new JComboBox<Object>(new String[]{I18.get("print_land_second_0"), I18.get("print_land_second_1"), I18.get("print_land_second_2"), I18.get("print_land_second_3"), I18.get("print_land_second_4"), I18.get("print_land_second_5")});
        // quick hack
        addwhat.add(new Label());
        styleOptionsRight.add(addwhat);
        styleOptions.add("West", styleOptionsLeft);
        styleOptions.add("Center", styleOptionsRight);
        return styleOptions;
    }


    /**
     * Description of the Method
     *
     * @param options Description of the Parameter
     */
    public void commit(Hashtable<String, Object> options) {
        options.put("format", formatBox.getSelectedItem());
        options.put("dateformat", dateformat.getText());
        options.put("footnote", footnote.getText());
        options.put("co", new Integer(co.getSelectedIndex()));
        options.put("ed", new Integer(ed.getSelectedIndex()));
    }


    /**
     * Description of the Method
     *
     * @param songList Description of the Parameter
     * @param filename Description of the Parameter
     * @param options  Description of the Parameter
     * @return Description of the Return Value
     */
    @Override
    public boolean print(Vector<YassSong> songList, String filename, Hashtable<String, Object> options) {
        String imageCacheName = (String) options.get("covercache");

        //boolean landscape = ((String)options.get("orientation")).equals("Landscape");
        String format = (String) options.get("format");
        foottext = (String) options.get("footnote");
        dformat = (String) options.get("dateformat");
        int co = ((Integer) (options.get("co"))).intValue();
        int ed = ((Integer) (options.get("ed"))).intValue();
        showCovers = co == 2;

        show[0] = true;
        // no
        show[1] = co == 1;
        show[2] = true;
        // title
        show[3] = true;
        // artist
        show[4] = ed == 1;
        show[5] = ed == 2;
        show[6] = ed == 3;
        show[7] = ed == 4;
        show[8] = ed == 5;
        ncol = 0;
        for (boolean aShow : show) {
            if (aShow) {
                ncol++;
            }
        }
        if (showCovers && !show[1]) {
            ncol++;
        }

        if (showCovers) {
            Document.compress = false;
        }
        ;

        rect = PageSize.A4;
        if (format.equals(I18.get("print_land_format_1"))) {
            rect = PageSize.A5;
        }
        if (format.equals(I18.get("print_land_format_2"))) {
            rect = PageSize.LETTER;
        }
        rect.rotate();
        mleft = 30;
        mright = 30;
        mtop = 20;
        mbottom = 30;
        Document document = new Document(rect, mleft, mright, mtop, mbottom);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            writer.setPageEvent(
                    new PdfPageEventHelper() {
                        public void onEndPage(PdfWriter writer, Document document) {
                            try {
                                Rectangle page = document.getPageSize();
                                PdfPTable foot = new PdfPTable(3);
                                foot.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                                foot.getDefaultCell().setBorderWidth(0);
                                if (foottext != null && foottext.trim().length() > 0) {
                                    foot.addCell(new Phrase(new Chunk(foottext, FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
                                }
                                foot.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                                foot.addCell(new Phrase(new Chunk(writer.getPageNumber() + "", FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));

                                if (dformat != null && dformat.trim().length() > 0) {
                                    foot.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault());
                                    String DATE_FORMAT = dformat;
                                    try {
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
                                        sdf.setTimeZone(java.util.TimeZone.getDefault());
                                        String currentTime = sdf.format(cal.getTime());
                                        foot.addCell(new Phrase(new Chunk(currentTime, FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
                                    } catch (Exception e) {
                                    }
                                }
                                foot.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                                foot.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(), writer.getDirectContent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

            document.open();

            int i = 1;
            String alpha = "";
            String tmpAlpha = "";
            String lastAlpha = "$";
            boolean firstChapter = true;
            PdfPTable datatable = createPDFTable();

            MultiColumnText mct = new MultiColumnText();
            mcol = 20;
            mct.addRegularColumns(document.left(), document.right(), mcol, 2);

            for (Enumeration<YassSong> e = songList.elements(); e.hasMoreElements(); ) {
                YassSong s = e.nextElement();
                String nr = i + "";
                i++;
                String complete = s.getComplete();
                String title = s.getTitle();
                String artist = s.getSortedArtist();
                if (artist == null) {
                    artist = s.getArtist();
                }
                String language = s.getLanguage();
                String edition = s.getEdition();
                String genre = s.getGenre();
                String year = s.getYear();
                String folder = s.getFolder();

                String t = "";
                int tn = title.length();

                if (tn > 1) {
                    t = alpha + title.toLowerCase().substring(1, 2);
                } else if (tn == 1) {
                    t = title.toUpperCase().substring(0, 1);
                }

                tmpAlpha = alpha;
                alpha = title.toUpperCase().substring(0, 1);
                boolean newChapter = !alpha.equals(lastAlpha);
                lastAlpha = tmpAlpha;

                if (i % 2 == 1) {
                    datatable.getDefaultCell().setGrayFill(0.8f);
                } else {
                    datatable.getDefaultCell().setGrayFill(1);
                }

                datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                if (show[0]) {
                    datatable.addCell(new Phrase(new Chunk(nr, FontFactory.getFont(FontFactory.HELVETICA, 9, Font.PLAIN, new Color(0, 0, 0)))));
                }
                if (showCovers) {
                    String at = YassSong.toFilename(artist + " - " + title + " @ " + folder);
                    String cacheFile = imageCacheName + File.separator + at + ".jpg";
                    com.lowagie.text.Image img = null;
                    Phrase para = new Phrase();
                    try {
                        img = com.lowagie.text.Image.getInstance(cacheFile);
                        img.scalePercent(50);
                        img.setAlignment(Image.LEFT | Image.UNDERLYING);
                    } catch (Exception ignored) {
                    }
                    if (img != null) {
                        para.add(new Chunk(img, 0f, 0f));
                    }
                    datatable.addCell(para);
                } else if (show[1]) {
                    datatable.addCell(new Phrase(new Chunk(complete, FontFactory.getFont(FontFactory.HELVETICA, 9, Font.PLAIN, new Color(0, 0, 0)))));
                }

                if (show[2]) {
                    Phrase phr;
                    phr = new Phrase(new Chunk(title, FontFactory.getFont(FontFactory.HELVETICA, 9, Font.PLAIN, new Color(0, 0, 0))));
                    //if (version != null)
                    //    phr.add(new Chunk("  " + version, FontFactory.getFont(FontFactory.HELVETICA, 7, Font.ITALIC, new Color(84, 84, 84))));
                    datatable.addCell(phr);
                }
                if (show[3]) {
                    datatable.addCell(new Phrase(new Chunk(artist, FontFactory.getFont(FontFactory.HELVETICA, 9, Font.PLAIN, new Color(0, 0, 0)))));
                }
                if (show[4]) {
                    datatable.addCell(new Phrase(new Chunk(language, FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
                }
                if (show[5]) {
                    datatable.addCell(new Phrase(new Chunk(edition, FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
                }
                if (show[6]) {
                    datatable.addCell(new Phrase(new Chunk(genre, FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
                }
                if (show[7]) {
                    datatable.addCell(new Phrase(new Chunk(year, FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
                }
                if (show[8]) {
                    datatable.addCell(new Phrase(new Chunk(folder, FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
                }
            }

            mct.addElement(datatable);
            document.add(mct);
        } catch (Exception e) {
            e.printStackTrace();
        }

        document.close();
        return true;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    PdfPTable createPDFTable() {
        PdfPTable datatable = new PdfPTable(ncol);
        try {
            float twidth = rect.getRight() - mleft - mright - mcol;
            twidth = twidth / 2;

            float headerwidths[] = new float[ncol];
            int i = 0;
            if (show[0]) {
                headerwidths[i] = 40;
                twidth -= headerwidths[i];
                i++;
            }
            if (show[1] || showCovers) {
                headerwidths[i] = showCovers ? 40 : 14;
                twidth -= headerwidths[i];
                i++;
            }
            boolean more = show[4] || show[5] || show[6] || show[7];
            if (!more) {
                headerwidths[i] = showCovers ? twidth / 2 + 40 : twidth / 2 - 40;
                i++;
                headerwidths[i] = showCovers ? twidth / 2 + 40 : twidth / 2 - 40;
                i++;
            } else {
                headerwidths[i] = showCovers ? (twidth - 80) / 2 + 40 : (twidth - 80) / 2 - 40;
                i++;
                headerwidths[i] = showCovers ? (twidth - 80) / 2 + 40 : (twidth - 80) / 2 - 40;
                i++;

                if (show[4]) {
                    headerwidths[i] = 80;
                    i++;
                }
                if (show[5]) {
                    headerwidths[i] = 80;
                    i++;
                }
                if (show[6]) {
                    headerwidths[i] = 80;
                    i++;
                }
                if (show[7]) {
                    headerwidths[i] = 80;
                    i++;
                }
                if (show[8]) {
                    headerwidths[i] = 80;
                    i++;
                }
            }
            datatable.setWidths(headerwidths);
            datatable.setWidthPercentage(100);
            datatable.getDefaultCell().setPadding(3);
            datatable.getDefaultCell().setBorderWidth(0);
            datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            datatable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            datatable.getDefaultCell().setGrayFill(1);
            datatable.getDefaultCell().setUseAscender(true);

            if (show[0]) {
                datatable.addCell(new Phrase(new Chunk("", FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[1] || showCovers) {
                datatable.addCell(new Phrase(new Chunk("", FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[2]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_land_title_0"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
            }
            if (show[3]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_land_title_1"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
            }

            if (show[4]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_land_title_2"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[5]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_land_title_3"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[6]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_land_title_4"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[7]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_land_title_5"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[8]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_land_title_6"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }

            datatable.setHeaderRows(1);
            // this is the end of the table header
            datatable.getDefaultCell().setBorderWidth(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datatable;
    }
}

