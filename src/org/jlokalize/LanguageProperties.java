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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.tools.common.Utils;
import org.tools.i18n.Property;
import org.tools.io.Resource;

/**
 * This class wraps Property to track modifications (via a HashMap and the
 * value null), to have some identifiers (base, language, country, variant) and
 * to only work on text keys (no comment keys) but also work on comments
 * simultaneously, therefore hiding these details from the higher level editor
 * frame and table model.
 *
 * @author Trilarion 2010-2011
 */
public class LanguageProperties {

    /** Default ending of a key that is a comment. Regular keys cannot end with this. */
    private static final String commentID = ".comment";
    /** The underlying properties structure to load and save and keep the originals. */
    private Property prop = new Property();
    /** The more versatile (than Property) map holding the key / text pairs (can also have null which we use for deleted keys) */
    private HashMap<String, String> map = new HashMap<String, String>(200);
    /* Base name, i.e. project identifier */
    private String base;
    /** Clear name in the actual Locale, depends on language, country, variant and current Locale */
    private String clearname;
    /** Complete identifiers of the language, i.e. language, country and variant. */
    private String language, country, variant;
    /** Is master structure or not */
    private boolean master = false;

    /* For creating new languages */
    public LanguageProperties() {
    }

    /**
     * Creates a language structure with a property already set. Copies to map.
     * Is used for creating languages from a file.
     * 
     * @param prop The property.
     */
    public LanguageProperties(Property prop) {
        this.prop = prop;
        copyToMap();
    }

    /**
     * @return True if master flag is set.
     */
    public boolean isMaster() {
        return master;
    }

    /**
     * Sets the master flag.
     * 
     * @param value True if it should be set.
     */
    public void setMaster(boolean value) {
        master = value;
    }

    /**
     * @return The clear name.
     */
    public String getClearName() {
        return clearname;
    }

    /**
     * Updates the clear name according to the language, country, variant and 
     * current Locale settings. If all of these are null, just use the base name.
     */
    public void setClearName() {
        if (language == null) {
            // no language code, use base
            clearname = base;
        } else {
            // construct locale from language, country, variant
            Locale locale;
            if (country == null) {
                locale = new Locale(language);
            } else {
                if (variant == null) {
                    locale = new Locale(language, country);
                } else {
                    locale = new Locale(language, country, variant);
                }
            }
            // get current language and express this language in it for the description
            clearname = Utils.capitalize(locale.getDisplayName());
        }
    }

    /**
     * @return The standard convention file name: base_language_country_variant (only for those who are not null).
     */
    public String toFileName() {
        String s = null;
        if (base != null) {
            s = base;
        }
        if (language != null) {
            s = s + "_" + language;
        }
        if (country != null) {
            s = s + "_" + country;
        }
        if (variant != null) {
            s = s + "_" + variant;
        }
        return s;
    }

    /**
     * @return The language (used for comparison).
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @return The country (used for comparison).
     */
    public String getCountry() {
        return country;
    }

    /**
     * @return The variant (used for comparison).
     */
    public String getVariant() {
        return variant;
    }

    /**
     * @param base The new base identifier.
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * @return The base identifier.
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets language, country, variant all at the same time.
     * 
     * @param language New language.
     * @param country New country.
     * @param variant New variant.
     */
    public void setLanguageCodes(String language, String country, String variant) {
        this.language = language;
        this.country = country;
        this.variant = variant;
    }

    /**
     * Sets language, country, variant from a String array as we get it from the
     * new language dialog.
     * 
     * @param code A string array.
     */
    public void setLanguageCodes(String[] code) {
        if (code.length > 0) {
            language = code[0];
        } else {
            language = null;
        }
        if (code.length > 1) {
            country = code[1];
        } else {
            country = null;
        }
        if (code.length > 2) {
            variant = code[2];
        } else {
            variant = null;
        }
    }

    /**
     * For those keys who are not ending with the commentID, i.e. are not comments,
     * look if they are contained in the map. Used to determine status in the keys
     * table.
     * 
     * @param key The key.
     * @return True if contained and not a comment key.
     */
    public boolean containsAsKey(String key) {
        if (key.endsWith(commentID)) {
            return false;
        }
        return map.get(key) != null;
    }

    /**
     * Returns the text content for a key which is not a comment key.
     * 
     * @param key The key.
     * @return The text of the key in the map or null if key is not contained in the map or if key is a comment key.
     */
    public String getKeyText(String key) {
        if (key.endsWith(commentID)) {
            return null;
        }
        return map.get(key);
    }

    /**
     * Sets new text for a key, potentially also adding new keys to the map.
     * 
     * @param key The key (not ending with commentID)
     * @param text The new text.
     */
    public void putKeyText(String key, String text) {
        if (!key.endsWith(commentID)) {
            map.put(key, text);
        }
    }

    /**
     * For a key (not ending with commentID), return the entry in the map if 
     * commentID is added. The comment for a key is the content of a key to whom
     * the commentID is added.
     * 
     * @param key The key.
     * @return The comment content belonging to this key or null if no comment is existing.
     */
    public String getKeyComment(String key) {
        if (key.endsWith(commentID)) {
            return null;
        }
        return map.get(key + commentID);
    }

