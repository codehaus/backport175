/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.defaultvalue;

import junit.framework.TestCase;
import org.codehaus.backport175.reader.Annotations;

/**
 * @test.defaultvalue.TestAnnotations.IntAnno
 * @test.defaultvalue.TestAnnotations.StringAnno
 * @test.defaultvalue.TestAnnotations.FloatArrayAnno
 * @test.defaultvalue.TestAnnotations.AnnoArrayAnno
 * @test.defaultvalue.TestAnnotations.DefaultedClassAnno
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DefaultTest extends TestCase {

    private static final Class KLASS = DefaultTest.class;

    public void testInt() {
        TestAnnotations.IntAnno anno = (TestAnnotations.IntAnno) Annotations.getAnnotation(TestAnnotations.IntAnno.class, KLASS);
        assertNotNull(anno);
        assertEquals(3, anno.value());
    }

    public void testString() {
        TestAnnotations.StringAnno anno = (TestAnnotations.StringAnno) Annotations.getAnnotation(TestAnnotations.StringAnno.class, KLASS);
        assertNotNull(anno);
        assertEquals("hello", anno.s());
    }

    public void testFloatArray() {
        TestAnnotations.FloatArrayAnno anno = (TestAnnotations.FloatArrayAnno) Annotations.getAnnotation(TestAnnotations.FloatArrayAnno.class, KLASS);
        assertNotNull(anno);
        float[] ff = anno.ff();
        assertNotNull(ff);
        assertEquals(2, ff.length);
        assertTrue(0f == ff[0]);
        assertTrue(1f == ff[1]);
    }

    public void testAnno() {
        TestAnnotations.AnnoArrayAnno anno = (TestAnnotations.AnnoArrayAnno) Annotations.getAnnotation(TestAnnotations.AnnoArrayAnno.class, KLASS);
        assertNotNull(anno);
        TestAnnotations.AnAnno[] annos = anno.annos();
        assertEquals(2, annos.length);
        assertEquals("one", annos[0].message());
        assertEquals("two", annos[1].message());
    }

    public void testDefaultedClassAnno() {
        TestAnnotations.DefaultedClassAnno anno = (TestAnnotations.DefaultedClassAnno) Annotations.getAnnotation(TestAnnotations.DefaultedClassAnno.class, KLASS);
        assertNotNull(anno);
        assertEquals(TestAnnotations.SomeNestedClass.class, anno.klass());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DefaultTest.class);
    }
}
