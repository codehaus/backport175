/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://backport175.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.reader;

import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.Annotations;
import org.codehaus.backport175.reader.bytecode.AnnotationElement;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.HashSet;

import test.Target;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationReaderTest extends TestCase {

    public AnnotationReaderTest(String name) {
        super(name);
    }

    public void testToString() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Complex", Target.METHOD
        );
        assertEquals(
                "@test.reader.TestAnnotations$Complex(" +
                "i=111, " +
                "doubleArr=[1.1, 2.2, 3.3, 4.4], " +
                "type=double[][][].class, " +
                "enumeration=org.codehaus.backport175.reader.bytecode.AnnotationElement$Type.ANNOTATION, " +
                "typeArr=[test.Target[].class, test.Target.class]" +
                ")",
                annotation.toString()
        );
    }

    public void testClassIsAnnotationPresent() {
        assertTrue(Annotations.isAnnotationPresent(
                TestAnnotations.VoidTyped.class, Target.class
        ));
        assertFalse(Annotations.isAnnotationPresent(Target.class, Target.class));
    }

    public void testClassAnn1() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$VoidTyped", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.VoidTyped.class, type);
    }

    public void testClassAnn2() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$DefaultString", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.DefaultString.class, type);

        TestAnnotations.DefaultString ann = (TestAnnotations.DefaultString)annotation;
        assertEquals("hello", ann.value());
    }

    public void testClassAnn3() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Simple", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Simple.class, type);

        TestAnnotations.Simple ann = (TestAnnotations.Simple)annotation;
        assertEquals("foo", ann.val());
        assertEquals("bar", ann.s());
    }

    public void testClassAnn4() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$StringArray", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.StringArray.class, type);

        TestAnnotations.StringArray ann = (TestAnnotations.StringArray)annotation;
        String[] ss = ann.ss();
        assertEquals("hello", ss[0]);
        assertEquals("world", ss[1]);
    }

    public void testClassAnn5() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$LongArray", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.LongArray.class, type);

        TestAnnotations.LongArray ann = (TestAnnotations.LongArray)annotation;
        long[] longArr = ann.l();
        assertEquals(1l, longArr[0]);
        assertEquals(2l, longArr[1]);
        assertEquals(6l, longArr[2]);
    }

    public void testClassAnnDuplicatedMapping() {
        Annotation[] annotations = Annotations.getAnnotations(Target.class);
        int found = 0;
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            if (annotation instanceof TestAnnotations.LongArray) {
                found++;
            }
        }
        assertEquals(1, found);
    }

    public void testClassAnnNotThere() {
        Annotation annotation = Annotations.getAnnotation("noThere", Target.class);
        assertNull(annotation);
    }

    public void testClassAnn6() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Complex", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Complex.class, type);

        TestAnnotations.Complex ann = (TestAnnotations.Complex)annotation;
        assertEquals(3, ann.i());

        double[] doubleArr = ann.doubleArr();
        assertEquals(1.1D, doubleArr[0], 0);
        assertEquals(1.2D, doubleArr[1], 0);
        assertEquals(1234.123456d, doubleArr[2], 0);

        assertEquals(String[][].class, ann.type());
    }

    public void testClassAnn7() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$NestedAnnotation", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotation.class, type);

        TestAnnotations.NestedAnnotation ann = (TestAnnotations.NestedAnnotation)annotation;
        TestAnnotations.Simple simple = ann.ann();
        assertEquals("foo", simple.val());
    }

    public void testClassAnn8() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$NestedAnnotationArray", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotationArray.class, type);

        TestAnnotations.NestedAnnotationArray ann = (TestAnnotations.NestedAnnotationArray)annotation;
        TestAnnotations.Simple[] simpleAnnArray = ann.annArr();
        assertEquals("foo", simpleAnnArray[0].val());
        assertEquals("bar", simpleAnnArray[1].val());
    }

    public void testClassAnnArray() {
        Annotation[] annotations = Annotations.getAnnotations(Target.class);
        Set set = new HashSet();
        for (int i = 0; i < annotations.length; i++) {
            set.add(annotations[i].annotationType());
        }
        assertTrue(set.contains(TestAnnotations.NestedAnnotationArray.class));
        assertTrue(set.contains(TestAnnotations.VoidTyped.class));
        assertTrue(set.contains(TestAnnotations.Complex.class));
        assertTrue(set.contains(TestAnnotations.StringArray.class));
        assertTrue(set.contains(TestAnnotations.DefaultString.class));
        assertTrue(set.contains(TestAnnotations.Simple.class));
        assertTrue(set.contains(TestAnnotations.NestedAnnotation.class));
        assertTrue(set.contains(TestAnnotations.LongArray.class));
        annotations = Annotations.getAnnotations(Target.class);
    }

    public void testFieldIsAnnotationPresent() {
        assertTrue(Annotations.isAnnotationPresent(
                TestAnnotations.Simple.class, Target.FIELD
        ));
        assertFalse(Annotations.isAnnotationPresent(Target.class, Target.FIELD));
    }

    public void testFieldAnn1() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$VoidTyped", Target.FIELD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.VoidTyped.class, type);
    }

    public void testFieldAnn2() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$DefaultString", Target.FIELD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.DefaultString.class, type);

        TestAnnotations.DefaultString ann = (TestAnnotations.DefaultString)annotation;
        assertEquals("hello", ann.value());
    }

    public void testFieldAnn3() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Simple", Target.FIELD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Simple.class, type);

        TestAnnotations.Simple ann = (TestAnnotations.Simple)annotation;
        assertEquals("foo", ann.val());
        assertEquals("bar", ann.s());
    }

    public void testFieldAnn4() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$StringArray", Target.FIELD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.StringArray.class, type);

        TestAnnotations.StringArray ann = (TestAnnotations.StringArray)annotation;
        String[] ss = ann.ss();
        assertEquals("hello", ss[0]);
        assertEquals("world", ss[1]);
    }

    public void testFieldAnn5() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$LongArray", Target.FIELD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.LongArray.class, type);

        TestAnnotations.LongArray ann = (TestAnnotations.LongArray)annotation;
        long[] longArr = ann.l();
        assertEquals(1l, longArr[0]);
        assertEquals(2l, longArr[1]);
        assertEquals(6l, longArr[2]);
    }

    public void testFieldAnn6() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Complex", Target.FIELD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Complex.class, type);

        TestAnnotations.Complex ann = (TestAnnotations.Complex)annotation;
        assertEquals(3, ann.i());

        double[] doubleArr = ann.doubleArr();
        assertEquals(1.1D, doubleArr[0], 0);
        assertEquals(1.2D, doubleArr[1], 0);
        assertEquals(1234.123456d, doubleArr[2], 0);

        assertEquals(int.class, ann.type());
    }

    public void testFieldAnn7() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$NestedAnnotation", Target.FIELD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotation.class, type);

        TestAnnotations.NestedAnnotation ann = (TestAnnotations.NestedAnnotation)annotation;
        TestAnnotations.Simple simple = ann.ann();
        assertEquals("foo", simple.val());
    }

    public void testFieldAnn8() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$NestedAnnotationArray", Target.FIELD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotationArray.class, type);

        TestAnnotations.NestedAnnotationArray ann = (TestAnnotations.NestedAnnotationArray)annotation;
        TestAnnotations.Simple[] simpleAnnArray = ann.annArr();
        assertEquals("foo", simpleAnnArray[0].val());
        assertEquals("bar", simpleAnnArray[1].val());
    }

    public void testFieldAnnArray() {
        Annotation[] annotations = Annotations.getAnnotations(Target.FIELD);
        Set set = new HashSet();
        for (int i = 0; i < annotations.length; i++) {
            set.add(annotations[i].annotationType());
        }
        assertTrue(set.contains(TestAnnotations.NestedAnnotationArray.class));
        assertTrue(set.contains(TestAnnotations.VoidTyped.class));
        assertTrue(set.contains(TestAnnotations.Complex.class));
        assertTrue(set.contains(TestAnnotations.StringArray.class));
        assertTrue(set.contains(TestAnnotations.DefaultString.class));
        assertTrue(set.contains(TestAnnotations.Simple.class));
        assertTrue(set.contains(TestAnnotations.NestedAnnotation.class));
        assertTrue(set.contains(TestAnnotations.LongArray.class));
        annotations = Annotations.getAnnotations(Target.FIELD);
    }

    public void testConstructorIsAnnotationPresent() {
        assertTrue(Annotations.isAnnotationPresent(
                TestAnnotations.Complex.class, Target.CONSTRUCTOR
        ));
        assertFalse(Annotations.isAnnotationPresent(Target.class, Target.CONSTRUCTOR));
    }

    public void testConstructorAnn1() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$VoidTyped", Target.CONSTRUCTOR
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.VoidTyped.class, type);
    }

    public void testConstructorAnn2() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$DefaultString", Target.CONSTRUCTOR
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.DefaultString.class, type);

        TestAnnotations.DefaultString ann = (TestAnnotations.DefaultString)annotation;
        assertEquals("hello", ann.value());
    }

    public void testConstructorAnn3() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Simple", Target.CONSTRUCTOR
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Simple.class, type);

        TestAnnotations.Simple ann = (TestAnnotations.Simple)annotation;
        assertEquals("foo", ann.val());
        assertEquals("bar", ann.s());
    }

    public void testConstructorAnn4() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$StringArray", Target.CONSTRUCTOR
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.StringArray.class, type);

        TestAnnotations.StringArray ann = (TestAnnotations.StringArray)annotation;
        String[] ss = ann.ss();
        assertEquals("hello", ss[0]);
        assertEquals("world", ss[1]);
    }

    public void testConstructorAnn5() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$LongArray", Target.CONSTRUCTOR
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.LongArray.class, type);

        TestAnnotations.LongArray ann = (TestAnnotations.LongArray)annotation;
        long[] longArr = ann.l();
        assertEquals(1l, longArr[0]);
        assertEquals(2l, longArr[1]);
        assertEquals(6l, longArr[2]);
    }

    public void testConstructorAnn6() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$NestedAnnotation", Target.CONSTRUCTOR
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotation.class, type);

        TestAnnotations.NestedAnnotation ann = (TestAnnotations.NestedAnnotation)annotation;
        TestAnnotations.Simple simple = ann.ann();
        assertEquals("foo", simple.val());
    }

    public void testConstructorAnn7() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$NestedAnnotationArray", Target.CONSTRUCTOR
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotationArray.class, type);

        TestAnnotations.NestedAnnotationArray ann = (TestAnnotations.NestedAnnotationArray)annotation;
        TestAnnotations.Simple[] simpleAnnArray = ann.annArr();
        assertEquals("foo", simpleAnnArray[0].val());
        assertEquals("bar", simpleAnnArray[1].val());
    }

    public void testConstructorAnn8() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Complex", Target.CONSTRUCTOR
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Complex.class, type);

        TestAnnotations.Complex ann = (TestAnnotations.Complex)annotation;
        assertEquals(111, ann.i());

        double[] doubleArr = ann.doubleArr();
        assertEquals(1.1D, doubleArr[0], 0);
        assertEquals(2.2D, doubleArr[1], 0);
        assertEquals(3.3D, doubleArr[2], 0);
        assertEquals(4.4D, doubleArr[3], 0);

        assertEquals(double[][][].class, ann.type());

        Class[] types = ann.typeArr();
        assertEquals(Target[].class, types[0]);
        assertEquals(Target.class, types[1]);

        AnnotationElement.Type enumRef = ann.enumeration();
        assertTrue(enumRef.equals(AnnotationElement.Type.ANNOTATION));
    }

    public void testConstructorAnnArray() {
        Annotation[] annotations = Annotations.getAnnotations(Target.CONSTRUCTOR);
        Set set = new HashSet();
        for (int i = 0; i < annotations.length; i++) {
            set.add(annotations[i].annotationType());
        }
        assertTrue(set.contains(TestAnnotations.NestedAnnotationArray.class));
        assertTrue(set.contains(TestAnnotations.VoidTyped.class));
        assertTrue(set.contains(TestAnnotations.Complex.class));
        assertTrue(set.contains(TestAnnotations.StringArray.class));
        assertTrue(set.contains(TestAnnotations.DefaultString.class));
        assertTrue(set.contains(TestAnnotations.Simple.class));
        assertTrue(set.contains(TestAnnotations.NestedAnnotation.class));
        assertTrue(set.contains(TestAnnotations.LongArray.class));
        annotations = Annotations.getAnnotations(Target.CONSTRUCTOR);
    }

    public void testMethodIsAnnotationPresent() {
        assertTrue(Annotations.isAnnotationPresent(
                TestAnnotations.NestedAnnotationArray.class, Target.METHOD
        ));
        assertFalse(Annotations.isAnnotationPresent(Target.class, Target.METHOD));
    }

    public void testMethodAnn1() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$VoidTyped", Target.METHOD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.VoidTyped.class, type);
    }

    public void testMethodAnn2() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$DefaultString", Target.METHOD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.DefaultString.class, type);

        TestAnnotations.DefaultString ann = (TestAnnotations.DefaultString)annotation;
        assertEquals("hello", ann.value());
    }

    public void testMethodAnn3() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Simple", Target.METHOD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Simple.class, type);

        TestAnnotations.Simple ann = (TestAnnotations.Simple)annotation;
        assertEquals("foo", ann.val());
        assertEquals("bar", ann.s());
    }

    public void testMethodAnn4() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$StringArray", Target.METHOD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.StringArray.class, type);

        TestAnnotations.StringArray ann = (TestAnnotations.StringArray)annotation;
        String[] ss = ann.ss();
        assertEquals("hello", ss[0]);
        assertEquals("world", ss[1]);
    }

    public void testMethodAnn5() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$LongArray", Target.METHOD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.LongArray.class, type);

        TestAnnotations.LongArray ann = (TestAnnotations.LongArray)annotation;
        long[] longArr = ann.l();
        assertEquals(1l, longArr[0]);
        assertEquals(2l, longArr[1]);
        assertEquals(6l, longArr[2]);
    }

    public void testMethodAnn6() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$NestedAnnotation", Target.METHOD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotation.class, type);

        TestAnnotations.NestedAnnotation ann = (TestAnnotations.NestedAnnotation)annotation;
        TestAnnotations.Simple simple = ann.ann();
        assertEquals("foo", simple.val());
    }

    public void testMethodAnn7() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$NestedAnnotationArray", Target.METHOD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotationArray.class, type);

        TestAnnotations.NestedAnnotationArray ann = (TestAnnotations.NestedAnnotationArray)annotation;
        TestAnnotations.Simple[] simpleAnnArray = ann.annArr();
        assertEquals("foo", simpleAnnArray[0].val());
        assertEquals("bar", simpleAnnArray[1].val());
    }

    public void testMethodAnn8() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Complex", Target.METHOD
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Complex.class, type);

        TestAnnotations.Complex ann = (TestAnnotations.Complex)annotation;
        assertEquals(111, ann.i());

        double[] doubleArr = ann.doubleArr();
        assertEquals(1.1D, doubleArr[0], 0);
        assertEquals(2.2D, doubleArr[1], 0);
        assertEquals(3.3D, doubleArr[2], 0);
        assertEquals(4.4D, doubleArr[3], 0);

        assertEquals(double[][][].class, ann.type());

        Class[] types = ann.typeArr();
        assertEquals(Target[].class, types[0]);
        assertEquals(Target.class, types[1]);

        AnnotationElement.Type enumRef = ann.enumeration();
        assertTrue(enumRef.equals(AnnotationElement.Type.ANNOTATION));
    }

    public void testMethodAnnArray() {
        Annotation[] annotations = Annotations.getAnnotations(Target.METHOD);
        Set set = new HashSet();
        for (int i = 0; i < annotations.length; i++) {
            set.add(annotations[i].annotationType());
        }
        assertTrue(set.contains(TestAnnotations.NestedAnnotationArray.class));
        assertTrue(set.contains(TestAnnotations.VoidTyped.class));
        assertTrue(set.contains(TestAnnotations.Complex.class));
        assertTrue(set.contains(TestAnnotations.StringArray.class));
        assertTrue(set.contains(TestAnnotations.DefaultString.class));
        assertTrue(set.contains(TestAnnotations.Simple.class));
        assertTrue(set.contains(TestAnnotations.NestedAnnotation.class));
        assertTrue(set.contains(TestAnnotations.LongArray.class));
        annotations = Annotations.getAnnotations(Target.METHOD);
    }

    public void testReadInResolvedValues() {
        Annotation annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Complex", Target.METHOD
        );
        TestAnnotations.Complex ann = (TestAnnotations.Complex)annotation;
        assertEquals(111, ann.i());
        double[] doubleArr = ann.doubleArr();
        assertEquals(1.1D, doubleArr[0], 0);
        assertEquals(2.2D, doubleArr[1], 0);
        assertEquals(3.3D, doubleArr[2], 0);
        assertEquals(4.4D, doubleArr[3], 0);
        assertEquals(double[][][].class, ann.type());
        Class[] types = ann.typeArr();
        assertEquals(Target[].class, types[0]);
        assertEquals(Target.class, types[1]);
        AnnotationElement.Type enumRef = ann.enumeration();
        assertTrue(enumRef.equals(AnnotationElement.Type.ANNOTATION));

        // second time -> values are not resolved again but cache is used
        annotation = Annotations.getAnnotation(
                "test.reader.TestAnnotations$Complex", Target.METHOD
        );
        ann = (TestAnnotations.Complex)annotation;
        assertEquals(111, ann.i());
        doubleArr = ann.doubleArr();
        assertEquals(1.1D, doubleArr[0], 0);
        assertEquals(2.2D, doubleArr[1], 0);
        assertEquals(3.3D, doubleArr[2], 0);
        assertEquals(4.4D, doubleArr[3], 0);
        assertEquals(double[][][].class, ann.type());
        types = ann.typeArr();
        assertEquals(Target[].class, types[0]);
        assertEquals(Target.class, types[1]);
        enumRef = ann.enumeration();
        assertTrue(enumRef.equals(AnnotationElement.Type.ANNOTATION));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AnnotationReaderTest.class);
    }
}
