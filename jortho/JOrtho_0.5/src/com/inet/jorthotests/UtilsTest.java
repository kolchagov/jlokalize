/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2009 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 * Created on 16.06.2009
 */
package com.inet.jorthotests;

import com.inet.jorthodictionaries.BookUtils;

import junit.framework.TestCase;

public class UtilsTest  extends TestCase{
    
    public void testCalcDiff(){
        assertEquals( 3, BookUtils.calcDiff( "", "abc" ) );
        assertEquals( 3, BookUtils.calcDiff( "abc", "" ) );
        assertEquals( 3, BookUtils.calcDiff( "abc", "def" ) );
        
        assertEquals( 1, BookUtils.calcDiff( "abcd", "abzd" ) );
        assertEquals( 1, BookUtils.calcDiff( "abcd", "abd" ) );
        assertEquals( 1, BookUtils.calcDiff( "abd", "abcd" ) );
    }

}
