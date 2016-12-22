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

import java.awt.Component;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.tools.common.CentralStatic;
import org.tools.common.TreeNode;
import org.tools.i18n.PropertyWithStats;
import org.tools.io.Resource;
import org.tools.io.ResourceUtils;

/**
 * Renderer for the cells of the languages tree. Displays a suitable flag and
 * the languages clear name. If no suitable flag is found (based on country and
 * language codes) than the standard icon is displayed.
 * 
 * @author Trilarion 2010-2011
 */
public class LanguageTreeRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LanguageTreeRenderer.class.getName());

    /**
     * Renders the cell, mostly setting the language name, adding (Master) if the
     * current node is a Master node and setting a country flag. The file names
     * of the icons are either derived from the country codes or if these are not
     * given than from the language codes where a manual mapping to appropriate
     * country codes has already been performed. See LanguageMapFlag.
     * 
     * @param tree The original tree.
     * @param value The cell object.
     * @param sel Is selected.
     * @param expanded Is expanded.
     * @param leaf Is leaf.
     * @param row Which row.
     * @param hasFocus Has focus.
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value != null) {
            @SuppressWarnings("unchecked")
            TreeNode<LanguageProperties> node = (TreeNode<LanguageProperties>) value;

            // the ending is not part of the clear name but only part of the tree rendering
            String ending = "";
            if (node.getData().isMaster() == true) {
                PropertyWithStats lang = CentralStatic.retrieve("lang-prop");
                ending = " (" + lang.get("languagetree.master") + ")";
            }
            setText(node.getData().getClearName() + ending);

            // relatively complex algorithm for determining a nice flag
            ImageIcon icon = null;
            LanguageProperties props = node.getData();
            // first get the country code (we have flags for each country code stored
            String code = props.getCountry();
            try {
                if (code != null) {
                    // get the flag for the country code or the generic one if there is none
                    icon = getFlag(Main.jarPath + "JLokalize.jar/icons/flags/" + code.toLowerCase() + ".png");
                } else {
                    // no country code, try the language code
                    code = props.getLanguage();
                    if (code != null) {
                        Properties langFlagMap = CentralStatic.retrieve("LanguageFlagMap");
                        if (langFlagMap.containsKey(code)) {
                            // but convert first the language code to a manually chosen country code, if such a conversion exists
                            code = langFlagMap.getProperty(code);
                        }
                        // get the icon for the manually chosen country code or try the language code directly (for some languages, e.g. de -> DE, it is already right)
                        icon = getFlag(Main.jarPath + "JLokalize.jar/icons/flags/" + code.toLowerCase() + ".png");
                    } else {
                        // no country code, no language code, just decide according to base (most probably will end as generic flag)
                        code = props.getBase();
                        icon = getFlag(Main.jarPath + "JLokalize.jar/icons/flags/" + code.toLowerCase() + ".png");
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            setIcon(icon);
        }
        return this;
    }

    /**
     * Helper function, called each time the cell is rendered. Each time a file
     * is loaded again which is ineffective, but not a visible performance problem.
     * 
     * @param fileName The name of the file.
     * @return The icon to depict the language or the generic flag icon (the star) if no other flag could be found.
     */
    private ImageIcon getFlag(String fileName) throws IOException {
        ImageIcon icon = null;
        Resource res = ResourceUtils.asResource(fileName);
        if (res.exists()) {
            icon = new ImageIcon(ImageIO.read(res.getInputStream()));
        } else {
            icon = CentralStatic.retrieve("GenericFlag");
        }
        return icon;
    }
}
