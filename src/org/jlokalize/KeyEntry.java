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
 * One entry in the model of the keys table (left lower side). Contains the key
 * and all texts shown on the right side, i.e. the text and the comment, but also
 * the default values (inherited values from the master). It is transferred between
 * the model and the other parts of the program.
 * 
 * @author Trilarion 2011
 */
public class KeyEntry {
    public String text, defaultText, comment, defaultComment, key;    
}
