/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.compiler.validation;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TypeRefAnnoTarget {

    /**
     * @test.compiler.validation.ValidationTest.TypeRefAnno(1)
     */
    public void nonWellFormedAnno0() {
    }

    /**
     * @test.compiler.validation.ValidationTest.TypeRefAnno(test.compiler.validation.Target)
     */
    public void nonWellFormedAnno1() {
    }

    /**
     * @test.compiler.validation.ValidationTest.TypeRefAnno(test.compiler.validation.MyEnum.VALUE)
     */
    public void nonWellFormedAnno2() {
    }
}