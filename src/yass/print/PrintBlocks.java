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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import yass.I18;
import yass.YassSong;

import javax.swing.*;
import java.awt.*;
import java.awt.Font;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class PrintBlocks implements PrintPlugin {
    String foottext = "", dformat = null;
    Rectangle rect;
    int ncol = 0;
    int mleft, mright, mtop, mbottom, mcol;
    boolean show[] = new boolean[9], showCovers;
    JTextField footnote, dateformat;
    JCheckBox co = null, index = null, nr = null, downscale = null;
    JComboBox<?> ed = null, order = null, ch = null;
    JComboBox<?> formatBox = null;
    float titleIndent = 0;
    float titlecolw = 0;

    int fsSep = 8, fsHead = 8, fsBody = 7, fsNote = 7, fsFoot = 7, fsVersion = 5;
    int widthNo = 30, widthNote = 100;
    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    float headerwidths[] = null;

    /**
     * Gets the title attribute of the PrintBlocks object
     *
     * @return The title value
     */
    public String getTitle() {
        return I18.get("print_block_title");
    }

    /**
     * Gets the description attribute of the PrintBlocks object
     *
     * @return The description value
     */
    public String getDescription() {
        return I18.get("print_block_description");
    }

    /**
     * Gets the control attribute of the PrintBlocks object
     *
     * @return The control value
     */
    public JPanel getControl() {
        JPanel styleOptions = new JPanel(new BorderLayout());
        styleOptions.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JPanel styleOptionsLeft = new JPanel(new GridLayout(0, 1));
        JPanel styleOptionsRight = new JPanel(new GridLayout(0, 1));
        styleOptionsLeft.add(new JLabel(I18.get("print_block_format")));
        styleOptionsRight.add(formatBox = new JComboBox<Object>(new String[]{I18.get("print_block_format_0"), I18.get("print_block_format_1"), I18.get("print_block_format_2")}));
        styleOptionsLeft.add(new JLabel(I18.get("print_block_footer")));
        styleOptionsRight.add(footnote = new JTextField(I18.get("print_block_footer_text")));
        styleOptionsLeft.add(new JLabel(I18.get("print_block_date")));
        styleOptionsRight.add(dateformat = new JTextField(I18.get("print_block_date_text")));
        styleOptionsLeft.add(new JLabel(I18.get("print_block_group")));
        styleOptionsRight.add(order = new JComboBox<Object>(new String[]{I18.get("print_block_group_0"), I18.get("print_block_group_1"), I18.get("print_block_group_2"), I18.get("print_block_group_3"), I18.get("print_block_group_4"), I18.get("print_block_group_5")}));
        order.setSelectedIndex(0);
        styleOptionsLeft.add(new JLabel(I18.get("print_block_add")));

        JPanel addwhat = new JPanel(new GridLayout(1, 0));
        addwhat.add(index = new JCheckBox(I18.get("print_block_add_0")));
        index.setSelected(true);
        nr = new JCheckBox(I18.get("print_block_add_1"));
        //addwhat.add(nr);
        //nr.setSelected(false);
        addwhat.add(co = new JCheckBox(I18.get("print_block_add_2")));
        co.setSelected(true);
        styleOptionsRight.add(addwhat);

        styleOptionsLeft.add(new JLabel(""));
        addwhat = new JPanel(new GridLayout(1, 0));
        //addwhat.add(new JLabel(""));
        addwhat.add(new JLabel(I18.get("print_block_second")));
        addwhat.add(ed = new JComboBox<Object>(new String[]{I18.get("print_block_second_0"), I18.get("print_block_second_1"), I18.get("print_block_second_2"), I18.get("print_block_second_3"), I18.get("print_block_second_4"), I18.get("print_block_second_5"), I18.get("print_block_second_6")}));
        ed.setSelectedIndex(6);
        styleOptionsRight.add(addwhat);

        styleOptionsLeft.add(new JLabel(I18.get("print_block_font")));
        styleOptionsRight.add(downscale = new JCheckBox(I18.get("print_block_scale")));
        downscale.setSelected(true);

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
        options.put("nr", Boolean.valueOf(nr.isSelected()));
        options.put("co", Boolean.valueOf(co.isSelected()));
        options.put("chapters", Boolean.valueOf(index.isSelected()));
        options.put("ed", new Integer(ed.getSelectedIndex()));
        options.put("order", new Integer(order.getSelectedIndex()));
        options.put("downscale", Boolean.valueOf(downscale.isSelected()));
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

        String format = (String) options.get("format");
        foottext = (String) options.get("footnote");
        dformat = (String) options.get("dateformat");
        boolean showComplete = ((Boolean) (options.get("co"))).booleanValue();
        boolean showChapters = ((Boolean) (options.get("chapters"))).booleanValue();
        boolean showNr = ((Boolean) (options.get("nr"))).booleanValue();
        int ed = ((Integer) (options.get("ed"))).intValue();
        int order = ((Integer) (options.get("order"))).intValue();
        boolean scaleItems = ((Boolean) (options.get("downscale"))).booleanValue();

        show[0] = showNr;
        // no
        show[1] = false;
        show[2] = true;
        // title
        show[3] = ed == 1;
        //
        show[4] = ed == 2;
        show[5] = ed == 3;
        show[6] = ed == 4;
        show[7] = ed == 5;
        show[8] = ed == 6;
        ncol = 0;
        for (boolean aShow : show) {
            if (aShow) {
                ncol++;
            }
        }

        rect = PageSize.A4;
        if (format.equals(I18.get("print_block_format_1"))) {
            rect = PageSize.A5;
        }
        if (format.equals(I18.get("print_block_format_2"))) {
            rect = PageSize.LETTER;
        }
        mleft = 30;
        mright = 30;
        mtop = 30;
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
                                    foot.addCell(new Phrase(new Chunk(foottext, FontFactory.getFont(FontFactory.HELVETICA, fsFoot, Font.PLAIN, new Color(100, 100, 100)))));
                                }
                                foot.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                                int n = writer.getPageNumber();
                                foot.addCell(new Phrase(new Chunk(n + "", FontFactory.getFont(FontFactory.HELVETICA, fsFoot, Font.PLAIN, new Color(100, 100, 100)))));

                                if (dformat != null && dformat.trim().length() > 0) {
                                    foot.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getDefault());
                                    String DATE_FORMAT = dformat;
                                    try {
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
                                        sdf.setTimeZone(java.util.TimeZone.getDefault());
                                        String currentTime = sdf.format(cal.getTime());
                                        foot.addCell(new Phrase(new Chunk(currentTime, FontFactory.getFont(FontFactory.HELVETICA, fsFoot, Font.PLAIN, new Color(100, 100, 100)))));
                                    } catch (Exception ignored) {
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

            MultiColumnText mct = new MultiColumnText();
            mcol = 20;
            mct.addRegularColumns(document.left(), document.right(), mcol, 2);

            int ordering = YassSong.ordering;
            switch (order) {
                case 0:
                    YassSong.ordering = YassSong.SORT_BY_ARTIST;
                    break;
                case 1:
                    YassSong.ordering = YassSong.SORT_BY_LANGUAGE;
                    break;
                case 2:
                    YassSong.ordering = YassSong.SORT_BY_EDITION;
                    break;
                case 3:
                    YassSong.ordering = YassSong.SORT_BY_GENRE;
                    break;
                case 4:
                    YassSong.ordering = YassSong.SORT_BY_YEAR;
                    break;
                case 5:
                    YassSong.ordering = YassSong.SORT_BY_FOLDER;
                    break;
            }
            Collections.sort(songList);
            YassSong.ordering = ordering;

            PdfPTable datatable = createPDFTable();
            datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            Paragraph para;

            String videoString = I18.get("print_block_video");

            String lastWhat = "";
            String lastSection = " ";
            for (Enumeration<?> e = songList.elements(); e.hasMoreElements(); ) {
                YassSong s = (YassSong) e.nextElement();
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

                String orderWhat = title;
                switch (order) {
                    case 0:
                        orderWhat = artist;
                        break;
                    case 1:
                        orderWhat = language;
                        break;
                    case 2:
                        orderWhat = edition;
                        break;
                    case 3:
                        orderWhat = genre;
                        break;
                    case 4:
                        orderWhat = year;
                        break;
                    case 5:
                        orderWhat = folder;
                        break;
                }

                boolean newChapter = !orderWhat.toLowerCase().equals(lastWhat);

                if (newChapter) {
                    String newSection = "";
                    if (orderWhat.length() > 0) {
                        newSection = orderWhat.toUpperCase().substring(0, 1).toUpperCase();
                    }
                    if (newSection.equals("�")) {
                        newSection = "A";
                    }
                    if (newSection.equals("�")) {
                        newSection = "�";
                    }
                    if (newSection.equals("�")) {
                        newSection = "�";
                    }

                    boolean differentStart = !newSection.startsWith(lastSection);

                    if (showChapters && orderWhat.length() > 0 && differentStart) {
                        lastSection = newSection;

                        if (lastSection.compareTo("A") >= 0) {
                            if (show[0]) {
                                datatable.addCell("");
                            }
                            datatable.addCell("");
                            if (show[3] || show[4] || show[5] || show[6] || show[7] || show[8]) {
                                datatable.addCell("");
                            }

                            if (show[0]) {
                                datatable.addCell("");
                            }
                            datatable.addCell("");
                            if (show[3] || show[4] || show[5] || show[6] || show[7] || show[8]) {
                                datatable.addCell("");
                            }

                            datatable.getDefaultCell().setGrayFill(.8f);
                            if (show[0]) {
                                datatable.addCell("");
                            }
                            para = new Paragraph(lastSection, FontFactory.getFont(FontFactory.HELVETICA, fsSep, Font.BOLD, new Color(0, 0, 0)));
                            datatable.addCell(para);
                            if (show[3] || show[4] || show[5] || show[6] || show[7] || show[8]) {
                                datatable.addCell("");
                            }
                            datatable.getDefaultCell().setGrayFill(1);

                            if (show[0]) {
                                datatable.addCell("");
                            }
                            datatable.addCell("");
                            if (show[3] || show[4] || show[5] || show[6] || show[7] || show[8]) {
                                datatable.addCell("");
                            }
                        }
                    }
                    para = new Paragraph(orderWhat, FontFactory.getFont(FontFactory.HELVETICA, fsHead, Font.PLAIN, new Color(0, 0, 0)));
                    //para.setSpacingBefore(6);
                    //para.setSpacingAfter(3);
                    lastWhat = orderWhat.toLowerCase();

                    if (show[0]) {
                        datatable.addCell("");
                    }

                    PdfPCell cell = new PdfPCell(para);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setBorderWidth(0);
                    //cell.setNoWrap(true);
                    if (show[3] || show[4] || show[5] || show[6] || show[7] || show[8]) {
                        cell.setColspan(2);
                    }

                    cell.setPaddingTop(3);
                    cell.setPaddingBottom(3);
                    cell.setPaddingLeft(0);
                    cell.setPaddingRight(0);
                    datatable.addCell(cell);
                }

                if (show[0]) {
                    datatable.addCell(new Phrase(new Chunk(nr, FontFactory.getFont(FontFactory.HELVETICA, fsNote, Font.PLAIN, new Color(0, 0, 0)))));
                }

				/*
                 *  BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
				 *  float stringWidth = helvetica.getWidthPoint(titlev, 9);
				 *  while (titlecolw < stringWidth) {
				 *  titlev = titlev.substring(0, titlev.length() - 1);
				 *  stringWidth = helvetica.getWidthPoint(titlev, 9);
				 *  }
				 *  System.out.println(stringWidth);
				 */
                Chunk ch;

				/*
				 *  BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
				 *  float stringWidth = helvetica.getWidthPoint(titlev, 9);
				 *  while (titlecolw < stringWidth) {
				 *  titlev = titlev.substring(0, titlev.length() - 1);
				 *  stringWidth = helvetica.getWidthPoint(titlev, 9);
				 *  }
				 *  System.out.println(stringWidth);
				 */
                Chunk ch2 = null;

				/*
				 *  BaseFont helvetica = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
				 *  float stringWidth = helvetica.getWidthPoint(titlev, 9);
				 *  while (titlecolw < stringWidth) {
				 *  titlev = titlev.substring(0, titlev.length() - 1);
				 *  stringWidth = helvetica.getWidthPoint(titlev, 9);
				 *  }
				 *  System.out.println(stringWidth);
				 */
                Chunk ch3 = null;

                Phrase phr = new Phrase(ch = new Chunk(title, FontFactory.getFont(FontFactory.HELVETICA, fsBody, Font.PLAIN, new Color(0, 0, 0))));
                //if (version != null) {
                //    phr.add(ch2 = new Chunk("  " + version, FontFactory.getFont(FontFactory.HELVETICA, fsVersion, Font.ITALIC, new Color(84, 84, 84))));
                String hasvideo = (showComplete && complete.equals("V")) ? videoString : null;
                if (hasvideo != null) {
                    phr.add(ch3 = new Chunk("  " + hasvideo, FontFactory.getFont(FontFactory.HELVETICA, fsVersion, Font.ITALIC, new Color(84, 84, 84))));
                }

                boolean nowrap = false;
                if (scaleItems) {
                    nowrap = true;
                    int size = fsBody;
                    int size2 = fsVersion;
                    float ww = ch.getWidthPoint();
                    if (ch2 != null) {
                        ww += ch2.getWidthPoint();
                    }
                    if (ch3 != null) {
                        ww += ch3.getWidthPoint();
                    }
                    if (ww >= headerwidths[show[0] ? 1 : 0] - 10) {
                        Phrase phr2 = new Phrase(ch = new Chunk(title, FontFactory.getFont(FontFactory.HELVETICA, --size, Font.PLAIN, new Color(0, 0, 0))));
                        //if (version != null) {
                        //    phr2.add(ch2 = new Chunk("  " + version, FontFactory.getFont(FontFactory.HELVETICA, --size2, Font.ITALIC, new Color(84, 84, 84))));
                        if (hasvideo != null) {
                            phr2.add(ch3 = new Chunk("  " + hasvideo, FontFactory.getFont(FontFactory.HELVETICA, size2, Font.ITALIC, new Color(84, 84, 84))));
                        }
                        ww = ch.getWidthPoint();
                        if (ch2 != null) {
                            ww += ch2.getWidthPoint();
                        }
                        if (ch3 != null) {
                            ww += ch3.getWidthPoint();
                        }
                        if (ww < headerwidths[show[0] ? 1 : 0] - 10) {
                            phr = phr2;
                        } else {
                            nowrap = false;
                        }
                    }
                }

                PdfPCell cell = new PdfPCell(phr);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorderWidth(0);
                cell.setNoWrap(nowrap);
                cell.setPaddingTop(1);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(10);
                cell.setPaddingRight(0);
                datatable.addCell(cell);

                phr = null;
                String chs = null;
                if (show[3]) {
                    chs = artist;
                }
                if (show[4]) {
                    chs = language;
                }
                if (show[5]) {
                    chs = edition;
                }
                if (show[6]) {
                    chs = genre;
                }
                if (show[7]) {
                    chs = year;
                }
                if (show[8]) {
                    chs = folder;
                }

                nowrap = false;
                if (chs != null) {
                    int size = fsNote;
                    phr = new Phrase(ch = new Chunk(chs, FontFactory.getFont(FontFactory.HELVETICA, fsNote, Font.PLAIN, new Color(0, 0, 0))));
                    //System.out.println(artist+" - "+title+"  "+ch.getWidthPoint()+" "+headerwidths[1]);

                    if (scaleItems) {
                        nowrap = true;
                        if (ch.getWidthPoint() >= headerwidths[show[0] ? 2 : 1]) {
                            Phrase phr2 = new Phrase(ch = new Chunk(chs, FontFactory.getFont(FontFactory.HELVETICA, --size, Font.PLAIN, new Color(0, 0, 0))));
                            if (ch.getWidthPoint() < headerwidths[show[0] ? 2 : 1]) {
                                phr = phr2;
                            } else {
                                nowrap = false;
                            }
                        }
                    }
                }
                if (phr != null) {
                    cell = new PdfPCell(phr);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setBorderWidth(0);
                    cell.setNoWrap(nowrap);
                    cell.setPaddingTop(1);
                    cell.setPaddingBottom(3);
                    cell.setPaddingLeft(0);
                    cell.setPaddingRight(0);
                    datatable.addCell(cell);
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

    PdfPTable createPDFTable() {
        PdfPTable datatable = new PdfPTable(ncol);
        try {
            float twidth = rect.getRight() - mleft - mright;
            //-mcol;
            twidth /= 2;

            headerwidths = new float[ncol];

            int i = 0;
            if (show[0]) {
                headerwidths[i] = widthNo;
                twidth -= headerwidths[i];
                i++;
            }

            if (show[1] || showCovers) {
                headerwidths[i] = showCovers ? 30 : 14;
                twidth -= headerwidths[i];
                i++;
            }

            boolean more = show[3] || show[4] || show[5] || show[6] || show[7] || show[8];
            if (!more) {
                headerwidths[i] = twidth;
                titlecolw = twidth;
                i++;
            } else {
                headerwidths[i] = twidth - widthNote;
                titlecolw = twidth - widthNote;
                i++;

                if (show[3]) {
                    headerwidths[i] = widthNote;
                    i++;
                }
                if (show[4]) {
                    headerwidths[i] = widthNote;
                    i++;
                }
                if (show[5]) {
                    headerwidths[i] = widthNote;
                    i++;
                }
                if (show[6]) {
                    headerwidths[i] = widthNote;
                    i++;
                }
                if (show[7]) {
                    headerwidths[i] = widthNote;
                    i++;
                }
                if (show[8]) {
                    headerwidths[i] = widthNote;
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
            datatable.getDefaultCell().setBorderWidth(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datatable;
    }
}

