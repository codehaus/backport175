/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.validation;

public class EnumAnnoTarget {

    /**
     * @test.validation.ValidationTest.EnumAnno(test.validation.MyEnum.class)
     */
    public void nonWellFormedAnno0() {
    }

    /**
     * @test.validation.ValidationTest.EnumAnno(test.validation.MyEnum.WALUE)
     */
    public void nonWellFormedAnno1() {
    }

    /**
     * @test.validation.ValidationTest.EnumAnno(123)
     */
    public void nonWellFormedAnno2() {
    }
}