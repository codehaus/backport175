/*******************************************************************************
 * Copyright (c) 2005 BEA 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    BEA - initial API and implementation
 *******************************************************************************/
package test.annotation;

import junit.framework.TestCase;
import org.codehaus.backport175.reader.Annotation;

/**
 * @test.annotation.NoDuplicateTest.A("ok")
 * @test.annotation.NoDuplicateTest.A("bad")
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class NoDuplicateTest extends TestCase {

    /**
     * @test.annotation.NoDuplicateTest.A
     * @test.annotation.NoDuplicateTest.A
     */
    public void method() {}

    /**
     * @test.annotation.NoDuplicateTest.A
     * @test.annotation.NoDuplicateTest.A
     */
    public int field;

    /**
     * @test.annotation.NoDuplicateTest.A
     * @test.annotation.NoDuplicateTest.A
     */
    public NoDuplicateTest() {
    }

    public void testAnnotationCFailedSoNoAnnotationAtAll() {
        Annotation[] anns = org.codehaus.backport175.reader.Annotations.getAnnotations(NoDuplicateTest.class);
        assertEquals(0, anns.length);

        
    }

    //-- junit
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(NoDuplicateTest.class);
    }

    //-- annotation
    public static interface A {

        public String value();

    }

}
