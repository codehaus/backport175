/*******************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.validation;

public class IntArrayAnnoTarget {

    /**
     * @test.validation.ValidationTest.IntArrayAnno({{1, 2, 3, 4}, 1)
     */
    public void nonWellFormedAnno0() {
    }

    /**
     * @test.validation.ValidationTest.IntArrayAnno({"error", 2, 3, 4})
     */
    public void nonWellFormedAnno1() {
    }

    /**
     * @test.validation.ValidationTest.IntArrayAnno(1)
     */
    public void nonWellFormedAnno2() {
    }
}