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
package test.noduplicate;

import junit.framework.TestCase;
import org.codehaus.backport175.reader.Annotation;

import java.lang.reflect.Method;

/**
 * @test.noduplicate.NoDuplicateTest.A("ok")
 * Xtest.noduplicate.NoDuplicateTest.A("bad")
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class NoDuplicateTest extends TestCase {

    /**
     * @test.noduplicate.NoDuplicateTest.A
     * @test.noduplicate.NoDuplicateTest.A
     */
    public void method() {}

    /**
     * @test.noduplicate.NoDuplicateTest.A
     * @test.noduplicate.NoDuplicateTest.A
     */
    public int field;

    /**
     * @test.noduplicate.NoDuplicateTest.A
     * @test.noduplicate.NoDuplicateTest.A
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
