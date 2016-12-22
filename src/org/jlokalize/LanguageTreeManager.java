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

import java.util.Comparator;
import org.tools.common.TreeNode;

/**
 * Helper utilities for handling trees composed of TreeNodes.
 * - Insertion in the tree according to a language, country, variant hierarchy
 *   including nodes in between.
 * - Removing nodes from the tree.
 * - Sorting children in the tree.
 * - Getting, setting Master nodes in the tree
 * 
 * @author Trilarion 2010-2011
 */
public class LanguageTreeManager {

    /**
     * Private constructor to avoid instantiation.
     */
    private LanguageTreeManager() {
    }

    /**
     * Automatically inserts a new LanguageProperty as a node in the tree at the
     * right place and inserts any missing nodes in between, so even a new root
     * is created if no root was given before.
     * 
     * The position is specified by the combination of language, country and
     * variant tag.
     * 
     * The base is either taken from root or if there is no root then it is taken
     * from the parameter.
     * 
     * The clear name is set for all created nodes.
     * 
     * Comment: Already existing nodes with the same properties are replaced by
     * the new LanguageProperty.
     * 
     * Comment: The inserting in a children list of nodes is not sorted.
     *
     * @param root
     * @param lang
     * @return The new root, in case it has changed.
     */
    public static TreeNode<LanguageProperties> insertLangPropInTree(TreeNode<LanguageProperties> root, LanguageProperties lang) {
        // wrap the given data in a new node
        TreeNode<LanguageProperties> node = new TreeNode<LanguageProperties>();
        node.setData(lang);

        // if there is no root yet (root is null), create a new one with the base inherited from the given data
        if (root == null) {
            root = new TreeNode<LanguageProperties>();
            LanguageProperties newlang = new LanguageProperties();
            newlang.setBase(lang.getBase());
            newlang.setClearName();
            root.setData(newlang);
        }

        // inherit the base from the root (if there was not root, we effectively keep the base)
        String base = root.getData().getBase();
        lang.setBase(base);
        lang.setClearName();

        // if there is no language tag, make it the new root and return
        String language = lang.getLanguage();
        if (language == null) {
            root = root.replaceWith(node);
            return root;
        }

        // end of top level
        // we can assume language != null, search for suitable child

        TreeNode<LanguageProperties> child = null;
        for (TreeNode<LanguageProperties> n : root.asUnmodifiableList()) {
            LanguageProperties p = n.getData();
            if (language.equals(p.getLanguage())) {
                child = n;
                break;
            }
        }

        // not found, must make it anew with language tag from given lang
        if (child == null) {
            child = new TreeNode<LanguageProperties>();
            LanguageProperties newlang = new LanguageProperties();
            newlang.setBase(base);
            newlang.setLanguageCodes(language, null, null);
            newlang.setClearName();
            child.setData(newlang);
            root.add(child);
        }

        // if country == null, replace the newly created child with node, because node is the one for this level and return
        String country = lang.getCountry();
        if (country == null) {
            child.replaceWith(node);
            return root;
        }

        // end of language level
        // we can assume country != null, search for suitable child of child

        TreeNode<LanguageProperties> parent = child;
        child = null;
        for (TreeNode<LanguageProperties> n : parent.asUnmodifiableList()) {
            LanguageProperties p = n.getData();
            if (country.equals(p.getCountry())) {
                child = n;
                break;
            }
        }

        // not found, must make it anew with language/country tag from given lang
        if (child == null) {
            child = new TreeNode<LanguageProperties>();
            LanguageProperties newlang = new LanguageProperties();
            newlang.setBase(base);
            newlang.setLanguageCodes(language, country, null);
            newlang.setClearName();
            child.setData(newlang);
            parent.add(child);
        }

        // if variant == null, replace child with the node because it is the one for this level and return
        String variant = lang.getVariant();
        if (variant == null) {
            child.replaceWith(node);
            return root;
        }

        // end of country level
        // we can assume variant != null, search for suitable child and replace or add

        parent = child;
        child = null;
        for (TreeNode<LanguageProperties> n : parent.asUnmodifiableList()) {
            LanguageProperties p = n.getData();
            if (variant.equals(p.getVariant())) {
                child = n;
                break;
            }
        }

        if (child == null) {
            // not found, add it
            parent.add(node);
        } else {
            // or replace it
            child.replaceWith(node);
        }

        // done, it should have been added or replaced somewhere, missing nodes should have been created automatically
        return root;
    }

