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

package yass;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class YassLanguageUtils {

    public static final String OTHER = "Other";

    private LanguageDetector detector;

    private List<Locale> SUPPORTED_LOCALES = Arrays.asList(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH,
                                                           Locale.ITALIAN, Locale.forLanguageTag("es"),
                                                           Locale.forLanguageTag("pt"), Locale.forLanguageTag("pl"),
                                                           Locale.forLanguageTag("tr"), Locale.forLanguageTag("hu"),
                                                           Locale.CHINESE, Locale.forLanguageTag("ru"));

    public YassLanguageUtils() {
        try {
            initLanguageDetector();
        } catch (IOException e) {
            detector = null;
        }
    }

    public String detectLanguage(String lyrics) {
        String language;
        TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
        TextObject txtObject = textObjectFactory.forText(lyrics);
        Optional<LdLocale> detectedLanguage;
        if (detector != null) {
            detectedLanguage = detector.detect(txtObject);
        } else {
            return OTHER;
        }
        if (detectedLanguage.isPresent()) {
            language = Locale.forLanguageTag(detectedLanguage.get().getLanguage())
                             .getDisplayLanguage(Locale.ENGLISH);
        } else {
            language = Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH);
        }
        return language;
    }

    private void initLanguageDetector() throws IOException {
        List<LanguageProfile> languageProfiles;
        languageProfiles = new LanguageProfileReader()
                .read(Arrays.asList("en", "de", "es", "fr", "it", "pt", "pl", "tr", "zh-CN"));
        this.detector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                                               .withProfiles(languageProfiles)
                                               .build();
    }

    public String determineLanguageCode(String language) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (isLanguageSupportedLocale(language, locale)) {
                return locale.getLanguage().toUpperCase();
            }
        }
        return Locale.ENGLISH.getLanguage().toUpperCase();
    }

    private boolean isLanguageSupportedLocale(String language, Locale locale) {
        for (Locale supportedLocale : SUPPORTED_LOCALES) {
            if (language != null && language.equals(locale.getDisplayLanguage(supportedLocale))) {
                return true;
            }
        }
        return false;
    }

}
