/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://backport175.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.java5;

import org.codehaus.backport175.reader.Annotation;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationReaderTest extends TestCase {

    private static final Field field;
    private static final Method method;
    private static final Constructor constructor;

    static {
        try {
            field = Target.class.getDeclaredField("field");
            method = Target.class.getDeclaredMethod("method", new Class[]{});
            constructor = Target.class.getDeclaredConstructor(new Class[]{});
        } catch (Exception e) {
            fail(e.toString());
            throw new RuntimeException(e.toString());
        }
    }

    public AnnotationReaderTest(String name) {
        super(name);
    }

    public void testClassAnnReflection() {
        Class klass = Target.class;
        java.lang.annotation.Annotation[] annotations = klass.getAnnotations();
        assertTrue(annotations.length > 1);
    }

    public void testMethodAnnReflection() {
        java.lang.annotation.Annotation[] annotations = method.getAnnotations();
        assertTrue(annotations.length > 0);
    }

    public void testJava5ClassAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                test.java5.Target.Test.class, Target.class
        );
        Class type = reader.annotationType();
        assertEquals(Target.Test.class, type);

        Target.Test test = (Target.Test)reader;
        assertEquals("test", test.test());
    }

    public void testJava5ConstructorAnnotation() {
        assertFalse(true);
    }

    public void testJava5MethodAnnotation() {
        assertFalse(true);
    }

    public void testJava5FieldAnnotation() {
        assertFalse(true);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AnnotationReaderTest.class);
    }
}
