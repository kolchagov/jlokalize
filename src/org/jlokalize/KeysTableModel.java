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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;
import org.tools.common.CentralStatic;
import org.tools.common.TreeNode;
import org.tools.i18n.PropertyWithStats;

/**
 * Model for the data of the keys table. Main purpose is to control the content
 * of the keys table, but also prepare the content for the renderer and create
 * internal lists of keys given a currently selected node in the language tree,
 * as well as updating the keys list.
 * 
 * More or less the heart of what you can do with the program within one language
 * structure.
 * 
 * The keys shown in the table are the union of the keys given by the current node
 * and the parent/master node including deleted keys (this is handled by the language
 * structure itself). However the status depends on the existence in either of these
 * language structures and on the modified flag. Not only a key is stored but also
 * a text, comment pair. They are always sorted alphabetically.
 * 
 * To ensure that this is done right is quite a tough task.
 *
 * @author Trilarion 2010-2011
 */
public class KeysTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    /** The underlying data structure */
    private LanguageProperties prop;
    /** The parent structure, we need separate object, not automatic parent of LanguageProperties */
    private LanguageProperties parent;
    /** A list of all keys, including their status */
    private List<String> keys = new LinkedList<String>();
    /** The current active row in the table */
    private int activeRow = -1;
    /** Progress bar which is located below the keys table. */
    private final JProgressBar statusProgressBar;

    /**
     * Constructor importing the progress bar (so we can control it from here)
     * 
     * @param statusProgressBar The editor frame's progress bar.
     */
    public KeysTableModel(JProgressBar statusProgressBar) {
        this.statusProgressBar = statusProgressBar;
    }

    // begin of AbstractTableModel implementation specific methods    
    /**
     * Determines the number of rows in the table.
     * 
     * @return Length of keys list.
     */
    @Override
    public int getRowCount() {
        return keys.size();
    }

    /**
     * Determines the number of columns in the table. Fixed. 1
     * 
     * @return 1
     */
    @Override
    public int getColumnCount() {
        return 1;
    }

    /**
     * Determines the class of the objects in the table for each column.
     * 
     * @param columnIndex The column.
     * @return String.class
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    /**
     * Determines the headings of each column.
     * 
     * @param column The column.
     * @return The name.
     */
    @Override
    public String getColumnName(int column) {
        PropertyWithStats lang = CentralStatic.retrieve("lang-prop");
        String title = lang.get("keystable.title");
        if (keys.size() > 0) {
            return title + " (" + countNumEverywhere() + ")";
        }
        return title;
    }

    /**
     * Returns the value at a specific row and column. Is used for rendering.
     * So we create a new KeysTableEntry object containing all necessary information,
     * eg. key, modified, here/not here.
     * 
     * @param rowIndex The row.
     * @param columnIndex The column.
     * @return A KeysTableEntry object.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // update the modified propertes (for code simplicity we do it every times)
        KeysTableEntry entry = new KeysTableEntry();
        String key = keys.get(rowIndex);
        entry.key = key;
        entry.modified = prop.modified(key);
        entry.status = determineStatus(key);
        return entry;
    }
    // end of AbstractTableModel implementation specific methods

    /**
     * If a new node is selected in the language tree, update the keys table
     * completely. Node is used to obtain the master/parent node, the joint keys
     * list and rebuild the whole thing.
     * 
     * @param node A TreeNode.
     */
    public void update(TreeNode<LanguageProperties> node) {

        // get master node
        TreeNode<LanguageProperties> master = LanguageTreeManager.getMasterNode(node.getRoot());

        // figure out, what this combination of node and master means and set prop and parent
        prop = node.getData();
        if (node != node.getRoot() && node != master) {
            if (node.getParent() == node.getRoot()) {
                parent = master.getData();
            } else {
                parent = node.getParent().getData();
            }
        } else {
            parent = null;
        }
        
        // complete rebuild
        rebuild();
    }
        
    /**
     * Just clears the whole table. Just when closing a project.
     */
    public void clear() {

        // clear all internal variables
        keys.clear();
        prop = null;
        parent = null;
        activeRow = -1;

        // update status bar
        updateProgressBar();

        // signal that the table is updated
        fireTableDataChanged();
        fireTableStructureChanged();
    }

    /**
     * Returns an entry from the table for use outside. There a new KeyEntry
     * object is created with all necessary information (keys, texts, comments).
     * Also the activeRow is set.
     * 
     * @param rowIndex The row.
     * @return A KeyEntry object.
     */
    public KeyEntry getEntry(int rowIndex) {
        String key = keys.get(rowIndex);
        KeyEntry entry = new KeyEntry();
        entry.key = key;
        entry.text = prop.getKeyText(key);
        entry.comment = prop.getKeyComment(key);
        if (parent != null) {
            entry.defaultText = parent.getKeyText(key);
            entry.defaultComment = parent.getKeyComment(key);
        }
        activeRow = rowIndex;
        return entry;
    }
    
    /**
     * Sometimes we need to keep track of newly created keys or such.
     * 
     * @param key The key to search for.
     * @return The current index in the key list or -1 if not in list. 
     */
    public int getRow(String key) {
        return keys.indexOf(key);
    }

    /**
     * This is used to prevent inserting of keys that are already in the main
     * data structure. However keys from the parent are not regarded.
     * 
     * @param key The key.
     * @return True if contained in the main data structure.
     */
    public boolean containsKey(String key) {
        return prop.containsAsKey(key);
    }

    /**
     * Updates the currently active key with new text and comment.
     * 
     * @param text The new text.
     * @param comment The new comment.
     */
    public void updateLastKey(String text, String comment) {
        if (activeRow != -1) {
            String key = keys.get(activeRow);

            String oldText = prop.getKeyText(key);
            String oldComment = prop.getKeyComment(key);
            boolean changed = false;
            // only update if text has changed and is of length > 0
            if (!text.equals(oldText) && text.length() > 0) {
                prop.putKeyText(key, text);
                changed = true;
            }
            if (!comment.equals(oldComment) && comment.length() > 0) {
                prop.putKeyComment(key, comment);
                changed = true;
            }
            if (changed) {
                // update the status bar
                updateProgressBar();

                // signal re-rendering of the row
                fireTableCellUpdated(activeRow, 0);
                // we also need to update the table header, but there seems not
                // to exist a single event for that (fireTableStructureChanged()
                // is update everything) - so we fix this from oustide with
                // access to the whole table
            }
        }
    }

    /**
     * Used for faster moving in the table. Jumps to the next key that has status
     * OnlyInParent.
     * 
     * @param row Actual row.
     * @return Next row or -1 if none is existing.
     */
    public int getNextNotHereKey(int row) {
        // search from row + 1 until the end
        int i;
        for (i = row + 1; i < keys.size(); i++) {
            if (determineStatus(keys.get(i)) == KeyStatus.OnlyInParent) {
                return i;
            }
        }
        // search from the beginning until the last one
        for (i = 0; i < row; i++) {
            if (determineStatus(keys.get(i)) == KeyStatus.OnlyInParent) {
                return i;
            }
        }
        // nothing found
        return -1;
    }

    /**
     * Insert a new key in main data structure.
     * 
     * @param key The new key.
     * @return False if key was already present, otherwise true.
     */
    public boolean insertKey(String key) {
        if (prop.containsAsKey(key)) {
            // this should never happen, but obviously if the key is already there, we fail
            return false;
        }
        // we don't have it, put a new empty key in
        prop.putKeyText(key, "");

        // if it is not in the list, add it and re-sort 
        if (!keys.contains(key)) {
            keys.add(key);
            Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
            // after resorting nothing is selected
            activeRow = -1;
            // signal that everything has changed
            fireTableDataChanged(); // keys have changed
            fireTableStructureChanged(); // column name may have changed                
        } else {
            // just signal that the row containing key has changed
            fireTableCellUpdated(keys.indexOf(key), 0);
        }

        // update the status bar
        updateProgressBar();

        return true;
    }

    /**
     * Removes the key of the active row.
     * 
     * @return True, if the key was a member of the main data structure (we cannot
     * remove keys from a parent).
     */
    public boolean removeKey() {
        String key = keys.get(activeRow);

        // it should a key in the list
        if (!prop.containsAsKey(key)) {
            return false;
        }

        // we try to remove it from the current language
        prop.removeKey(key);

        // update the row in question
        fireTableCellUpdated(keys.indexOf(key), 0);

        // update the status bar
        updateProgressBar();

        return true;
    }

    /**
     * Rename the key of the active row to a new name. This can prolong the table
     * since the old key can still be contained in the parent. In another way
     * it can shorten the table, if the new key is also present in the parent,
     * but the old key wasn't.
     * 
     * Comment: Be aware that the function on LanguageProperties like renameKey, ... 
     * handle the issue of comment within themselves. So no need to worry about
     * here.
     * 
     * @param newKey The new key.
     * @return New active row.
     */
    public int renameKey(String newKey) {
        String key = keys.get(activeRow);

        // test if newkey is not yet there
        if (keys.contains(newKey)) {
            return -1;
        }

        // newkey is not in list, do the renaming
        prop.renameKey(key, newKey);

        // add new key to keys list
        keys.add(newKey);

        // need to sort again
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);

        // selection will be on new key
        activeRow = keys.indexOf(newKey);

        // update status bar
        updateProgressBar();

        // signal that the table has changed
        fireTableDataChanged();
        fireTableStructureChanged();

        return activeRow;
    }

    /**
     * Restores the content of the active row to the status it had upon loading
     * of the structure. The whole logic is inside of LaguageProperties. However
     * a rebuild is necessary.
     */
    public void restore() {
        String key = keys.get(activeRow);
        // is it modified
        if (prop.modified(key)) {
            // restore it
            prop.restore(key);
            // completely reset the language
            rebuild();
            // need to focus again (is done from outside)
        }
    }

    /**
     * Internal function! Determines the status of a key (used for the renderer).
     * 
     * @param key The key.
     * @return  The KeyStatus.
     */
    private KeyStatus determineStatus(String key) {
        boolean wehave = prop.containsAsKey(key);
        boolean upstream = parent == null || parent.containsAsKey(key);

        if (upstream == true) {
            if (wehave == true) {
                return KeyStatus.Everywhere;
            } else {
                return KeyStatus.OnlyInParent;
            }
        } else {
            if (wehave == true) {
                return KeyStatus.OnlyHere;
            } else {
                return KeyStatus.AlreadyDeleted;
            }
        }
    }

    /**
     * Internal function! Updates the progress bar.
     */
    private void updateProgressBar() {
        // update progress bar
        int all = 0;
        for (String key : keys) {
            KeyStatus status = determineStatus(key);
            if (status == KeyStatus.Everywhere || status == KeyStatus.OnlyInParent) {
                all++;
            }
        }

        if (all > 0) {
            all = 100 * countNumEverywhere() / all;
            // set content (i18n)
            PropertyWithStats lang = CentralStatic.retrieve("lang-prop");            
            String text = String.format("%s %d%%", lang.get("keystable.coverage"), all);
            statusProgressBar.setValue(all);
            statusProgressBar.setString(text);
            statusProgressBar.setToolTipText(text);
        } else {
            statusProgressBar.setValue(0);
            statusProgressBar.setString(null);
            statusProgressBar.setToolTipText(null);
        }
    }

    /**
     * Internal function! Calculates the number of keys, which are everywhere.
     *
     * @return Number of good keys.
     */
    private int countNumEverywhere() {
        int num = 0;
        for (String key : keys) {
            if (determineStatus(key) == KeyStatus.Everywhere) {
                num++;
            }
        }
        return num;
    }
    
    /**
     * Internal function! Rebuilds the keys lists. Sorts them. Updates the progress
     * bar, fire events that the content has changed.
     */
    private void rebuild() {
        // clear keys list and active row
        keys.clear();
        activeRow = -1;

        // combine keyset from this and parent
        Set<String> keySet = prop.getAllTextKeysAsSet();
        if (parent != null) {
            // we have two nodes to deal with
            keySet.addAll(parent.getAllTextKeysAsSet());

        }
        // convert to list
        keys.addAll(keySet);
        // sort keys list
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);

        // update status bar
        updateProgressBar();

        // signal updates to keys and column names (this is only done by fireTableStructureChanged() )
        fireTableDataChanged();
        fireTableStructureChanged();

    }    
}
