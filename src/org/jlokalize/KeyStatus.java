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
 * The status of a key in the keys table can be four-fold. It can be here and
 * in the master (-> Everywhere), or only here (->OnlyHere), or only in parent
 * (->Only in Parent), or nowhere at all (was either here or in the master but
 * is deleted now). It determines the layout of the table cells.
 * 
 * @author Trilarion 2011
 */
public enum KeyStatus {
    OnlyHere, OnlyInParent, Everywhere, AlreadyDeleted
}
