/*******************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.annotation;

import junit.framework.TestCase;
import org.codehaus.backport175.reader.Annotation;

import java.lang.reflect.Method;

/**
 * @test.annotation.NoDuplicateTest.A("ok")
 * Xtest.annotation.NoDuplicateTest.A("bad")
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

    public void testAnnotationCFailedSoNoAnnotationAtAllOnClass() {
        Annotation[] anns = org.codehaus.backport175.reader.Annotations.getAnnotations(NoDuplicateTest.class);
        assertEquals(0, anns.length);
    }

    public void testAnnotationCFailedSoNoAnnotationAtAllOnMethod() throws Throwable {
        Annotation[] anns = org.codehaus.backport175.reader.Annotations.getAnnotations(
                NoDuplicateTest.class.getDeclaredMethod("method", new Class[0]));
        assertEquals(0, anns.length);
    }

    public void testAnnotationCFailedSoNoAnnotationAtAllOnField() throws Throwable {
        Annotation[] anns = org.codehaus.backport175.reader.Annotations.getAnnotations(
                NoDuplicateTest.class.getDeclaredField("field"));
        assertEquals(0, anns.length);
    }

    public void testAnnotationCFailedSoNoAnnotationAtAllOnCtor() throws Throwable {
        Annotation[] anns = org.codehaus.backport175.reader.Annotations.getAnnotations(
                NoDuplicateTest.class.getDeclaredConstructor(new Class[0]));
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