    /**
     * Sets new comment for a key, i.e. sets new text for a key who additionally
     * has commentID added. Potentially a new key can be added here.
     * 
     * @param key The key (without commentID).
     * @param comment The new comment text.
     */
    public void putKeyComment(String key, String comment) {
        if (!key.endsWith(commentID)) {
            map.put(key + commentID, comment);
        }
    }

    /**
     * Removing the content of a key, i.e. putting null in the entry for the key
     * and the commentID.
     * 
     * @param key The key whose text and comment is removed.
     */
    public void removeKey(String key) {
        if (key.endsWith(commentID)) {
            return;
        }
        map.put(key, null);
        map.put(key + commentID, null);
    }

    /**
     * Renames a key, neither the old key nor the new key can end with commentID.
     * Removes content of old key and inserts content for new key.
     * 
     * @param oldKey The old key.
     * @param newKey The new key.
     */
    public void renameKey(String oldKey, String newKey) {
        if (oldKey.endsWith(commentID) || newKey.endsWith(commentID)) {
            return;
        }
        putKeyText(newKey, getKeyText(oldKey));
        putKeyComment(newKey, getKeyComment(oldKey));
        removeKey(oldKey);
    }

    /**
     * Determines if a key has been modified. Now there are different conditions
     * which indicate that a key has been modified (relative to the original).
     * We test for them all:
     * - is only modified, if the key is contained (not ending with commentID)
     * - is modified, if it is not contained in the original property (newly created)
     * - if the content is null, then it was removed
     * - if the content is not the same as the content of the original property (also if comments differ)
     * 
     * @param key The key to test.
     * @return True if modified.
     */
    public boolean modified(String key) {
        if (key.endsWith(commentID)) {
            return false;
        }
        // check if we have the key at all
        if (!map.containsKey(key)) {
            return false;
        }
        // if in map but not in prop, it was newly created
        if (!prop.containsKey(key)) {
            return true;
        }
        // if value is null, than it was removed
        if (map.get(key) == null) {
            return true;
        }
        // the key text was changed
        if (!map.get(key).equals(prop.get(key))) {
            return true;
        }
        // comment text was newly added
        if (!prop.containsKey(key + commentID) && map.containsKey(key + commentID)) {
            return true;
        }
        // comment was changed
        if (prop.containsKey(key) && prop.get(key).equals(map.get(key + commentID))) {
            return true;
        }
        return false;
    }

    /**
     * Restores the behavior of a key as it was during loading. First we remove
     * completely the entries, then we copy from the original, but only if the
     * original contains such a key.
     * 
     * @param key The key to restore.
     */
    public void restore(String key) {
        if (key.endsWith(commentID)) {
            return;
        }
        // first remove what we have there
        map.remove(key);
        map.remove(key + commentID);
        // then copy from prop if there is a entry
        if (prop.containsKey(key)) {
            putKeyText(key, prop.get(key));
            if (prop.containsKey(key + commentID)) {
                putKeyComment(key, prop.get(key + commentID));
            }
        }
    }

    /**
     * On rare occasions we want to remove all keys. We do this directly here.
     */
    public void removeAllKeys() {
        for (String key : map.keySet()) {
            map.put(key, null);
        }
    }

    /**
     * Get all valid keys (i.e. that not ending with commentID).
     * 
     * @return A Set of keys.
     */
    public Set<String> getAllTextKeysAsSet() {
        Set<String> set = map.keySet();
        // we need to copy it once more (otherwise iterating over it and deleting results in a ConcurrentModificationException)
        Set<String> newset = new HashSet<String>(200);
        for (String key : set) {
            // if (!key.endsWith(commentID) && map.get(key) != null) {
            if (!key.endsWith(commentID)) {
                newset.add(key);
            }
        }
        return newset;
    }
    
    /**
     * Tests all keys if they are modified.
     * 
     * @return True if at least one key is modified.
     */
    public boolean anyKeyModified() {
        for (String key: getAllTextKeysAsSet()) {
            if (modified(key) == true) {
                return true;
            }
        }
        return false;
    }

    /**
     * Puts the current state into props and save them.
     * 
     * @param resource The resource to save to.
     * @return  True, if saving was successfully.
     */
    public boolean save(Resource resource) {
        // copy from map to prop
        for (String key : map.keySet()) {
            String text;
            text = map.get(key);
            if (text == null) {
                prop.removeKey(key);
            } else {
                prop.put(key, text);
            }
        }
        copyToMap();
        prop.setLocation(resource);
        return prop.save();
    }

    /**
     * Use for comparison (language, country, variant must be equal) to check if
     * in the language tree there is already such a language.
     * 
     * @param other Another object of this type.
     * @return True if language, country and variant are equal.
     */
    public boolean isEqual(LanguageProperties other) {
        if (clearname.equals(other.getClearName())) {
            return true;
        }
        return false;
    }

    /**
     * Used in the beginning and after each save action to start with an unmodified map.
     */
    private void copyToMap() {
        // copy to map
        map.clear();
        for (String key : prop.getKeysAsSet()) {
            map.put(key, prop.get(key));
        }
    }
}
