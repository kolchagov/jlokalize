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

import java.util.Locale;
import org.tools.io.Resource;

/**
 * Very simple container for an id string, a Locale and the corresponding resource.
 * Used in the language setup in Main, where resource depicts the language file to
 * use later on, id is the clear name of the language and locale the Locale we have
 * to set. No field encapsulation. 
 * 
 * @author Trilarion 2011
 */
public class LocaleResource {
    public String id;
    public Locale locale;
    public Resource resource;

    /**
     * Simple constructor setting all variables.
     */
    public LocaleResource(String id, Locale locale, Resource resource) {
        this.id = id;
        this.locale = locale;
        this.resource = resource;
    }
    
}
