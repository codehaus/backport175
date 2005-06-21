/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.primitives;

import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.Annotations;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PrimitiveTest extends TestCase {

    public PrimitiveTest(String name) {
        super(name);
    }

    public void testLong() {
        Annotation annotation = Annotations.getAnnotation(
                Target.Long.class, Target.class
        );
        assertEquals(Target.Long.class, annotation.annotationType());
        Target.Long ann = (Target.Long)annotation;
        assertEquals(123456789123456789L, ann.v());
        assertEquals(1L, ann.arr()[0]);
        assertEquals(2L, ann.arr()[1]);
        assertEquals(3L, ann.arr()[2]);
        assertEquals(4L, ann.arr()[3]);
    }

    public void testInt() {
        Annotation annotation = Annotations.getAnnotation(
                Target.Integer.class, Target.class
        );
        assertEquals(Target.Integer.class, annotation.annotationType());
        Target.Integer ann = (Target.Integer)annotation;
        assertEquals(1, ann.v());
        assertEquals(1, ann.arr()[0]);
        assertEquals(2, ann.arr()[1]);
        assertEquals(3, ann.arr()[2]);
        assertEquals(4, ann.arr()[3]);
    }

    public void testDouble() {
        Annotation annotation = Annotations.getAnnotation(
                test.primitives.Target.Double.class, Target.class
        );
        assertEquals(Target.Double.class, annotation.annotationType());
        Target.Double ann = (Target.Double)annotation;
        assertEquals(1.123456789123456789D, ann.v(), 0);
        assertEquals(1.0D, ann.arr()[0], 0);
        assertEquals(2.4D, ann.arr()[1], 0);
        assertEquals(3.56D, ann.arr()[2], 0);
        assertEquals(4.0D, ann.arr()[3], 0);
    }

    public void testFloat() {
        Annotation annotation = Annotations.getAnnotation(
                Target.Float.class, Target.class
        );
        assertEquals(Target.Float.class, annotation.annotationType());
        Target.Float ann = (Target.Float)annotation;
        assertEquals(1.0F, ann.v(), 0);
        assertEquals(1.1F, ann.arr()[0], 0);
        assertEquals(2.3455F, ann.arr()[1], 0);
        assertEquals(3.0F, ann.arr()[2], 0);
        assertEquals(4.0F, ann.arr()[3], 0);
    }

    public void testBoolean() {
        Annotation annotation = Annotations.getAnnotation(
                Target.Boolean.class, Target.class
        );
        assertEquals(Target.Boolean.class, annotation.annotationType());
        Target.Boolean ann = (Target.Boolean)annotation;
        assertTrue(ann.v());
        assertTrue(ann.arr()[0]);
        assertFalse(ann.arr()[1]);
        assertTrue(ann.arr()[2]);
        assertFalse(ann.arr()[3]);
    }

    public void testChar() {
        Annotation annotation = Annotations.getAnnotation(
                Target.Char.class, Target.class
        );
        assertEquals(Target.Char.class, annotation.annotationType());
        Target.Char ann = (Target.Char)annotation;
        assertEquals('a', ann.v());
        assertEquals('b', ann.arr()[0]);
        assertEquals('C', ann.arr()[1]);
        assertEquals('D', ann.arr()[2]);
        assertEquals('e', ann.arr()[3]);
    }

    public void testDefaultedLong() {
        Annotation annotation = Annotations.getAnnotation(
                Target.DefaultedLong.class, Target.class
        );
        assertEquals(0L, ((Target.DefaultedLong)annotation).l());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(PrimitiveTest.class);
    }
}
