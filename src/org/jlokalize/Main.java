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

import com.jgoodies.looks.windows.WindowsLookAndFeel;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jlokalize.ui.EditorFrame;
import org.tools.common.CentralStatic;
import org.tools.common.Utils;
import org.tools.i18n.Property;
import org.tools.i18n.PropertyWithStats;
import org.tools.io.Resource;
import org.tools.io.ResourceUtils;
import org.tools.ui.ListSelectDlg;
import org.tools.ui.LookAndFeel;

/**
 * This is the main entry point for the JLokalize application. We setup all
 * necessary things and then createAndRun an editor frame. Upon shutdown we save
 * the options.
 *
 * @author Trilarion 2010-2011
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    /**
     * Hard coded version string, used for detecting of the options are
     * outdated, i.e. from another version
     */
    private static final String VERSION = "1.1a";
    /**
     * A global options variable, accessible by everyone and set relatively
     * early on.
     */
    public static final PropertyWithStats options = new PropertyWithStats();
    /**
     * Path to the jar file.
     */
    // public static final String jarPath = "";
    public static String jarPath;
    /**
     * Path to the user directory, we will store the options and the language
     * files and the logs there
     */
    // private static final String usrPath = "";
    private static final String usrPath = System.getProperty("user.home") + ResourceUtils.Delimiter;

    /**
     * Private constructor to avoid instantiation.
     */
    private Main() {
    }

    /**
     * Main entry point for the application. We setup the logger, the options
     * and languages, the look and feel, the spell checker and then the editor
     * frame is started.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // determine the jar path, ugly long expression to just get the directory of the jar file right
            URL jarURL = Main.class.getProtectionDomain().getCodeSource().getLocation();
            try {
                jarPath = (new File(jarURL.toURI()).getParent() + ResourceUtils.Delimiter);
            } catch (URISyntaxException e) {
                jarPath = (new File(jarURL.getPath())).getParent() + ResourceUtils.Delimiter;
            }

            // tell logger to use file log and to overwrite it everytimes
            setupLogger();

            // read options or create new default ones if not existent        
            setupConfiguration();

            // set system look and feel (we might need it already in the choose language dialog)
            try {
                UIManager.setLookAndFeel(new WindowsLookAndFeel());
                //fix
                UIManager.put("Button.margin", new Insets(5, 10, 5, 10));
            } catch (UnsupportedLookAndFeelException ex) {
                //fallback
                LookAndFeel.setSystemLookAndFeel();
            }

            // set-up language information
            if (setupLanguage(options.get("program.current.language")) == false) {
                // if the dialog was aborted, shut down at this moment, because at startup a language is actually needed
                return;
            }

            // loading all available dictionaries
            setupSpellchecker();

            // all setups done, createAndRun the main frame, i.e. the editor frame
            EditorFrame mainFrame = new EditorFrame();
            mainFrame.setVisible(true);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Setup for the logger. Create the directory JLokalize.config if it is not
     * yet existing. The setup of the logger is quite standard. We will append
     * all output to the existing log file.
     *
     * Comment: We use the standard package java.util.logging
     *
     * @throws IOException If some file operation failed here.
     */
    private static void setupLogger() throws IOException {
        // if directory JLokalize.config is not yet existing, create it beforehand
        File file = new File(usrPath + "JLokalize.config");
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdir();
        }

        // setup of the logger
        Handler handler = null;
        handler = new FileHandler(usrPath + "JLokalize.config" + File.separator + "JLokalize.log", false);
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.INFO);
        Logger.getLogger("").addHandler(handler);

        // our first log message (just to get the date and time)
        LOG.log(Level.INFO, "JLokalize starting");
        LOG.log(Level.INFO, "jar path: {0}", jarPath);
        LOG.log(Level.INFO, "user path: {0}", usrPath);
    }

    /**
     * Registers available dictionaries. If none is available, sets the option
     * to use the spell checker to false.
     */
    private static void setupSpellchecker() {
        SpellCheckerIntegration.registerDictionaries();
        if (SpellCheckerIntegration.numAvailable == 0) {
            Main.options.put("pref.dictionary.use", "false");
        }
    }

    /**
     * Setup for the configuration. If an options file (StatProperty) is in the
     * default place, just load from it. Otherwise copy from inside the jar file
     * the default options and save them immediately (after setting the user
     * directory). Also copy the language files in this case.
     *
     * @throws IOException If a reading/writing access failed.
     */
    private static void setupConfiguration() throws IOException {
        Resource resource = ResourceUtils.asResource(usrPath + "JLokalize.config/JLokalize.options");

        if (!resource.exists()) {
            newConfiguration(resource);
        } else {
            // it exists, just load it with statistics
            options.setLocation(resource);
            options.load();

            // additional versions check
            if (!VERSION.equals(options.get("program.version"))) {
                newConfiguration(resource);
            }
        }
    }

    /**
     * Internal function! Call by the setup of the configuration. Idea is that
     * if the hard coded version is different from the saved options version or
     * if no saved options are found, create them new with the copy from within
     * the jar file.
     *
     * @param resource Place to put the copied options.
     * @throws IOException If something goes wrong.
     */
    private static void newConfiguration(Resource resource) throws IOException {
        // config directory is not existing, copy from jar file and save at the default location
        Resource opt = ResourceUtils.asResource("../../config/default.options");
        options.setLocation(opt);
        options.load(false); // without statistics
        options.put("program.open.default.directory", System.getProperty("user.dir"));
        options.setLocation(resource);
        options.save();

        // also copy all available language files
        Resource languagesDir;
        languagesDir = ResourceUtils.asResource(jarPath + "JLokalize.jar/lang/");
        List<Resource> languages = languagesDir.list("^(JLokalize).*(\\.properties)$");
        for (Resource in : languages) {
            String name = in.getName();
            Resource out = ResourceUtils.asResource(usrPath + "JLokalize.config/lang/" + name);
            ResourceUtils.copy(in, out);
        }
    }

    /**
     * From all available languages (properties files in JLokalize.config/lang)
     * either choose one via a selection dialog or take an options entry and
     * load a PropertyWithStats object from it. English is set as master. The
     * default locale of the gui is also updated. The dialog is displayed if the
     * preferred language equals "none".
     *
     * @param prefLanguage An id code of the preferred language or "none".
     * @return True if indeed a language was chosen.
     * @throws IOException If any IO operation fails.
     */
    public static boolean setupLanguage(String prefLanguage) throws IOException {
        // get list of all files matching the language file pattern
        Resource directory = ResourceUtils.asResource(usrPath + "JLokalize.config/lang/");
        List<Resource> resources = directory.list("^(JLokalize).+(\\.properties)$");
        // the double "\\" is only one "\" in the string        

        // map consisting of clear name (the key) and a locale/resource pair for each language we have
        Map<String, LocaleResource> languages = new HashMap<String, LocaleResource>(10);

        // going through all available language files and make listings
        for (Resource resource : resources) {
            String id = resource.getName();
            id = id.substring(id.indexOf('_') + 1, id.lastIndexOf('.'));
            // here the id string is divided in substrings (country, language, variant) and the different constructors of Locale called
            Locale locale = Utils.createLocaleFromFileName(id);
            String name = Utils.capitalize(locale.getDisplayName());
            languages.put(name, new LocaleResource(id, locale, resource));
        }

        // check if there were some loaded at all
        if (languages.isEmpty()) {
            LOG.log(Level.SEVERE, "Did not found any existing language file. Contact the authors!");
        }

        // display language selection dialog here only if name is not "none"
        LocaleResource lresource = null;
        if ("none".equals(prefLanguage)) {

            // contains a list of clear names, used in the choose language dialog
            List<String> names = Utils.asSortedList(languages.keySet());
            // i18n the dialog
            String title = "Language Selection";
            String buttonText = "Select";
            if (CentralStatic.contains("lang-prop")) {
                Property i18n = CentralStatic.retrieve("lang-prop");
                title = i18n.get("language.change.title");
                buttonText = i18n.get("language.change.select");
            }
            String name = ListSelectDlg.createAndRun(null, title, buttonText, names);
            if (name == null) {
                // the dialog was aborted and we do not have a preferred language, so we do nothing
                return false;
            }
            // one of the names must be selected, get the corresponding LocaleResource object back
            lresource = languages.get(name);

        } else {

            // there is an id given in the options, try to find it in our list (id is not key of the hashmap, so we have to iterate)
            for (LocaleResource lres : languages.values()) {
                if (lres.id.equals(prefLanguage)) {
                    lresource = lres;
                    break;
                }
            }
            if (lresource == null) {
                // somehow the options are corrupted and a language is defined where we haven't any file for
                LOG.log(Level.SEVERE, "JLokalize language entry in config corrupt! Please delete/modify config file.");
            }
        }
        // now lresource holds the language that is chosen

        // also try to obtain english which will be used as master (by default)
        LocaleResource master = null;
        for (LocaleResource lres : languages.values()) {
            if ("en".equals(lres.id)) {
                master = lres;
                break;
            }
        }

        // now load the default language(s) and store in CentralStatic
        PropertyWithStats prop;
        if (master == null || lresource.id.equals(master.id)) {
            prop = new PropertyWithStats();
            prop.setLocation(lresource.resource);
            prop.load();
        } else {
            prop = PropertyWithStats.chainLoad(lresource.resource, master.resource);
        }
        CentralStatic.store("lang-prop", prop);

        // set the default locale, so the gui knows we have a new one
        Locale.setDefault(lresource.locale);
        JOptionPane.setDefaultLocale(Locale.getDefault());
        // somehow JOptionsPane is not updated on Locale.setDefault, so we have to call it additionally

        // store the new language (its id) in the options
        options.put("program.current.language", lresource.id);

        return true;
    }

    /**
     * This is called before exiting the application, when the editor frame is
     * disposed. We save the options and the statistics of the used language
     * object.
     */
    public static void Shutdown() {
        options.save();
        PropertyWithStats lang = CentralStatic.retrieve("lang-prop");
        lang.saveStatsOnly();
    }
}
