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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.tools.common.CentralStatic;
import org.tools.i18n.Property;

/**
 * Renders entries in the keys table. Uses the DefaultTableCellRenderer and modifies
 * it to set the text color according to the KeyStatus, the tool tips and the
 * modified bar on the right side of the cells. All necessary information for the
 * layout of the cells are provided by the getValueAt() method of the model.
 * 
 * @author Trilarion 2010-2011
 */
public class KeysTableRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;
    /** Width of the red bar that is shown on the right side of the cell if a key has been modified */
    private static final int ModifiedBarWidth = 5;
    /** We keep track of the modified property so we can react to it in the paint() method */
    private boolean modified = false;

    /**
     * Renders the cell. Sets the key as text (done by the toString method of
     * the (KeysTableEntry) object value. Sets the text color depending on the
     * KeyStatus. Sets also tool tips. For all the other thing we rely on the
     * DefaultTableCellRenderer.
     * 
     * @param table The table.
     * @param value The cell entry as given by the model.
     * @param isSelected If is selected.
     * @param hasFocus If is has focus.
     * @param row Current row.
     * @param column Current column.
     * @return The component, i.e. this.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        KeysTableEntry entry = (KeysTableEntry) value;
        Property i18n = CentralStatic.retrieve("lang-prop");
        switch (entry.status) {
            case OnlyHere:
                this.setToolTipText(i18n.get("keystable.notupstream"));
                setForeground(Color.MAGENTA);
                break;
            case OnlyInParent:
                this.setToolTipText(i18n.get("keystable.nothere"));
                setForeground(Color.BLUE);
                break;
            case AlreadyDeleted:
                this.setToolTipText(i18n.get("keystable.deleted"));
                setForeground(Color.LIGHT_GRAY);
                break;
            default:
                this.setToolTipText(null);
                setForeground(Color.BLACK);
                break;
        }
        modified = entry.modified;
        if (modified) {
            String text = this.getToolTipText();
            if (text == null) {
                text = i18n.get("keystable.modified");
            } else {
                text += " " + i18n.get("keystable.modified");
            }
            this.setToolTipText(text);
        }
        return this;
    }

    /**
     * The paint method which is modified to paint a red bar if a cell entry is
     * modified.
     * 
     * @param g Graphics context
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (modified == true) {
            Color oldCol = g.getColor();
            g.setColor(Color.RED);
            g.fillRect(getWidth() - ModifiedBarWidth - 1, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(oldCol);
        }
    }
}
