/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.filtering;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import org.codehaus.backport175.reader.Annotations;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class MemberFilteringTest extends TestCase {

    public static interface A {}
    public static interface B {}

    private static Field fieldA;
    private static Field fieldB;
    private static Method methodA;
    private static Method methodB;
    private static Constructor ctorA;
    private static Constructor ctorB;

    static {
        try {
            fieldA = Target.class.getDeclaredField("A");
            fieldB = Target.class.getDeclaredField("B");
            ctorA = Target.class.getDeclaredConstructor(new Class[0]);
            ctorB = Target.class.getDeclaredConstructor(new Class[]{int[][].class, Object[].class, boolean.class});
            methodA = Target.class.getDeclaredMethod(
                    "A",
                    new Class[]{
                        String.class, int.class, double.class,
                        float.class, byte.class, char.class,
                        short.class, long.class, boolean.class
                    }
            );
            methodB = Target.class.getDeclaredMethod(
                    "B",
                    new Class[]{
                        String.class, int.class, double.class,
                        float.class, byte.class, char.class,
                        short.class, long.class, boolean.class
                    }
            );
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public MemberFilteringTest(String name) {
        super(name);
    }

    public void testConstructorAnnotationFilter() {
        assertTrue(Annotations.isAnnotationPresent(A.class, ctorA));
        assertTrue(Annotations.isAnnotationPresent(B.class, ctorB));
        assertFalse(Annotations.isAnnotationPresent(B.class, ctorA));
        assertFalse(Annotations.isAnnotationPresent(A.class, ctorB));
    }

    public void testMethodAnnotationFilter() {
        assertTrue(Annotations.isAnnotationPresent(A.class, methodA));
        assertTrue(Annotations.isAnnotationPresent(B.class, methodB));
        assertFalse(Annotations.isAnnotationPresent(B.class, methodA));
        assertFalse(Annotations.isAnnotationPresent(A.class, methodB));
    }

    public void testFieldAnnotationFilter() {
        assertTrue(Annotations.isAnnotationPresent(A.class, fieldA));
        assertTrue(Annotations.isAnnotationPresent(B.class, fieldB));
        assertFalse(Annotations.isAnnotationPresent(B.class, fieldA));
        assertFalse(Annotations.isAnnotationPresent(A.class, fieldB));
    }

    // === for testing Java 5 reflection compatibility ===

//    public void testClassAnnReflection() {
//        Class klass = Target.class;
//        java.lang.reader.Annotation[] annotations = klass.getAnnotations();
//        assertTrue(annotations.length > 1);
//    }
//
//    public void testMethodAnnReflection() {
//        java.lang.reader.Annotation[] annotations = method.getAnnotations();
//        assertTrue(annotations.length > 0);
//    }
//
//    public void testReadRealJava5Ann() {
//        Annotation reader = org.codehaus.backport175.reader.TestAnnotations.getAnnotation(
//                "test.Target$Test", Target.class
//        );
//        Class type = reader.annotationType();
//        assertEquals(Target.Test.class, type);
//
//        Target.Test test = (Target.Test)reader;
//        assertEquals("test", test.test());
//    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(MemberFilteringTest.class);
    }
}
