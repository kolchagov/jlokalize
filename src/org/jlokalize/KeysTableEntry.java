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

/**
 * Used by the keys table model to hold the key and it's status and the modified
 * flag because this all determines the layout of a key in the keys table, and will
 * therefore be transferred to the renderer.
 * 
 * @author Trilarion 2011
 */
public class KeysTableEntry {
    
    public String key;
    public KeyStatus status;
    public boolean modified;
    
    /**
     * Automatically used in the key table cell renderer to determine the cell
     * text. Just returns the key.
     * 
     * @return The key.
     */
    @Override
    public String toString() {
        return key;
    }

}