/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://backport175.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.annotation;

import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.bytecode.AnnotationElement;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.HashSet;

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
            fail();
            throw new RuntimeException(e.toString());
        }
    }

    public AnnotationReaderTest(String name) {
        super(name);
    }

    public void testToString() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Complex", method
        );
        assertEquals(
                "@test.annotation.TestAnnotations$Complex(" +
                "i=111, " +
                "doubleArr=[1.1, 2.2, 3.3, 4.4], " +
                "type=double[][][].class, " +
                "enumeration=org.codehaus.backport175.reader.bytecode.AnnotationElement$Type.ANNOTATION, " +
                "typeArr=[test.annotation.Target[].class, test.annotation.Target.class]" +
                ")",
                annotation.toString()
        );
    }

    public void testClassIsAnnotationPresent() {
        assertTrue(org.codehaus.backport175.reader.Annotations.isAnnotationPresent(
                TestAnnotations.VoidTyped.class, Target.class
        ));
        assertFalse(org.codehaus.backport175.reader.Annotations.isAnnotationPresent(Target.class, Target.class));
    }

    public void testClassAnn1() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$VoidTyped", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.VoidTyped.class, type);
    }

    public void testClassAnn2() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$DefaultString", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.DefaultString.class, type);

        TestAnnotations.DefaultString ann = (TestAnnotations.DefaultString)annotation;
        assertEquals("hello", ann.value());
    }

    public void testClassAnn3() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Simple", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Simple.class, type);

        TestAnnotations.Simple ann = (TestAnnotations.Simple)annotation;
        assertEquals("foo", ann.val());
        assertEquals("bar", ann.s());
    }

    public void testClassAnn4() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$StringArray", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.StringArray.class, type);

        TestAnnotations.StringArray ann = (TestAnnotations.StringArray)annotation;
        String[] ss = ann.ss();
        assertEquals("hello", ss[0]);
        assertEquals("world", ss[1]);
    }

    public void testClassAnn5() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$LongArray", Target.class
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
        Annotation[] annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(Target.class);
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
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "noThere", Target.class
        );
        assertNull(annotation);
    }

    public void testClassAnn6() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Complex", Target.class
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
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$NestedAnnotation", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotation.class, type);

        TestAnnotations.NestedAnnotation ann = (TestAnnotations.NestedAnnotation)annotation;
        TestAnnotations.Simple simple = ann.ann();
        assertEquals("foo", simple.val());
    }

    public void testClassAnn8() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$NestedAnnotationArray", Target.class
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotationArray.class, type);

        TestAnnotations.NestedAnnotationArray ann = (TestAnnotations.NestedAnnotationArray)annotation;
        TestAnnotations.Simple[] simpleAnnArray = ann.annArr();
        assertEquals("foo", simpleAnnArray[0].val());
        assertEquals("bar", simpleAnnArray[1].val());
    }

    public void testClassAnnArray() {
        Annotation[] annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(Target.class);
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
        annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(Target.class);
    }

    public void testFieldIsAnnotationPresent() {
        assertTrue(org.codehaus.backport175.reader.Annotations.isAnnotationPresent(
                TestAnnotations.Simple.class, field
        ));
        assertFalse(org.codehaus.backport175.reader.Annotations.isAnnotationPresent(Target.class, field));
    }

    public void testFieldAnn1() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$VoidTyped", field
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.VoidTyped.class, type);
    }

    public void testFieldAnn2() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$DefaultString", field
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.DefaultString.class, type);

        TestAnnotations.DefaultString ann = (TestAnnotations.DefaultString)annotation;
        assertEquals("hello", ann.value());
    }

    public void testFieldAnn3() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Simple", field
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Simple.class, type);

        TestAnnotations.Simple ann = (TestAnnotations.Simple)annotation;
        assertEquals("foo", ann.val());
        assertEquals("bar", ann.s());
    }

    public void testFieldAnn4() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$StringArray", field
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.StringArray.class, type);

        TestAnnotations.StringArray ann = (TestAnnotations.StringArray)annotation;
        String[] ss = ann.ss();
        assertEquals("hello", ss[0]);
        assertEquals("world", ss[1]);
    }

    public void testFieldAnn5() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$LongArray", field
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
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Complex", field
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
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$NestedAnnotation", field
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotation.class, type);

        TestAnnotations.NestedAnnotation ann = (TestAnnotations.NestedAnnotation)annotation;
        TestAnnotations.Simple simple = ann.ann();
        assertEquals("foo", simple.val());
    }

    public void testFieldAnn8() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$NestedAnnotationArray", field
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotationArray.class, type);

        TestAnnotations.NestedAnnotationArray ann = (TestAnnotations.NestedAnnotationArray)annotation;
        TestAnnotations.Simple[] simpleAnnArray = ann.annArr();
        assertEquals("foo", simpleAnnArray[0].val());
        assertEquals("bar", simpleAnnArray[1].val());
    }

    public void testFieldAnnArray() {
        Annotation[] annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(field);
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
        annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(field);
    }

    public void testConstructorIsAnnotationPresent() {
        assertTrue(org.codehaus.backport175.reader.Annotations.isAnnotationPresent(
                TestAnnotations.Complex.class, constructor
        ));
        assertFalse(org.codehaus.backport175.reader.Annotations.isAnnotationPresent(Target.class, constructor));
    }

    public void testConstructorAnn1() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$VoidTyped", constructor
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.VoidTyped.class, type);
    }

    public void testConstructorAnn2() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$DefaultString", constructor
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.DefaultString.class, type);

        TestAnnotations.DefaultString ann = (TestAnnotations.DefaultString)annotation;
        assertEquals("hello", ann.value());
    }

    public void testConstructorAnn3() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Simple", constructor
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Simple.class, type);

        TestAnnotations.Simple ann = (TestAnnotations.Simple)annotation;
        assertEquals("foo", ann.val());
        assertEquals("bar", ann.s());
    }

    public void testConstructorAnn4() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$StringArray", constructor
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.StringArray.class, type);

        TestAnnotations.StringArray ann = (TestAnnotations.StringArray)annotation;
        String[] ss = ann.ss();
        assertEquals("hello", ss[0]);
        assertEquals("world", ss[1]);
    }

    public void testConstructorAnn5() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$LongArray", constructor
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
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$NestedAnnotation", constructor
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotation.class, type);

        TestAnnotations.NestedAnnotation ann = (TestAnnotations.NestedAnnotation)annotation;
        TestAnnotations.Simple simple = ann.ann();
        assertEquals("foo", simple.val());
    }

    public void testConstructorAnn7() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$NestedAnnotationArray", constructor
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotationArray.class, type);

        TestAnnotations.NestedAnnotationArray ann = (TestAnnotations.NestedAnnotationArray)annotation;
        TestAnnotations.Simple[] simpleAnnArray = ann.annArr();
        assertEquals("foo", simpleAnnArray[0].val());
        assertEquals("bar", simpleAnnArray[1].val());
    }

    public void testConstructorAnn8() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Complex", constructor
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
        Annotation[] annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(constructor);
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
        annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(constructor);
    }

    public void testMethodIsAnnotationPresent() {
        assertTrue(org.codehaus.backport175.reader.Annotations.isAnnotationPresent(
                TestAnnotations.NestedAnnotationArray.class, method
        ));
        assertFalse(org.codehaus.backport175.reader.Annotations.isAnnotationPresent(Target.class, method));
    }

    public void testMethodAnn1() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$VoidTyped", method
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.VoidTyped.class, type);
    }

    public void testMethodAnn2() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$DefaultString", method
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.DefaultString.class, type);

        TestAnnotations.DefaultString ann = (TestAnnotations.DefaultString)annotation;
        assertEquals("hello", ann.value());
    }

    public void testMethodAnn3() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Simple", method
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.Simple.class, type);

        TestAnnotations.Simple ann = (TestAnnotations.Simple)annotation;
        assertEquals("foo", ann.val());
        assertEquals("bar", ann.s());
    }

    public void testMethodAnn4() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$StringArray", method
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.StringArray.class, type);

        TestAnnotations.StringArray ann = (TestAnnotations.StringArray)annotation;
        String[] ss = ann.ss();
        assertEquals("hello", ss[0]);
        assertEquals("world", ss[1]);
    }

    public void testMethodAnn5() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$LongArray", method
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
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$NestedAnnotation", method
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotation.class, type);

        TestAnnotations.NestedAnnotation ann = (TestAnnotations.NestedAnnotation)annotation;
        TestAnnotations.Simple simple = ann.ann();
        assertEquals("foo", simple.val());
    }

    public void testMethodAnn7() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$NestedAnnotationArray", method
        );
        Class type = annotation.annotationType();
        assertEquals(TestAnnotations.NestedAnnotationArray.class, type);

        TestAnnotations.NestedAnnotationArray ann = (TestAnnotations.NestedAnnotationArray)annotation;
        TestAnnotations.Simple[] simpleAnnArray = ann.annArr();
        assertEquals("foo", simpleAnnArray[0].val());
        assertEquals("bar", simpleAnnArray[1].val());
    }

    public void testMethodAnn8() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Complex", method
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
        Annotation[] annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(method);
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
        annotations = org.codehaus.backport175.reader.Annotations.getAnnotations(method);
    }

    public void testReadInResolvedValues() {
        Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Complex", method
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
        annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.annotation.TestAnnotations$Complex", method
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

    // === for testing Java 5 reflection compatibility ===

//    public void testClassAnnReflection() {
//        Class klass = Target.class;
//        java.lang.annotation.Annotation[] annotations = klass.getAnnotations();
//        assertTrue(annotations.length > 1);
//    }
//
//    public void testMethodAnnReflection() {
//        java.lang.annotation.Annotation[] annotations = method.getAnnotations();
//        assertTrue(annotations.length > 0);
//    }
//
//    public void testReadRealJava5Ann() {
//        Annotation annotation = org.codehaus.backport175.reader.TestAnnotations.getAnnotation(
//                "test.annotation.Target$Test", Target.class
//        );
//        Class type = annotation.annotationType();
//        assertEquals(Target.Test.class, type);
//
//        Target.Test test = (Target.Test)annotation;
//        assertEquals("test", test.test());
//    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AnnotationReaderTest.class);
    }
}
