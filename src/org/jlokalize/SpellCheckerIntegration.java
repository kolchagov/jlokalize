/*
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jlokalize;

import com.inet.jortho.SpellChecker;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JTextArea;

/**
 * Connection to the JOrtho code. Handles availability of dictionaries and
 * registering/unregistering on the components.
 * 
 * @author Trilarion 2011
 */
public class SpellCheckerIntegration {
    
    /** Number of available dictionaries */
    public static int numAvailable = 0;        
    
    /**
     * Avoid instantiation
     */
    private SpellCheckerIntegration() {}    
    
    /**
     * Unregisters the spell checker from two text areas.
     * 
     * @param c1 Text area 1.
     * @param c2 Text area 2.
     */
    public static void unregisterComponents(JTextArea c1, JTextArea c2) {
        SpellChecker.unregister(c1);
        SpellChecker.unregister(c2);
    }    

    /**
     * Registers the spell checker on two text areas.
     * 
     * @param c1 Text area 1.
     * @param c2 Text area 2.
     */
    public static void registerComponents(JTextArea c1, JTextArea c2) {
        SpellChecker.register(c1);
        SpellChecker.register(c2);
    }
    
    /**
     * Registers the Dictionaries. First calculate which ones are available, then
     * register them.
     */
    public static void registerDictionaries() {
        String names = checkDictionariesAvailable();
        SpellChecker.registerDictionaries(Main.class.getProtectionDomain().getCodeSource().getLocation(), names, null);
        SpellChecker.getOptions().setLanguageDisableVisible(true);
    }
    
    /**
     * Internal function! Tests for a number of dictionaries (all that could be
     * there). Then builds a comma separated string.
     * 
     * @return A comma separated string containing all available dictionaries.
     */
    private static String checkDictionariesAvailable() {
        List<String> list = new LinkedList<String>();

        // check the availability of each of these, if so add to list
        checkDictionary(list, "en");
        checkDictionary(list, "de");
        checkDictionary(list, "es");
        checkDictionary(list, "fr");
        checkDictionary(list, "it");
        checkDictionary(list, "nl");
        checkDictionary(list, "pl");
        checkDictionary(list, "ru");
        
        numAvailable = list.size();

        // for all members of the list, add a comma and a space between them
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < list.size() - 1; i++) {
            sb.append(list.get(i));
            sb.append(", ");
        }
        if (!list.isEmpty()) {
            sb.append(list.get(list.size() - 1));
        }

        // return the comma separated list
        return sb.toString();
    }

    /**
     * Internal function! Checks if a specific dictionary is available.
     * Dictionary file name syntax is: dictionary_country_code.ortho
     * We apply a simple file exists test.
     * 
     * @param list List of available codes.
     * @param code Code to test.
     */
    private static void checkDictionary(List<String> list, String code) {
        String name = Main.jarPath + "dictionary_" + code + ".ortho";
        File file = new File(name);
        if (file.exists()) {
            list.add(code);
        }
    }
}
