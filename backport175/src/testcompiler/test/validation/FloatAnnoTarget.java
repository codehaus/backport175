/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.validation;

public class FloatAnnoTarget {

    /**
     * @test.validation.ValidationTest.FloatAnno({123.456})
     */
    public void nonWellFormedAnno0() {
    }

    /**
     * @test.validation.ValidationTest.FloatAnno("123.456F")
     */
    public void nonWellFormedAnno1() {
    }

    /**
     * @test.validation.ValidationTest.FloatAnno(4)
     */
    public void nonWellFormedAnno2() {
    }
}