    /**
     * Removes a node from the tree, if it isn't the root and hasn't any children.
     * Otherwise only the keys are deleted.
     * 
     * @param node The node to remove.
     * @return True if the whole node could be deleted, false otherwise.
     */
    public static boolean removeNodeFromTree(TreeNode<LanguageProperties> node) {
        // if it hasn't children and isn't root, we can remove the node completely
        if (node.isRoot() == false && node.isLeaf() == true) {
            node.removeUsFromTree();
            return true;
        }
        // otherwise we just remove all keys but do not delete the node
        node.getData().removeAllKeys();
        return false;
    }

    /**
     * Updates all language properties in the subtree specified by root so new
     * clear names are shown. This is important if for example the default Locale
     * has changed.
     * 
     * @param root The top node for specifying the tree.
     */
    public static void setClearNamesInTree(TreeNode<LanguageProperties> root) {
        // if root is null return immediately
        if (root == null) {
            return;
        }
        // call the setClearName function on the nodes data
        LanguageProperties lang = root.getData();
        lang.setClearName();
        // recursive calling all children to set clear names also there
        for (TreeNode<LanguageProperties> node : root.asUnmodifiableList()) {
            setClearNamesInTree(node);
        }
    }
    
    
    /**
     * Sort the tree for clear names.
     * 
     * @param node A tree node.
     */
    public static void sortTreeForClearNames(TreeNode<LanguageProperties> node) {
        // only if the node is not null
        if (node == null) {
            return;
        }
        Comparator<TreeNode<LanguageProperties>> comparator = new Comparator<TreeNode<LanguageProperties>>() {
            @Override
            public int compare(TreeNode<LanguageProperties> o1, TreeNode<LanguageProperties> o2) {
                return o1.getData().getClearName().compareTo(o2.getData().getClearName());
            }
        };
        node.sortSubTree(comparator);
    }

    /**
     * Returns the master node, i.e. the first one that has the master flag set.
     * There should be only one.
     * 
     * @param root The root node.
     * @return The node with the master flag set.
     */
    public static TreeNode<LanguageProperties> getMasterNode(TreeNode<LanguageProperties> root) {
        for (TreeNode<LanguageProperties> node : root.subTreeNodesList()) {
            if (node.getData().isMaster()) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Tests if at least one language in the tree has modified keys.
     * 
     * @param root The root node of the language tree.
     * @return True if at least one language contains at least one modified key.
     */
    public static boolean anyNodeContainsModifiedKeys(TreeNode<LanguageProperties> root) {
        for (TreeNode<LanguageProperties> node: root.subTreeNodesList()) {
            if (node.getData().anyKeyModified() == true) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience function! Gets the current master node, removed the master flag
     * and sets the master flag of the new node.
     * 
     * @param root Root node of tree.
     * @param master New master node.
     */
    public static void setMasterNode(TreeNode<LanguageProperties> root, TreeNode<LanguageProperties> master) {
        TreeNode<LanguageProperties> old = LanguageTreeManager.getMasterNode(root);
        if (old != null) {
            old.getData().setMaster(false);
        }
        if (master != null) {
            master.getData().setMaster(true);
        }
    }
    
    /**
     * Checks if anywhere in the tree there is a Language that is equal (same clear name).
     * 
     * @param root Root node of tree.
     * @param language LanguageProperties to compare with.
     * @return True if any equal member is found in the tree defined by root.
     */
    public static boolean contains(TreeNode<LanguageProperties> root, LanguageProperties language) {
        for (TreeNode<LanguageProperties> node: root.subTreeNodesList()) {
            if (node.getData().isEqual(language)) {
                return true;
            }
        }
        return false;
    }
}