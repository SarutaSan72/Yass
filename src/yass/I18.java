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

import com.sun.xml.internal.ws.api.ResourceLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class I18 {

    /**
     * Description of the Field
     */
    public static ResourceBundle bundle = null;
    private static String lang = "en";

    private static String userdir = System.getProperty("user.home")
            + File.separator + ".yass" + File.separator + "i18";

    /**
     * Description of the Method
     *
     * @param key Description of the Parameter
     * @return Description of the Return Value
     */
    public static String get(String key) {
        return bundle.getString(key);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public static String getLanguage() {
        return lang;
    }

    /**
     * Sets the language attribute of the YassMain class
     *
     * @param s The new language value
     */
    public static void setLanguage(String s) {
        if (s == null) {
            s = "en";
        }
        lang = s;
        Locale loc = new Locale(lang);

        if (new File(userdir).exists()) {
            try {
                bundle = ResourceBundle.getBundle("yass", loc,
                        java.net.URLClassLoader
                                .newInstance(new URL[]{new File(userdir).toURI().toURL()}));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bundle = ResourceBundle.getBundle("yass.resources.i18.yass", loc);
        }
    }

    /**
     * Gets the languageFolder attribute of the I18 class
     *
     * @param s Description of the Parameter
     * @return The languageFolder value
     */
    public static URL getResource(String s) {
        if (new File(userdir).exists()) {
            File f = new File(userdir + File.separator + lang + File.separator
                    + s);
            try {
                return f.toURI().toURL();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        String filename = "/yass/resources/i18/" + lang + "/" + s;
        URL url = I18.class.getResource(filename);
        java.net.URLConnection uc = null;
        try {
            uc = url.openConnection();
            if (uc.getContentLength() > 0) {
                // System.out.println("i18 " + filename);
                return url;
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            try {
                if (uc != null) {
                    uc.getOutputStream().close();
                    uc.getInputStream().close();
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        filename = "/yass/resources/i18/default/" + s;
        // System.out.println("i18 " + filename);
        return I18.class.getResource(filename);
    }

    public static InputStream getResourceAsStream(String s) {
        if (new File(userdir).exists()) {
            File f = new File(userdir + File.separator + lang + File.separator
                    + s);
            try {
                return new FileInputStream(f);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        String filename = "/yass/resources/i18/" + lang + "/" + s;
        InputStream is = I18.class.getResourceAsStream(filename);
        if (is != null) return is;

        filename = "/yass/resources/i18/default/" + s;
        // System.out.println("i18 " + filename);
        is = I18.class.getResourceAsStream(filename);
        if (is != null) return is;

        System.out.println("not found: " + s);
        return null;
    }

    public static Image getImage(String s) {
        if (new File(userdir).exists()) {
            File f = new File(userdir + File.separator + lang + File.separator + s);
            try {
                //System.out.println("reading " + f.getAbsolutePath());
                return ImageIO.read(f);
            } catch (Exception e) {}
        }

        String filename = "/yass/resources/i18/" + lang + "/" + s;
        InputStream is = I18.class.getResourceAsStream(filename);
        if (is != null) {
            try {
                //System.out.println("reading " + filename);
                return ImageIO.read(is);
            } catch (Exception e) {
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        filename = "/yass/resources/i18/default/" + s;
        is = I18.class.getResourceAsStream(filename);
        if (is != null) {
            try {
                //System.out.println("reading " + filename);
                return ImageIO.read(is);
            } catch (Exception e) {
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        System.out.println("not found: " + s);
        return null;
    }

    /**
     * Gets the copyright attribute of the YassActions object
     *
     * @return The copyright value
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static String getCopyright(String version, String date) {
        /*
        if (lang.equals("de")) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body><font size=+2><u>Yass</u>&#161;</font> by Saruta<br>Version: "
                    + version + " (" + date + ")<br>mail@yass-along.com<br>");
            sb.append("<a href=\"http://www.yass-along.com\">http://www.yass-along.com</a><br><br>");

            sb.append("Yass ist Freeware. Die Verwendung ist kostenlos. <br><br>");
            sb.append("Ohne meine ausdrückliche Erlaubnis darf Yass nicht für kommerzielle Zwecke<br>");
            sb.append("genutzt werden, und nicht bearbeitet oder weiter gegeben werden.<br>");
            sb.append("Verweise auf die Webseite sind jedoch erlaubt.<br>");
            sb.append("Bitte kontaktieren Sie mich bei Bedarf.<br><br>");
            sb.append("Yass verwendet: ");
            sb.append("Java Look & Feel Graphics Repository, JavaZoom JLayer/MP3SPI/VorbisSPI<br>");
            sb.append("and Tritonus Sequencer, iText, Jazzy Spell Checker, TeX Hyphenator, JInput,<br>");
            sb.append("VFFMpeg Objects (fobs), Java Media Framework (JMF), Robert Eckstein's Wizard code.<br>");
            sb.append("Speed measure 'Inverse Duration' basiert auf Marcel Taeumels Ansatz (http://uman.sf.net).<br>");
            sb.append("Spanische Übersetzung von Pantera.<br>");
            sb.append("Ungarische Übersetzung von Skyli.<br>");
            sb.append("Lizensen finden Sie im Hilfebereich.");
            return sb.toString();
        } else if (lang.equals("es")) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><body><font size=+2><u>Yass</u>&#161;</font> de Saruta<br>Versión: "
                    + version + " (" + date + ")<br>mail@yass-along.com<br>");
            sb.append("<a href=\"http://www.yass-along.com\">http://www.yass-along.com</a><br><br>");
            sb.append("Yass es freeware, puedes usarlo sin costo. <br><br>");
            sb.append("Está prohibido comercializar, distribuir o conjuntar Yass a otros productos<br>");
            sb.append("sin mi permiso explícito. Sin embargo, puedes enlazar a este programa.<br>");
            sb.append("Por favor, contáctame para mayor información.<br><br>");
            sb.append("Yass usa: ");
            sb.append("Repositorio de Gráficos Java Look & Feel, JavaZoom JLayer/MP3SPI/VorbisSPI<br>");
            sb.append("y Secuenciador Tritonus, iText, Corrector Ortográfico Jazzy, Silabificador TeX, JInput,<br>");
            sb.append("Objetos VFFMpeg (fobs), Entorno de Trabajo Multimedia de Java (JMF), código Wizard de Robert Eckstein.<br>");
            sb.append("Medición de velocidad 'Inverse Duration' basada en el enfoque de Marcel Taeumel (http://uman.sf.net).<br>");
            sb.append("Traducción al español por Pantero.<br>");
            sb.append("Traducción al húngaro por Skyli.<br>");
            sb.append("Las licencias se muestran en la sección de ayuda.");
            return sb.toString();
        }
        */

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><font size=+2><u>Yass</u>&#161;</font> by Saruta<br>Version: " + version + " (" + date + ")<br>");
        sb.append("mail@yass-along.com<br>");
        sb.append("<a href=\"http://yass-along.com\">http://yass-along.com</a><br>");
        sb.append("<br>");
        sb.append("Copyright (C) 2014 Saruta<br>");
        sb.append("This program is free software: you can redistribute it and/or modify<br>");
        sb.append("it under the terms of the GNU General Public License as published by<br>");
        sb.append("the Free Software Foundation, either version 3 of the License, or<br>");
        sb.append("(at your option) any later version.<br>");
        sb.append("<br>");
        sb.append("This program is distributed in the hope that it will be useful,<br>");
        sb.append("but WITHOUT ANY WARRANTY; without even the implied warranty of<br>");
        sb.append("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>");
        sb.append("GNU General Public License for more details.<br>");
        sb.append("<br>");
        sb.append("You should have received a copy of the GNU General Public License<br>");
        sb.append("along with this program. If not, see <a href=\"http://www.gnu.org/licenses/\"><http://www.gnu.org/licenses/></a>.<br>");
        sb.append("<br>");
        sb.append("Yass uses: ");
        sb.append("Java Look & Feel Graphics Repository, JavaZoom JLayer/MP3SPI/VorbisSPI<br>");
        sb.append("and Tritonus Sequencer, iText, Jazzy Spell Checker, TeX Hyphenator, JInput,<br>");
        sb.append("VFFMpeg Objects (fobs), Java Media Framework (JMF), Robert Eckstein's Wizard code.<br>");
        sb.append("Speed measure 'Inverse Duration' based on Marcel Taeumel's approach (http://uman.sf.net).<br>");
        sb.append("Spanish translation by Pantera.<br>");
        sb.append("Hungarian translation by Skyli.<br>");
        sb.append("Licenses are stated in the help section.");
        return sb.toString();
    }
}
