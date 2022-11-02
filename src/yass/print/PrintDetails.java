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
public class PrintDetails implements PrintPlugin {
    String foottext = "", dformat = null;
    Rectangle rect;
    int ncol = 0, pageColNum;
    int mleft, mright, mtop, mbottom, mcol;
    boolean show[] = new boolean[11], showCovers;
    JTextField footnote, dateformat;
    JCheckBox vb = null, co = null;
    JComboBox<String> formatBox;


    /**
     * Gets the title attribute of the PrintDetails object
     *
     * @return The title value
     */
    public String getTitle() {
        return I18.get("print_details_title");
    }


    /**
     * Gets the description attribute of the PrintDetails object
     *
     * @return The description value
     */
    public String getDescription() {
        return I18.get("print_details_description");
    }


    /**
     * Gets the control attribute of the PrintDetails object
     *
     * @return The control value
     */
    public JPanel getControl() {
        JPanel styleOptions = new JPanel(new BorderLayout());
        styleOptions.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JPanel styleOptionsLeft = new JPanel(new GridLayout(0, 1));
        JPanel styleOptionsRight = new JPanel(new GridLayout(0, 1));
        styleOptionsLeft.add(new JLabel(I18.get("print_details_format")));
        styleOptionsRight.add(formatBox = new JComboBox<>(new String[]{I18.get("print_details_format_0"), I18.get("print_details_format_1"), I18.get("print_details_format_2")}));
        styleOptionsLeft.add(new JLabel(I18.get("print_details_footer")));
        styleOptionsRight.add(footnote = new JTextField(I18.get("print_details_footer_text")));
        styleOptionsLeft.add(new JLabel(I18.get("print_details_date")));
        styleOptionsRight.add(dateformat = new JTextField(I18.get("print_details_date_text")));
        styleOptionsLeft.add(new JLabel(I18.get("print_details_sort")));
        String str = "";
        if ((YassSong.ordering & YassSong.SORT_BY_TITLE) != 0) {
            str = str + I18.get("print_details_sort_0");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_ARTIST) != 0) {
            str = str + I18.get("print_details_sort_1");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_COMPLETE) != 0) {
            str = str + I18.get("print_details_sort_2");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_DUETSINGER) != 0) {
            str = str + I18.get("print_details_sort_3");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_GENRE) != 0) {
            str = str + I18.get("print_details_sort_4");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_EDITION) != 0) {
            str = str + I18.get("print_details_sort_5");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_LANGUAGE) != 0) {
            str = str + I18.get("print_details_sort_6");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_YEAR) != 0) {
            str = str + I18.get("print_details_sort_7");
        }
        if ((YassSong.ordering & YassSong.SORT_BY_FOLDER) != 0) {
            str = str + I18.get("print_details_sort_8");
        }
        if ((YassSong.ordering & YassSong.SORT_INVERSE) != 0) {
            str = str + " " + I18.get("print_details_sort_9");
        }
        styleOptionsRight.add(new Label(str));
        styleOptionsLeft.add(new JLabel(I18.get("print_details_add")));
        JPanel addwhat = new JPanel();
        addwhat.add(vb = new JCheckBox(I18.get("print_details_add_0")));
        addwhat.add(co = new JCheckBox(I18.get("print_details_add_1")));
        styleOptionsRight.add(addwhat);
        vb.setSelected(true);
        vb.setToolTipText(I18.get("print_details_add_2"));
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
        options.put("vb", new Boolean(vb.isSelected()));
        options.put("co", new Boolean(co.isSelected()));
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
        Boolean vb = (Boolean) options.get("vb");
        Boolean co = (Boolean) options.get("co");
        Integer type = new Integer(2);
        boolean showDetails = type.intValue() == 2;
        boolean landscape = type.intValue() != 0;
        pageColNum = (type.intValue() == 1) ? 2 : 1;
        showCovers = co.booleanValue();

        show[0] = true;
        // no
        show[1] = vb.booleanValue();
        show[2] = true;
        // title
        show[3] = true;
        // artist
        show[4] = showDetails;
        show[5] = showDetails;
        show[6] = showDetails;
        show[7] = showDetails;
        show[8] = showDetails;
        show[9] = false;
        show[10] = showCovers;
        // cover
        ncol = 0;
        for (boolean aShow : show) {
            if (aShow) {
                ncol++;
            }
        }
        if (show[1] && show[10]) {
            ncol--;
        }

        if (showCovers) {
            Document.compress = false;
        }
        ;

        //Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        rect = PageSize.A4;
        if (format.equals(I18.get("print_details_format_1"))) {
            rect = PageSize.A5;
        }
        if (format.equals(I18.get("print_details_format_2"))) {
            rect = PageSize.LETTER;
        }
        if (landscape) {
            rect = rect.rotate();
        }
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

							/*
                             *  String abc="A";
							 *  PdfPTable head = new PdfPTable(2);
							 *  head.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
							 *  head.getDefaultCell().setBorderWidth(0);
							 *  head.addCell(new Phrase(new Chunk("Titles A-Z", FontFactory.getFont(FontFactory.HELVETICA, 16, Font.PLAIN, new Color(100, 100, 100)))));
							 *  head.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
							 *  head.addCell(new Phrase(new Chunk(abc, FontFactory.getFont(FontFactory.HELVETICA, 16, Font.PLAIN, new Color(100, 100, 100)))));
							 *  head.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
							 *  head.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - document.topMargin() + head.getTotalHeight(), writer.getDirectContent());
							 */
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

            document.open();

            MultiColumnText mct = new MultiColumnText();
            mcol = 20;
            if (pageColNum == 2) {
                mct.addRegularColumns(document.left(), document.right(), mcol, 2);
            } else {
                mct.addRegularColumns(document.left(), document.right(), 0, 1);
            }

            int i = 1;
            String alpha = "";
            String tmpAlpha = "";
            String lastAlpha = "$";
            boolean firstChapter = true;
            PdfPTable datatable = createPDFTable();
            //Paragraph para = new Paragraph("0-9", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, new Color(0, 0, 0)));
            //Chapter chapter = new Chapter(para,1);
            //chapter.setNumberDepth(0);
            // bug: cannot add chapter to multicolumntext;

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

				/*
				 *  if (firstChapter) {
				 *  if (alpha.startsWith("A")) {
				 *  firstChapter = false;
				 *  mct.addElement(para);
				 *  mct.addElement(datatable);
				 *  datatable = createPDFTable();
				 *  para = new Paragraph(alpha, FontFactory.getFont(FontFactory.HELVETICA, 12, Font.PLAIN, new Color(0, 0, 0)));
				 *  chapter = new Chapter(para,1);
				 *  chapter.setNumberDepth(0);
				 *  }
				 *  }
				 *  else if (newChapter) {
				 *  mct.addElement(para);
				 *  mct.addElement(datatable);
				 *  datatable = createPDFTable();
				 *  para = new Paragraph(alpha, FontFactory.getFont(FontFactory.HELVETICA, 12, Font.PLAIN, new Color(0, 0, 0)));
				 *  chapter = new Chapter(para,1);
				 *  chapter.setNumberDepth(0);
				 *  }
				 */
                if (i % 2 == 1) {
                    datatable.getDefaultCell().setGrayFill(0.8f);
                } else {
                    datatable.getDefaultCell().setGrayFill(1);
                }

                datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                if (show[0]) {
                    datatable.addCell(new Phrase(new Chunk(nr, FontFactory.getFont(FontFactory.HELVETICA, 9, Font.PLAIN, new Color(0, 0, 0)))));
                }
                if (show[10]) {
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

            //mct.addElement(para);
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
            float twidth = rect.getRight() - mleft - mright;
            if (pageColNum == 2) {
                twidth -= mcol;
                twidth /= 2;
            }

            float headerwidths[] = new float[ncol];
            int i = 0;
            if (show[0]) {
                headerwidths[i] = pageColNum == 2 ? 40 : 30;
                twidth -= headerwidths[i];
                i++;
            }
            if (show[1] || show[10]) {
                if (show[10]) {
                    headerwidths[i] = pageColNum == 2 ? 40 : 30;
                } else {
                    headerwidths[i] = pageColNum == 2 ? 20 : 14;
                }
                twidth -= headerwidths[i];
                i++;
            }
            if (show[2]) {
                headerwidths[i] = showCovers ? 220 : 200;
                twidth -= headerwidths[i];
                i++;
            }
            if (show[3]) {
                headerwidths[i] = showCovers ? 180 : 200;
                twidth -= headerwidths[i];
                i++;
            }
            if (show[4]) {
                headerwidths[i] = (twidth * 1 / 7);
                i++;
            }
            if (show[5]) {
                headerwidths[i] = (twidth * 2 / 7);
                i++;
            }
            if (show[6]) {
                headerwidths[i] = (twidth * 1 / 7);
                i++;
            }
            if (show[7]) {
                headerwidths[i] = (twidth * 1 / 7);
                i++;
            }
            if (show[8]) {
                headerwidths[i] = (twidth * 2 / 7);
                i++;
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
            if (show[1] || show[10]) {
                datatable.addCell(new Phrase(new Chunk("", FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[2]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_details_title_0"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
            }
            if (show[3]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_details_title_1"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(0, 0, 0)))));
            }

            if (show[4]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_details_title_6"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[5]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_details_title_5"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[6]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_details_title_4"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[7]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_details_title_7"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
            }
            if (show[8]) {
                datatable.addCell(new Phrase(new Chunk(I18.get("print_details_title_8"), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.PLAIN, new Color(100, 100, 100)))));
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

