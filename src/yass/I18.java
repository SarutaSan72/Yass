package yass;

import java.io.File;
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
                                .newInstance(new URL[]{new File(userdir).toURL()}));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bundle = ResourceBundle.getBundle("i18.yass", loc);
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
                return f.toURL();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        String filename = "/i18/" + lang + "/" + s;
        URL url = I18.class.getClass().getResource(filename);
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
            }
        }
        filename = "/i18/default/" + s;
        // System.out.println("i18 " + filename);
        return I18.class.getClass().getResource(filename);
    }

    /**
     * Gets the copyright attribute of the YassActions object
     *
     * @return The copyright value
     */
    public static String getCopyright(String version, String date) {
        if (lang.equals("de")) {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><body><font size=+2><u>Yass</u>&#161;</font> by Saruta<br>Version: "
                    + version + " (" + date + ")<br>mail@yass-along.com<br>");
            sb.append("<a href=\"http://www.yass-along.com\">http://www.yass-along.com</a><br><br>");

            sb.append("Yass ist Freeware. Die Verwendung ist kostenlos. <br><br>");
            sb.append("Ohne meine ausdr�ckliche Erlaubnis darf Yass nicht f�r kommerzielle Zwecke<br>");
            sb.append("genutzt werden, und nicht bearbeitet oder weiter gegeben werden.<br>");
            sb.append("Verweise auf die Webseite sind jedoch erlaubt.<br>");
            sb.append("Bitte kontaktieren Sie mich bei Bedarf.<br><br>");
            sb.append("Yass verwendet: ");
            sb.append("Java Look & Feel Graphics Repository, JavaZoom JLayer/MP3SPI/VorbisSPI<br>");
            sb.append("and Tritonus Sequencer, iText, Jazzy Spell Checker, TeX Hyphenator, JInput,<br>");
            sb.append("VFFMpeg Objects (fobs), Java Media Framework (JMF), Robert Eckstein's Wizard code.<br>");
            sb.append("Speed measure 'Inverse Duration' basiert auf Marcel Taeumels Ansatz (http://uman.sf.net).<br>");
            sb.append("Spanisch �bersetzung von Pantera.<br>");
            sb.append("Lizensen finden Sie im Hilfebereich.");
            return sb.toString();
        } else if (lang.equals("es")) {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><body><font size=+2><u>Yass</u>&#161;</font> de Saruta<br>Versi�n: "
                    + version + " (" + date + ")<br>mail@yass-along.com<br>");
            //sb.append("<html><body>Otro Sistema de Canciones M�s de Saruta<br>Versi�n: "
            //		+ version + " (" + date + ")<br>mail@yass-along.com<br>");
            sb.append("<a href=\"http://www.yass-along.com\">http://www.yass-along.com</a><br><br>");
            sb.append("Yass es freeware, puedes usarlo sin costo. <br><br>");
            sb.append("Est� prohibido comercializar, distribuir o conjuntar Yass a otros productos<br>");
            sb.append("sin mi permiso expl�cito. Sin embargo, puedes enlazar a este programa.<br>");
            sb.append("Por favor, cont�ctame para mayor informaci�n.<br><br>");
            sb.append("Yass usa: ");
            sb.append("Repositorio de Gr�ficos Java Look & Feel, JavaZoom JLayer/MP3SPI/VorbisSPI<br>");
            sb.append("y Secuenciador Tritonus, iText, Corrector Ortogr�fico Jazzy, Silabificador TeX, JInput,<br>");
            sb.append("Objetos VFFMpeg (fobs), Entorno de Trabajo Multimedia de Java (JMF), c�digo Wizard de Robert Eckstein.<br>");
            sb.append("Medici�n de velocidad 'Inverse Duration' basada en el enfoque de Marcel Taeumel (http://uman.sf.net).<br>");
            sb.append("Traducci�n al espa�ol por Pantero.<br>");
            sb.append("Las licencias se muestran en la secci�n de ayuda.");
            return sb.toString();
        }

        StringBuffer sb = new StringBuffer();
        sb.append("<html><body><font size=+2><u>Yass</u>&#161;</font> by Saruta<br>Version: "
                + version + " (" + date + ")<br>mail@yass-along.com<br>");
        sb.append("<a href=\"http://www.yass-along.com\">http://www.yass-along.com</a><br><br>");

        sb.append("Yass is freeware. You may use it at no cost. <br><br>");
        sb.append("You are not allowed to commercialize, bundle, or distribute Yass<br>");
        sb.append("without my explicit permission. You may, however, link to this software.<br>");
        sb.append("Please contact me for further information.<br><br>");
        sb.append("Yass uses: ");
        sb.append("Java Look & Feel Graphics Repository, JavaZoom JLayer/MP3SPI/VorbisSPI<br>");
        sb.append("and Tritonus Sequencer, iText, Jazzy Spell Checker, TeX Hyphenator, JInput,<br>");
        sb.append("VFFMpeg Objects (fobs), Java Media Framework (JMF), Robert Eckstein's Wizard code.<br>");
        sb.append("Speed measure 'Inverse Duration' based on Marcel Taeumel's approach (http://uman.sf.net).<br>");
        sb.append("Spanish translation by Pantera.<br>");
        sb.append("Licenses are stated in the help section.");
        return sb.toString();
    }
}
