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
package org.jlokalize.i18n;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.jlokalize.LanguageTreeProject;
import org.junit.Test;

/**
 *
 * @author Trilarion
 */
public class LanguageTreeTest {

    private static final Logger LOG = Logger.getLogger(LanguageTreeTest.class.getName());

    /**
     * 
     * @throws IOException
     */
    @Test
    public void ReadingIntoTreeTest() throws IOException {
        LanguageTreeProject project = new LanguageTreeProject();
        File file = new File("lang//JLokalize.properties");
        project.open(null);
    }
}
