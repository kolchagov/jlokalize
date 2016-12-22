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

import java.util.ArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.tools.common.TreeNode;

/**
 * Model for the languages tree. It implements the TreeModel interface but is not
 * relying on the DefaultTreeModel, but on the inherent tree structure of TreeNode
 * instead. We have to implement some functionality, but most of it is straight
 * forward. The only additional thing that this model does is storing a so called
 * 'current' node.
 *
 * @author Trilarion 2010-2011
 */
public class LanguageTreeModel implements TreeModel {

    /** List of listeners to be told about changes in the table. */
    private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>(5);
    /** The current node in the language tree. Contains all information. (even master, root, ...) */
    private TreeNode<LanguageProperties> current = null;
    
    // start TreeModel implemented methods

    /**
     * @return The root if a node is specified, otherwise null.
     */
    @Override
    public Object getRoot() {
        if (current != null) {
            return current.getRoot();
        }
        return null;
    }
    
    /**
     * @param parent The parent node.
     * @param index The child index.
     * @return The child node at a given index.
     */
    @Override
    public Object getChild(Object parent, int index) {
        return castToTreeNode(parent).get(index);
    }

    /**
     * @param parent The parent node.
     * @return Number of children.
     */
    @Override
    public int getChildCount(Object parent) {
        return castToTreeNode(parent).getChildCount();
    }

    /**
     * @param node A node.
     * @return True if is a leaf (i.e. does not have any children).
     */
    @Override
    public boolean isLeaf(Object node) {
        return castToTreeNode(node).isLeaf();
    }

    /**
     *  Not supported yet - we don't need it.
     * 
     * @param path
     * @param newValue
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param parent The parent node.
     * @param child A child node.
     * @return The index of child in parent.
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return castToTreeNode(parent).indexOf(castToTreeNode(child));
    }

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     * 
     * @param l Listener.
     */
    @Override
    public void addTreeModelListener(TreeModelListener l) {
         treeModelListeners.add(l);
    }

    /**
     * Removes a listener previously added with addTreeModelListener().
     * 
     * @param l Listener.
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }
    
    // end TreeModel implemented methods
    
    /**
     * Helper function! Avoids adding SuppressWarnings("unchecked") annotations
     * all the time. Casts to TreeNode<LanguageProperties>, our real type.
     * 
     * @param object A node of this tree.
     * @return The node with the real type.
     */
    @SuppressWarnings("unchecked")
    private static TreeNode<LanguageProperties> castToTreeNode(Object object) {
        return (TreeNode<LanguageProperties>) object;
    }    
    
    /**
     * Sets a new node as current node. No update of the tree structure.
     * Call to structureChanged by yourself.
     * 
     * @param current New current node.
     */
    public void setCurrentNode(TreeNode<LanguageProperties> current) {
        this.current = current;
    }    

    /**
     * @return The current node.
     */
    public TreeNode<LanguageProperties> getCurrentNode() {
        return current;
    }
    
    /**
     * Is called whenever the tree structure is changed (added new node, deleted
     * node, ...). We just tell all listener that everything has changed, which
     * is the easy way out. We might make it more elaborate later on.
     */
    public void structureChanged() {
        TreePath path = null;
        Object root = getRoot();
        if (root != null) {
            path = new TreePath(root);
        }
        TreeModelEvent event = new TreeModelEvent((Object) this, path);
        /* Funny thing is that new TreeModelEvent((Object) this, null) directly
         * is not working, because of an ambiguity in the definitions! */

        // Finally tell all listeners.
        for (TreeModelListener l : treeModelListeners) {
            l.treeStructureChanged(event);
        }
    }
}
