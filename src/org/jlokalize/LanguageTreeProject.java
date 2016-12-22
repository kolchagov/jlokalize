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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileFilter;
import org.tools.common.TreeNode;
import org.tools.common.Utils;
import org.tools.i18n.Property;
import org.tools.io.Resource;
import org.tools.io.ResourceUtils;

/**
 * A complete project description. Each project has: - a directory - a base
 * identifier (file name syntax is directory/base_languagecodes.extension) - a
 * TreeNode<LanguageProperties> holding the data
 *
 * And we can change the base and directory as well as loading the data from
 * files, saving to files or creating new empty projects.
 *
 * @author Trilarion 2011
 */
public class LanguageTreeProject {

    private static final Logger LOG = Logger.getLogger(LanguageTreeProject.class.getName());
    /**
     * Standard extension of a language (properties) file
     */
    private static final String StandardLanguageExtension = ".properties";
    /**
     * Standard naming scheme for a File Open/Save dialog
     */
    private static final String StandardLanguageDescription = "Java properties files (*.properties)";
    /**
     * The directory of the project
     */
    private Resource directory;
    /**
     * The root node of the languages tree that is build and which is
     * representing the data of the project
     */
    private TreeNode<LanguageProperties> root;
    /**
     * The base which defines the file names and the name of the root node, can
     * be changed via the 'save as' menu
     */
    private String base;
    public static final FileFilter FFilter = new FileFilter() {
        /**
         * Filter for accepting files. We accept all directories and all files
         * with the right Extension.
         */
        @Override
        public boolean accept(File f) {
            // all directories
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            if (f.isFile() && name.endsWith(StandardLanguageExtension)) {
                return true;
            }
            return false;
        }

        /**
         * Displays the description.
         */
        @Override
        public String getDescription() {
            return StandardLanguageDescription;
        }
    };

    /**
     * Sets a new directory.
     *
     * @param resource The directory.
     * @return True if resource is a directory and it is set.
     */
    public boolean setDir(Resource resource) {
        if (resource.exists()) {
            this.directory = resource;
            // is directory?
            return true;
        }
        return false;
    }

    /**
     * @return The current directory.
     */
    public Resource getDir() {
        return directory;
    }

    /**
     * Resets the whole project. We could also dispose of the current object and
     * create a new instance of this class. This is kind of a shortcut.
     */
    public void reset() {
        directory = null;
        root = null;
        base = null;
    }

    /**
     * Given a base stores this identifier and creates an empty root node. We
     * need this for creating empty projects (not by loading).
     *
     * @param base New base identifier.
     * @return Returns the new root.
     */
    public TreeNode<LanguageProperties> createNew(String base) {
        this.base = base;
        // create new LanguageProperties
        LanguageProperties language = new LanguageProperties();
        language.setBase(base);
        language.setClearName();
        root = LanguageTreeManager.insertLangPropInTree(null, language);
        root.getData().setMaster(true);
        return root;
    }

    /**
     * @return The root node.
     */
    public TreeNode<LanguageProperties> getRoot() {
        return root;
    }

    /**
     * @return The base identifier.
     */
    public String getBase() {
        return base;
    }

    /**
     * Given a file from a project, searches for all files that match a certain
     * regular expression (base at the beginning, then arbitrary stuff, then the
     * extension at the end). Then obtains the language codes from the middle
     * part of the file name (language, country, variant) and inserts a node in
     * a newly created tree according to the language codes, automatically
     * adding all missing nodes.
     *
     * @param file One of the files from the project (doesn't matter which one).
     * @return True if opening was successful.
     */
    public boolean open(File file) {
        try {
            // get parent directory and file name from the chosen file
            directory = ResourceUtils.asResource(file.getParentFile());
            base = file.getName();
            String selectedFile = base;
            // get the extension
            String extension = base.substring(base.lastIndexOf('.'));
            // from file name get everything until the first "_" or if there is none until the last "."
            int k;
            k = base.indexOf('_');
            if (k == -1) {
                k = base.lastIndexOf('.');
            }
            base = base.substring(0, k);
            // bugFix: always get selected file, then read all languages
            List<Resource> resources = directory.list(selectedFile);
            // load all languages in this directory that start with the same base identifier (will be project name)
            // get all files with basename inside and finally with ".properties" using a regular expression
            String regEx = "^(" + base + "_).*(\\" + extension + ")$";
            resources.addAll(directory.list(regEx));
            // the double "\\" is only one "\" in the string

            // comment: instead of using the extension we could as well just use the StandardLanguageExtension since during
            // saving it is used anyways

            // Sorting here will result in less overhead during the insertion in the tree but is not strictly neccessary
            Collections.sort(resources, Resource.comparator);

            // the root node of the tree
            root = null;

            // going through the list of matching files
            for (Resource resource : resources) {

                // process file name to extract the language codes
                String id = resource.getName();
                id = id.substring(base.length(), id.lastIndexOf('.'));
                String[] code = id.split("_");
                code = Arrays.copyOfRange(code, 1, code.length);
                if (Utils.isValidLanguageCode(code)) {
                    // load from file and create new properties
                    Property prop = new Property();
                    prop.setLocation(resource);
                    prop.load();
                    LanguageProperties lang = new LanguageProperties(prop);
                    // sets base and language tags
                    lang.setBase(base);
                    lang.setLanguageCodes(code);
                    // insert in the tree
                    root = LanguageTreeManager.insertLangPropInTree(root, lang);

                } else {
                    LOG.log(Level.INFO, "Encountered invalid language code in file name: {0}", id);
                }
            }

            // sort according to clear names
            LanguageTreeManager.sortTreeForClearNames(root);

            root.getData().setMaster(true);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Opening project {0} failed.", base);
            // maybe also need to close files here?
            reset();
            return false;
        }
        return true;
    }

    /**
     * Chzanges the base for all nodes of the project. Used by the 'save as'
     * menu.
     *
     * @param file The file to extract the base from.
     */
    public void rebase(File file) {
        base = null;
        try {
            directory = ResourceUtils.asResource(file.getParentFile());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        base = file.getName();
        // from file name get everything until the first "_" or if there is none until the last "."
        int k;
        k = base.indexOf('_');
        if (k == -1) {
            k = base.lastIndexOf('.');
        }
        if (k != -1) {
            base = base.substring(0, k);
        }

        // set new base
        for (TreeNode<LanguageProperties> node : root.subTreeNodesList()) {
            LanguageProperties props = node.getData();
            props.setBase(base);
        }
    }

    /**
     * Traverse through the nodes list and saves the languages to a file whos
     * name is derived from directory, base and toFileName() from
     * LanguageProperties.
     */
    public void save() {
        // traverse through root structure
        for (TreeNode<LanguageProperties> node : root.subTreeNodesList()) {
            LanguageProperties language = node.getData();
            Resource resource = null;
            try {
                resource = ResourceUtils.subResource(directory, language.toFileName() + StandardLanguageExtension);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            language.save(resource);
        }
    }
}
